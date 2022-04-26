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
import shadows.apotheosis.deadly.commands.*;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;

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
		ApothBossSpawnCommand.register(e.getDispatcher());
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
