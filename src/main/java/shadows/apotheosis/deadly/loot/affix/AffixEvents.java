package shadows.apotheosis.deadly.loot.affix;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.BlockEvent.HarvestDropsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class AffixEvents {

	public static final List<ItemStack> NUGGETS = new ArrayList<>();
	static { //TODO: Config or auto-scan for all nuggetX
		NUGGETS.add(new ItemStack(Items.GOLD_NUGGET));
		NUGGETS.add(new ItemStack(Items.IRON_NUGGET));
	}

	@SubscribeEvent
	public void crit(CriticalHitEvent e) {
		Map<Affix, Float> affixes = AffixHelper.getAffixes(e.getEntityPlayer().getHeldItemMainhand());

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
		if (stack.isEmpty() || !stack.hasTagCompound() || !isEffective(stack, e.getState())) return;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		if (affixes.containsKey(Affixes.SIFTING) && e.getWorld().rand.nextFloat() <= affixes.get(Affixes.SIFTING)) {
			e.getDrops().add(NUGGETS.get(e.getWorld().rand.nextInt(NUGGETS.size())).copy());
		}
	}

	private static boolean isEffective(ItemStack stack, IBlockState state) {
		for (String s : stack.getItem().getToolClasses(stack)) {
			if (state.getBlock().isToolEffective(s, state)) return true;
		}
		return false;
	}

	@SubscribeEvent
	public void tick(PlayerTickEvent e) {
		EntityPlayer player = e.player;
		if (e.phase == Phase.END) return;
		ItemStack active = player.getActiveItemStack();
		if (active.getItem().isShield(active, player) && active.hasTagCompound()) {
			Map<Affix, Float> affixes = AffixHelper.getAffixes(active);
			if (affixes.containsKey(Affixes.RESISTANCE)) {
				player.addPotionEffect(new PotionEffect(MobEffects.RESISTANCE, (int) (affixes.get(Affixes.RESISTANCE) * 20)));
			}
		}
	}

}
