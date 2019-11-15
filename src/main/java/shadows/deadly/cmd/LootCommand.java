package shadows.deadly.cmd;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import shadows.deadly.loot.LootManager;

public class LootCommand extends CommandBase {

	@Override
	public String getName() {
		return "loot";
	}

	@Override
	public String getUsage(ICommandSender sender) {
		return "/loot";
	}

	@Override
	public void execute(MinecraftServer server, ICommandSender sender, String[] args) throws CommandException {
		if (sender instanceof EntityPlayer) {
			Block.spawnAsEntity(((EntityPlayer) sender).world, sender.getPosition(), LootManager.genLootItem(((EntityPlayer) sender).world.rand));
		}
	}

}
