package dev.shadowsoffire.apotheosis.compat;

import dev.shadowsoffire.apotheosis.Apotheosis;
import net.minecraftforge.fml.ModList;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.api.PatchouliAPI.IPatchouliAPI;

public class PatchouliCompat {

    public static void register() {
        IPatchouliAPI api = PatchouliAPI.get();
        if (!api.isStub()) {
            api.setConfigFlag("apotheosis:enchanting", Apotheosis.enableEnch);
            api.setConfigFlag("apotheosis:adventure", Apotheosis.enableAdventure);
            api.setConfigFlag("apotheosis:spawner", Apotheosis.enableSpawner);
            api.setConfigFlag("apotheosis:garden", Apotheosis.enableGarden);
            api.setConfigFlag("apotheosis:potion", Apotheosis.enablePotion);
            api.setConfigFlag("apotheosis:village", Apotheosis.enableVillage);
            api.setConfigFlag("apotheosis:wstloaded", ModList.get().isLoaded("wstweaks"));
            api.setConfigFlag("apotheosis:curiosloaded", ModList.get().isLoaded("curios"));
        }
    }

}
