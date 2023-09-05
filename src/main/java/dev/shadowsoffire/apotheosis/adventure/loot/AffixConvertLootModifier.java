package dev.shadowsoffire.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AffixConvertLootModifier extends LootModifier {

    public static final Codec<AffixConvertLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, AffixConvertLootModifier::new));

    protected AffixConvertLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Apotheosis.enableAdventure) return generatedLoot;
        for (LootPatternMatcher m : AdventureConfig.AFFIX_CONVERT_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                RarityClamp rarities = AdventureConfig.AFFIX_CONVERT_RARITIES.get(context.getLevel().dimension().location());
                RandomSource rand = context.getRandom();
                float luck = context.getLuck();
                for (ItemStack s : generatedLoot) {
                    if (!LootCategory.forItem(s).isNone() && AffixHelper.getAffixes(s).isEmpty() && rand.nextFloat() <= m.chance()) {
                        LootController.createLootItem(s, LootRarity.random(rand, luck, rarities), rand);
                    }
                }
                break;
            }
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
