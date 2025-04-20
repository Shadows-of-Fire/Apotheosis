package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.LinkedHashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Components;
import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.util.RadialUtil;
import dev.shadowsoffire.apotheosis.util.RadialUtil.RadialData;
import dev.shadowsoffire.placebo.util.CachedObject;
import dev.shadowsoffire.placebo.util.CachedObject.CachedObjectSource;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.level.BlockEvent;

public class RadialBonus extends GemBonus {

    public static final Codec<RadialBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(RadialData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, RadialBonus::new));

    public static final ResourceLocation GEM_RADIAL_DATA_CACHED_OBJECT = Apotheosis.loc("gem_radial_data");

    protected final Map<Purity, RadialData> values;

    public RadialBonus(GemClass gemClass, Map<Purity, RadialData> values) {
        super(gemClass);
        this.values = values;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        RadialData data = this.values.get(gem.purity());
        return Component.translatable("affix.apotheosis:breaker/effect/radial.desc", data.x(), data.y()).withStyle(ChatFormatting.YELLOW);
    }

    // EventPriority.LOW
    public static void onBreak(BlockEvent.BreakEvent e) {
        Player player = e.getPlayer();
        RadialData data = getRadialData(player.getMainHandItem());
        if (data != null) {
            RadialUtil.attemptRadialMining(e, data);
        }
    }

    @Nullable
    public static RadialData getRadialData(ItemStack tool) {
        return CachedObjectSource.getOrCreate(tool, GEM_RADIAL_DATA_CACHED_OBJECT, RadialBonus::getRadialDataImpl, CachedObject.hashComponents(Apoth.Components.SOCKETED_GEMS, Apoth.Components.SOCKETS));
    }

    @Nullable
    private static RadialData getRadialDataImpl(ItemStack tool) {
        if (tool.has(Components.SOCKETED_GEMS)) {
            GemInstance inst = SocketHelper.getGems(tool).streamValidGems().filter(g -> g.getBonus().orElse(null) instanceof RadialBonus).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                return ((RadialBonus) inst.getBonus().get()).values.get(inst.purity());
            }
        }
        return null;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {

        private final Map<Purity, RadialData> values = new LinkedHashMap<>();

        public Builder value(Purity rarity, int x, int y, int xOff, int yOff) {
            RadialData data = new RadialData(x, y, xOff, yOff);
            this.values.put(rarity, data);
            return this;
        }

        public RadialBonus build(GemClass gClass) {
            return new RadialBonus(gClass, this.values);
        }

    }

}
