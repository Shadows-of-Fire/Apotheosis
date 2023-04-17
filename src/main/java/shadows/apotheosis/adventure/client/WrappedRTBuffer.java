package shadows.apotheosis.adventure.client;

import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

public class WrappedRTBuffer implements MultiBufferSource {

	private final MultiBufferSource wrapped;

	public WrappedRTBuffer(MultiBufferSource wrapped) {
		this.wrapped = wrapped;
	}

	@Override
	public VertexConsumer getBuffer(RenderType type) {
		return new GhostVertexBuilder(this.wrapped.getBuffer(type), 0x99);
	}

}
