package shadows.apotheosis.spawn.spawner;

import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;

public class ApothSpawnerItem extends BlockItem {

	public ApothSpawnerItem() {
		super(Blocks.SPAWNER, new Item.Properties().tab(ItemGroup.TAB_MISC));
		this.setRegistryName("minecraft", "spawner");
	}

	@Override
	public String getCreatorModId(ItemStack itemStack) {
		return Apotheosis.MODID;
	}

	@Override
	public ITextComponent getName(ItemStack stack) {
		if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
			CompoundNBT tag = stack.getTag().getCompound("BlockEntityTag");
			if (tag.contains("SpawnData")) {
				String name = tag.getCompound("SpawnData").getString("id");
				String key = "entity." + name.replace(':', '.');
				TextFormatting color = TextFormatting.WHITE;
				try {
					EntityType<?> t = ForgeRegistries.ENTITIES.getValue(new ResourceLocation(name));
					EntityClassification cat = t.getCategory();
					switch (cat) {
					case CREATURE:
						color = TextFormatting.DARK_GREEN;
						break;
					case MONSTER:
						color = TextFormatting.RED;
						break;
					case WATER_AMBIENT:
					case WATER_CREATURE:
						color = TextFormatting.BLUE;
					default:
						break;
					}
				} catch (Exception ex) {

				}
				return new TranslationTextComponent("item.apotheosis.spawner", new TranslationTextComponent(key)).withStyle(color);
			}
		}
		return super.getName(stack);
	}

}
