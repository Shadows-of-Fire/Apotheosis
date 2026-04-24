package dev.shadowsoffire.apotheosis.advancements;

import java.util.Optional;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

public class GemCutTrigger extends SimpleCriterionTrigger<GemCutTrigger.Instance> {

    @Override
    public Codec<Instance> codec() {
        return Instance.CODEC;
    }

    public void trigger(ServerPlayer player, ItemStack stack) {
        UnsocketedGem gem = UnsocketedGem.of(stack);
        if (gem.isValid()) {
            this.trigger(player, inst -> inst.test(stack, gem));
        }
    }

    public static record Instance(Optional<ContextAwarePredicate> player, ItemPredicate gem, Optional<Purity> purity) implements SimpleCriterionTrigger.SimpleInstance {

        public static final Codec<Instance> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(Instance::player),
            ItemPredicate.CODEC.fieldOf("item").forGetter(Instance::gem),
            Purity.CODEC.optionalFieldOf("purity").forGetter(Instance::purity))
            .apply(inst, Instance::new));

        public boolean test(ItemStack stack, UnsocketedGem inst) {
            return this.gem.test(stack) && (this.purity.isEmpty() || this.purity.get() == inst.purity());
        }
    }

}
