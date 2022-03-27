package shadows.apotheosis.deadly;

import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.deadly.commands.CategoryCheckCommand;
import shadows.apotheosis.deadly.commands.LootifyCommand;
import shadows.apotheosis.deadly.commands.ModifierCommand;
import shadows.apotheosis.deadly.commands.RarityCommand;
import shadows.apotheosis.deadly.loot.LootCategory;
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
		ModifierCommand.register(e.getDispatcher());
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
			else DeadlyModule.LOGGER.debug("Detected broken attribute modifier entry on item {}.  Attr={}, Modif={}", e.getItemStack(), ent.getKey(), ent.getValue());
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

	private static final Set<Float> values = ImmutableSet.of(0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F, 2.0F, 2.1F, 2.25F, 2.33F, 2.5F, 3F);

	/**
	 * This event handler makes the Draw Speed attribute work as intended.
	 * Modifiers targetting this attribute should use the MULTIPLY_BASE operation.
	 */
	@SubscribeEvent
	public void drawSpeed(LivingEntityUseItemEvent.Tick e) {
		if (e.getEntity() instanceof Player player) {
			double t = player.getAttribute(Apoth.Attributes.DRAW_SPEED).getValue() - 1;
			if (t == 0 || !LootCategory.forItem(e.getItem()).isRanged()) return;
			float clamped = values.stream().filter(f -> f >= t).min(Float::compareTo).orElse(3F);
			while (clamped > 0) {
				if (e.getEntity().tickCount % (int) Math.floor(1 / Math.min(1, t)) == 0) e.setDuration(e.getDuration() - 1);
				clamped--;
			}
		}
	}

}
