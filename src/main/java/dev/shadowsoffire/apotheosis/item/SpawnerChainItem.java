package dev.shadowsoffire.apotheosis.item;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.item.ItemStack;

/**
 * Spawner Chains are dropped when a Rogue Spawner is broken, which often happens in hostile surroundings.
 * <p>
 * To avoid the drop being destroyed before it can be picked up, the item entity is immune to fire and explosion damage.
 */
public class SpawnerChainItem extends TooltipItem {

    public SpawnerChainItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canBeHurtBy(ItemStack stack, DamageSource src) {
        return super.canBeHurtBy(stack, src) && !src.is(DamageTypeTags.IS_FIRE) && !src.is(DamageTypeTags.IS_EXPLOSION);
    }

}
