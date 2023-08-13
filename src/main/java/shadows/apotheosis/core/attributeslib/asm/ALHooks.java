package shadows.apotheosis.core.attributeslib.asm;

import java.util.List;

import net.minecraft.client.gui.screens.inventory.EffectRenderingInventoryScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraftforge.common.MinecraftForge;
import shadows.apotheosis.core.attributeslib.api.GatherEffectScreenTooltipsEvent;

/**
 * Contains coremod-injected hooks.
 */
public class ALHooks {

    /**
     * Injected immediately after the following line of code:
     * <code><pre>
     * List&lt;Component&gt; list = List.of(this.getEffectName(mobeffectinstance), MobEffectUtil.formatDuration(mobeffectinstance, 1.0F));
     * </pre></code>
     * This overrides the value of the list to the event-modified tooltip lines.
     * 
     * @param screen     The screen rendering the tooltip.
     * @param effectInst The effect instance whose tooltip is being rendered.
     * @param tooltip    The existing tooltip lines, which consist of the name and the duration.
     * @return The new tooltip lines, modified by the event.
     */
    public static List<Component> getEffectTooltip(EffectRenderingInventoryScreen<?> screen, MobEffectInstance effectInst, List<Component> tooltip) {
        var event = new GatherEffectScreenTooltipsEvent(screen, effectInst, tooltip);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getTooltip();
    }

}
