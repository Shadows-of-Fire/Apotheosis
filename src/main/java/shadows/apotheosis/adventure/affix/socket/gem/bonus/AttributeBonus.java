package shadows.apotheosis.adventure.affix.socket.gem.bonus;

import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.affix.socket.gem.GemClass;
import shadows.apotheosis.adventure.affix.socket.gem.GemItem;
import shadows.apotheosis.adventure.loot.LootRarity;
import shadows.apotheosis.core.attributeslib.AttributesLib;
import shadows.apotheosis.core.attributeslib.api.IFormattableAttribute;
import shadows.placebo.codec.EnumCodec;
import shadows.placebo.util.StepFunction;

public class AttributeBonus extends GemBonus {

    
    public static Codec<AttributeBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(a -> a.attribute),
            new EnumCodec<>(Operation.class).fieldOf("operation").forGetter(a -> a.operation),
            VALUES_CODEC.fieldOf("values").forGetter(a -> a.values))
        .apply(inst, AttributeBonus::new));
    

    protected final Attribute attribute;
    protected final Operation operation;
    protected final Map<LootRarity, StepFunction> values;

    public AttributeBonus(GemClass gemClass, Attribute attr, Operation op, Map<LootRarity, StepFunction> values) {
        super(Apotheosis.loc("attribute"), gemClass);
        this.attribute = attr;
        this.operation = op;
        this.values = values;
    }

    @Override
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
        map.accept(this.attribute, read(gem, rarity, GemItem.getUUIDs(gem).get(0)));
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        return IFormattableAttribute.toComponent(this.attribute, read(gem, rarity, UUID.randomUUID()), AttributesLib.getTooltipFlag());
    }

    @Override
    public AttributeBonus validate() {
        Preconditions.checkNotNull(this.attribute, "Invalid AttributeBonus with null attribute");
        Preconditions.checkNotNull(this.operation, "Invalid AttributeBonus with null operation");
        Preconditions.checkNotNull(this.values, "Invalid AttributeBonus with null values");
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return 1;
    }

    public AttributeModifier read(ItemStack gem, LootRarity rarity, UUID id) {
        return new AttributeModifier(id, "apoth.gem_modifier", this.values.get(rarity).get(0), this.operation);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

}
