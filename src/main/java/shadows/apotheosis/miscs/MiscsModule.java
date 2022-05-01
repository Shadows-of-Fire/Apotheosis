package shadows.apotheosis.miscs;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.RegisterCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shadows.apotheosis.Apotheosis.ApotheosisConstruction;

public class MiscsModule {
    public static final Logger LOGGER = LogManager.getLogger("Apotheosis : MISCS");

    @SubscribeEvent
    public void preInit(ApotheosisConstruction e) {

    }

    @SubscribeEvent
    public void init(FMLCommonSetupEvent e) {
        MinecraftForge.EVENT_BUS.register(new MiscsModuleForgeEvents());
    }

    @SubscribeEvent
    public void registerCaps(RegisterCapabilitiesEvent event) {
        event.register(ItemPickupIgnoreListCapability.class);
    }


}
