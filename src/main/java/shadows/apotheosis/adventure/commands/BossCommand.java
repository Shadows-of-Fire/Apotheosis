package shadows.apotheosis.adventure.commands;

import javax.annotation.Nullable;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;

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
import shadows.apotheosis.adventure.boss.BossItem;
import shadows.apotheosis.adventure.boss.BossItemManager;
import shadows.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.placebo.json.WeightedJsonReloadListener.IDimensional;

public class BossCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_BOSS = (ctx, builder) -> SharedSuggestionProvider.suggest(BossItemManager.INSTANCE.getKeys().stream().map(ResourceLocation::toString), builder);

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal("spawn_boss").requires(c -> c.hasPermission(2));

        // Brigadier doesn't really do branching commands very well.
        builder.then(
            Commands.argument("pos", Vec3Argument.vec3())
                .then(Commands.argument("boss", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS)
                    .then(Commands.argument("rarity", StringArgumentType.word()).suggests(LootifyCommand.SUGGEST_RARITY)
                        .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), StringArgumentType.getString(c, "rarity"))))
                    .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), ResourceLocationArgument.getId(c, "boss"), null)))
                .executes(c -> spawnBoss(c, Vec3Argument.getVec3(c, "pos"), null, null)));

        builder.then(
            Commands.argument("entity", EntityArgument.entity())
                .then(Commands.argument("boss", ResourceLocationArgument.id()).suggests(SUGGEST_BOSS)
                    .then(Commands.argument("rarity", StringArgumentType.word()).suggests(LootifyCommand.SUGGEST_RARITY)
                        .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), ResourceLocationArgument.getId(c, "boss"), StringArgumentType.getString(c, "rarity"))))
                    .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), ResourceLocationArgument.getId(c, "boss"), null)))
                .executes(c -> spawnBoss(c, EntityArgument.getEntity(c, "entity").position(), null, null)));

        root.then(builder);
    }

    public static int spawnBoss(CommandContext<CommandSourceStack> c, Vec3 pos, @Nullable ResourceLocation bossId, @Nullable String rarityId) {
        Entity nullableSummoner = c.getSource().getEntity();
        Player summoner = nullableSummoner instanceof Player ? (Player) nullableSummoner : c.getSource().getLevel().getNearestPlayer(pos.x(), pos.y(), pos.z(), 64, false);
        if (summoner == null) {
            c.getSource().sendFailure(Component.literal("No available player context!"));
            return -1;
        }

        BossItem boss = bossId == null ? BossItemManager.INSTANCE.getRandomItem(summoner.random, summoner.getLuck(), IDimensional.matches(summoner.level), IStaged.matches(summoner)) : BossItemManager.INSTANCE.getValue(bossId);
        if (boss == null) {
            if (bossId == null) {
                c.getSource().sendFailure(Component.literal("Unknown boss: " + bossId));
            }
            else {
                c.getSource().sendFailure(Component.literal("No bosses available for the current context!"));
            }
            return -2;
        }

        Mob bossEntity;

        if (rarityId != null) {
            LootRarity rarity = LootRarity.byId(rarityId);
            if (rarity == null) {
                c.getSource().sendFailure(Component.literal("Unknown rarity: " + rarityId));
                return -3;
            }
            bossEntity = boss.createBoss((ServerLevelAccessor) summoner.level, new BlockPos(pos), summoner.random, summoner.getLuck(), rarity);
        }
        else {
            bossEntity = boss.createBoss((ServerLevelAccessor) summoner.level, new BlockPos(pos), summoner.random, summoner.getLuck());
        }

        c.getSource().getLevel().addFreshEntityWithPassengers(bossEntity);
        return 0;
    }

}
