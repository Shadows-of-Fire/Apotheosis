package shadows.apotheosis.miscs;

import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import shadows.apotheosis.Apotheosis;

public class MiscCapability {

    public static final Capability<IItemPickupIgnoreListHandler> ITEM_PICKUP_IGNORE_LIST_CAPABILITY =
            CapabilityManager.get(new CapabilityToken<>() { });

    public static final ResourceLocation ID_ITEM_PICKUP_IGNORE_LIST = new ResourceLocation(Apotheosis.MODID, "ipilist");
}
