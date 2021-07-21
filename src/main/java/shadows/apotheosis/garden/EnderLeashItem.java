package shadows.apotheosis.garden;

import java.util.List;

import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import shadows.apotheosis.Apotheosis;

public class EnderLeashItem extends Item {

	public EnderLeashItem() {
		super(new Item.Properties().stacksTo(1).durability(15).tab(Apotheosis.APOTH_GROUP));
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, PlayerEntity player, Entity entity) {
		if (stack.getOrCreateTagElement("entity_data").isEmpty() && entity instanceof AnimalEntity) {
			CompoundNBT tag = entity.serializeNBT();
			if (!player.level.isClientSide) {
				entity.remove();
				stack.getTag().put("entity_data", tag);
				stack.getTag().putString("name", entity.getDisplayName().getString());
				this.playSound(player);
			}
			return true;

		}
		return super.onLeftClickEntity(stack, player, entity);

	}

	@Override
	public ActionResultType useOn(ItemUseContext ctx) {
		CompoundNBT tag = ctx.getItemInHand().getOrCreateTagElement("entity_data");
		if (!tag.isEmpty()) {
			BlockPos pos = ctx.getClickedPos().relative(ctx.getClickedFace());
			if (!ctx.getLevel().isClientSide) {
				Entity e = EntityType.loadEntityRecursive(tag, ctx.getLevel(), a -> a);
				if (e != null) {
					e.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
					((ServerWorld) ctx.getLevel()).loadFromChunk(e);
					ctx.getItemInHand().getTag().remove("entity_data");
					this.playSound(ctx.getPlayer());
					ctx.getItemInHand().hurtAndBreak(1, ctx.getPlayer(), pl -> pl.broadcastBreakEvent(ctx.getHand()));
					return ActionResultType.SUCCESS;
				}
			}
		}
		return ActionResultType.FAIL;
	}

	@Override
	public CompoundNBT getShareTag(ItemStack stack) {
		CompoundNBT tag = super.getShareTag(stack);
		if (tag == null) return null;
		tag = tag.copy();
		CompoundNBT entity = new CompoundNBT();
		if (tag.getCompound("entity_data").contains("id")) entity.putString("id", tag.getCompound("entity_data").getString("id"));
		tag.put("entity_data", entity);
		return tag;
	}

	void playSound(PlayerEntity player) {
		player.level.playSound(null, player.blockPosition(), SoundEvents.ENDERMAN_TELEPORT, SoundCategory.AMBIENT, 1, 1);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTag()) {
			CompoundNBT tag = stack.getOrCreateTagElement("entity_data");
			if (tag.isEmpty()) tooltip.add(new TranslationTextComponent("info.apotheosis.noentity"));
			else {
				tooltip.add(new TranslationTextComponent("info.apotheosis.containedentity", stack.getTag().getString("name")));
			}
		}
	}

	@Override
	public boolean isFoil(ItemStack stack) {
		return stack.hasTag() && !stack.getOrCreateTagElement("entity_data").isEmpty();
	}

}