package shadows.apotheosis.adventure.affix.effect;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import com.google.common.collect.ImmutableList;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.minecraftforge.event.entity.player.PlayerEvent.HarvestCheck;
import net.minecraftforge.registries.IRegistryDelegate;
import shadows.apotheosis.Apoth.Affixes;
import shadows.apotheosis.adventure.affix.Affix;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.affix.AffixInstance;
import shadows.apotheosis.adventure.affix.AffixType;
import shadows.apotheosis.adventure.loot.LootCategory;
import shadows.apotheosis.adventure.loot.LootRarity;

public class OmneticAffix extends Affix {

	private static final List<IRegistryDelegate<Item>> TIERS = buildTierArray();
	private static String[] descs = { "misc.apotheosis.iron", "misc.apotheosis.diamond", "misc.apotheosis.netherite" };

	public OmneticAffix() {
		super(AffixType.EFFECT);
	}

	@Override
	public boolean canApplyTo(ItemStack stack, LootRarity rarity) {
		return LootCategory.forItem(stack) == LootCategory.BREAKER;
	}

	@Override
	public void addInformation(ItemStack stack, LootRarity rarity, float level, Consumer<Component> list) {
		list.accept(new TranslatableComponent("affix." + this.getRegistryName() + ".desc", new TranslatableComponent(descs[getListOffset(rarity) / 3])).withStyle(ChatFormatting.YELLOW));
	}

	private static List<IRegistryDelegate<Item>> buildTierArray() {
		List<IRegistryDelegate<Item>> items = new ArrayList<>();
		items.add(Items.IRON_PICKAXE.delegate);
		items.add(Items.IRON_AXE.delegate);
		items.add(Items.IRON_SHOVEL.delegate);
		items.add(Items.DIAMOND_PICKAXE.delegate);
		items.add(Items.DIAMOND_AXE.delegate);
		items.add(Items.DIAMOND_SHOVEL.delegate);
		items.add(Items.NETHERITE_PICKAXE.delegate);
		items.add(Items.NETHERITE_AXE.delegate);
		items.add(Items.NETHERITE_SHOVEL.delegate);
		return ImmutableList.copyOf(items);
	}

	public void harvest(HarvestCheck e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			AffixInstance inst = AffixHelper.getAffixes(stack).get(Affixes.OMNETIC);
			if (inst != null) {
				for (int i = 0; i < 3; i++) {
					Item item = TIERS.get(getListOffset(inst.rarity()) + i).get();
					if (item.isCorrectToolForDrops(e.getTargetBlock())) {
						e.setCanHarvest(true);
						return;
					}
				}
			}
		}
	}

	// EventPriority.HIGHEST
	public void speed(BreakSpeed e) {
		ItemStack stack = e.getPlayer().getMainHandItem();
		if (!stack.isEmpty()) {
			AffixInstance inst = AffixHelper.getAffixes(stack).get(Affixes.OMNETIC);
			if (inst != null) {
				float speed = e.getOriginalSpeed();
				for (int i = 0; i < 3; i++) {
					Item item = TIERS.get(getListOffset(inst.rarity()) + i).get();
					speed = Math.max(getBaseSpeed(e.getPlayer(), item, e.getState(), e.getPos()), speed);
				}
				e.setNewSpeed(speed);
			}
		}
	}

	private static int getListOffset(LootRarity rarity) {
		return 3 * Mth.clamp(rarity.ordinal() - LootRarity.RARE.ordinal(), 0, 2);
	}

	static float getBaseSpeed(Player player, Item tool, BlockState state, BlockPos pos) {
		float f = tool.getDestroySpeed(ItemStack.EMPTY, state);
		if (f > 1.0F) {
			int i = EnchantmentHelper.getBlockEfficiency(player);
			ItemStack itemstack = player.getMainHandItem();
			if (i > 0 && !itemstack.isEmpty()) {
				f += i * i + 1;
			}
		}

		if (MobEffectUtil.hasDigSpeed(player)) {
			f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
		}

		if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
			float f1;
			switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
			case 0:
				f1 = 0.3F;
				break;
			case 1:
				f1 = 0.09F;
				break;
			case 2:
				f1 = 0.0027F;
				break;
			case 3:
			default:
				f1 = 8.1E-4F;
			}

			f *= f1;
		}

		if (player.isEyeInFluid(FluidTags.WATER) && !EnchantmentHelper.hasAquaAffinity(player)) {
			f /= 5.0F;
		}

		if (!player.isOnGround()) {
			f /= 5.0F;
		}
		return f;
	}

}
