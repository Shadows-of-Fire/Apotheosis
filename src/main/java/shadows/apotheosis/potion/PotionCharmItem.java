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

public class PotionCharmItem extends Item {

	public PotionCharmItem() {
		super(new Item.Properties().maxStackSize(1).maxDamage(192).group(ItemGroup.MISC));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (!hasPotion(stack)) return;
		if (stack.getOrCreateTag().getBoolean("charm_enabled") && entity instanceof ServerPlayerEntity) {
			Potion p = PotionUtils.getPotionFromItem(stack);
			EffectInstance contained = p.getEffects().get(0);
			EffectInstance active = ((ServerPlayerEntity) entity).getActivePotionEffect(contained.getPotion());
			if (active == null || active.getDuration() < (active.getPotion() == Effects.NIGHT_VISION ? 210 : 5)) {
				((ServerPlayerEntity) entity).addPotionEffect(new EffectInstance(contained.getPotion(), (int) Math.ceil(contained.getDuration() / 24D) + (contained.getPotion() == Effects.NIGHT_VISION ? 210 : 5), contained.getAmplifier(), false, false));
				if (stack.attemptDamageItem(1, world.rand, (ServerPlayerEntity) entity)) stack.shrink(1);
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
			tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc", new TranslationTextComponent(p.getEffects().get(0).getEffectName())).mergeStyle(TextFormatting.GRAY));
			tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc2").mergeStyle(TextFormatting.GRAY));
			tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".enabled", stack.getOrCreateTag().getBoolean("charm_enabled")).mergeStyle(TextFormatting.BLUE));
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
		return new TranslationTextComponent("item.apotheosis.potion_charm", new TranslationTextComponent(p.getEffects().get(0).getEffectName()));
	}

	protected boolean hasPotion(ItemStack stack) {
		return stack.hasTag() && stack.getTag().contains("Potion");
	}

	@Override
	public void fillItemGroup(ItemGroup group, NonNullList<ItemStack> stacks) {
		if (this.isInGroup(group)) {
			ItemStack stack = new ItemStack(this);
			PotionUtils.addPotionToItemStack(stack, Potions.LONG_INVISIBILITY);
			stacks.add(stack);
		}
	}

}
