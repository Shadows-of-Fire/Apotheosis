package dev.shadowsoffire.apotheosis.util;

import java.util.Arrays;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticUtil {

    /**
     * Applies the Omnetic data to the break speed event. This is done by calculating the break speed for each omnetic tool and selecting the maximum speed.
     * <p>
     * If the max omnetic-provided speed is lower than the current speed, nothing changes.
     */
    public static void applyOmneticData(PlayerEvent.BreakSpeed e, OmneticData data) {
        float speed = e.getOriginalSpeed();
        for (ItemStack item : data.items()) {
            speed = Math.max(OmneticUtil.getBaseSpeed(e.getEntity(), item, e.getState(), e.getPosition().orElse(BlockPos.ZERO)), speed);
        }
        e.setNewSpeed(Math.max(speed, e.getNewSpeed()));
    }

    /**
     * Applies the Omnetic data to the harvest check event. This is done by checking if any of the omnetic tools can harvest the block.
     */
    public static void applyOmneticData(HarvestCheck e, OmneticData data) {
        for (ItemStack item : data.items()) {
            if (item.isCorrectToolForDrops(e.getTargetBlock())) {
                e.setCanHarvest(true);
                break;
            }
        }
    }

    /**
     * Resolves the base dig speed for a player. This is effectively a copy of {@link Player#getDigSpeed}
     * with the event-firing code near the end removed.
     */
    public static float getBaseSpeed(Player player, ItemStack tool, BlockState state, BlockPos pos) {
        float f = tool.getDestroySpeed(state);
        if (f > 1.0F) {
            f += (float) player.getAttributeValue(Attributes.MINING_EFFICIENCY);
        }

        if (MobEffectUtil.hasDigSpeed(player)) {
            f *= 1.0F + (MobEffectUtil.getDigSpeedAmplification(player) + 1) * 0.2F;
        }

        if (player.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            f *= switch (player.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) {
                case 0 -> 0.3F;
                case 1 -> 0.09F;
                case 2 -> 0.0027F;
                default -> 8.1E-4F;
            };
        }

        f *= (float) player.getAttributeValue(Attributes.BLOCK_BREAK_SPEED);
        if (player.isEyeInFluidType(NeoForgeMod.WATER_TYPE.value())) {
            f *= (float) player.getAttribute(Attributes.SUBMERGED_MINING_SPEED).getValue();
        }

        if (!player.onGround()) {
            f /= 5.0F;
        }

        return f;
    }

    public static record OmneticData(String name, ItemStack[] items) {

        public static Codec<OmneticData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.STRING.fieldOf("name").forGetter(OmneticData::name),
                Codec.list(ItemStack.CODEC).xmap(l -> l.toArray(new ItemStack[0]), Arrays::asList).fieldOf("items").forGetter(OmneticData::items))
            .apply(inst, OmneticData::new));

    }
}
