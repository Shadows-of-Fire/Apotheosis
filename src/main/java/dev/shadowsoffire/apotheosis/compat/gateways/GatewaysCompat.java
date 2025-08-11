package dev.shadowsoffire.apotheosis.compat.gateways;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.WaveEntity;

public class GatewaysCompat {

    public static void register() {
        WaveEntity.CODEC.register(Apotheosis.loc("invader"), InvaderWaveEntity.CODEC);
        WaveEntity.CODEC.register(Apotheosis.loc("elite"), EliteWaveEntity.CODEC);
        Reward.CODEC.register(Apotheosis.loc("affix_item"), AffixItemReward.CODEC);
        Reward.CODEC.register(Apotheosis.loc("gem"), GemReward.CODEC);
    }

}
