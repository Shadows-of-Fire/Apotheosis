package shadows.apotheosis.deadly.objects;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Random;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.item.BookItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.text.IFormattableTextComponent;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AnvilUpdateEvent;
import shadows.apotheosis.deadly.DeadlyModule;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.EquipmentType;
import shadows.apotheosis.deadly.affix.IAffixSensitiveItem;
import shadows.apotheosis.deadly.affix.LootRarity;

public class AffixTomeItem extends BookItem implements IAffixSensitiveItem {

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
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (!this.hasEffect(stack)) {
			tooltip.add(new TranslationTextComponent("info.apotheosis.affix_tome"));
			tooltip.add(new TranslationTextComponent("info.apotheosis.affix_tome2", new TranslationTextComponent("rarity.apoth." + this.rarity.name().toLowerCase(Locale.ROOT))));
		} else {
			Map<Affix, Float> afx = AffixHelper.getAffixes(stack);
			afx.forEach((a, l) -> {
				tooltip.add(a.getDisplayName(l));
			});
		}
	}

	@Override
	public ITextComponent getName() {
		return ((IFormattableTextComponent) super.getName()).mergeStyle(this.rarity.getColor());
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		return new TranslationTextComponent(this.getTranslationKey(stack)).mergeStyle(this.rarity.getColor());
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return AffixHelper.hasAffixes(stack);
	}

	public LootRarity getRarity() {
		return this.rarity;
	}

	public static boolean updateAnvil(AnvilUpdateEvent ev) {
		ItemStack weapon = ev.getLeft();
		ItemStack book = ev.getRight();
		if (!(book.getItem() instanceof AffixTomeItem)) return false;

		if (!AffixHelper.hasAffixes(book) && AffixHelper.hasAffixes(weapon)) { //Scrapping Mode
			LootRarity rarity = AffixHelper.getRarity(weapon);
			if (rarity == null) return false;
			if (rarity.ordinal() > ((AffixTomeItem) book.getItem()).rarity.ordinal()) return false; //Can only scrap items of <= rarity.
			Map<Affix, Float> wepAfx = AffixHelper.getAffixes(weapon);
			int size = Math.max(1, MathHelper.ceil(wepAfx.size() / 2D));
			List<Affix> keys = new ArrayList<>(wepAfx.keySet());
			long seed = 1831;
			for (Affix e : keys) {
				seed ^= e.getRegistryName().hashCode();
			}
			seed ^= ev.getPlayer().getXPSeed();
			rand.setSeed(seed);
			while (keys.size() > size) {
				Affix lost = keys.get(rand.nextInt(keys.size()));
				wepAfx.remove(lost);
				keys.remove(lost);
			}
			ItemStack out = new ItemStack(DeadlyModule.RARITY_TOMES.get(rarity));
			AffixHelper.setAffixes(out, wepAfx);
			ev.setMaterialCost(1);
			ev.setCost(wepAfx.size() * 18);
			ev.setOutput(out);
		} else if (AffixHelper.hasAffixes(book)) { //Application Mode
			Map<Affix, Float> bookAfx = AffixHelper.getAffixes(book);
			Map<Affix, Float> wepAfx = AffixHelper.getAffixes(weapon);
			boolean wepTome = weapon.getItem() instanceof AffixTomeItem;
			EquipmentType type = EquipmentType.getTypeFor(weapon);
			if (type == null && !wepTome) return false;
			ITextComponent name = weapon.getDisplayName();
			ItemStack out = weapon.copy();
			int baseCost = wepAfx.size() * 4;
			int cost = 0;
			for (Map.Entry<Affix, Float> e : bookAfx.entrySet()) {
				Affix afx = e.getKey();
				if (!afx.canApply(type) && !wepTome) continue;
				float curLvl = wepAfx.getOrDefault(afx, 0F);
				if (curLvl == 0) {
					name = afx.chainName(name, null);
					wepAfx.put(afx, e.getValue());
					cost += 12;
				} else {
					wepAfx.put(afx, afx.upgradeLevel(curLvl, e.getValue()));
					cost += 18;
				}
			}
			if (cost == 0) return false;
			cost += baseCost;
			if (!wepTome) out.setDisplayName(((IFormattableTextComponent) name).mergeStyle(((AffixTomeItem) book.getItem()).rarity.getColor()));
			AffixHelper.setAffixes(out, wepAfx);
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