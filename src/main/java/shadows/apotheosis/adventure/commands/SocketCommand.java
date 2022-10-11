package shadows.apotheosis.adventure.commands;

import java.util.Map;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class SocketCommand {

	public static void register(LiteralArgumentBuilder<CommandSourceStack> root) {
		root.then(Commands.literal("set_sockets").requires(c -> c.hasPermission(2)).then(Commands.argument("sockets", IntegerArgumentType.integer()).executes(c -> {
			Player p = c.getSource().getPlayerOrException();
			ItemStack stack = p.getMainHandItem();

			LootCategory cat = LootCategory.forItem(stack);
			if (cat == LootCategory.NONE) {
				c.getSource().sendFailure(new TextComponent("The target item cannot receive sockets!"));
				return 1;
			}

			int sockets = IntegerArgumentType.getInteger(c, "sockets");
			Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
			affixes.put(Affixes.SOCKET, new AffixInstance(Affixes.SOCKET, stack, LootRarity.COMMON, sockets));
			AffixHelper.setAffixes(stack, affixes);
			return 0;
		})));
	}

}
