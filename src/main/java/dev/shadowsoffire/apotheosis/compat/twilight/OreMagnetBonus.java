package dev.shadowsoffire.apotheosis.compat.twilight;

import java.util.Map;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;

public class OreMagnetBonus extends GemBonus {

    public static final Codec<OreMagnetBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.mapCodec(Codec.intRange(0, 4096)).fieldOf("values").forGetter(a -> a.values))
        .apply(inst, OreMagnetBonus::new));

    protected final Map<Purity, Integer> values;

    public OreMagnetBonus(GemClass gemClass, Map<Purity, Integer> values) {
        super(gemClass);
        this.values = values;
    }

    @Override
    public InteractionResult onItemUse(GemInstance inst, UseOnContext ctx) {
        BlockState state = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (state.isAir()) {
            return null;
        }
        Level level = ctx.getLevel();
        Player player = ctx.getPlayer();
        player.startUsingItem(ctx.getHand());
        // The ore magnet only checks that the use duration (72000 - param) is > 10
        // https://github.com/TeamTwilight/twilightforest/blob/1.21.x/src/main/java/twilightforest/item/OreMagnetItem.java#L76
        AdventureTwilightCompat.ORE_MAGNET.value().releaseUsing(inst.gemStack(), level, player, 0);
        player.stopUsingItem();
        int cost = this.values.get(inst.purity());
        ctx.getItemInHand().hurtAndBreak(cost, player, LivingEntity.getSlotForHand(ctx.getHand()));
        return super.onItemUse(inst, ctx);
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
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        return Component.translatable("bonus." + this.getTypeKey() + ".desc", this.values.get(gem.purity())).withStyle(ChatFormatting.YELLOW);
    }

}
