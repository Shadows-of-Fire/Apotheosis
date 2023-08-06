package dev.shadowsoffire.apotheosis.ench.anvil;

import java.util.Map;

import dev.shadowsoffire.apotheosis.Apoth;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class AnvilTile extends BlockEntity {

    protected final Object2IntMap<Enchantment> enchantments = new Object2IntOpenHashMap<>();

    public AnvilTile(BlockPos pos, BlockState state) {
        super(Apoth.Tiles.ANVIL.get(), pos, state);
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        ItemStack stack = new ItemStack(Items.ANVIL);
        EnchantmentHelper.setEnchantments(this.enchantments, stack);
        tag.put("enchantments", stack.getEnchantmentTags());
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        ListTag enchants = tag.getList("enchantments", Tag.TAG_COMPOUND);
        Map<Enchantment, Integer> map = EnchantmentHelper.deserializeEnchantments(enchants);
        this.enchantments.clear();
        this.enchantments.putAll(map);
    }

    public Object2IntMap<Enchantment> getEnchantments() {
        return this.enchantments;
    }

}
