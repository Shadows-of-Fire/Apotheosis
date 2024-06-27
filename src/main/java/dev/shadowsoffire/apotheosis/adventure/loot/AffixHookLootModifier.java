package dev.shadowsoffire.apotheosis.adventure.loot;

import com.mojang.serialization.Codec;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.socket.SocketHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class AffixHookLootModifier extends LootModifier {

    public static final Codec<AffixHookLootModifier> CODEC = Codec.unit(AffixHookLootModifier::new);

    protected AffixHookLootModifier() {
        super(new LootItemCondition[0]);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        if (!Apotheosis.enableAdventure) return generatedLoot;
        if (ctx.hasParam(LootContextParams.TOOL)) {
            ItemStack tool = ctx.getParam(LootContextParams.TOOL);
            SocketHelper.getGems(tool).modifyLoot(generatedLoot, ctx);
            AffixHelper.streamAffixes(tool).forEach(inst -> inst.modifyLoot(generatedLoot, ctx));
        }
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
