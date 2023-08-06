package dev.shadowsoffire.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig.LootPatternMatcher;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AffixLootModifier extends LootModifier {

    public static final Codec<AffixLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, AffixLootModifier::new));

    protected AffixLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Apotheosis.enableAdventure) return generatedLoot;
        for (LootPatternMatcher m : AdventureConfig.AFFIX_ITEM_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                if (context.getRandom().nextFloat() <= m.chance()) {
                    var player = GemLootPoolEntry.findPlayer(context);
                    if (player == null) return generatedLoot;
                    ItemStack affixItem = LootController.createRandomLootItem(context.getRandom(), null, player, context.getLevel());
                    if (affixItem.isEmpty()) break;
                    affixItem.getTag().putBoolean("apoth_rchest", true);
                    generatedLoot.add(affixItem);
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
