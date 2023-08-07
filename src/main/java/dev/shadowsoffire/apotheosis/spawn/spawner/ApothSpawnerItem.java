package dev.shadowsoffire.apotheosis.spawn.spawner;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.registries.ForgeRegistries;

public class ApothSpawnerItem extends BlockItem {

    public ApothSpawnerItem() {
        super(Blocks.SPAWNER, new Item.Properties());
    }

    @Override
    public String getCreatorModId(ItemStack itemStack) {
        return Apotheosis.MODID;
    }

    @Override
    public Component getName(ItemStack stack) {
        if (stack.hasTag() && stack.getTag().contains("BlockEntityTag")) {
            CompoundTag tag = stack.getTag().getCompound("BlockEntityTag");
            if (tag.contains("SpawnData")) {
                String name = tag.getCompound("SpawnData").getCompound("entity").getString("id");
                String key = "entity." + name.replace(':', '.');
                ChatFormatting color = ChatFormatting.WHITE;
                try {
                    EntityType<?> t = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(name));
                    MobCategory cat = t.getCategory();
                    switch (cat) {
                        case CREATURE:
                            color = ChatFormatting.DARK_GREEN;
                            break;
                        case MONSTER:
                            color = ChatFormatting.RED;
                            break;
                        case WATER_AMBIENT:
                        case WATER_CREATURE:
                            color = ChatFormatting.BLUE;
                        default:
                            break;
                    }
                }
                catch (Exception ex) {

                }
                return Component.translatable("item.apotheosis.spawner", Component.translatable(key)).withStyle(color);
            }
        }
        return super.getName(stack);
    }

}
