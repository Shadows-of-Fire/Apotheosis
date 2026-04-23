package dev.shadowsoffire.apotheosis.compat.gateways.tiered_gate;

import java.util.List;
import java.util.function.Consumer;

import org.joml.Matrix3x2fStack;

import dev.shadowsoffire.apotheosis.Apotheosis;
import dev.shadowsoffire.apotheosis.tiers.WorldTier;
import dev.shadowsoffire.apothic_attributes.api.AttributeHelper;
import dev.shadowsoffire.gateways.client.GatewaysClient;
import dev.shadowsoffire.gateways.entity.GatewayEntity;
import dev.shadowsoffire.gateways.gate.Failure;
import dev.shadowsoffire.gateways.gate.Reward;
import dev.shadowsoffire.gateways.gate.Wave;
import dev.shadowsoffire.gateways.gate.WaveEntity;
import dev.shadowsoffire.gateways.gate.WaveModifier;
import dev.shadowsoffire.placebo.PlaceboClient;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.StringUtil;
import net.minecraft.world.item.Item.TooltipContext;
import net.minecraft.world.item.TooltipFlag;

/**
 * Client code specific to {@link TieredGateway}
 */
public class TieredGateClient {

    public static void appendPearlTooltip(TieredGateway gate, TooltipContext ctx, Consumer<Component> tooltips, TooltipFlag flag) {
        MutableComponent comp;

        int waveIdx = PlaceboClient.getTooltipScrollIndex(gate.getNumWaves());
        Wave wave = gate.getWave(waveIdx);

        WorldTier tier = gate.settings().tier();
        if (WorldTier.getTier(Minecraft.getInstance().player) != tier) {
            tooltips.accept(Apotheosis.lang("tooltip", "requires_world_tier", tier.toComponent()).withStyle(ChatFormatting.RED));
        }

        if (Minecraft.getInstance().hasShiftDown()) {
            comp = Component.translatable("tooltip.gateways.wave", waveIdx + 1, gate.getNumWaves()).withStyle(ChatFormatting.GRAY);
            comp.append(CommonComponents.SPACE);
            comp.append(Component.translatable("tooltip.gateways.scroll").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY).withUnderlined(false)));
            tooltips.accept(comp);
            comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.entities").withStyle(Style.EMPTY.withColor(0x87CEEB)));
            tooltips.accept(comp);
            for (WaveEntity entity : wave.entities()) {
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", entity.getDescription()).withStyle(Style.EMPTY.withColor(0x87CEEB)));
                tooltips.accept(comp);
            }

