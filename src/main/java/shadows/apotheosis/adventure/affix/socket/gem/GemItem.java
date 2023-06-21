package shadows.apotheosis.adventure.affix.socket.gem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import javax.annotation.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity;

public class GemItem extends Item {

	public static final String UUID_ARRAY = "uuids";
	public static final String GEM = "gem";

	public GemItem(Properties pProperties) {
		super(pProperties);
	}

	@Override
	@SuppressWarnings("removal")
	public void appendHoverText(ItemStack pStack, Level pLevel, List<Component> tooltip, TooltipFlag pIsAdvanced) {
		Gem gem = getGem(pStack);
		if (gem == null) {
			tooltip.add(Component.literal("Errored gem with no bonus!").withStyle(ChatFormatting.GRAY));
			return;
		}
		int facets = getFacets(pStack);
		gem.addInformation(pStack, getLootRarity(pStack), facets, tooltip::add);
	}

	@Override
	public Component getName(ItemStack pStack) {
		Gem gem = getGem(pStack);
		LootRarity rarity = getLootRarity(pStack);
		if (gem == null || rarity == null) return super.getName(pStack);
		MutableComponent comp = Component.translatable(this.getDescriptionId(pStack));
		int facets = GemItem.getFacets(pStack);
		if (facets > 0 && facets == gem.getMaxFacets(rarity)) {
			comp = Component.translatable("item.apotheosis.gem.flawless", comp);
		} else if (facets == 0) {
			comp = Component.translatable("item.apotheosis.gem.cracked", comp);
		}
		return comp.withStyle(Style.EMPTY.withColor(rarity.color()));
	}

	@Override
	public String getDescriptionId(ItemStack pStack) {
		Gem gem = getGem(pStack);
		if (gem == null) return super.getDescriptionId();
		return super.getDescriptionId(pStack) + "." + gem.getId();
	}

	@Override
	public boolean isFoil(ItemStack pStack) {
		Gem gem = getGem(pStack);
		LootRarity rarity = getLootRarity(pStack);
		if (gem == null || rarity == null) return super.isFoil(pStack);
		int facets = GemItem.getFacets(pStack);
		return facets > 0 && facets == gem.getMaxFacets(rarity);
	}

	/**
	 * Retrieves the attribute modifier UUID(s) from a gem itemstack.
	 * @param gem The gem stack
	 * @returns The stored UUID(s), creating them if they do not exist.
	 */
	public static List<UUID> getUUIDs(ItemStack gemStack) {
		Gem gem = getGem(gemStack);
		if (gem == null || gem.getNumberOfUUIDs() == 0) return Collections.emptyList();
		CompoundTag tag = gemStack.getOrCreateTag();
		if (tag.contains(UUID_ARRAY)) {
			ListTag list = tag.getList(UUID_ARRAY, Tag.TAG_INT_ARRAY);
			List<UUID> ret = new ArrayList<>(list.size());
			for (Tag t : list) {
				ret.add(NbtUtils.loadUUID(t));
			}
			if (ret.size() <= gem.getNumberOfUUIDs()) return generateAndSave(ret, gem.getNumberOfUUIDs(), gemStack);
			return ret;
		}
		return generateAndSave(new ArrayList<>(gem.getNumberOfUUIDs()), gem.getNumberOfUUIDs(), gemStack);
	}

	private static List<UUID> generateAndSave(List<UUID> base, int amount, ItemStack gemStack) {
		int needed = amount - base.size();
		for (int i = 0; i < needed; i++) {
			base.add(UUID.randomUUID());
		}
		ListTag list = new ListTag();
		for (UUID id : base) {
			list.add(NbtUtils.createUUID(id));
		}
		gemStack.getOrCreateTag().put(UUID_ARRAY, list);
		return base;
	}

	/**
	 * Sets the ID of the gem stored in this gem stack.
	 * @param gemStack The gem stack
	 * @param gem The Gem to store
	 */
	public static void setGem(ItemStack gemStack, Gem gem) {
		gemStack.getOrCreateTag().putString(GEM, gem.getId().toString());
	}

	/**
	 * Retrieves the underlying Gem instance of this gem stack.
	 * @param gem The gem stack
	 * @returns The backing Gem, or null if the gem does not exist or is invalid.
	 */
	@Nullable
	public static Gem getGem(ItemStack gem) {
		if (gem.getItem() != Apoth.Items.GEM.get() || !gem.hasTag()) return null;
		var tag = gem.getTag();
		if (tag.contains(GEM)) return GemManager.INSTANCE.getValue(new ResourceLocation(tag.getString(GEM)));
		return null;
	}

	public static void setFacets(ItemStack stack, int facets) {
		stack.getOrCreateTag().putInt("facets", facets);
	}

	public static int getFacets(ItemStack stack) {
		return stack.hasTag() ? stack.getTag().getInt("facets") : 0;
	}

	public static void setLootRarity(ItemStack stack, LootRarity rarity) {
		stack.getOrCreateTag().putString(AffixHelper.RARITY, rarity.id());
	}

	@Nullable
	public static LootRarity getLootRarity(ItemStack stack) {
		Gem gem = getGem(stack);
		return gem == null ? null : gem.clamp(AffixHelper.getRarity(stack.getTag()));
	}

	@Override
	public boolean canBeHurtBy(DamageSource src) {
		return super.canBeHurtBy(src) && src != DamageSource.ANVIL;
	}

	@Override
	public void fillItemCategory(CreativeModeTab group, NonNullList<ItemStack> items) {
		if (group == CreativeModeTab.TAB_SEARCH) {
			GemManager.INSTANCE.getValues().stream().sorted((g1, g2) -> g1.getId().compareTo(g2.getId())).forEach(gem -> {
				ItemStack stack = new ItemStack(this);
				setGem(stack, gem);
				setLootRarity(stack, gem.getMaxRarity());
				setFacets(stack, gem.getMaxFacets(gem.getMaxRarity()));
				items.add(stack);
			});
		}
	}

}
