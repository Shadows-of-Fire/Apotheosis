package shadows.deadly.cmd;

import java.util.Random;

import net.minecraft.block.Block;
import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import shadows.deadly.loot.LootManager;
import shadows.deadly.loot.LootRarity;

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
			Random rand = ((EntityPlayer) sender).world.rand;
			LootRarity rarity = LootRarity.random(rand);
			Block.spawnAsEntity(((EntityPlayer) sender).world, sender.getPosition(), LootManager.genLootItem(LootManager.getRandomEntry(rand, rarity), rand, rarity));
		}
	}

}
