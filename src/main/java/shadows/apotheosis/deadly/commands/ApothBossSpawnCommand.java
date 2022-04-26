package shadows.apotheosis.deadly.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.minecraft.Util;
import net.minecraft.commands.CommandSource;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.WorldCoordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.BossItem;
import shadows.apotheosis.deadly.loot.LootRarity;
import shadows.apotheosis.deadly.reload.AffixLootManager;
import shadows.apotheosis.deadly.reload.BossItemManager;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collections;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;

public class ApothBossSpawnCommand {

    public static final SuggestionProvider<CommandSourceStack> SUGGEST_RARITY = (ctx, builder) -> {
        return SharedSuggestionProvider.suggest(Arrays.stream(LootRarity.values()).map(LootRarity::name), builder);
    };

    public static void register(CommandDispatcher<CommandSourceStack> pDispatcher) {
        pDispatcher.register(
                Commands.literal("apothboss")
                        .requires(c -> c.hasPermission(2))
                        .executes(ApothBossSpawnCommand::SpawnBoss)
                        .then(Commands.argument("pos", BlockPosArgument.blockPos())
//                                    .executes(c-> SpawnBoss(c, c.getArgument("pos", BlockPos.class))))
                                .executes(ApothBossSpawnCommand::SpawnBoss)
                        .then(Commands.argument("rarity", StringArgumentType.word())
                                .suggests(SUGGEST_RARITY)
//                                .executes(c-> SpawnBoss(c, c.getArgument("pos", BlockPos.class), LootRarity.valueOf(c.getArgument("rarity", String.class))))));
                                .executes(ApothBossSpawnCommand::SpawnBoss))));
    }

    static int SpawnBoss(CommandContext<CommandSourceStack> ctx)
    {
        try {
            ServerPlayer player = ctx.getSource().getPlayerOrException();
            BlockPos pos = ctx.getArgument("pos", WorldCoordinates.class).getBlockPos(ctx.getSource());
            if(pos == BlockPos.ZERO)
                pos = player.blockPosition().relative(player.getDirection(), 10);
            String rarityStringVal = ctx.getArgument("rarity", String.class);
            LootRarity rarity = rarityStringVal == null || rarityStringVal.isEmpty() ? LootRarity.random(ThreadLocalRandom.current()) : LootRarity.valueOf(rarityStringVal.toUpperCase(Locale.ROOT));
            var item = BossItemManager.INSTANCE.getRandomItem(ThreadLocalRandom.current());
            if(!item.isPresent())
                ctx.getSource().sendFailure(new TextComponent("Failed to summon boss! No bosses defined"));
            else
            {
                ServerLevel world = player.getLevel();
                Mob ent = item.get().createBoss(world, pos, ThreadLocalRandom.current());
                world.addFreshEntity(ent);
                ctx.getSource().sendSuccess(new TextComponent(ent.getName().getString() + " has been summoned."), false);
            }

        } catch (CommandSyntaxException e) {
            e.printStackTrace();
            try {
                ctx.getSource().sendFailure(new TextComponent("Something went wrong with command, see server logs for more info!"));
            }catch (Exception e2)
            {
                e2.printStackTrace();
            }

        }

        return 0;
    }
}
