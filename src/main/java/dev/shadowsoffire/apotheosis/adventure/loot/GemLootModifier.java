package dev.shadowsoffire.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig;
import dev.shadowsoffire.apotheosis.adventure.AdventureConfig.LootPatternMatcher;
import dev.shadowsoffire.apotheosis.adventure.compat.GameStagesCompat.IStaged;
import dev.shadowsoffire.apotheosis.adventure.socket.gem.GemRegistry;
import dev.shadowsoffire.placebo.reload.WeightedDynamicRegistry.IDimensional;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class GemLootModifier extends LootModifier {

    public static final Codec<GemLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, GemLootModifier::new));

    protected GemLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        if (!Apotheosis.enableAdventure) return generatedLoot;
        for (LootPatternMatcher m : AdventureConfig.GEM_LOOT_RULES) {
            if (m.matches(context.getQueriedLootTableId())) {
                if (context.getRandom().nextFloat() <= m.chance()) {
                    var player = GemLootPoolEntry.findPlayer(context);
                    if (player == null) return generatedLoot;
                    float luck = context.getLuck();
                    ItemStack gem = GemRegistry.createRandomGemStack(context.getRandom(), context.getLevel(), luck, IDimensional.matches(context.getLevel()), IStaged.matches(player));
                    generatedLoot.add(gem);
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
