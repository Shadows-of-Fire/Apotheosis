package dev.shadowsoffire.apotheosis.tiers;

import java.util.Arrays;
import java.util.Map;
import java.util.function.IntFunction;

import com.mojang.serialization.Codec;
import com.mojang.serialization.Keyable;
import com.mojang.serialization.MapCodec;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.net.WorldTierPayload;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apotheosis.util.ApothMiscUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.util.ByIdMap;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.player.Player;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.network.PacketDistributor;

/**
 * World Tiers for Apothic content, each increasing the quality of loot received and the overall strength of the monsters in the world.
 * <p>
 * The final tier, Apotheosis, allows for endlessly increasing the difficulty in exchange for additional rewards, but does not
 * adjust weights or availability of content.
 */
public enum WorldTier implements StringRepresentable {
    HAVEN("haven"),
    FRONTIER("frontier"),
    ASCENT("ascent"),
    SUMMIT("summit"),
    PINNACLE("pinnacle");

    public static final IntFunction<WorldTier> BY_ID = ByIdMap.continuous(Enum::ordinal, values(), ByIdMap.OutOfBoundsStrategy.ZERO);
    public static final Codec<WorldTier> CODEC = StringRepresentable.fromValues(WorldTier::values);
    public static final StreamCodec<ByteBuf, WorldTier> STREAM_CODEC = ByteBufCodecs.idMapper(BY_ID, Enum::ordinal);

    private String name;

    private WorldTier(String name) {
        this.name = name;
    }

    @Override
    public String getSerializedName() {
        return this.name;
    }

    public MutableComponent toComponent() {
        return Apotheosis.lang("text", "world_tier." + this.getSerializedName());
    }

    public ResourceLocation getUnlockAdvancement() {
        return switch (this) {
            case HAVEN -> Apoth.Advancements.WORLD_TIER_HAVEN;
            case FRONTIER -> Apoth.Advancements.WORLD_TIER_FRONTIER;
            case ASCENT -> Apoth.Advancements.WORLD_TIER_ASCENT;
            case SUMMIT -> Apoth.Advancements.WORLD_TIER_SUMMIT;
            case PINNACLE -> Apoth.Advancements.WORLD_TIER_PINNACLE;
        };
    }

    /**
     * Returns the current world tier for a player.
     * <p>
     * For real players, this returns the value of {@link Attachments#WORLD_TIER}.
     * <p>
     * For fake players, this method first attempts to resolve the real player with the same UUID, and retrieve their world tier.
     * If it is successfully able to do so, it will also attach the real player's world tier to the fake player, allowing the
     * correct one to be resolved when the player is offline.
     */
    public static WorldTier getTier(Player player) {
        if (player instanceof FakePlayer fp) {
            MinecraftServer server = fp.getServer();
            ServerPlayer realPlayer = server.getPlayerList().getPlayer(fp.getUUID());
            if (realPlayer != null) {
                WorldTier realTier = getTier(realPlayer);
                fp.setData(Attachments.WORLD_TIER, realTier);
                return realTier;
            }
        }
        return player.getData(Attachments.WORLD_TIER);
    }

    public static void setTier(Player player, WorldTier tier) {
        WorldTier oldTier = player.getData(Attachments.WORLD_TIER);
        if (oldTier == tier && !isTutorialActive(player)) {
            return;
        }

        player.setData(Attachments.WORLD_TIER, tier);
        if (player instanceof ServerPlayer sp) {
            PacketDistributor.sendToPlayer(sp, new WorldTierPayload(tier));

            for (TierAugment aug : TierAugmentRegistry.getAugments(oldTier, Target.PLAYERS)) {
                aug.remove((ServerLevel) sp.level(), player);
            }

            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
                aug.apply((ServerLevel) sp.level(), player);
            }

            player.setData(Attachments.TIER_AUGMENTS_APPLIED, true);
            player.awardStat(Apoth.Stats.WORLD_TIERS_ACTIVATED);
        }

    }

    public static boolean isUnlocked(Player player, WorldTier tier) {
        return ApothMiscUtil.hasAdvancement(player, tier.getUnlockAdvancement());
    }

    /**
     * Checks if the World Tier tutorial is active. The tutorial is active if the player is in Haven (the default), and has never clicked the "activate" button.
     * <p>
     * The tutorial being active has the following side effects:
     * <ul>
     * <li>Affix items have their name set to "Unidentified %s" instead of the real affix name</li>
     * <li>Affix items have their affix descriptions removed, and replaced with text directing the player to open the Tier Select screen</li>
     * <li>Upon opening the Tier Select screen, the screen will immediately open the World Tier Tutorial GUI Layer</li>
     * </ul>
     */
    public static boolean isTutorialActive(Player player) {
        if (FMLEnvironment.dist.isClient() && player.level().isClientSide) {
            return ClientAccess.isTutorialActive(player);
        }
        return getTier(player) == WorldTier.HAVEN && ((ServerPlayer) player).getStats().getValue(Stats.CUSTOM.get(Apoth.Stats.WORLD_TIERS_ACTIVATED)) == 0;
    }

    public static <T> MapCodec<Map<WorldTier, T>> mapCodec(Codec<T> elementCodec) {
        return Codec.simpleMap(WorldTier.CODEC, elementCodec,
            Keyable.forStrings(() -> Arrays.stream(WorldTier.values()).map(StringRepresentable::getSerializedName)));
    }

    private static class ClientAccess {
        private static boolean isTutorialActive(Player player) {
            return getTier(player) == WorldTier.HAVEN && Minecraft.getInstance().player.getStats().getValue(Stats.CUSTOM.get(Apoth.Stats.WORLD_TIERS_ACTIVATED)) == 0;
        }
    }
}
