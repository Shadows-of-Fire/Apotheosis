package dev.shadowsoffire.apotheosis.socket.gem.storage;

import java.util.EnumMap;
import java.util.HashSet;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import dev.shadowsoffire.apotheosis.Apoth;
import dev.shadowsoffire.apotheosis.Apoth.Tiles;
import dev.shadowsoffire.apotheosis.socket.gem.Gem;
import dev.shadowsoffire.apotheosis.socket.gem.GemItem;
import dev.shadowsoffire.apotheosis.socket.gem.GemRegistry;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.UnsocketedGem;
import dev.shadowsoffire.placebo.block_entity.TickingBlockEntity;
import dev.shadowsoffire.placebo.network.VanillaPacketDispatcher;
import dev.shadowsoffire.placebo.reload.DynamicHolder;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;

public abstract class GemCaseTile extends BlockEntity implements TickingBlockEntity {

    protected final Object2ObjectMap<DynamicHolder<Gem>, EnumMap<Purity, Integer>> gems = new Object2ObjectLinkedOpenHashMap<>();
    protected final Set<GemCaseMenu> activeContainers = new HashSet<>();
    protected final IItemHandler itemHandler = new GemCaseItemHandler();
    protected final int maxCount;
    private final Int2ObjectMap<UnsocketedGem> slotIndicies = new Int2ObjectOpenHashMap<>();

    // Client-side only: Animation state for gem position switching
    private GemCaseAnimationState animationState;

    public GemCaseTile(BlockEntityType<?> type, BlockPos pos, BlockState state, int maxCount) {
        super(type, pos, state);
        this.maxCount = maxCount;
    }

    /**
     * Inserts a gem into the safe.
     * Gems beyond the storage limit will be voided.
     */
    public void depositGem(ItemStack stack) {
        UnsocketedGem gem = UnsocketedGem.of(stack);
        if (!gem.isValid()) return;

        Purity purity = gem.purity();
        EnumMap<Purity, Integer> map = this.getGems(gem.gem());
        map.put(purity, Math.min(this.maxCount, map.get(purity) + stack.getCount()));

        if (!this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }
        this.setChanged();
    }

    /**
     * Extracts a gem from the safe, creating a new ItemStack with the requested gem/purity/count.
     * <p>
     * If the requested count exceeds the stored count, only the stored count will be extracted.
     */
    public ItemStack extractGem(DynamicHolder<Gem> gem, Purity purity, int count) {
        EnumMap<Purity, Integer> map = this.getGems(gem);
        int stored = map.get(purity);
        if (stored < count) {
            count = stored;
        }

        if (count <= 0 || !gem.isBound()) {
            return ItemStack.EMPTY;
        }

        map.put(purity, stored - count);

        ItemStack stack = GemItem.createStack(gem.get(), purity, count);

        if (!this.level.isClientSide()) {
            VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
        }

        this.setChanged();

        return stack;
    }

    public boolean upgradeGem(DynamicHolder<Gem> gem, Purity purity, Container matInv) {
        GemUpgradeMatch match = this.getUpgradeMatch(gem, purity, matInv);
        if (match != null) {
            match.execute(matInv, this.getGems(gem));

            if (!this.level.isClientSide()) {
                VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
            }

            this.setChanged();
            return true;
        }
        else {
            return false;
        }
    }

    @Nullable
    public GemUpgradeMatch getUpgradeMatch(DynamicHolder<Gem> gem, Purity purity, Container matInv) {
        EnumMap<Purity, Integer> map = this.getGems(gem);
        if (map.get(purity) >= maxCount) return null;
        return GemUpgradeMatch.findMatch(this.level, purity, map, matInv);
    }

    public int getCount(DynamicHolder<Gem> gem, Purity purity) {
        return this.getGems(gem).get(purity);
    }

    public int getCount(Gem gem, Purity purity) {
        return this.getCount(GemRegistry.INSTANCE.holder(gem), purity);
    }

    /**
     * Returns the underlying purity-to-count map for the provided gem.
     */
    protected final EnumMap<Purity, Integer> getGems(DynamicHolder<Gem> gem) {
        return this.gems.computeIfAbsent(gem, g -> {
            EnumMap<Purity, Integer> map = new EnumMap<>(Purity.class);
            for (Purity p : Purity.values()) {
                map.put(p, 0);
            }
            return map;
        });
    }

    /**
     * Gets the animation state for this gem case, lazily initializing it on the client.
     * Should only be called on the client side.
     */
    public GemCaseAnimationState getAnimationState() {
        if (this.animationState == null) {
            this.animationState = new GemCaseAnimationState(this.level.getRandom());
        }
        return this.animationState;
    }

