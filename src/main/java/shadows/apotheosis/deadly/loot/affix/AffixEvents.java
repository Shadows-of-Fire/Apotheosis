package shadows.apotheosis.deadly.loot.affix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AffixEvents {

	public static final List<ItemStack> NUGGETS = new ArrayList<>();
	static { //TODO: Config or auto-scan for all nuggetX
		NUGGETS.add(new ItemStack(Items.GOLD_NUGGET));
		NUGGETS.add(new ItemStack(Items.IRON_NUGGET));
	}

	@SubscribeEvent
	public void crit(CriticalHitEvent e) {
		Map<Affix, Float> affixes = AffixHelper.getAffixes(e.getPlayer().getHeldItemMainhand());

		if (!e.isVanillaCritical() && affixes.containsKey(Affixes.ALWAYS_CRIT)) {
			e.setResult(Result.ALLOW);
		}

		if (affixes.containsKey(Affixes.CRIT_DAMAGE)) {
			e.setDamageModifier(affixes.get(Affixes.CRIT_DAMAGE) + e.getDamageModifier());
		}
	}

	@SubscribeEvent
	public void harvest(HarvestDropsEvent e) {
		if (e.getHarvester() == null) return;
		ItemStack stack = e.getHarvester().getHeldItemMainhand();
		if (stack.isEmpty() || !stack.hasTag() || !isEffective(stack, e.getState())) return;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		if (affixes.containsKey(Affixes.SIFTING) && e.getWorld().getRandom().nextFloat() <= affixes.get(Affixes.SIFTING)) {
			e.getDrops().add(NUGGETS.get(e.getWorld().getRandom().nextInt(NUGGETS.size())).copy());
		}
	}

	private static boolean isEffective(ItemStack stack, BlockState state) {
		for (ToolType s : stack.getItem().getToolTypes(stack)) {
			if (state.getBlock().isToolEffective(state, s)) return true;
		}
		return false;
	}

	@SubscribeEvent
	public void tick(PlayerTickEvent e) {
		PlayerEntity player = e.player;
		if (e.phase == Phase.END) return;
		ItemStack active = player.getActiveItemStack();
		if (active.getItem().isShield(active, player) && active.hasTag()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(active);
			if (affixes.containsKey(Affixes.RESISTANCE)) {
				player.addPotionEffect(new EffectInstance(Effects.RESISTANCE, (int) (affixes.get(Affixes.RESISTANCE) * 20)));
			}
		}
	}

}
