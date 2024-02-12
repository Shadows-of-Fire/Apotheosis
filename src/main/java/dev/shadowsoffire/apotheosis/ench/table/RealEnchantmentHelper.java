package dev.shadowsoffire.apotheosis.ench.table;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.Lists;

import dev.shadowsoffire.apotheosis.ench.EnchModule;
import dev.shadowsoffire.apotheosis.ench.EnchantmentInfo;
import dev.shadowsoffire.apotheosis.ench.table.ApothEnchantmentMenu.Arcana;
import net.minecraft.Util;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.WeightedEntry.IntrusiveBase;
import net.minecraft.util.random.WeightedRandom;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraftforge.registries.ForgeRegistries;

public class RealEnchantmentHelper {

    /**
     * Determines the level of the given enchantment table slot.
     * An item with 0 enchantability cannot be enchanted, so this method returns zero.
     * Slot 2 (the highest level slot) always receives a level equal to power * 2.
     * Slot 1 recieves between 60% and 80% of Slot 2.
     * Slot 0 receives between 20% and 40% of Slot 2.
     *
     * @param rand   Pre-seeded random.
     * @param num    Enchantment Slot Number [0-2]
     * @param eterna Enchantment Power (Eterna Level)
     * @param stack  Itemstack to be enchanted.
     * @return The level that the table will use for this specific slot.
     */
    public static int getEnchantmentCost(RandomSource rand, int num, float eterna, ItemStack stack) {
        int level = Math.round(eterna * 2);
        if (num == 2) return level;
        float lowBound = 0.6F - 0.4F * (1 - num);
        float highBound = 0.8F - 0.4F * (1 - num);
        return Math.max(1, Math.round(level * Mth.nextFloat(rand, lowBound, highBound)));
    }

