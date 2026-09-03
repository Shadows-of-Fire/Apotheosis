package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.affix.Affix;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import dev.shadowsoffire.apothic_attributes.api.ALObjects;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

/**
 * Causes entities to drop a % increased amount of loot when killed with primarily cold damage.
 * <p>
 * Primarily is defined as at least 75% of the damage the entity took being cold damage.
 */
public class FrozenDropsBonus extends GemBonus {

    public static Codec<FrozenDropsBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(Codec.floatRange(0, 100)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, FrozenDropsBonus::new));

    protected final Map<Purity, Float> values;

    public FrozenDropsBonus(GemClass gemClass, Map<Purity, Float> values) {
        super(gemClass);
        this.values = values;
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public boolean supports(Purity purity) {
        return this.values.containsKey(purity);
    }

    @Override
    public Component getSocketBonusTooltip(GemView inst, AttributeTooltipContext ctx) {
        float value = this.values.get(inst.purity());
        Component dmgName = Component.translatable(ALObjects.Attributes.COLD_DAMAGE.value().getDescriptionId()).withStyle(ChatFormatting.BLUE);
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", dmgName, Affix.fmt(value * 100)).withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public void modifyLoot(GemInstance inst, ObjectArrayList<ItemStack> loot, LootContext ctx) {
        Entity entity = ctx.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity instanceof Mob mob && ctx.getParamOrNull(LootContextParams.DIRECT_ATTACKING_ENTITY) instanceof ServerPlayer) {

            float coldDmgTaken = mob.getData(Attachments.COLD_DAMAGE_TAKEN);

            if (coldDmgTaken / mob.getMaxHealth() >= 0.55F) {
                increaseLootDrops(loot, values.get(inst.purity()), ctx.getRandom());
            }

            // TODO: Spawn some kind of visual effect - can we even do that from within a loot table?
            // I guess we can recompute the value in a different hook
        }
    }

    /**
     * Increases the amount of loot in the list by a percentage amount.
     * <p>
     * For items where count * (1+percent) would be a partial value, that partial value is rolled as a random chance to be rounded up or down.
     */
    private static void increaseLootDrops(ObjectArrayList<ItemStack> loot, float percent, RandomSource rand) {
        for (int i = 0; i < loot.size(); i++) {
            ItemStack stack = loot.get(i);
            if (stack.is(Apoth.Tags.CANNOT_BE_DUPLICATED)) {
                continue;
            }

            int max = stack.getMaxStackSize();

            // Figure out how much we want to increase the loot drops by.
            float target = stack.getCount() * percent;
            int newCount = (int) target;
            if (target - newCount > 0.001F) {
                if (rand.nextFloat() <= (target - newCount)) {
                    newCount++;
                }
            }

            if (stack.getCount() < max) {
                // Try to place as much as possible in the existing stack.
                int added = Math.min(newCount, max - stack.getCount());
                stack.grow(added);
                newCount -= added;
            }

            while (newCount > 0) {
                // Start creating new stacks to hold any leftovers.
                int added = Math.min(newCount, max);
                ItemStack newStack = stack.copyWithCount(added);
                loot.add(i, newStack);
                i++;
                newCount -= added;
            }

        }
    }

}
