package dev.shadowsoffire.apotheosis.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntFunction;

import com.google.common.base.Predicate;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.net.RadialStatePayload;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import io.netty.buffer.ByteBuf;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ByIdMap;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.HitResult.Type;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.network.PacketDistributor;

public class RadialUtil {

    private static ThreadLocal<Set<UUID>> breakers = ThreadLocal.withInitial(HashSet::new);

    /**
     * Updates the players radial state to the next state, and notifies them of the change.
     */
    public static void toggleRadialState(Player player) {
        RadialState state = RadialState.getState(player);
        RadialState next = state.next();
        RadialState.setState(player, next);
        player.sendSystemMessage(Apotheosis.sysMessageHeader().append(Apotheosis.lang("misc", "radial_state_updated", next.toComponent(), state.toComponent()).withStyle(ChatFormatting.YELLOW)));
        PacketDistributor.sendToPlayer((ServerPlayer) player, new RadialStatePayload(next));
    }

    /**
     * Executes the radial mining effect with the given context and radial data.
     * <p>
     * If radial mining is disabled for the player, this method does nothing.
     */
    public static void attemptRadialMining(BlockEvent.BreakEvent e, RadialData data) {
        Player player = e.getPlayer();
        if (RadialState.isRadialMiningEnabled(player)) {
            RadialUtil.breakExtraBlocks(player, e.getPos(), data);
        }
    }