    /**
     * Creates a list of enchantments for a specific slot given various variables.
     *
     * @param rand      Pre-seeded random.
     * @param stack     Itemstack to be enchanted.
     * @param level     Enchanting Slot XP Level
     * @param quanta    Quanta Level
     * @param arcana    Arcana Level
     * @param treasure  If treasure enchantments can show up.
     * @param blacklist A list of all enchantments that may not be selected.
     * @return A list of enchantments based on the seed, item, and eterna/quanta/arcana levels.
     */
    public static List<EnchantmentInstance> selectEnchantment(RandomSource rand, ItemStack stack, int level, float quanta, float arcana, float rectification, boolean treasure, Set<Enchantment> blacklist) {
        List<EnchantmentInstance> chosenEnchants = Lists.newArrayList();
        int enchantability = stack.getEnchantmentValue();
        int srcLevel = level;
        if (enchantability > 0) {
            float quantaFactor = 1 + getQuantaFactor(rand, quanta, rectification);
            // if (!FMLEnvironment.production) EnchModule.LOGGER.debug("Quanta: {} | Rect: {} | Quanta Roll: {}", quanta, rectification, quantaFactor);
            level = Mth.clamp(Math.round(level * quantaFactor), 1, (int) (EnchantingStatRegistry.getAbsoluteMaxEterna() * 4));
            Arcana arcanaVals = Arcana.getForThreshold(arcana);
            List<EnchantmentInstance> allEnchants = getAvailableEnchantmentResults(level, stack, treasure, blacklist);
            Map<Enchantment, Integer> enchants = EnchantmentHelper.getEnchantments(stack);
            allEnchants.removeIf(e -> enchants.containsKey(e.enchantment)); // Remove duplicates.
            List<ArcanaEnchantmentData> possibleEnchants = allEnchants.stream().map(d -> new ArcanaEnchantmentData(arcanaVals, d)).collect(Collectors.toList());
            if (!possibleEnchants.isEmpty()) {
                chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).get().data);
                removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));

                if (arcana >= 25F && !possibleEnchants.isEmpty()) {
                    chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).get().data);
                    removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));
                }

                if (arcana >= 75F && !possibleEnchants.isEmpty()) {
                    chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).get().data);
                }

                int randomBound = 50;
                if (level > 45) {
                    level = (int) (srcLevel * 1.15F);
                }

                while (rand.nextInt(randomBound) <= level) {
                    if (!chosenEnchants.isEmpty()) removeIncompatible(possibleEnchants, Util.lastOf(chosenEnchants));

                    if (possibleEnchants.isEmpty()) {
                        break;
                    }

                    chosenEnchants.add(WeightedRandom.getRandomItem(rand, possibleEnchants).get().data);
                    level /= 2;
                }
            }
        }
        return ((IEnchantableItem) stack.getItem()).selectEnchantments(chosenEnchants, rand, stack, srcLevel, quanta, arcana, treasure);
    }

    /**
     * Removes all enchantments from the list that are incompatible with the passed enchantment.
     */
    public static void removeIncompatible(List<ArcanaEnchantmentData> list, EnchantmentInstance data) {
        Iterator<ArcanaEnchantmentData> iterator = list.iterator();

        while (iterator.hasNext()) {
            if (!data.enchantment.isCompatibleWith(iterator.next().data.enchantment)) {
                iterator.remove();
            }
        }

    }

    /**
     * @param power         The current enchanting power.
     * @param stack         The ItemStack being enchanted.
     * @param allowTreasure If treasure enchantments are allowed.
     * @param blacklist     A list of all enchantments that may not be selected.
     * @return All possible enchantments that are eligible to be placed on this item at a specific power level.
     */
    public static List<EnchantmentInstance> getAvailableEnchantmentResults(int power, ItemStack stack, boolean allowTreasure, Set<Enchantment> blacklist) {
        List<EnchantmentInstance> list = new ArrayList<>();
        IEnchantableItem item = (IEnchantableItem) stack.getItem();
        allowTreasure = item.isTreasureAllowed(stack, allowTreasure);
        for (Enchantment enchantment : ForgeRegistries.ENCHANTMENTS) {
            EnchantmentInfo info = EnchModule.getEnchInfo(enchantment);
            if (info.isTreasure() && !allowTreasure || !info.isDiscoverable()) continue;
            if (blacklist.contains(enchantment)) continue;
            if (enchantment.canApplyAtEnchantingTable(stack) || item.forciblyAllowsTableEnchantment(stack, enchantment)) {
                for (int level = info.getMaxLevel(); level > enchantment.getMinLevel() - 1; --level) {
                    if (power >= info.getMinPower(level) && power <= info.getMaxPower(level)) {
                        list.add(new EnchantmentInstance(enchantment, level));
                        break;
                    }
                }
            }
        }
        return list;
    }

    /**
     * Generates a quanta factor, which is a value within the range [-1, 1] used to scale the final power.
     * <p>
     * The initial value is normally distributed within [-1, 1] with mean = 0 and stdev = 0.33.
     * <p>
     * This is done by using {@link RandomSource#nextGaussian()} which returns a normally distributed
     * value with mean = 0 and stdev = 1, and dividing it by three (reducing the stdev to 0.33).<br>
     * Any values outside the range [-1, 1] are clamped to fit the range.
     * <p>
     * Finally, values that would be blocked by rectification are uniformly distributed across the remaining space.<br>
     * The resulting distribution is some weird frankenstein that is normal over [-1, 1] but approaches uniform over [0, 1]
     * as rectification increases.
     *
     * @param rand          The pre-seeded enchanting random.
     * @param quanta        The quanta value, in [0, 100].
     * @param rectification The rectification value, in [0, 100].
     * @return A quanta factor that should be multiplied with the base power to retrieve the final power.
     */
    public static float getQuantaFactor(RandomSource rand, float quanta, float rectification) {
        float gaussian = (float) rand.nextGaussian();
        float factor = Mth.clamp(gaussian / 3F, -1F, 1F);

        float rectPercent = rectification / 100F;

        if (factor < rectPercent - 1) {
            factor = Mth.nextFloat(rand, rectPercent - 1, 1);
        }

        return quanta * factor / 100F;
    }

    public static class ArcanaEnchantmentData extends IntrusiveBase {
        EnchantmentInstance data;

        public ArcanaEnchantmentData(Arcana arcana, EnchantmentInstance data) {
            super(arcana.getRarities()[data.enchantment.getRarity().ordinal()]);
            this.data = data;
        }
    }
}
