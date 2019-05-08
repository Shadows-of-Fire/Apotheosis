package shadows.garden;

import java.util.List;

import net.minecraft.client.resources.I18n;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.SoundEvents;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.storage.AnvilChunkLoader;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import shadows.Apotheosis;

public class ItemFarmersLeash extends Item {

	public ItemFarmersLeash() {
		this.setRegistryName(Apotheosis.MODID, "farmers_leash");
		this.setTranslationKey(Apotheosis.MODID + "." + getRegistryName().getPath());
		this.setCreativeTab(CreativeTabs.MISC);
		this.setMaxStackSize(1);
		this.setMaxDamage(15);
	}

	@Override
	public boolean isEnchantable(ItemStack stack) {
		return false;
	}

	@Override
	public boolean onLeftClickEntity(ItemStack stack, EntityPlayer player, Entity entity) {
		if (stack.getOrCreateSubCompound("entity_data").isEmpty() && entity instanceof EntityAnimal) {
			NBTTagCompound tag = new NBTTagCompound();
			if (entity.writeToNBTAtomically(tag)) {
				if (!player.world.isRemote) {
					entity.setDead();
					stack.getTagCompound().setTag("entity_data", tag);
					playSound(player);
				}
				return true;
			}
		}
		return super.onLeftClickEntity(stack, player, entity);
	}

	@Override
	public EnumActionResult onItemUse(EntityPlayer player, World world, BlockPos pos, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
		NBTTagCompound tag = player.getHeldItem(hand).getOrCreateSubCompound("entity_data");
		if (!tag.isEmpty()) {
			pos = pos.offset(facing);
			if (!world.isRemote) {
				Entity e = AnvilChunkLoader.readWorldEntityPos(tag, world, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, true);
				if (e != null) {
					player.getHeldItem(hand).getTagCompound().removeTag("entity_data");
					playSound(player);
					player.getHeldItem(hand).damageItem(1, player);
					return EnumActionResult.SUCCESS;
				}
			}
		}
		return EnumActionResult.FAIL;
	}

	@Override
	public NBTTagCompound getNBTShareTag(ItemStack stack) {
		NBTTagCompound tag = super.getNBTShareTag(stack);
		if (tag == null) return null;
		tag = tag.copy();
		NBTTagCompound entity = new NBTTagCompound();
		if (tag.getCompoundTag("entity_data").hasKey("id")) entity.setString("id", tag.getCompoundTag("entity_data").getString("id"));
		tag.setTag("entity_data", entity);
		return tag;
	}

	void playSound(EntityPlayer player) {
		player.world.playSound(null, player.getPosition(), SoundEvents.ENTITY_ENDERMEN_TELEPORT, SoundCategory.AMBIENT, 1, 1);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack stack, World worldIn, List<String> tooltip, ITooltipFlag flagIn) {
		if (stack.hasTagCompound()) {
			NBTTagCompound tag = stack.getOrCreateSubCompound("entity_data");
			if (tag.isEmpty()) tooltip.add(I18n.format("info.apotheosis.noentity"));
			else {
				String name = EntityList.getTranslationName(new ResourceLocation(tag.getString("id")));
				tooltip.add(I18n.format("info.apotheosis.containedentity", name));
			}
		}
	}

	@Override
	public boolean hasEffect(ItemStack stack) {
		return stack.hasTagCompound() && !stack.getOrCreateSubCompound("entity_data").isEmpty();
	}

}