    /**
     * Performs the actual extra breaking of blocks
     *
     * @param player          The player breaking the block
     * @param pos             The position of the originally broken block
     * @param tool            The tool being used (which has this affix on it)
     * @param data            The level of this affix, in this case, the mode of operation.
     * @param srcDestroySpeed The destroy speed of the block being broken.
     */
    public static void breakExtraBlocks(Player player, BlockPos pos, RadialData data) {
        if (!breakers.get().add(player.getUUID())) {
            return; // Prevent multiple break operations from cascading, and don't execute when sneaking.
        }

        try {
            breakBlockRadius(player, pos, data);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        breakers.get().remove(player.getUUID());
    }

    /**
     * Returns a list of all blocks that would be broken by the radial breaking operation.
     * <p>
     * The list of all blocks is eagerly computed and returned, so be careful with large radii.
     * 
     * @param player The player breaking the block
     * @param srcPos The position of the originally broken block
     * @param data   The level of this affix, in this case, the mode of operation.
     */
    public static List<BlockPos> getBrokenBlocks(Player player, Direction direction, BlockPos srcPos, RadialData data) {
        Level level = player.level();
        if (data.x < 2 && data.y < 2) {
            return List.of();
        }

        int lowerY = (int) Math.ceil(-data.y / 2D), upperY = (int) Math.round(data.y / 2D);
        int lowerX = (int) Math.ceil(-data.x / 2D), upperX = (int) Math.round(data.x / 2D);

        List<BlockPos> broken = new ArrayList<>();

        float srcDestroySpeed = level.getBlockState(srcPos).getDestroySpeed(level, srcPos);

        for (int iy = lowerY; iy < upperY; iy++) {
            for (int ix = lowerX; ix < upperX; ix++) {
                BlockPos genPos = new BlockPos(srcPos.getX() + ix + data.xOff, srcPos.getY() + iy + data.yOff, srcPos.getZ());

                if (player.getDirection().getAxis() == Axis.X) {
                    genPos = new BlockPos(genPos.getX() - (ix + data.xOff), genPos.getY(), genPos.getZ() + ix + data.xOff);
                }

                if (direction.getAxis().isVertical()) {
                    genPos = rotateDown(genPos, iy + data.yOff, player.getDirection());
                }

                if (genPos.equals(srcPos)) {
                    continue;
                }

                BlockState state = level.getBlockState(genPos);
                float stateDestroySpeed = state.getDestroySpeed(level, genPos);
                if (!state.isAir() && stateDestroySpeed != -1 && stateDestroySpeed <= srcDestroySpeed * 3F && isEffective(state, player, genPos)) {
                    broken.add(genPos);
                }
            }
        }

        return broken;
    }

    /**
     * Traces the player's look vector and returns the result.
     */
    public static HitResult tracePlayerLook(Player player) {
        Vec3 base = player.getEyePosition(0);
        Vec3 look = player.getLookAngle();
        double reach = player.getAttributeValue(Attributes.BLOCK_INTERACTION_RANGE);
        Vec3 target = base.add(look.x * reach, look.y * reach, look.z * reach);
        Level level = player.level();
        return level.clip(new ClipContext(base, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
    }

    public static void breakBlockRadius(Player player, BlockPos srcPos, RadialData data) {
        HitResult trace = tracePlayerLook(player);
        if (trace == null || trace.getType() != Type.BLOCK) {
            return;
        }

        BlockHitResult res = (BlockHitResult) trace;
        Direction face = res.getDirection(); // Face of the block currently being looked at by the player.

        List<BlockPos> broken = getBrokenBlocks(player, face, srcPos, data);
        for (BlockPos pos : broken) {
            if (player instanceof ServerPlayer sp) {
                sp.gameMode.destroyBlock(pos);
            }
            else {
                // TODO: This should be used when BreakEvent is fired on the client, but currently this is unreachable.
                ClientAccess.breakClientBlock(pos);
            }
        }
    }

    static BlockPos rotateDown(BlockPos pos, int y, Direction horizontal) {
        Vec3i vec = horizontal.getNormal();
        return new BlockPos(pos.getX() + vec.getX() * y, pos.getY() - y, pos.getZ() + vec.getZ() * y);
    }

    public static boolean isEffective(BlockState state, Player player, BlockPos pos) {
        return player.hasCorrectToolForDrops(state, player.level(), pos);
    }

    public static record RadialData(int x, int y, int xOff, int yOff) {

        public static Codec<RadialData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                Codec.INT.fieldOf("x").forGetter(RadialData::x),
                Codec.INT.fieldOf("y").forGetter(RadialData::y),
                Codec.INT.fieldOf("xOff").forGetter(RadialData::xOff),
                Codec.INT.fieldOf("yOff").forGetter(RadialData::yOff))
            .apply(inst, RadialData::new));

    }

    public static enum RadialState {
        REQUIRE_NOT_SNEAKING(p -> !p.isShiftKeyDown()),
        REQUIRE_SNEAKING(Player::isShiftKeyDown),
        ENABLED(p -> true),
        DISABLED(p -> false);

        public static final IntFunction<RadialState> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
        public static final Codec<RadialState> CODEC = PlaceboCodecs.enumCodec(RadialState.class);
        public static final StreamCodec<ByteBuf, RadialState> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

        private Predicate<Player> condition;

        RadialState(Predicate<Player> condition) {
            this.condition = condition;
        }

        /**
         * @return If the radial breaking feature is enabled while the player is in the current state
         */
        public static boolean isRadialMiningEnabled(Player input) {
            return getState(input).condition.apply(input);
        }

        public RadialState next() {
            return switch (this) {
                case REQUIRE_NOT_SNEAKING -> REQUIRE_SNEAKING;
                case REQUIRE_SNEAKING -> ENABLED;
                case ENABLED -> DISABLED;
                case DISABLED -> REQUIRE_NOT_SNEAKING;
            };
        }

        public Component toComponent() {
            return Component.translatable("misc.apotheosis.radial_state." + this.name().toLowerCase(Locale.ROOT));
        }

        /**
         * Returns the current radial break state for the given player.
         *
         * @param player The player
         * @return The current radial state, defaulting to {@link #REQUIRE_NOT_SNEAKING}.
         */
        public static RadialState getState(Player player) {
            return player.getData(Apoth.Attachments.RADIAL_MINING_MODE);
        }

        public static void setState(Player player, RadialState state) {
            player.setData(Apoth.Attachments.RADIAL_MINING_MODE, state);
        }
    }

    private static class ClientAccess {

        public static void breakClientBlock(BlockPos pos) {
            Minecraft.getInstance().gameMode.destroyBlock(pos);
        }
    }

}
