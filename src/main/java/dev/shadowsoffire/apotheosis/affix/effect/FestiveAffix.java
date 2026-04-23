package dev.shadowsoffire.apotheosis.affix.effect;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.affix.AffixBuilder;
import dev.shadowsoffire.apotheosis.affix.AffixDefinition;
import dev.shadowsoffire.apotheosis.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.util.IFestiveMarker;
import dev.shadowsoffire.placebo.util.StepFunction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingDropsEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

/**
 * Loot Pinata
 */
public class FestiveAffix extends Affix {

    public static Codec<FestiveAffix> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            affixDef(),
            LootCategory.SET_CODEC.fieldOf("categories").forGetter(a -> a.categories),
            LootRarity.mapCodec(FestiveData.CODEC).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, FestiveAffix::new));

    protected final Set<LootCategory> categories;
    protected final Map<LootRarity, FestiveData> values;

    public FestiveAffix(AffixDefinition def, Set<LootCategory> categories, Map<LootRarity, FestiveData> values) {
        super(def);
        this.categories = categories;
        this.values = values;
    }

    @Override
    public MutableComponent getDescription(AffixInstance inst, AttributeTooltipContext ctx) {
        return Component.translatable("affix." + this.id() + ".desc", fmt(100 * this.getTrueLevel(inst.getRarity(), inst.level())));
    }

    @Override
    public Component getAugmentingText(AffixInstance inst, AttributeTooltipContext ctx) {
        MutableComponent comp = this.getDescription(inst, ctx);

        Component minComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 0)));
        Component maxComp = Component.translatable("%s%%", fmt(100 * this.getTrueLevel(inst.getRarity(), 1)));
        return comp.append(valueBounds(minComp, maxComp));
    }

    @Override
    public boolean canApplyTo(ItemStack stack, LootCategory cat, LootRarity rarity) {
        return this.categories.contains(cat) && this.values.containsKey(rarity);
    }

    private float getTrueLevel(LootRarity rarity, float level) {
        return this.values.get(rarity).chance().get(level);
    }

    // EventPriority.LOW
    public static void markEquipment(LivingDeathEvent e) {
        if (e.getEntity() instanceof Player || e.getEntity().getPersistentData().getBooleanOr("apoth.no_pinata", false)) {
            return;
        }

        ResourceHandler<ItemResource> inv = e.getEntity().getCapability(Capabilities.Item.ENTITY);

        if (inv != null) {
            for (int i = 0; i < inv.size(); i++) {
                ItemResource res = inv.getResource(i);
                int amount = inv.getAmountAsInt(i);
                if (!res.isEmpty() && amount > 0) {
                    ItemStack stack = res.toStack(amount);
                    ((IFestiveMarker) (Object) stack).setMarked(true);
                }
            }
        }

        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack i = e.getEntity().getItemBySlot(slot);
            if (!i.isEmpty()) {
                ((IFestiveMarker) (Object) i).setMarked(true);
            }
        }
    }

    @Override
    public void modifyEntityLoot(AffixInstance inst, LivingDropsEvent e) {
        LivingEntity dead = e.getEntity();
        if (dead instanceof Player || dead.getPersistentData().getBooleanOr("apoth.no_pinata", false)) {
            return;
        }
        if (e.getSource().getEntity() instanceof Player player && !e.getDrops().isEmpty()) {
            if (inst != null && inst.isValid() && player.level().getRandom().nextFloat() < this.getTrueLevel(inst.rarity().get(), inst.level())) {
                player.level().playSound(null, dead.getX(), dead.getY(), dead.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4.0F,
                    (1.0F + (player.level().getRandom().nextFloat() - player.level().getRandom().nextFloat()) * 0.2F) * 0.7F);
                ((ServerLevel) player.level()).sendParticles(ParticleTypes.EXPLOSION_EMITTER, dead.getX(), dead.getY(), dead.getZ(), 2, 1.0D, 0.0D, 0.0D, 0);

                List<ItemEntity> drops = new ArrayList<>(e.getDrops());
                for (ItemEntity item : drops) {
                    if (((IFestiveMarker) (Object) item.getItem()).isMarked()) {
                        continue;
                    }

                    int rolls = this.values.get(inst.rarity().get()).rolls();
                    for (int i = 0; i < rolls; i++) {
                        e.getDrops().add(new ItemEntity(player.level(), item.getX(), item.getY(), item.getZ(), item.getItem().copy()));
                    }
                }

                for (ItemEntity item : e.getDrops()) {
                    item.setPos(dead.getX(), dead.getY(), dead.getZ());
                    item.setDeltaMovement(-0.3 + dead.level().getRandom().nextDouble() * 0.6, 0.3 + dead.level().getRandom().nextDouble() * 0.3, -0.3 + dead.level().getRandom().nextDouble() * 0.6);
                }
            }
        }
    }

    // Lowest prio + receive cancelled
    public static void removeMarker(LivingDropsEvent e) {
        e.getDrops().stream().forEach(ent -> {
            ItemStack s = ent.getItem();
            ((IFestiveMarker) (Object) s).setMarked(false);
            ent.setItem(s);
        });
    }

    @Override
    public Codec<? extends Affix> getCodec() {
        return CODEC;
    }

    @Override
    public boolean isLevelIndependent(AffixInstance inst) {
        return this.values.get(inst.getRarity()).chance.isConstant();
    }

    public static Builder builder() {
        return new Builder();
    }

    /**
     * Data for the Festive Affix.
     * 
     * @param chance The chance of the festive affix triggering, as a step function.
     * @param rolls  The number of extra copies of items to drop when the affix triggers.
     */
    public static record FestiveData(StepFunction chance, int rolls) {

        public static final Codec<FestiveData> CODEC = RecordCodecBuilder.create(inst -> inst
            .group(
                StepFunction.CODEC.fieldOf("chance").forGetter(FestiveData::chance),
                Codec.INT.fieldOf("rolls").forGetter(FestiveData::rolls))
            .apply(inst, FestiveData::new));
    }

    public static class Builder extends AffixBuilder<Builder> {

        protected final Set<LootCategory> categories = new LinkedHashSet<>();
        protected final Map<LootRarity, FestiveData> values = new HashMap<>();

        public Builder categories(LootCategory... cats) {
            for (LootCategory cat : cats) {
                this.categories.add(cat);
            }
            return this;
        }

        public Builder value(LootRarity rarity, StepFunction chance, int rolls) {
            this.values.put(rarity, new FestiveData(chance, rolls));
            return this;
        }

        public FestiveAffix build() {
            return new FestiveAffix(this.definition, this.categories, this.values);
        }
    }

}