    public void saveGemData(CompoundTag tag) {
        CompoundTag gems = new CompoundTag();
        for (DynamicHolder<Gem> gem : this.gems.keySet()) {
            EnumMap<Purity, Integer> map = this.gems.get(gem);
            CompoundTag purityTag = new CompoundTag();
            for (Purity p : Purity.values()) {
                int count = map.get(p);
                if (count > 0) {
                    purityTag.putInt(p.getSerializedName(), count);
                }
            }
            gems.put(gem.getId().toString(), purityTag);
        }
        tag.put("gems", gems);
    }

    public void loadGemData(CompoundTag tag) {
        CompoundTag gems = tag.getCompound("gems");
        for (String key : gems.getAllKeys()) {
            ResourceLocation res = ResourceLocation.tryParse(key);
            DynamicHolder<Gem> gem = GemRegistry.INSTANCE.holder(res);
            if (!gem.isBound()) continue;
            CompoundTag purityTag = gems.getCompound(key);
            if (purityTag.isEmpty()) {
                this.gems.remove(gem);
            }
            else {
                EnumMap<Purity, Integer> map = new EnumMap<>(Purity.class);
                for (Purity p : Purity.values()) {
                    map.put(p, purityTag.getInt(p.getSerializedName()));
                }
                this.gems.put(gem, map);
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.saveAdditional(tag, regs);
        saveGemData(tag);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider regs) {
        super.loadAdditional(tag, regs);
        loadGemData(tag);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveGemData(tag);
        return tag;
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt, HolderLookup.Provider registries) {
        CompoundTag tag = pkt.getTag();
        loadGemData(tag);
        this.activeContainers.forEach(GemCaseMenu::onChanged);
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void clientTick(Level level, BlockPos pos, BlockState state) {
        GemCaseAnimationState animState = this.getAnimationState();

        int uniqueGems = 0;
        for (DynamicHolder<Gem> gem : this.gems.keySet()) {
            int count = 0;
            for (Purity p : Purity.ALL_PURITIES) {
                count += this.getCount(gem, p);
            }

            if (count > 0) {
                uniqueGems++;
            }
        }

        Player player = level.getNearestPlayer(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D, 4, false);

        animState.tick(Math.min(uniqueGems, 16), player != null);
    }

    public void addListener(GemCaseMenu ctr) {
        this.activeContainers.add(ctr);
    }

    public void removeListener(GemCaseMenu ctr) {
        this.activeContainers.remove(ctr);
    }

    public IItemHandler getItemHandler(Direction dir) {
        return this.itemHandler;
    }

    private UnsocketedGem getGemForSlot(int slot) {
        if (this.slotIndicies.size() != this.gems.size() * Purity.values().length) {
            this.slotIndicies.clear();
            int index = 0;
            for (DynamicHolder<Gem> gem : this.gems.keySet()) {
                for (Purity p : Purity.values()) {
                    this.slotIndicies.put(index++, new UnsocketedGem(gem, p, ItemStack.EMPTY));
                }
            }
        }
        return this.slotIndicies.getOrDefault(slot, new UnsocketedGem(GemRegistry.INSTANCE.emptyHolder(), Purity.CRACKED, ItemStack.EMPTY));
    }

    private class GemCaseItemHandler implements IItemHandler {

        /**
         * We have to account for every possible gem+purity combination as a slot.
         */
        @Override
        public int getSlots() {
            return 1 + GemCaseTile.this.gems.size() * Purity.values().length;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            if (slot < 0 || slot >= this.getSlots()) return ItemStack.EMPTY;
            UnsocketedGem gem = GemCaseTile.this.getGemForSlot(slot);
            int count = GemCaseTile.this.getCount(gem.gem(), gem.purity());
            if (count <= 0) return ItemStack.EMPTY;
            return GemItem.createStack(gem.gem().get(), gem.purity(), count);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            UnsocketedGem gem = UnsocketedGem.of(stack);
            if (!gem.isValid()) return stack;

            if (!simulate) {
                GemCaseTile.this.depositGem(stack);
            }
            return ItemStack.EMPTY;
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (slot < 0 || slot >= this.getSlots() || amount <= 0) return ItemStack.EMPTY;
            UnsocketedGem gem = GemCaseTile.this.getGemForSlot(slot);

            if (simulate) {
                int count = GemCaseTile.this.getCount(gem.gem(), gem.purity());
                return getStackInSlot(slot).copyWithCount(Math.min(count, amount));
            }
            else {
                return GemCaseTile.this.extractGem(gem.gem(), gem.purity(), amount);
            }
        }

        @Override
        public int getSlotLimit(int slot) {
            return GemCaseTile.this.maxCount;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return UnsocketedGem.of(stack).isValid();
        }

    }

    public static class BasicGemCaseTile extends GemCaseTile {

        public BasicGemCaseTile(BlockPos pos, BlockState state) {
            super(Tiles.GEM_CASE, pos, state, Short.MAX_VALUE);
        }

    }

    public static class EnderGemCaseTile extends GemCaseTile {

        public EnderGemCaseTile(BlockPos pos, BlockState state) {
            super(Tiles.ENDER_GEM_CASE, pos, state, Integer.MAX_VALUE);
        }

    }

}
