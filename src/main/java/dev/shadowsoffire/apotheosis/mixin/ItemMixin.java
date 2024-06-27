package dev.shadowsoffire.apotheosis.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.table.IEnchantableItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

@Mixin(Item.class)
public class ItemMixin implements IEnchantableItem {

    /**
     * @author Shadows
     * @reason Enables all items to be enchantable by default.
     * @return
     */
    @Overwrite
    public int getEnchantmentValue() {
        return Apotheosis.enableEnch ? 1 : 0;
    }

    @Redirect(method = "isEnchantable(Lnet/minecraft/world/item/ItemStack;)Z", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/Item;isDamageable(Lnet/minecraft/world/item/ItemStack;)Z", remap = false))
    private boolean apoth_ignoreDamageForEnchantable(Item ths, ItemStack stack) {
        return true;
    }

}
