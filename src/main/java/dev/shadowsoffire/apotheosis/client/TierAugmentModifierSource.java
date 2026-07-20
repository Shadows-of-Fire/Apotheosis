package dev.shadowsoffire.apotheosis.client;

import java.util.Comparator;

import com.mojang.blaze3d.vertex.PoseStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apotheosis.tiers.augments.AttributeAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugment.Target;
import dev.shadowsoffire.apotheosis.tiers.augments.TierAugmentRegistry;
import dev.shadowsoffire.apothic_attributes.client.ModifierSource;
import dev.shadowsoffire.apothic_attributes.client.ModifierSourceType;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;

/**
 * ModifierSource for attribute modifiers originating from TierAugments applied to the local player.
 * <p>
 * Extracts modifiers from {@link AttributeAugment}s bound to the player's active {@link WorldTier}
 * with {@link Target#PLAYERS}. Renders as the world tier button icon.
 */
public class TierAugmentModifierSource extends ModifierSource<TierAugment> {

    private static final Comparator<TierAugment> COMPARATOR = Comparator
        .comparing((TierAugment t) -> t.tier().ordinal())
        .thenComparingInt(TierAugment::sortIndex);

    public static final ModifierSourceType<TierAugment> TIER_AUGMENT = ModifierSourceType.register(new ModifierSourceType<TierAugment>(){

        @Override
        public void extract(LivingEntity entity, java.util.function.BiConsumer<AttributeModifier, ModifierSource<?>> map) {
            if (!(entity instanceof Player player)) {
                return;
            }
            WorldTier tier = WorldTier.getTier(player);
            for (TierAugment aug : TierAugmentRegistry.getAugments(tier, Target.PLAYERS)) {
                if (aug instanceof AttributeAugment attr) {
                    AttributeModifier modif = attr.modifier().createDeterministic(attr.id());
                    map.accept(modif, new TierAugmentModifierSource(aug));
                }
            }
        }

        @Override
        public int getPriority() {
            return 50;
        }

    });

    public TierAugmentModifierSource(TierAugment data) {
        super(TIER_AUGMENT, COMPARATOR, data);
    }

    @Override
    public void render(GuiGraphics gfx, Font font, int x, int y) {
        // The button texture sheet is 30x90 (three vertical frames); render only the first frame scaled to a 9x9 icon.
        ResourceLocation tex = Apotheosis.loc("textures/gui/buttons/" + this.data.tier().getSerializedName() + ".png");
        PoseStack pose = gfx.pose();
        pose.pushPose();
        float scale = 9F / 30F;
        pose.scale(scale, scale, 1F);
        pose.translate(x / scale, y / scale, 0F);
        gfx.blit(tex, 0, 0, 0, 0, 30, 30, 30, 90);
        pose.popPose();
    }

    /**
     * Forces class-load so the static {@link #TIER_AUGMENT} registration runs before {@link ModifierSourceType#getTypes()} is first queried.
     */
    public static void bootstrap() {}

}
