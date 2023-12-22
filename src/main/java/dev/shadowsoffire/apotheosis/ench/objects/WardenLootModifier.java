package dev.shadowsoffire.apotheosis.ench.objects;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.ench.Ench;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

public class WardenLootModifier extends LootModifier {

    public static final Codec<WardenLootModifier> CODEC = RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, WardenLootModifier::new));
    public static final ResourceLocation WARDEN_TABLE_ID = new ResourceLocation("minecraft", "entities/warden");

    public WardenLootModifier(LootItemCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }

    @Override
    protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext ctx) {
        if (Apotheosis.enableEnch && WARDEN_TABLE_ID.equals(ctx.getQueriedLootTableId())) {
            int amount = 1;
            if (ctx.getRandom().nextFloat() <= 0.10F + ctx.getLootingModifier() * 0.10F) {
                amount++;
            }
            loot.add(new ItemStack(Ench.Items.WARDEN_TENDRIL.get(), amount));
        }
        return loot;
    }

}
