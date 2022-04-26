package shadows.apotheosis.deadly.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.*;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.affix.IAffixSensitiveItem;
import shadows.apotheosis.deadly.loot.LootRarity;

public class AffixTomeItem extends BookItem implements IAffixSensitiveItem {

	public static final String TYPE = "apoth.booktype";

	static Random rand = new Random();

	protected final LootRarity rarity;

	public AffixTomeItem(LootRarity rarity, Properties props) {
		super(props);
		this.rarity = rarity;
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level world, List<Component> tooltip, TooltipFlag flagIn) {
		if (!this.isFoil(stack)) {
			tooltip.add(new TranslatableComponent("info.apotheosis.affix_tome").withStyle(ChatFormatting.GRAY));
			tooltip.add(new TranslatableComponent("info.apotheosis.affix_tome2", new TranslatableComponent("rarity.apoth." + this.rarity.name().toLowerCase(Locale.ROOT)).withStyle(Style.EMPTY.withColor(this.rarity.getColor()))).withStyle(ChatFormatting.GRAY));
		} else {
			if (stack.getTag().contains(TYPE)) tooltip.add(new TranslatableComponent("info.apotheosis.retrieved_from", new TranslatableComponent("lootCategory.apotheosis." + LootCategory.values()[stack.getTag().getInt(TYPE)].toString().toLowerCase(Locale.ROOT))).withStyle(ChatFormatting.BLUE));
			Map<Affix, Float> afx = AffixHelper.getAffixes(stack);
			afx.forEach((a, l) -> {
				tooltip.add(a.getDisplayName(l));
			});
		}
	}

	@Override
	public Component getDescription() {
		return ((MutableComponent) super.getDescription()).setStyle(Style.EMPTY.withColor(this.rarity.getColor()));
	}

	@Override
	public Component getName(ItemStack stack) {
		return new TranslatableComponent(this.getDescriptionId(stack)).setStyle(Style.EMPTY.withColor(this.rarity.getColor()));
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return AffixHelper.hasAffixes(stack);
	}

	public LootRarity getRarity() {
		return this.rarity;
	}

	public static boolean updateAnvil(AnvilUpdateEvent ev) {
		ItemStack lhsItem = ev.getLeft();
		ItemStack rhsItem = ev.getRight();

		if (!(rhsItem.getItem() instanceof AffixTomeItem)) return false;
		if (lhsItem.getItem() instanceof AffixTomeItem) return false;

		LootCategory lhsItemLootCat = LootCategory.forItem(lhsItem);
		if(lhsItemLootCat == null) return false;

		if (!AffixHelper.hasAffixes(rhsItem) && AffixHelper.hasAffixes(lhsItem)) { //Scrapping Mode
			LootRarity rarity = AffixHelper.getRarity(lhsItem);
			if (rarity == null) return false; // can lhsItem have affixes without rarity?
			if (rarity.ordinal() > ((AffixTomeItem) rhsItem.getItem()).rarity.ordinal()) return false; //Can only scrap items of <= rarity.
			Map<Affix, Float> lhsItemAffixes = AffixHelper.getAffixes(lhsItem);
			int size = Math.max(1, Mth.ceil(lhsItemAffixes.size() / 2D));
			List<Affix> keys = new ArrayList<>(lhsItemAffixes.keySet());
			long seed = 1831;
			for (Affix e : keys) {
				seed ^= e.getRegistryName().hashCode();
			}
			seed ^= ev.getPlayer().getEnchantmentSeed();
			rand.setSeed(seed);
			while (keys.size() > size) {
				Affix lost = keys.get(rand.nextInt(keys.size()));
				lhsItemAffixes.remove(lost);
				keys.remove(lost);
			}
			ItemStack resultTome = new ItemStack(DeadlyModule.RARITY_TOMES.get(rarity));
			AffixHelper.setAffixes(resultTome, lhsItemAffixes);
			ev.setMaterialCost(1);
			ev.setCost(lhsItemAffixes.size() * 18);

			resultTome.getOrCreateTag().putInt(TYPE, lhsItemLootCat.ordinal());
			ev.setOutput(resultTome);
		} else if (AffixHelper.hasAffixes(rhsItem)) { //Application Mode
			Map<Affix, Float> bookAffixes = AffixHelper.getAffixes(rhsItem);
			Map<Affix, Float> itemAffixes = AffixHelper.getAffixes(lhsItem);
			Component name = lhsItem.getHoverName();
			ItemStack out = lhsItem.copy();
			int baseCost = itemAffixes.size() * 4;
			int cost = 0;
			for (Map.Entry<Affix, Float> affixEntry : bookAffixes.entrySet()) {
				Affix afx = affixEntry.getKey();
				if (!afx.canApply(lhsItemLootCat)) continue;
				float curLvl = itemAffixes.getOrDefault(afx, 0F);
				if (curLvl == 0) {
					name = afx.chainName(name, true);
					itemAffixes.put(afx, affixEntry.getValue());
					cost += 12;
				} else {
					itemAffixes.put(afx, afx.upgradeLevel(curLvl, affixEntry.getValue()));
					cost += 18;
				}
			}
			if (cost == 0) return false;
			cost += baseCost;
			out.setHoverName(((MutableComponent) name).withStyle(Style.EMPTY.withColor(((AffixTomeItem) rhsItem.getItem()).rarity.getColor())));
			AffixHelper.setAffixes(out, itemAffixes);
			out.setCount(1);
			ev.setMaterialCost(1);
			ev.setCost(cost);
			ev.setOutput(out);
		}
		return true;
	}

	@Override
	public boolean receivesAttributes(ItemStack stack) {
		return false;
	}

	@Override
	public boolean receivesTooltips(ItemStack stack) {
		return false;
	}
}