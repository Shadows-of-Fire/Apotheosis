package dev.shadowsoffire.apotheosis.adventure.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.inventory.InventoryMenu;

public class GrayBufferSource implements MultiBufferSource {

    private final MultiBufferSource wrapped;

    public GrayBufferSource(MultiBufferSource wrapped) {
        this.wrapped = wrapped;
    }

    @Override
    public VertexConsumer getBuffer(RenderType type) {
        if (type.format() == DefaultVertexFormat.NEW_ENTITY) {
            return this.wrapped.getBuffer(AdventureModuleClient.gray(InventoryMenu.BLOCK_ATLAS));
        }
        return this.wrapped.getBuffer(type);
    }

}
