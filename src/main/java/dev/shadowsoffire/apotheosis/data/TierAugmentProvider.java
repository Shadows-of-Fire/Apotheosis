package dev.shadowsoffire.apotheosis.data;

import java.util.concurrent.CompletableFuture;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.AttributeAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import dev.shadowsoffire.apothic_enchanting.Ench;
import dev.shadowsoffire.placebo.json.RandomAttributeModifier;
import dev.shadowsoffire.placebo.util.StepFunction;
import dev.shadowsoffire.placebo.util.data.DynamicRegistryProvider;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup.Provider;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier.Operation;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class TierAugmentProvider extends DynamicRegistryProvider<TierAugment> {

    public TierAugmentProvider(PackOutput output, CompletableFuture<Provider> registries) {
        super(output, registries, TierAugmentRegistry.INSTANCE);
    }

    @Override
    public String getName() {
        return "Tier Augments";
    }

    @Override
    public void generate() {
        // Haven
        this.addAttribute("haven/max_eterna", WorldTier.HAVEN, Target.PLAYERS, 90, Ench.Attributes.MAX_ETERNA, Operation.ADD_VALUE, -70);

        // Frontier
        this.addAttribute("frontier/max_eterna", WorldTier.FRONTIER, Target.PLAYERS, 90, Ench.Attributes.MAX_ETERNA, Operation.ADD_VALUE, -55);
        this.addAttribute("frontier/armor", WorldTier.FRONTIER, Target.MONSTERS, 100, Attributes.ARMOR, Operation.ADD_VALUE, 4);

        this.addAttribute("frontier/experience", WorldTier.FRONTIER, Target.PLAYERS, 100, ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, 0.35F);
        this.addAttribute("frontier/luck", WorldTier.FRONTIER, Target.PLAYERS, 200, Attributes.LUCK, Operation.ADD_MULTIPLIED_TOTAL, 0.20F);

        // Ascent
        this.addAttribute("ascent/max_eterna", WorldTier.ASCENT, Target.PLAYERS, 90, Ench.Attributes.MAX_ETERNA, Operation.ADD_VALUE, -40);
        this.addAttribute("ascent/armor", WorldTier.ASCENT, Target.MONSTERS, 100, Attributes.ARMOR, Operation.ADD_VALUE, 8);
        this.addAttribute("ascent/armor_pierce", WorldTier.ASCENT, Target.MONSTERS, 200, ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, 5);

        this.addAttribute("ascent/experience", WorldTier.ASCENT, Target.PLAYERS, 100, ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, 0.55F);
        this.addAttribute("ascent/luck", WorldTier.ASCENT, Target.PLAYERS, 200, Attributes.LUCK, Operation.ADD_MULTIPLIED_TOTAL, 0.30F);

        // Summit
        this.addAttribute("summit/max_eterna", WorldTier.SUMMIT, Target.PLAYERS, 90, Ench.Attributes.MAX_ETERNA, Operation.ADD_VALUE, -25);
        this.addAttribute("summit/armor", WorldTier.SUMMIT, Target.MONSTERS, 100, Attributes.ARMOR, Operation.ADD_VALUE, 12);
        this.addAttribute("summit/armor_toughness", WorldTier.SUMMIT, Target.MONSTERS, 150, Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 10);
        this.addAttribute("summit/armor_pierce", WorldTier.SUMMIT, Target.MONSTERS, 200, ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, 15F);
        this.addAttribute("summit/prot_pierce", WorldTier.SUMMIT, Target.MONSTERS, 300, ALObjects.Attributes.PROT_PIERCE, Operation.ADD_VALUE, 10F);

        this.addAttribute("summit/experience", WorldTier.SUMMIT, Target.PLAYERS, 100, ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, 0.75F);
        this.addAttribute("summit/luck", WorldTier.SUMMIT, Target.PLAYERS, 200, Attributes.LUCK, Operation.ADD_MULTIPLIED_TOTAL, 0.50F);

        // Pinnacle
        this.addAttribute("pinnacle/armor", WorldTier.PINNACLE, Target.MONSTERS, 100, Attributes.ARMOR, Operation.ADD_VALUE, 16);
        this.addAttribute("pinnacle/armor_toughness", WorldTier.PINNACLE, Target.MONSTERS, 150, Attributes.ARMOR_TOUGHNESS, Operation.ADD_VALUE, 20);
        this.addAttribute("pinnacle/armor_pierce", WorldTier.PINNACLE, Target.MONSTERS, 200, ALObjects.Attributes.ARMOR_PIERCE, Operation.ADD_VALUE, 25F);
        this.addAttribute("pinnacle/prot_pierce", WorldTier.PINNACLE, Target.MONSTERS, 300, ALObjects.Attributes.PROT_PIERCE, Operation.ADD_VALUE, 20F);

        this.addAttribute("pinnacle/experience", WorldTier.PINNACLE, Target.PLAYERS, 100, ALObjects.Attributes.EXPERIENCE_GAINED, Operation.ADD_MULTIPLIED_TOTAL, 1.25F);
        this.addAttribute("pinnacle/luck", WorldTier.PINNACLE, Target.PLAYERS, 200, Attributes.LUCK, Operation.ADD_MULTIPLIED_TOTAL, 1.00F);
    }

    private void addAttribute(String path, WorldTier tier, Target target, int sortIdx, Holder<Attribute> attr, Operation op, float value) {
        Identifier id = Apotheosis.loc(path);
        RandomAttributeModifier modif = new RandomAttributeModifier(attr, op, StepFunction.constant(value));
        this.add(id, new AttributeAugment(tier, target, sortIdx, modif, id));
    }
}
