package shadows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.enchantment.Enchantment;
import net.minecraft.item.EnchantedBookItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.Style;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.ench.EnchModule;
import shadows.ench.altar.RenderPrismaticAltar;
import shadows.ench.altar.TilePrismaticAltar;

@EventBusSubscriber(modid = Apotheosis.MODID, value = Dist.CLIENT, bus = Bus.MOD)
public class ApotheosisClient {

	private static final Map<IRegistryDelegate<Enchantment>, List<ITextComponent>> ENCH_TOOLTIPS = new HashMap<>();

	public static void tooltips(ItemTooltipEvent e) {
		Item i = e.getItemStack().getItem();
		if (Apotheosis.enableEnch) {
			if (EnchModule.allowWeb && i == Items.COBWEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.cobweb"));
			else if (i == ApotheosisObjects.PRISMATIC_WEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.prismatic_cobweb"));
		}
		if (i == Items.ENCHANTED_BOOK) {
			for (Map.Entry<IRegistryDelegate<Enchantment>, List<ITextComponent>> ent : ENCH_TOOLTIPS.entrySet()) {
				if (onlyHasEnchant(e.getItemStack(), ent.getKey().get())) {
					ent.getValue().forEach(s -> e.getToolTip().add(s));
					return;
				}
			}
		}
	}

	private static boolean onlyHasEnchant(ItemStack book, Enchantment ench) {
		ListNBT list = EnchantedBookItem.getEnchantments(book);
		if (list.size() == 1) {
			CompoundNBT tag = list.getCompound(0);
			ResourceLocation id = new ResourceLocation(tag.getString("id"));
			Enchantment enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
			if (enchantment == ench) return true;
		}
		return false;
	}

	@SubscribeEvent
	public static void init(FMLClientSetupEvent e) {
		ITextComponent masterwork = new TranslationTextComponent("info.apotheosis.masterwork").setStyle(new Style().setColor(TextFormatting.DARK_GREEN));
		ITextComponent twisted = new TranslationTextComponent("info.apotheosis.twisted").setStyle(new Style().setColor(TextFormatting.DARK_PURPLE));
		ITextComponent corrupted = new TranslationTextComponent("info.apotheosis.corrupted").setStyle(new Style().setColor(TextFormatting.DARK_RED));
		if (Apotheosis.enchTooltips) {
			if (Apotheosis.enableSpawner) registerTooltip(ApotheosisObjects.CAPTURING, "enchantment.apotheosis.capturing.desc");
			if (Apotheosis.enablePotion) registerTooltip(ApotheosisObjects.TRUE_INFINITY, masterwork, "", "enchantment.apotheosis.true_infinity.desc");
			if (Apotheosis.enableEnch) {
				registerTooltip(ApotheosisObjects.HELL_INFUSION, masterwork, "", "enchantment.apotheosis.hell_infusion.desc");
				registerTooltip(ApotheosisObjects.MOUNTED_STRIKE, "enchantment.apotheosis.mounted_strike.desc");
				registerTooltip(ApotheosisObjects.DEPTH_MINER, twisted, "", "enchantment.apotheosis.depth_miner.desc");
				registerTooltip(ApotheosisObjects.STABLE_FOOTING, "enchantment.apotheosis.stable_footing.desc");
				registerTooltip(ApotheosisObjects.SCAVENGER, masterwork, "", "enchantment.apotheosis.scavenger.desc");
				registerTooltip(ApotheosisObjects.LIFE_MENDING, corrupted, "", "enchantment.apotheosis.life_mending.desc");
				registerTooltip(ApotheosisObjects.ICY_THORNS, "enchantment.apotheosis.icy_thorns.desc");
				registerTooltip(ApotheosisObjects.TEMPTING, "enchantment.apotheosis.tempting.desc");
				registerTooltip(ApotheosisObjects.SHIELD_BASH, "enchantment.apotheosis.shield_bash.desc");
				registerTooltip(ApotheosisObjects.REFLECTIVE, "enchantment.apotheosis.reflective.desc");
				registerTooltip(ApotheosisObjects.BERSERK, corrupted, "", "enchantment.apotheosis.berserk.desc");
				registerTooltip(ApotheosisObjects.KNOWLEDGE, masterwork, "", "enchantment.apotheosis.knowledge.desc");
				registerTooltip(ApotheosisObjects.SPLITTING, "enchantment.apotheosis.splitting.desc");
				registerTooltip(ApotheosisObjects.NATURES_BLESSING, "enchantment.apotheosis.natures_blessing.desc");
				registerTooltip(ApotheosisObjects.REBOUNDING, "enchantment.apotheosis.rebounding.desc");
				registerTooltip(ApotheosisObjects.MAGIC_PROTECTION, twisted, "", "enchantment.apotheosis.magic_protection.desc");
			}
		} else {
			if (Apotheosis.enablePotion) registerTooltip(ApotheosisObjects.TRUE_INFINITY, masterwork);
			if (Apotheosis.enableEnch) {
				registerTooltip(ApotheosisObjects.HELL_INFUSION, masterwork);
				registerTooltip(ApotheosisObjects.DEPTH_MINER, twisted);
				registerTooltip(ApotheosisObjects.SCAVENGER, masterwork);
				registerTooltip(ApotheosisObjects.LIFE_MENDING, corrupted);
				registerTooltip(ApotheosisObjects.BERSERK, corrupted);
				registerTooltip(ApotheosisObjects.KNOWLEDGE, masterwork);
				registerTooltip(ApotheosisObjects.MAGIC_PROTECTION, twisted);
			}
		}
		if (Apotheosis.enableEnch) ClientRegistry.bindTileEntitySpecialRenderer(TilePrismaticAltar.class, new RenderPrismaticAltar());
		MinecraftForge.EVENT_BUS.addListener(ApotheosisClient::tooltips);
	}

	public static void registerTooltip(Enchantment e, Object... keys) {
		List<ITextComponent> tips = ENCH_TOOLTIPS.computeIfAbsent(e.delegate, d -> new ArrayList<>());
		for (Object s : keys) {
			if (s instanceof ITextComponent) tips.add((ITextComponent) s);
			else if (s instanceof String) tips.add(new TranslationTextComponent((String) s));
		}
	}

}
