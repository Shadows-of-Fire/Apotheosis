package shadows.apotheosis.potion;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class InvisCharmItem extends Item {

	public InvisCharmItem() {
		super(new Item.Properties().maxStackSize(1).maxDamage(64).group(ItemGroup.MISC));
	}

	@Override
	public void inventoryTick(ItemStack stack, World world, Entity entity, int slot, boolean isSelected) {
		if (stack.getOrCreateTag().getBoolean("charm_enabled") && entity instanceof ServerPlayerEntity && ((ServerPlayerEntity) entity).getActivePotionEffect(Effects.INVISIBILITY) == null) {
			((ServerPlayerEntity) entity).addPotionEffect(new EffectInstance(Effects.INVISIBILITY, 400, 0, false, false));
			if (stack.attemptDamageItem(1, world.rand, (ServerPlayerEntity) entity)) stack.shrink(1);
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
		return ActionResult.success(stack);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void addInformation(ItemStack stack, World world, List<ITextComponent> tooltip, ITooltipFlag flag) {
		tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".desc").applyTextStyle(TextFormatting.GRAY));
		tooltip.add(new TranslationTextComponent(this.getTranslationKey() + ".enabled", stack.getOrCreateTag().getBoolean("charm_enabled")).applyTextStyle(TextFormatting.BLUE));
	}

}
