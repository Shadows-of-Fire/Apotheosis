package dev.shadowsoffire.apotheosis.compat.gateways;

import java.util.Set;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mobs.util.AffixData;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item.TooltipContext;

public record AffixWaveModifier(AffixData data) implements WaveModifier {

    public static final Codec<AffixWaveModifier> CODEC = AffixData.CODEC.xmap(AffixWaveModifier::new, AffixWaveModifier::data);

    public AffixWaveModifier(float chance, Set<LootRarity> rarities) {
        this(new AffixData(chance, rarities));
    }

    @Override
    public Codec<? extends WaveModifier> getCodec() {
        return CODEC;
    }

    @Override
    public void apply(LivingEntity entity, GatewayEntity gate) {
        if (entity instanceof Mob mob) {
            GenContext ctx = GenContext.forPlayer(gate.summonerOrClosest());
            this.data.applyTo(mob, ctx, 0, false);
        }
    }

    @Override
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
        list.accept(Apotheosis.lang("modifier", "affix"));
    }

    public static AffixWaveModifier create(float chance, Set<LootRarity> rarities) {
        return new AffixWaveModifier(chance, rarities);
    }

    public static AffixWaveModifier create() {
        return create(1.0F, Set.of());
    }

}
