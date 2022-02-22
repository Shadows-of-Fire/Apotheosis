package shadows.apotheosis.deadly;

import java.util.Map;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.deadly.commands.CategoryCheckCommand;
import shadows.apotheosis.deadly.commands.LootifyCommand;
import shadows.apotheosis.deadly.commands.RarityCommand;
import shadows.apotheosis.deadly.loot.affix.Affix;
import shadows.apotheosis.deadly.loot.affix.AffixHelper;

public class DeadlyModuleEvents {

	@SubscribeEvent
	public void reloads(AddReloadListenerEvent e) {
	}

	@SubscribeEvent
	public void cmds(RegisterCommandsEvent e) {
		RarityCommand.register(e.getDispatcher());
		CategoryCheckCommand.register(e.getDispatcher());
		LootifyCommand.register(e.getDispatcher());
	}

	@SubscribeEvent(priority = EventPriority.LOWEST)
	public void sortModifiers(ItemAttributeModifierEvent e) {
		if (e.getModifiers() == null || e.getModifiers().isEmpty() || FMLEnvironment.dist == Dist.DEDICATED_SERVER) return;
		Multimap<Attribute, AttributeModifier> map = TreeMultimap.create((k1, k2) -> k1.getRegistryName().compareTo(k2.getRegistryName()), (v1, v2) -> {
			int compOp = Integer.compare(v1.getOperation().ordinal(), v2.getOperation().ordinal());
			int compValue = Double.compare(v2.getAmount(), v1.getAmount());
			return compOp == 0 ? compValue == 0 ? v1.getName().compareTo(v2.getName()) : compValue : compOp;
		});
		for (Map.Entry<Attribute, AttributeModifier> ent : e.getModifiers().entries()) {
			if (ent.getKey() != null && ent.getValue() != null) map.put(ent.getKey(), ent.getValue());
			else DeadlyModule.LOGGER.error("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", e.getItemStack(), ent.getKey(), ent.getValue());
		}
		ObfuscationReflectionHelper.setPrivateValue(ItemAttributeModifierEvent.class, e, map, "unmodifiableModifiers");
	}

	@SubscribeEvent
	public void affixModifiers(ItemAttributeModifierEvent e) {
		ItemStack stack = e.getItemStack();
		//if (stack.getItem() instanceof IAffixSensitiveItem && !((IAffixSensitiveItem) stack.getItem()).receivesAttributes(stack)) return;
		if (stack.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
			affixes.forEach((afx, lvl) -> afx.addModifiers(stack, lvl, e.getSlotType(), e::addModifier));
		}
	}

}
