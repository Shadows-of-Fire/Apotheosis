package shadows.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import shadows.apotheosis.Apotheosis;
import shadows.apotheosis.adventure.AdventureConfig;
import shadows.apotheosis.adventure.AdventureConfig.LootPatternMatcher;
import shadows.apotheosis.adventure.affix.AffixHelper;
import shadows.apotheosis.adventure.loot.LootRarity.Clamped;

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
                Clamped rarities = AdventureConfig.AFFIX_CONVERT_RARITIES.get(context.getLevel().dimension().location());
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
