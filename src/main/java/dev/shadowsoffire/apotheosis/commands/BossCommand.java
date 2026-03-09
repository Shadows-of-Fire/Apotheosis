package dev.shadowsoffire.apotheosis.commands;

import javax.annotation.Nullable;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.mobs.ApothMobEvents;
import dev.shadowsoffire.apotheosis.mobs.registries.InvaderRegistry;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;

public class BossCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS = (ctx, builder) -> SharedSuggestionProvider.suggest(InvaderRegistry.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("spawn_boss").requires(c -> c.hasPermission(2));

        // Brigadier doesn't really do branching commands very well.
        builder.then(
            Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("boss", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS)
                    .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(RarityCommand.SUGGEST_RARITY)
                        .then(Commands.argument("send_notification", BoolArgumentType.bool())
                            .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), ResourceLocationArgument.getId(c, "rarity"), BoolArgumentType.getBool(c, "send_notification"))))
                        .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), ResourceLocationArgument.getId(c, "rarity"))))
                    .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), null)))
                .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), null, null)));

        builder.then(
            Commands.argument("entity", EntityArgument.entity())
                .then(Commands.argument("boss", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS)
                    .then(Commands.argument("rarity", ResourceLocationArgument.id()).suggests(RarityCommand.SUGGEST_RARITY)
                        .then(Commands.argument("send_notification", BoolArgumentType.bool())
                            .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), ResourceLocationArgument.getId(c, "rarity"), BoolArgumentType.getBool(c, "send_notification"))))
                        .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), ResourceLocationArgument.getId(c, "boss"), ResourceLocationArgument.getId(c, "rarity"))))
                    .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), ResourceLocationArgument.getId(c, "boss"), null)))
                .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), null, null)));

        root.then(builder);
    }

    public static int spawnBoss(CommandContext<CommandSourceStack> c, Vec3 pos, @Nullable ResourceLocation bossId, @Nullable ResourceLocation rarityId) {
        return spawnBoss(c, pos, bossId, rarityId, false);
    }

    public static int spawnBoss(CommandContext<CommandSourceStack> c, Vec3 pos, @Nullable ResourceLocation bossId, @Nullable ResourceLocation rarityId, boolean sendNotification) {
        Entity nullableSummoner = c.getSource().getEntity();
        Player summoner = nullableSummoner instanceof Player ? (Player) nullableSummoner : c.getSource().getLevel().getNearestPlayer(pos.x(), pos.y(), pos.z(), 64, false);
        if (summoner == null) {
            c.getSource().sendFailure(Component.literal("No available player context!"));
            return -1;
        }

        GenContext ctx = GenContext.forPlayer(summoner);
        Invader boss = bossId == null ? InvaderRegistry.INSTANCE.getRandomItem(ctx) : InvaderRegistry.INSTANCE.getValue(bossId);
        if (boss == null) {
            if (bossId != null) {
                c.getSource().sendFailure(Component.literal("Unknown boss: " + bossId));
            }
            else {
                c.getSource().sendFailure(Component.literal("No bosses available for the current context!"));
            }
            return -2;
        }

        Mob bossEntity;

        if (rarityId != null) {
            DynamicHolder<LootRarity> rarity = RarityRegistry.INSTANCE.holder(rarityId);
            if (!rarity.isBound()) {
                c.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
                return -3;
            }
            bossEntity = boss.createBoss((ServerLevelAccessor) summoner.level(), BlockPos.containing(pos), ctx, rarity.get());
        }
        else {
            bossEntity = boss.createBoss((ServerLevelAccessor) summoner.level(), BlockPos.containing(pos), ctx);
        }

        c.getSource().getLevel().addFreshEntityWithPassengers(bossEntity);

        if (sendNotification) {
            ApothMobEvents.sendInvaderSpawnNotification(c.getSource().getLevel(), bossEntity);
        }

        return 0;
    }

}