            if (!wave.modifiers().isEmpty()) {
                comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.modifiers").withStyle(ChatFormatting.RED));
                tooltips.accept(comp);
                for (WaveModifier modif : wave.modifiers()) {
                    modif.appendHoverText(ctx, c -> {
                        tooltips.accept(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c.withStyle(ChatFormatting.RED)).withStyle(s -> s.withColor(ChatFormatting.RED))));
                    });
                }
            }

            comp = AttributeHelper.list().append(Component.translatable("tooltip.gateways.rewards").withStyle(s -> s.withColor(ChatFormatting.GOLD)));
            tooltips.accept(comp);
            for (Reward r : wave.rewards()) {
                r.appendHoverText(ctx, c -> {
                    tooltips.accept(AttributeHelper.list().append(Component.translatable("tooltip.gateways.dot", c).withStyle(s -> s.withColor(ChatFormatting.GOLD))));
                });
            }
        }
        else {
            comp = Component.translatable("tooltip.gateways.num_wave" + (gate.getNumWaves() == 1 ? "" : "s"), gate.getNumWaves()).withStyle(ChatFormatting.GRAY);
            comp.append(CommonComponents.SPACE);
            comp.append(Component.translatable("tooltip.gateways.shift").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
            tooltips.accept(comp);
        }

        List<Failure> failures = gate.failures();
        if (!failures.isEmpty()) {
            if (Minecraft.getInstance().hasControlDown()) {
                comp = Component.translatable("tooltip.gateways.failures").withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                tooltips.accept(comp);
                for (Failure f : failures) {
                    f.appendHoverText(ctx, c -> {
                        tooltips.accept(AttributeHelper.list().append(c.withStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
                    });
                }
            }
            else {
                comp = Component.translatable("tooltip.gateways.num_failure" + (failures.size() == 1 ? "" : "s"), failures.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.RED));
                comp.append(CommonComponents.SPACE);
                comp.append(Component.translatable("tooltip.gateways.ctrl").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
                tooltips.accept(comp);
            }
        }

        List<MutableComponent> deviations = gate.rules().buildDeviations();
        if (!deviations.isEmpty()) {
            if (Minecraft.getInstance().hasAltDown()) {
                comp = Component.translatable("tooltip.gateways.rules", deviations.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
                tooltips.accept(comp);
                deviations.forEach(c -> {
                    tooltips.accept(AttributeHelper.list().append(c.withStyle(ChatFormatting.DARK_GREEN)));
                });
            }
            else {
                comp = Component.translatable("tooltip.gateways.num_rule" + (deviations.size() == 1 ? "" : "s"), deviations.size()).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GREEN));
                comp.append(CommonComponents.SPACE);
                comp.append(Component.translatable("tooltip.gateways.alt").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_GRAY)));
                tooltips.accept(comp);
            }
        }

        List<Reward> rewards = gate.rewards();
        if (!rewards.isEmpty()) {
            comp = Component.translatable("tooltip.gateways.key_rewards").withStyle(Style.EMPTY.withColor(0x33AA20));
            tooltips.accept(comp);
            for (Reward r : rewards) {
                r.appendHoverText(ctx, c -> {
                    tooltips.accept(AttributeHelper.list().append(c.withStyle(Style.EMPTY.withColor(0x33AA20))));
                });
            }
        }
    }

    public static void renderBossBar(GatewayEntity gateEntity, Object guiGfx, int x, int y, boolean isInWorld) {
        TieredGatewayEntity gate = (TieredGatewayEntity) gateEntity;
        GuiGraphicsExtractor gfx = (GuiGraphicsExtractor) guiGfx;
        Matrix3x2fStack pose = gfx.pose();
        int color = gate.getGateway().color().getValue();
        int tintColor = 0xFF000000 | color;

        int wave = gate.getWave() + 1;
        int maxWave = gate.getGateway().getNumWaves();
        int enemies = gate.getActiveEnemies();
        int maxEnemies = gate.getCurrentWave().entities().stream().mapToInt(WaveEntity::getCount).sum();
        int y2 = y + 10 + Minecraft.getInstance().font.lineHeight;

        pose.pushMatrix();
        gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_BACKGROUND, x, y, 182, 5, tintColor);
        gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_BACKGROUND, x, y2, 182, 5, tintColor);
        pose.popMatrix();

        float waveProgress = 1F / maxWave;
        float progress = waveProgress * (maxWave - wave + 1);
        if (gate.isWaveActive()) {
            progress -= waveProgress * ((float) (maxEnemies - enemies) / maxEnemies);
        }

        int i = (int) (progress * 183.0F);
        if (i > 0) {
            gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y, i, 5, tintColor);
        }

        float maxTime = gate.getMaxWaveTime();
        if (gate.isWaveActive()) {
            i = (int) ((maxTime - gate.getTicksActive()) / maxTime * 183.0F);
            if (i > 0) {
                gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y2, i, 5, tintColor);
            }
        }
        else {
            maxTime = gate.getSetupTime();
            i = (int) (gate.getTicksActive() / maxTime * 183.0F);
            if (i > 0) {
                gfx.blitSprite(GatewaysClient.BLIT_PIPELINE, GatewaysClient.WHITE_PROGRESS, 182, 5, 0, 0, x, y2, i, 5, tintColor);
            }
        }

        Font font = Minecraft.getInstance().font;

        Component component = Component.literal(gate.getCustomName().getString()).withStyle(ChatFormatting.GOLD);
        int strWidth = font.width(component);
        int textX = x + 182 / 2 - strWidth / 2;
        int textY = y - 9;
        if (isInWorld) {
            GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
        }
        else {
            gfx.text(font, component, textX, textY, 0xFFFFFFFF, true);
        }
        textY = y2 - 9;

        int time = (int) maxTime - gate.getTicksActive();
        float tps = gateEntity.level().tickRateManager().tickrate();
        String str = I18n.get("boss.gateways.wave", wave, maxWave, StringUtil.formatTickDuration(time, tps), enemies);
        if (!gate.isWaveActive()) {
            if (gate.isLastWave()) {
                str = I18n.get("boss.gateways.done");
            }
            else {
                str = I18n.get("boss.gateways.starting", wave, StringUtil.formatTickDuration(time, tps));
            }
        }
        component = Component.literal(str).withStyle(ChatFormatting.GREEN);
        strWidth = font.width(component);
        textX = x + 182 / 2 - strWidth / 2;
        if (isInWorld) {
            GatewaysClient.drawReversedDropShadow(gfx, font, component, textX, textY);
        }
        else {
            gfx.text(font, component, textX, textY, 0xFFFFFFFF, true);
        }
    }

}
