package dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.BossEventSettings;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.GateRules;
import dev.shadowsoffire.gateways.gate.Gateway;
import dev.shadowsoffire.gateways.gate.GatewayRegistry;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.SpawnAlgorithms.SpawnAlgorithm;
import dev.shadowsoffire.gateways.gate.Wave;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

/**
 * A Gateway is the definition of a Gateway Entity.
 * 
 * @param settings     The {@linkplain TieredGateSettings settings} of the Gateway.
 * @param waves        The {@linkplain Wave waves} of the Gateway.
 * @param rewards      The {@linkplain Reward completion rewards} if the final wave is defeated. Always displayed.
 * @param failures     The {@linkplain Failure penalties} for failing the gateway.
 * @param rules        The {@linkplain GateRules rules} of the Gateway.
 * @param bossSettings The {@linkplain BossEventSettings boss event settings} for the Gateway.
 */
public record TieredGateway(TieredGateSettings settings, List<Wave> waves, List<Reward> rewards, List<Failure> failures, GateRules rules, BossEventSettings bossSettings) implements Gateway {

    public static final Codec<TieredGateway> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            TieredGateSettings.CODEC.fieldOf("settings").forGetter(TieredGateway::settings),
            Wave.CODEC.listOf().fieldOf("waves").forGetter(TieredGateway::waves),
            Reward.CODEC.listOf().optionalFieldOf("rewards", Collections.emptyList()).forGetter(TieredGateway::rewards),
            Failure.CODEC.listOf().optionalFieldOf("failures", Collections.emptyList()).forGetter(TieredGateway::failures),
            GateRules.CODEC.optionalFieldOf("rules", GateRules.DEFAULT).forGetter(TieredGateway::rules),
            BossEventSettings.CODEC.optionalFieldOf("boss_event", BossEventSettings.DEFAULT).forGetter(TieredGateway::bossSettings))
        .apply(inst, TieredGateway::new));

    @Override
    public SpawnAlgorithm spawnAlgo() {
        return this.settings.spawnAlgo();
    }

    @Override
    public TextColor color() {
        return this.settings.color();
    }

    @Override
    public Size size() {
        return this.settings.size();
    }

    @Override
    @Nullable
    public Component canOpen(Player player) {
        WorldTier tier = WorldTier.getTier(player);
        if (this.settings.tier() != tier) {
            return Apotheosis.lang("error", "gate_tier_incorrect", this.settings.tier().toComponent());
        }
        return null;
    }

    @Override
    public Holder<SoundEvent> soundtrack() {
        return this.settings.soundtrack();
    }

    @Override
    public GatewayEntity createEntity(Level level, Player summoner) {
        return new TieredGatewayEntity(level, summoner, GatewayRegistry.INSTANCE.holder(this));
    }

    @Override
    public void appendPearlTooltip(TooltipContext ctx, List<Component> tooltips, TooltipFlag flag) {
        TieredGateClient.appendPearlTooltip(this, ctx, tooltips, flag);
    }

    @Override
    public void renderBossBar(GatewayEntity gate, Object gfx, int x, int y, boolean isInWorld) {
        TieredGateClient.renderBossBar(gate, gfx, x, y, isInWorld);
    }

    public int getNumWaves() {
        return this.waves.size();
    }

    public Wave getWave(int n) {
        return this.waves.get(n);
    }

    @Override
    public Codec<TieredGateway> getCodec() {
        return CODEC;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private TieredGateSettings settings = null;
        private final List<Wave> waves = new ArrayList<>();
        private final List<Reward> rewards = new ArrayList<>();
        private final List<Failure> failures = new ArrayList<>();
        private GateRules rules = GateRules.DEFAULT;
        private BossEventSettings bossSettings = BossEventSettings.DEFAULT;

        public Builder settings(UnaryOperator<TieredGateSettings.Builder> config) {
            this.settings = config.apply(new TieredGateSettings.Builder()).build();
            return this;
        }

        /**
         * Adds a wave to this gateway.
         * 
         * @param wave The wave to add
         * @return This builder for chaining
         */
        public Builder wave(Wave wave) {
            this.waves.add(wave);
            return this;
        }

        /**
         * Adds a wave to this gateway.
         * 
         * @param config A unary operator to create the wave.
         * @return This builder for chaining
         */
        public Builder wave(UnaryOperator<Wave.Builder> config) {
            return this.wave(config.apply(new Wave.Builder()).build());
        }

        /**
         * Adds multiple waves to this gateway.
         * 
         * @param waves The waves to add
         * @return This builder for chaining
         */
        public Builder waves(List<Wave> waves) {
            this.waves.addAll(waves);
            return this;
        }

        /**
         * Adds a reward to this gateway.
         * 
         * @param reward The reward to add
         * @return This builder for chaining
         */
        public Builder keyReward(Reward reward) {
            this.rewards.add(reward);
            return this;
        }

        /**
         * Adds multiple rewards to this gateway.
         * 
         * @param rewards The rewards to add
         * @return This builder for chaining
         */
        public Builder keyRewards(List<Reward> rewards) {
            this.rewards.addAll(rewards);
            return this;
        }

        /**
         * Adds a failure condition to this gateway.
         * 
         * @param failure The failure to add
         * @return This builder for chaining
         */
        public Builder failure(Failure failure) {
            this.failures.add(failure);
            return this;
        }

        /**
         * Adds multiple failure conditions to this gateway.
         * 
         * @param failures The failures to add
         * @return This builder for chaining
         */
        public Builder failures(List<Failure> failures) {
            this.failures.addAll(failures);
            return this;
        }

        /**
         * Sets the rules for this gateway.
         * 
         * @param rules The rules to use
         * @return This builder for chaining
         */
        public Builder rules(GateRules rules) {
            this.rules = rules;
            return this;
        }

        /**
         * Sets the rules for this gateway using a configuration function.
         * 
         * @param config A unary operator to configure the GateRules
         * @return This builder for chaining
         */
        public Builder rules(UnaryOperator<GateRules.Builder> config) {
            return this.rules(config.apply(GateRules.builder()).build());
        }

        /**
         * Sets the boss event settings for this gateway.
         * 
         * @param bossSettings The boss event settings to use
         * @return This builder for chaining
         */
        public Builder bossSettings(BossEventSettings bossSettings) {
            this.bossSettings = bossSettings;
            return this;
        }

        /**
         * Builds a new NormalGateway with the configured parameters.
         * 
         * @return A new NormalGateway instance
         * @throws IllegalStateException if required parameters are missing
         */
        public TieredGateway build() {
            Preconditions.checkNotNull(this.settings, "Settings must be set before building a TieredGateway");

            if (waves.isEmpty()) {
                throw new IllegalStateException("Gateway must have at least one wave");
            }

            return new TieredGateway(
                settings,
                Collections.unmodifiableList(new ArrayList<>(waves)),
                Collections.unmodifiableList(new ArrayList<>(rewards)),
                Collections.unmodifiableList(new ArrayList<>(failures)),
                rules,
                bossSettings);
        }
    }

}
