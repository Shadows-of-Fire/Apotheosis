package shadows.apotheosis.potion;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionUtils;
import net.minecraft.potion.Potions;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;

public class PotionCharmItem extends Item {

	public PotionCharmItem() {
		super(new Item.Properties().maxStackSize(1).maxDamage(192).group(Apotheosis.APOTH_GROUP));
	}

	@Override
	public ItemStack getDefaultInstance() {
		return PotionUtils.addPotionToItemStack(super.getDefaultInstance(), Potions.LONG_INVISIBILITY);
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!hasPotion(stack)) return;
		if (stack.getOrCreateTag().getBoolean("charm_enabled") && entity instanceof ServerPlayerEntity) {
			Potion p = PotionUtils.getPotionFromItem(stack);
			EffectInstance contained = p.getEffects().get(0);
			EffectInstance active = ((ServerPlayerEntity) entity).getActivePotionEffect(contained.getPotion());
			if (active == null || active.getDuration() < (active.getPotion() == Effects.NIGHT_VISION ? 210 : 5)) {
				int durationOffset = contained.getPotion() == Effects.NIGHT_VISION ? 210 : 5;
				if (contained.getPotion() == Effects.REGENERATION) durationOffset += 50 >> contained.getAmplifier();
				EffectInstance newEffect = new EffectInstance(contained.getPotion(), (int) Math.ceil(contained.getDuration() / 24D) + durationOffset, contained.getAmplifier(), false, false);
				((ServerPlayerEntity) entity).addPotionEffect(newEffect);
				if (stack.attemptDamageItem(contained.getPotion() == Effects.REGENERATION ? 2 : 1, world.rand, (ServerPlayerEntity) entity)) stack.shrink(1);
			}
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.getOrCreateTag().getBoolean("charm_enabled");
	}

	@Override
	public ActionResult<ItemStack> onItemRightClick(World world, PlayerEntity player, Hand hand) {
		ItemStack stack = player.getHeldItem(hand);
		if (!world.isRemote) {
			stack.getOrCreateTag().putBoolean("charm_enabled", !stack.getTag().getBoolean("charm_enabled"));
		} else if (!stack.getTag().getBoolean("charm_enabled")) world.playSound(player, player.getPosition(), SoundEvents.ENTITY_EXPERIENCE_ORB_PICKUP, SoundCategory.PLAYERS, 1, 0.3F);
		return ActionResult.resultSuccess(stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		if (hasPotion(stack)) {
			Potion p = PotionUtils.getPotionFromItem(stack);
			EffectInstance effect = p.getEffects().get(0);
			TranslationTextComponent potionCmp = new TranslationTextComponent(effect.getEffectName());
			if (effect.getAmplifier() > 0) {
				potionCmp = new TranslationTextComponent("potion.withAmplifier", potionCmp, new TranslationTextComponent("potion.potency." + effect.getAmplifier()));
			}
			potionCmp.mergeStyle(effect.getPotion().getEffectType().getColor());
			tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc", potionCmp).mergeStyle(TextFormatting.GRAY));
			boolean enabled = stack.getOrCreateTag().getBoolean("charm_enabled");
			TranslationTextComponent enabledCmp = new TranslationTextComponent(this.getTranslationKey() + (enabled ? ".enabled" : ".disabled"));
			enabledCmp.mergeStyle(enabled ? TextFormatting.BLUE : TextFormatting.RED);
			if (effect.getDuration() > 20) {
				potionCmp = new TranslationTextComponent("potion.withDuration", potionCmp, EffectUtils.getPotionDurationString(effect, 1));
			}
			potionCmp.mergeStyle(effect.getPotion().getEffectType().getColor());
			tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc3", potionCmp).mergeStyle(TextFormatting.GRAY));
		}
	}

	@Override
	public int getMaxDamage(ItemStack stack) {
		if (!hasPotion(stack)) return 1;
		return 192;
	}

	@Override
	public ITextComponent getDisplayName(ItemStack stack) {
		if (!hasPotion(stack)) return new TranslationTextComponent("item.apotheosis.potion_charm_broke");
		Potion p = PotionUtils.getPotionFromItem(stack);
		EffectInstance effect = p.getEffects().get(0);
		TranslationTextComponent potionCmp = new TranslationTextComponent(effect.getEffectName());
		if (effect.getAmplifier() > 0) {
			potionCmp = new TranslationTextComponent("potion.withAmplifier", potionCmp, new TranslationTextComponent("potion.potency." + effect.getAmplifier()));
		}
		return new TranslationTextComponent("item.apotheosis.potion_charm", potionCmp);
	}

	public static boolean hasPotion(ItemStack stack) {
		return PotionUtils.getPotionFromItem(stack) != Potions.EMPTY;
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> items) {
		if (this.isInGroup(group)) {
			for (Potion potion : ForgeRegistries.POTION_TYPES) {
				if (potion.getEffects().size() == 1 && !potion.getEffects().get(0).getPotion().isInstant()) {
					items.add(PotionUtils.addPotionToItemStack(new ItemStack(this), potion));
				}
			}
		}

	}

}