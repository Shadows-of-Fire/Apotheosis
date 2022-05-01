package shadows.apotheosis.miscs;

import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public interface IItemPickupIgnoreListHandler {
    Tag writeTag();

    void readTag(Tag nbt);

    void reset();

    Set<ResourceLocation> getIgnoreList();

    void setIgnoreList(Set<ResourceLocation> ignoreList);

    void ignoreItem(ResourceLocation resLoc);

    void unignoreItem(ResourceLocation resLoc);
}
