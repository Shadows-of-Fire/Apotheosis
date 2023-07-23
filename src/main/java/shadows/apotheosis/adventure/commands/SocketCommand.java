package shadows.apotheosis.adventure.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;
import shadows.apotheosis.adventure.loot.LootCategory;

public class SocketCommand {

    public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
        root.then(Commands.literal("set_sockets").requires(c -> c.hasPermission(2)).then(Commands.argument("sockets", IntegerArgumentType.integer()).executes(c -> {
            Player p = c.getSource().getPlayerOrException();
            ItemStack stack = p.getMainHandItem();

            LootCategory cat = LootCategory.forItem(stack);
            if (cat.isNone()) {
                c.getSource().sendFailure(Component.literal("The target item cannot receive sockets!"));
                return 1;
            }

            int sockets = IntegerArgumentType.getInteger(c, "sockets");
            SocketHelper.setSockets(stack, sockets);
            return 0;
        })));
    }

}
