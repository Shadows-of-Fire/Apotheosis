package shadows.apotheosis.adventure.commands;

import java.util.List;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.adventure.affix.socket.SocketHelper;

public class SocketCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("socket").requires(c -> c.hasPermission(2)).then(Commands.argument("slot", IntegerArgumentType.integer()).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack stack = p.getMainHandItem();
			ItemStack gem = p.getOffhandItem();
			int sockets = SocketHelper.getSockets(stack);
			List<ItemStack> gems = SocketHelper.getGems(stack, sockets);
			gems.set(c.getArgument("slot", Integer.class), gem);
			SocketHelper.setGems(stack, gems);
			return 0;
		})));
	}

}
