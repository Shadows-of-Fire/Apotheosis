package shadows.apotheosis.deadly.loot;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import shadows.apotheosis.deadly.affix.Affix;
import shadows.apotheosis.deadly.affix.AffixHelper;
import shadows.apotheosis.deadly.affix.impl.RangedAffix;
import shadows.apotheosis.deadly.config.DeadlyConfig;

public class LootController {


    //pc3k: until all affixes get both prefix/suffix name variants im adding temp boolean to indicate default affix type (prefix or suffix)
    //affix will not be added to name if it already contains affix of this type (solves issue with naming 3 affixes item too)
    /**
     * Applies affixes to the passed in itemstack.
     */
    public static ItemStack lootifyItem(ItemStack stack, LootRarity rarity, Random rand) {
        LootCategory cat = LootCategory.forItem(stack);
        if (cat == null) return stack;

        AffixHelper.setRarity(stack, rarity);
        List<Affix> affixes = AffixHelper.getAffixesFor(cat, rarity);
        Collections.shuffle(affixes, rand);

        var rolledAffixes = affixes.stream().limit(rarity.getAffixes()).collect(Collectors.toList());

        var prefixNameGiver = rolledAffixes.stream().filter(affix -> affix.isPrefix()).findFirst();
        var suffixNameGiver = rolledAffixes.stream().filter(affix -> !affix.isPrefix()).findFirst();

//        if(rarity == LootRarity.EPIC || rarity == LootRarity.MYTHIC)
//        {
//            //pc3k: no idea about how affix modifiers are planned to affect affixes
//            // easiest one would be just roll multiplier value 1-1.5 or sth
//            // and apply it to item so it interacts with level generation below
//        }


        for (Affix affix : rolledAffixes) {
            float level = affix instanceof RangedAffix rangedAffix ? rangedAffix.generateLevel(stack, rand, null) : rand.nextFloat();
            //pc3k: as per info left in LootRarity - common items have affix power cut in half
            if(rarity == LootRarity.COMMON)
                level /= 2;
            AffixHelper.applyAffix(stack, affix, level);
        }

        if (rarity.ordinal() >= LootRarity.MYTHIC.ordinal() && DeadlyConfig.mythicUnbreakable) {
            var tag = stack.getOrCreateTag();
            tag.putBoolean("Unbreakable", true);
        }

        Component name = stack.getItem().getName(stack);
        if(prefixNameGiver.isPresent())
            name = prefixNameGiver.get().chainName(name, true);
        if(suffixNameGiver.isPresent())
            name = suffixNameGiver.get().chainName(name, false);

        stack.setHoverName(name);

        return stack;
    }

}