package shadows.apotheosis.deadly.affix.impl.ranged;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.affix.modifiers.AffixModifier;
import shadows.apotheosis.deadly.loot.LootCategory;
import shadows.apotheosis.deadly.loot.LootRarity;

import javax.annotation.Nullable;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Decreases how long it takes to fully charge a bow.
 */
public class DrawSpeedAffix extends RangedAffix {

    private static final float[] values = { 0.1F, 0.2F, 0.25F, 0.33F, 0.5F, 1.0F, 1.1F, 1.2F, 1.25F, 1.33F, 1.5F };

    @Override
    public boolean isPrefix() {
        return false;
    }

    public DrawSpeedAffix(LootRarity rarity, int weight) {
        super(rarity, 0.1F, 1.5F, weight);
    }

    @Override
    public boolean canApply(LootCategory lootCategory) { return lootCategory.isRanged(); }

    @Override
    public float generateLevel(ItemStack stack, Random rand, @Nullable AffixModifier modifier) {
        float lvl = range.getRandomValue(rand);
        if(modifier!=null)
            modifier.editLevel(lvl, range.getMin(), range.getMax());
        for (float value : values) {
            if (lvl <= value) {
                lvl = value;
                break;
            }
        }
        return lvl;
    }

    @Override
    public float upgradeLevel(float curLvl, float newLvl) {
        int curIdx = 0, newIdx = 0;
        for (int i = 0; i < values.length; i++) {
            if (values[i] == curLvl) curIdx = i;
            if (values[i] == newLvl) newIdx = i;
        }
        return values[Math.min(values.length - 1, curIdx > newIdx ? curIdx + newIdx / 2 : curIdx / 2 + newIdx)];
    }

    @Override
    public void addInformation(ItemStack stack, float level, Consumer<Component> list) {
        list.accept(loreComponent("affix." + this.getRegistryName() + ".desc", fmt(level*100)));
    }
}