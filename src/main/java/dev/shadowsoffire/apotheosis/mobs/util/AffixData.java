package dev.shadowsoffire.apotheosis.mobs.util;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.loot.LootCategory;
import dev.shadowsoffire.apotheosis.loot.LootRarity;
import dev.shadowsoffire.apotheosis.mobs.types.Elite;
import dev.shadowsoffire.apotheosis.mobs.types.Invader;
import dev.shadowsoffire.apotheosis.tiers.GenContext;
import dev.shadowsoffire.placebo.codec.PlaceboCodecs;
import net.minecraft.network.chat.Style;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;

/**
 * Affix data for {@link Elite}.
 *
 * @param enabled  If one of the miniboss's items (from the selected gear set) will be affixed.
 * @param rarities A pool of rarities. If empty, all rarities will be used.
 */
public record AffixData(float chance, Set<LootRarity> rarities) {

    public static final AffixData DEFAULT = new AffixData(-1, Set.of());

    public static final Codec<AffixData> CODEC = RecordCodecBuilder.create(inst -> inst.group(
        Codec.FLOAT.fieldOf("affix_chance").forGetter(AffixData::chance),
        PlaceboCodecs.setOf(LootRarity.CODEC).optionalFieldOf("rarities", Set.of()).forGetter(AffixData::rarities))
        .apply(inst, AffixData::new));

    @Nullable
    public EquipmentSlot applyTo(Mob mob, GenContext ctx, int enchLevel, boolean guaranteeDrop) {
        RandomSource rand = ctx.rand();
        if (rand.nextFloat() > this.chance()) {
            return null; // No affix applied, chance not met.
        }

        EquipmentSlot[] slots = getRandomSlots(rand);

        ItemStack temp = ItemStack.EMPTY;
        EquipmentSlot selectedSlot = null;
        for (EquipmentSlot slot : slots) {
            selectedSlot = slot;
            temp = mob.getItemBySlot(slot);
            if (!LootCategory.forItem(temp).isNone()) {
                break;
            }
        }

        if (LootCategory.forItem(temp).isNone()) {
            // LOGGER.error("Attempted to affix a miniboss with ID " + EliteRegistry.INSTANCE.getKey(this) + " but it is not wearing any affixable items!");
            return null;
        }

        var rarity = LootRarity.random(ctx, this.rarities());
        if (mob.hasCustomName()) {
            mob.setCustomName(mob.getCustomName().plainCopy().withStyle(Style.EMPTY.withColor(rarity.color())));
        }
        Invader.modifyBossItem(temp, mob.getName(), ctx, rarity, enchLevel, mob.level().registryAccess());
        if (guaranteeDrop) {
            mob.setDropChance(selectedSlot, 2F);
        }
        return selectedSlot;

    }

    /**
     * Returns an equipment slot array that is randomly shuffled.
     */
    private static EquipmentSlot[] getRandomSlots(RandomSource rand) {
        EquipmentSlot[] slots = EquipmentSlot.values();
        for (int i = slots.length - 1; i > 0; i--) {
            int index = rand.nextInt(i + 1);
            EquipmentSlot v = slots[index];
            slots[index] = slots[i];
            slots[i] = v;
        }

        return slots;
    }

}
