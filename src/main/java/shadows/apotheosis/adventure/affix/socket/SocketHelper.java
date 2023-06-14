package shadows.apotheosis.adventure.affix.socket;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.socket.gem.Gem;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.event.GetItemSocketsEvent;
import shadows.apotheosis.adventure.loot.LootRarity;

public class SocketHelper {

	public static final String GEMS = "gems";

	public static List<ItemStack> getGems(ItemStack stack) {
		return getGems(stack, getSockets(stack));
	}

	public static List<ItemStack> getGems(ItemStack stack, int size) {
		List<ItemStack> gems = NonNullList.withSize(size, ItemStack.EMPTY);
		if (size == 0 || stack.isEmpty()) return gems;
		int i = 0;
		CompoundTag afxData = stack.getTagElement(AffixHelper.AFFIX_DATA);
		if (afxData != null && afxData.contains(GEMS)) {
			ListTag gemData = afxData.getList(GEMS, Tag.TAG_COMPOUND);
			for (Tag tag : gemData) {
				gems.set(i++, ItemStack.of((CompoundTag) tag));
				if (i >= size) break;
			}
		}
		return gems;
	}

	public static List<Gem> getActiveGems(ItemStack stack) {
		return getGems(stack).stream().map(GemItem::getGem).filter(Objects::nonNull).toList();
	}

	public static void setGems(ItemStack stack, List<ItemStack> gems) {
		CompoundTag afxData = stack.getOrCreateTagElement(AffixHelper.AFFIX_DATA);
		ListTag gemData = new ListTag();
		for (ItemStack s : gems) {
			gemData.add(s.save(new CompoundTag()));
		}
		afxData.put(GEMS, gemData);
	}

	public static int getSockets(ItemStack stack) {
		var socketAffix = AffixHelper.getAffixes(stack).get(Affixes.SOCKET.get());
		var sockets = 0;
		if (socketAffix != null) {
			sockets = (int) socketAffix.level();
		}
		var event = new GetItemSocketsEvent(stack, sockets);
		MinecraftForge.EVENT_BUS.post(event);
		if (sockets != event.getSockets()) {
			sockets = event.getSockets();
			setSockets(stack, sockets);
		}
		return sockets;
	}

	public static void setSockets(ItemStack stack, int sockets) {
		Map<Affix, AffixInstance> affixes = AffixHelper.getAffixes(stack);
		affixes.put(Affixes.SOCKET.get(), new AffixInstance(Affixes.SOCKET.get(), stack, LootRarity.COMMON, sockets));
		AffixHelper.setAffixes(stack, affixes);
	}

	public static boolean hasEmptySockets(ItemStack stack) {
		return getGems(stack).stream().map(GemItem::getGem).anyMatch(Objects::isNull);
	}
	
	public static int getEmptySocket(ItemStack stack) {
		var gems = getGems(stack, getSockets(stack));
		for (int socket = 0; socket < gems.size(); socket++) {
			var gem = GemItem.getGem(gems.get(socket));
			if (gem == null) return socket;
		}
		return 0;
	}

}
