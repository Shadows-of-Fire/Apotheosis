package shadows.apotheosis.ench.enchantments.twisted;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.DiggingEnchantment;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class MinersFervorEnchant extends DiggingEnchantment {

    public MinersFervorEnchant() {
        super(Rarity.RARE, new EquipmentSlot[] { EquipmentSlot.MAINHAND });
    }

    @Override
    public int getMinCost(int enchantmentLevel) {
        return 45 + (enchantmentLevel - 1) * 30;
    }

    @Override
    public int getMaxCost(int enchantmentLevel) {
        return this.getMinCost(enchantmentLevel) + 50;
    }

    @Override
    public int getMaxLevel() {
        return 5;
    }

    @Override
    public Component getFullname(int level) {
        return ((MutableComponent) super.getFullname(level)).withStyle(ChatFormatting.DARK_PURPLE);
    }

    @Override
    protected boolean checkCompatibility(Enchantment e) {
        return super.checkCompatibility(e) && e != Enchantments.BLOCK_EFFICIENCY;
    }

    public void breakSpeed(PlayerEvent.BreakSpeed e) {
        Player p = e.getEntity();
        ItemStack stack = p.getMainHandItem();
        if (stack.isEmpty()) return;
        int level = stack.getEnchantmentLevel(this);
        if (level > 0) {
            if (stack.getDestroySpeed(e.getState()) > 1.0F) {
                float hardness = e.getState().getDestroySpeed(p.level, e.getPosition().orElse(BlockPos.ZERO));
                e.setNewSpeed(Math.min(29.9999F, 7.5F + 4.5F * level) * hardness);
            }
        }
    }

}
