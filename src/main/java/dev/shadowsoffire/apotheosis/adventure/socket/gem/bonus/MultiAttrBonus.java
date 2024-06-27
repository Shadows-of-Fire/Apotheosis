package dev.shadowsoffire.apotheosis.adventure.socket.gem.bonus;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.BiConsumer;

import com.google.common.base.Preconditions;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.loot.LootRarity;
import dev.shadowsoffire.apotheosis.adventure.loot.RarityRegistry;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemItem;
import dev.shadowsoffire.attributeslib.AttributesLib;
import dev.shadowsoffire.attributeslib.api.IFormattableAttribute;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

public class MultiAttrBonus extends GemBonus {

    public static Codec<MultiAttrBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            ModifierInst.CODEC.listOf().fieldOf("modifiers").forGetter(a -> a.modifiers),
            Codec.STRING.fieldOf("desc").forGetter(a -> a.desc))
        .apply(inst, MultiAttrBonus::new));

    protected final List<ModifierInst> modifiers;
    protected final String desc;

    public MultiAttrBonus(GemClass gemClass, List<ModifierInst> modifiers, String desc) {
        super(Apotheosis.loc("multi_attribute"), gemClass);
        this.modifiers = modifiers;
        this.desc = desc;
    }

    @Override
    public void addModifiers(ItemStack gem, LootRarity rarity, BiConsumer<Attribute, AttributeModifier> map) {
        List<UUID> uuids = GemItem.getUUIDs(gem);
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            map.accept(modifier.attr, modifier.build(uuids.get(i++), rarity));
        }
    }

    @Override
    public Component getSocketBonusTooltip(ItemStack gem, LootRarity rarity) {
        Object[] values = new Object[this.modifiers.size() * 2];
        int i = 0;
        for (ModifierInst modifier : this.modifiers) {
            values[i] = IFormattableAttribute.toComponent(modifier.attr, modifier.build(UUID.randomUUID(), rarity), AttributesLib.getTooltipFlag());
            values[this.modifiers.size() + i] = IFormattableAttribute.toValueComponent(modifier.attr, modifier.op, i, AttributesLib.getTooltipFlag());
            i++;
        }
        return Component.translatable(this.desc, values).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public MultiAttrBonus validate() {
        Preconditions.checkNotNull(this.modifiers, "Invalid AttributeBonus with null values");
        List<Set<LootRarity>> rarityChecks = new ArrayList<>();
        for (ModifierInst inst : this.modifiers) {
            var set = new HashSet<LootRarity>();
            RarityRegistry.INSTANCE.getValues().stream().filter(r -> inst.values.containsKey(r)).forEach(set::add);
            rarityChecks.add(set);
        }
        Preconditions.checkArgument(rarityChecks.stream().mapToInt(Set::size).allMatch(size -> size == rarityChecks.get(0).size()));
        return this;
    }

    @Override
    public boolean supports(LootRarity rarity) {
        return this.modifiers.get(0).values.containsKey(rarity);
    }

    @Override
    public int getNumberOfUUIDs() {
        return this.modifiers.size();
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    protected static record ModifierInst(Attribute attr, Operation op, Map<LootRarity, Float> values) {

        public static Codec<ModifierInst> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                ForgeRegistries.ATTRIBUTES.getCodec().fieldOf("attribute").forGetter(ModifierInst::attr),
                PlaceboCodecs.enumCodec(Operation.class).fieldOf("operation").forGetter(ModifierInst::op),
                LootRarity.mapCodec(Codec.FLOAT).fieldOf("values").forGetter(ModifierInst::values))
            .apply(inst, ModifierInst::new));

        public AttributeModifier build(UUID id, LootRarity rarity) {
            return new AttributeModifier(id, "apoth.gem_modifier", this.values.get(rarity), this.op);
        }

    }

}
