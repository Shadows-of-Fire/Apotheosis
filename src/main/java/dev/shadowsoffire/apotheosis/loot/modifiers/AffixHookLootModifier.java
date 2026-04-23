package dev.shadowsoffire.apotheosis.loot.modifiers;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.socket.SocketHelper;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.common.loot.LootModifier;

public class AffixHookLootModifier extends LootModifier {

    public static final MapCodec<AffixHookLootModifier> CODEC = RecordCodecBuilder.mapCodec(inst -> codecStart(inst).apply(inst, AffixHookLootModifier::new));

    public AffixHookLootModifier(LootItemCondition[] conditions, int priority) {
        super(conditions, priority);
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext ctx) {
        if (ctx.hasParameter(LootContextParams.TOOL)) {
            ItemStack tool = (ItemStack) ctx.getParameter(LootContextParams.TOOL);
            SocketHelper.getGems(tool).modifyLoot(generatedLoot, ctx);
            AffixHelper.streamAffixes(tool).forEach(inst -> inst.modifyLoot(generatedLoot, ctx));
        }
        else if (ctx.getOptionalParameter(LootContextParams.DIRECT_ATTACKING_ENTITY) instanceof LivingEntity living) {
            ItemStack weapon = living.getWeaponItem();
            if (weapon != null) {
                SocketHelper.getGems(weapon).modifyLoot(generatedLoot, ctx);
                AffixHelper.streamAffixes(weapon).forEach(inst -> inst.modifyLoot(generatedLoot, ctx));
            }
        }
        return generatedLoot;
    }

    @Override
    public MapCodec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

}
