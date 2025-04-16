package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apotheosis.util.OmneticUtil;
import dev.shadowsoffire.apotheosis.util.OmneticUtil.OmneticData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.BreakSpeed;
import net.neoforged.neoforge.event.entity.player.PlayerEvent.HarvestCheck;

public class OmneticBonus extends GemBonus {

    public static final Codec<OmneticBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(OmneticData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, OmneticBonus::new));

    protected final Map<Purity, OmneticData> values;

    public OmneticBonus(GemClass gemClass, Map<Purity, OmneticData> values) {
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
        return Component.translatable("affix.apotheosis:breaker/effect/omnetic.desc", Component.translatable("misc.apotheosis." + this.values.get(gem.purity()).name())).withStyle(ChatFormatting.YELLOW);
    }

    public static void harvest(HarvestCheck e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            GemInstance inst = SocketHelper.getGems(stack).streamValidGems().filter(g -> g.getBonus().orElse(null) instanceof OmneticBonus).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                OmneticData data = ((OmneticBonus) inst.getBonus().get()).values.get(inst.purity());
                OmneticUtil.applyOmneticData(e, data);
            }
        }
    }

    // EventPriority.HIGHEST
    public static void speed(BreakSpeed e) {
        ItemStack stack = e.getEntity().getMainHandItem();
        if (!stack.isEmpty()) {
            GemInstance inst = SocketHelper.getGems(stack).streamValidGems().filter(g -> g.getBonus().orElse(null) instanceof OmneticBonus).findFirst().orElse(null);
            if (inst != null && inst.isValid()) {
                OmneticData data = ((OmneticBonus) inst.getBonus().get()).values.get(inst.purity());
                OmneticUtil.applyOmneticData(e, data);
            }
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {

        private final Map<Purity, OmneticData> values = new LinkedHashMap<>();

        public Builder value(Purity rarity, String name, Item... items) {
            OmneticData data = new OmneticData(name, Arrays.stream(items).map(Item::getDefaultInstance).toArray(ItemStack[]::new));
            this.values.put(rarity, data);
            return this;
        }

        public OmneticBonus build(GemClass gClass) {
            return new OmneticBonus(gClass, this.values);
        }

    }

}
