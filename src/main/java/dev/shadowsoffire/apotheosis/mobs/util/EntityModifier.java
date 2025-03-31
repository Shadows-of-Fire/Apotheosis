package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.gateways.Gateways;
import dev.shadowsoffire.placebo.codec.CodecMap;
import dev.shadowsoffire.placebo.codec.CodecProvider;
import dev.shadowsoffire.placebo.json.ChancedEffectInstance;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.systems.gear.GearSet;
import dev.shadowsoffire.placebo.systems.gear.GearSet.SetPredicate;
import dev.shadowsoffire.placebo.systems.gear.GearSetRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.alchemy.PotionContents;

public interface EntityModifier extends CodecProvider<EntityModifier> {

    public static final CodecMap<EntityModifier> CODEC = new CodecMap<>("Apothic Entity Modifier");

    /**
     * Applies this modifier to the target mob.
     */
    void apply(Mob mob, GenContext ctx);

    /**
     * Generates a description for this entity modifier.
     * <p>
     * This is used in the world tier selection screen to show the guaranteed changes.
     */
    public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list);

    public static void initSerializers() {
        register("mob_effect", EffectModifier.CODEC);
        register("attribute", AttributeModifier.CODEC);
        register("gear_set", GearSetModifier.CODEC);
    }

    private static void register(String id, Codec<? extends EntityModifier> codec) {
        CODEC.register(Gateways.loc(id), codec);
    }

    /**
     * Applies a mob effect to the target entity.
     * <p>
     * The effect is applied with infinite duration, unless the entity is a creeper, in which case the duration is reduced to 5 minutes.
     */
    public static record EffectModifier(ChancedEffectInstance effect) implements EntityModifier {

        public static Codec<EffectModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ChancedEffectInstance.CONSTANT_CODEC.fieldOf("effect").forGetter(EffectModifier::effect))
            .apply(inst, EffectModifier::new));

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            int duration = mob instanceof Creeper ? 6000 : Integer.MAX_VALUE;
            mob.addEffect(this.effect.createDeterministic(duration));
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            List<Component> output = new ArrayList<>();
            PotionContents.addPotionTooltip(Arrays.asList(this.effect.createDeterministic(1)), output::add, 1, ctx.tickRate());
            list.accept(Component.literal(output.get(0).getString()));
        }

    }

    /**
     * Applies an attribute modifier to the target entity.
     * <p>
     * The modifier will be ignored if the entity does not have the attribute.
     */
    public static record AttributeModifier(RandomAttributeModifier modifier) implements EntityModifier {

        public static Codec<AttributeModifier> CODEC = RandomAttributeModifier.CODEC.xmap(AttributeModifier::new, AttributeModifier::modifier);

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            AttributeInstance inst = mob.getAttribute(this.modifier.attribute());
            if (inst == null) {
                return;
            }
            this.modifier.apply(Apotheosis.loc("rm_ " + mob.getRandom().nextInt()), ctx.rand(), mob);
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            // list.accept(modifier.attribute().value().toComponent(modifier.createDeterministic(Gateways.loc("gateway_random_modifier")),
            // ApothicAttributes.getTooltipFlag()));
            // TODO: Show the augmenting range for the modifier.
        }

    }

    /**
     * Applies a gear set to the target entity.
     * <p>
     * The applied gear set should be deterministic to a reasonable degree, since it must be translated to a single name.
     */
    public static record GearSetModifier(List<SetPredicate> gearSets, String desc) implements EntityModifier {

        public static Codec<GearSetModifier> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                SetPredicate.CODEC.listOf().fieldOf("valid_gear_sets").forGetter(GearSetModifier::gearSets),
                Codec.STRING.fieldOf("desc").forGetter(GearSetModifier::desc))
            .apply(inst, GearSetModifier::new));

        @Override
        public Codec<? extends EntityModifier> getCodec() {
            return CODEC;
        }

        @Override
        public void apply(Mob mob, GenContext ctx) {
            GearSet set = GearSetRegistry.INSTANCE.getRandomSet(ctx.rand(), ctx.luck(), this.gearSets);
            if (set != null) {
                set.apply(mob);
            }
        }

        @Override
        public void appendHoverText(TooltipContext ctx, Consumer<MutableComponent> list) {
            list.accept(Apotheosis.lang("info", "gear_set_modifier", Component.translatable(this.desc)));
        }

    }
}
