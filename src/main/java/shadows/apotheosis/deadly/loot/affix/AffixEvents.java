package shadows.apotheosis.deadly.loot.affix;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.command.CommandSource;
import net.minecraft.command.Commands;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.CriticalHitEvent;
import net.minecraftforge.event.world.BlockEvent.BreakEvent;
import net.minecraftforge.eventbus.api.Event.Result;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import shadows.apotheosis.deadly.loot.LootManager;
import shadows.apotheosis.deadly.loot.LootRarity;

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
	public void harvest(BreakEvent e) {
		if (e.getPlayer() == null) return;
		ItemStack stack = e.getPlayer().getHeldItemMainhand();
		if (stack.isEmpty() || !stack.hasTag() || !isEffective(stack, e.getState())) return;
		Map<Affix, Float> affixes = AffixHelper.getAffixes(stack);
		if (affixes.containsKey(Affixes.SIFTING) && e.getWorld().getRandom().nextFloat() <= affixes.get(Affixes.SIFTING)) {
			Block.spawnAsEntity(e.getPlayer().world, e.getPos(), NUGGETS.get(e.getWorld().getRandom().nextInt(NUGGETS.size())).copy());
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

	@SubscribeEvent
	public void starting(FMLServerStartingEvent e) {
		e.getServer().getCommandManager().getDispatcher().register(LiteralArgumentBuilder.<CommandSource>literal("affixloot").requires(c -> c.hasPermissionLevel(2)).then(Commands.argument("rarity", StringArgumentType.word()).suggests((a, b) -> {
			Arrays.stream(LootRarity.values()).map(r -> r.toString()).forEach(b::suggest);
			return b.buildFuture();
		}).executes(c -> {
			PlayerEntity p = c.getSource().asPlayer();
			p.addItemStackToInventory(LootManager.genLootItem(LootManager.getRandomEntry(p.world.rand, null), p.world.rand, LootRarity.valueOf(c.getArgument("rarity", String.class))));
			return 0;
		})));
	}

}
