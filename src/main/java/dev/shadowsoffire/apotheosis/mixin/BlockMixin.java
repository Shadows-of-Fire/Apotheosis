package dev.shadowsoffire.apotheosis.mixin;

import java.util.function.Supplier;

import javax.annotation.Nullable;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.LocalCapture;

import dev.shadowsoffire.apotheosis.adventure.affix.AffixHelper;
import dev.shadowsoffire.apotheosis.adventure.affix.AffixInstance;
import dev.shadowsoffire.apotheosis.adventure.affix.effect.TelepathicAffix;
import dev.shadowsoffire.apotheosis.ench.api.IEnchantingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

@Mixin(Block.class)
public abstract class BlockMixin implements IEnchantingBlock {

    @Inject(at = @At("HEAD"), method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", remap = false)
    private static void apoth_telepathicHead(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean dropXp, CallbackInfo ci) {
        if (pEntity != null && AffixHelper.getAffixes(pTool).values().stream().anyMatch(AffixInstance::enablesTelepathy)) {
            TelepathicAffix.blockDropTargetPos = new Vec3(pEntity.getX(), pEntity.getY(), pEntity.getZ());
        }
    }

    @Inject(at = @At("TAIL"), method = "dropResources(Lnet/minecraft/world/level/block/state/BlockState;Lnet/minecraft/world/level/Level;Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/entity/BlockEntity;Lnet/minecraft/world/entity/Entity;Lnet/minecraft/world/item/ItemStack;Z)V", remap = false)
    private static void apoth_telepathicTail(BlockState pState, Level pLevel, BlockPos pPos, @Nullable BlockEntity pBlockEntity, @Nullable Entity pEntity, ItemStack pTool, boolean dropXp, CallbackInfo ci) {
        TelepathicAffix.blockDropTargetPos = null;
    }

    @Inject(at = @At(value = "INVOKE", target = "net/minecraft/world/level/Level.addFreshEntity(Lnet/minecraft/world/entity/Entity;)Z"), method = "popResource(Lnet/minecraft/world/level/Level;Ljava/util/function/Supplier;Lnet/minecraft/world/item/ItemStack;)V", locals = LocalCapture.CAPTURE_FAILHARD)
    private static void apoth_telepathicTP(Level pLevel, Supplier<ItemEntity> pItemEntitySupplier, ItemStack pStack, CallbackInfo ci, ItemEntity itemEntity) {
        if (TelepathicAffix.blockDropTargetPos != null) {
            itemEntity.setPos(TelepathicAffix.blockDropTargetPos);
            itemEntity.setPickUpDelay(0);
        }
    }

}
