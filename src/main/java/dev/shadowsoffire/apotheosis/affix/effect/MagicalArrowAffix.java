package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.DamageSourceExtension;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityInvulnerabilityCheckEvent;

public class MagicalArrowAffix extends Affix {

    public static final Codec<MagicalArrowAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            PlaceboCodecs.setOf(LootRarity.CODEC).fieldOf("rarities").forGetter(a -> a.rarities))
        .apply(inst, MagicalArrowAffix::new));

    protected Set<LootRarity> rarities;

    public MagicalArrowAffix(AffixDefinition def, Set<LootRarity> rarities) {
        super(def);
        this.rarities = rarities;
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return cat.isRanged() && this.rarities.contains(rarity);
    }

    /**
     * Applies the magical arrow affix by mutating the damage type tags of the incoming damage source at the first event in the stack.
     */
    public static void modifyIncomingDamageTags(EntityInvulnerabilityCheckEvent e) {
        if (e.getSource().getDirectEntity() instanceof AbstractArrow arrow) {
            if (AffixHelper.streamAffixes(arrow).anyMatch(a -> a.getAffix() instanceof MagicalArrowAffix)) {
                DamageSourceExtension ext = (DamageSourceExtension) e.getSource();
                ext.addTag(Tags.DamageTypes.IS_MAGIC);
                ext.addTag(DamageTypeTags.BYPASSES_ARMOR);
            }
        }
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return true;
    }

}
