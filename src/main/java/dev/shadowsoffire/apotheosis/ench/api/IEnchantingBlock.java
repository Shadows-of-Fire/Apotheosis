package dev.shadowsoffire.apotheosis.ench.api;

import java.util.Collections;
import java.util.Set;

import org.jetbrains.annotations.ApiStatus;

import dev.shadowsoffire.apotheosis.ench.table.EnchantingStatRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.extensions.IForgeBlock;

/**
 * This is the main interface for all blocks that provide stats to an enchanting table.
 * The stats allocated to an enchanting table are as follows:
 * Enchanting Power: This is the primary stat, which controls the level at which the table enchants at.
 * Flux: This stat controls how volatile the real level is. With the base flux, the table ranges from -15% to +30% of the original.
 * Enchantment Count: This controls the number of enchantments that can be placed on an item.
 */
public interface IEnchantingBlock extends IForgeBlock {

    /**
     * Determines the maximum enchanting power (Eterna) that this block may contribute up to.
     *
     * @return The max Eterna this block may contribute towards. Once past this value, this block has no effect.
     * @apiNote Call via {@link EnchantingStatRegistry#getMaxEterna(BlockState, Level, BlockPos)}
     */
    @ApiStatus.OverrideOnly
    default float getMaxEnchantingPower(BlockState state, LevelReader world, BlockPos pos) {
        return 15F;
    }

    /**
     * Determines the amount of Quanta this block provides to an enchanting table.
     *
     * @return The change in Quanta caused by this block.
     * @apiNote Call via {@link EnchantingStatRegistry#getQuanta(BlockState, Level, BlockPos)}
     */
    @ApiStatus.OverrideOnly
    default float getQuantaBonus(BlockState state, LevelReader world, BlockPos pos) {
        return 0;
    }

    /**
     * Determines the amount of Arcana this block provides to an enchanting table.
     *
     * @return The change in Arcana caused by this block.
     * @apiNote Call via {@link EnchantingStatRegistry#getArcana(BlockState, Level, BlockPos)}
     */
    @ApiStatus.OverrideOnly
    default float getArcanaBonus(BlockState state, LevelReader world, BlockPos pos) {
        return 0;
    }

    /**
     * Determines how much "Quanta Rectification" this block provides.
     * 1F of Rectification reduces the negative threshold by 1%
     * [-Q, +Q] -> [-(1-QR/100F)Q, +Q]
     * At 100%, quanta only has a positive effect.
     *
     * @return The Quanta Rectification bonus from this block.
     * @apiNote Call via {@link EnchantingStatRegistry#getQuantaRectification(BlockState, Level, BlockPos)}
     */
    @ApiStatus.OverrideOnly
    default float getQuantaRectification(BlockState state, LevelReader world, BlockPos pos) {
        return 0;
    }

    /**
     * Determines how many extra clues can be viewed by the client when this block is present.
     *
     * @return The number of bonus clues to show.
     * @apiNote Call via {@link EnchantingStatRegistry#getBonusClues(BlockState, Level, BlockPos)}
     */
    @ApiStatus.OverrideOnly
    default int getBonusClues(BlockState state, LevelReader world, BlockPos pos) {
        return 0;
    }

    /**
     * Blacklisted enchantments are prevented from being rolled in the enchanting table, as if they were not discoverable.
     *
     * @return A list of all enchantments that are blacklisted by the presence of this block.
     */
    default Set<Enchantment> getBlacklistedEnchantments(BlockState state, LevelReader world, BlockPos pos) {
        return Collections.emptySet();
    }

    /**
     * Spawns Enchant particles in the world flowing towards the Enchanting Table.<br>
     * Only called on the client.
     *
     * @param state  The state of this block.
     * @param level  The level.
     * @param rand   The random.
     * @param pos    The position of the enchanting table.
     * @param offset The position of this shelf, relative to the table.
     */
    default void spawnTableParticle(BlockState state, Level level, RandomSource rand, BlockPos pos, BlockPos offset) {
        if (rand.nextInt(16) == 0) {
            if (EnchantingStatRegistry.getEterna(level.getBlockState(pos.offset(offset)), level, pos.offset(offset)) > 0) {
                if (level.isEmptyBlock(pos.offset(offset.getX() / 2, 0, offset.getZ() / 2))) {
                    level.addParticle(this.getTableParticle(state), pos.getX() + 0.5D, pos.getY() + 2.0D, pos.getZ() + 0.5D, offset.getX() + rand.nextFloat() - 0.5D, offset.getY() - rand.nextFloat() - 1.0F,
                        offset.getZ() + rand.nextFloat() - 0.5D);
                }
            }
        }
    }

    /**
     * To avoid having to duplicate the logic in {@link #spawnTableParticle} just to change the particle type,
     * this method is provided.<br>
     * If you need to do anything more complex, then override {@link #spawnTableParticle}
     *
     * @param state The state of this block.
     * @return The particle type this block will spawn when near an Enchanting Table.
     */
    default ParticleOptions getTableParticle(BlockState state) {
        return ParticleTypes.ENCHANT;
    }

    /**
     * Enchanting tables normally cannot roll treasure enchantments, but if a bookshelf block permits it, they can.
     *
     * @return If this block allows the table to roll treasure enchantments.
     */
    default boolean allowsTreasure(BlockState state, LevelReader world, BlockPos pos) {
        return false;
    }

}
