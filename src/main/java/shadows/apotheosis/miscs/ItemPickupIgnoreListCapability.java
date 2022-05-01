package shadows.apotheosis.miscs;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class ItemPickupIgnoreListCapability {

    public static ICapabilityProvider createProvider(final Player playerEntity) {
        return new ItemPickupIgnoreListCapability.Provider(playerEntity);
    }

    public static class ItemPickupIgnoreList implements IItemPickupIgnoreListHandler{
        protected final Player listOwner;
        protected Set<ResourceLocation> ignoreList;

        public ItemPickupIgnoreList(final Player playerEntity) {
            this.listOwner = playerEntity;
            ignoreList = new HashSet<>();
            this.reset();
        }

        @Override
        public Tag writeTag() {
            CompoundTag ipiListTag = new CompoundTag();

            ListTag tagList = new ListTag();
            this.getIgnoreList().forEach(entry ->{
                CompoundTag tag = new CompoundTag();
                tag.putString("item", entry.toString());
                tagList.add(tag);
            });
            ipiListTag.put("ipiList", tagList);
            return ipiListTag;
        }

        @Override
        public void readTag(Tag nbt) {
            ListTag tagList = ((CompoundTag) nbt).getList("ipiList", Tag.TAG_COMPOUND);
            if(tagList.isEmpty()) return;
            reset();
            for (int i = 0; i < tagList.size(); i++) {
                CompoundTag tag = tagList.getCompound(i);
                ignoreList.add(new ResourceLocation(tag.getString("item")));
            }
        }

        @Override
        public void reset() { ignoreList.clear(); }

        @Override
        public Set<ResourceLocation> getIgnoreList() { return Collections.unmodifiableSet(this.ignoreList); }

        @Override
        public void setIgnoreList(Set<ResourceLocation> ignoreList) { this.ignoreList = ignoreList; }

        @Override
        public void ignoreItem(ResourceLocation resLoc) {
            ignoreList.add(resLoc);
        }

        @Override
        public void unignoreItem(ResourceLocation resLoc) {
            ignoreList.remove(resLoc);
        }
    }

    public static class Provider implements ICapabilitySerializable<Tag> {

        final LazyOptional<IItemPickupIgnoreListHandler> optional;
        final IItemPickupIgnoreListHandler handler;

        Provider(final Player playerEntity) {
            this.handler = new ItemPickupIgnoreListCapability.ItemPickupIgnoreList(playerEntity);
            this.optional = LazyOptional.of(() -> this.handler);
        }

        @Nonnull
        @Override
        public <T> LazyOptional<T> getCapability(@Nullable Capability<T> capability, Direction facing) {
            return MiscCapability.ITEM_PICKUP_IGNORE_LIST_CAPABILITY.orEmpty(capability, this.optional);
        }

        @Override
        public Tag serializeNBT() {
            return handler.writeTag();
        }

        @Override
        public void deserializeNBT(Tag nbt) {
            handler.readTag(nbt);
        }
    }
}
