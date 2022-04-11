package shadows.apotheosis.ench.library;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ApotheosisObjects;
import shadows.placebo.recipe.VanillaPacketDispatcher;

public abstract class EnchLibraryTile extends TileEntity {

	protected final Object2IntMap<Enchantment> points = new Object2IntOpenHashMap<>();
	protected final Object2IntMap<Enchantment> maxLevels = new Object2IntOpenHashMap<>();
	protected final Set<EnchLibraryContainer> activeContainers = new HashSet<>();
	protected final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(EnchLibItemHandler::new);
	protected final int maxLevel;
	protected final int maxPoints;

	public EnchLibraryTile(TileEntityType<?> type, int maxLevel) {
		super(type);
		this.maxLevel = maxLevel;
		this.maxPoints = levelToPoints(maxLevel);
	}

	/**
	 * Inserts a book into this library.
	 * Handles the updating of the points and max levels maps.
	 * Extra enchantment levels that cannot be voided will be destroyed.
	 * @param book An enchanted book
	 */
	public void depositBook(ItemStack book) {
		if (book.getItem() != Items.ENCHANTED_BOOK) return;
		Map<Enchantment, Integer> enchs = EnchantmentHelper.getEnchantments(book);
		for (Map.Entry<Enchantment, Integer> e : enchs.entrySet()) {
			if (e.getKey() == null || e.getValue() == null) continue;
			int newPoints = Math.min(this.maxPoints, this.points.getInt(e.getKey()) + levelToPoints(e.getValue()));
			if (newPoints < 0) newPoints = maxPoints;
			this.points.put(e.getKey(), newPoints);
			this.maxLevels.put(e.getKey(), Math.min(this.maxLevel, Math.max(this.maxLevels.getInt(e.getKey()), e.getValue())));
		}
		if (enchs.size() > 0) VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		this.setChanged();
	}

	/**
	 * Sets the level on the provided itemstack to the requested level.
	 * Does nothing if the operation is impossible.
	 * Decrements point values equal to the amount of points required to jump between the current level and the requested level.
	 */
	public void extractEnchant(ItemStack stack, Enchantment ench, int level) {
		int curLvl = EnchantmentHelper.getEnchantments(stack).getOrDefault(ench, 0);
		if (stack.isEmpty() || !this.canExtract(ench, level, curLvl) || level == curLvl) return;
		Map<Enchantment, Integer> enchs = EnchantmentHelper.getEnchantments(stack);
		enchs.put(ench, level);
		EnchantmentHelper.setEnchantments(enchs, stack);
		this.points.put(ench, Math.max(0, (this.points.getInt(ench) - levelToPoints(level) + levelToPoints(curLvl)))); //Safety, should never be below zero anyway.
		if (!this.level.isClientSide()) VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
		this.setChanged();
	}

	/**
	 * Checks if this level of an enchantment can be extracted from this library, given the current level of the enchantment on the item.
	 * @param ench The enchantment being extracted
	 * @param level The desired target level
	 * @param currentLevel The current level of this enchantment on the item being applied to.
	 * @return If this level of this enchantment can be extracted.
	 */
	public boolean canExtract(Enchantment ench, int level, int currentLevel) {
		return this.maxLevels.getInt(ench) >= level && this.points.getInt(ench) >= levelToPoints(level) - levelToPoints(currentLevel);
	}

	/**
	 * Converts an enchantment level into the corresponding point value.
	 * @param level The level to convert.
	 * @return 2^(level - 1)
	 */
	public static int levelToPoints(int level) {
		return (int) Math.pow(2, level - 1);
	}

	@Override
	public CompoundNBT save(CompoundNBT tag) {
		CompoundNBT points = new CompoundNBT();
		for (Object2IntMap.Entry<Enchantment> e : this.points.object2IntEntrySet()) {
			points.putInt(e.getKey().getRegistryName().toString(), e.getIntValue());
		}
		tag.put("Points", points);
		CompoundNBT levels = new CompoundNBT();
		for (Object2IntMap.Entry<Enchantment> e : this.maxLevels.object2IntEntrySet()) {
			levels.putInt(e.getKey().getRegistryName().toString(), e.getIntValue());
		}
		tag.put("Levels", levels);
		return super.save(tag);
	}

	@Override
	public void load(BlockState state, CompoundNBT tag) {
		super.load(state, tag);
		CompoundNBT points = tag.getCompound("Points");
		for (String s : points.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.points.put(ench, points.getInt(s));
		}
		CompoundNBT levels = tag.getCompound("Levels");
		for (String s : levels.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.maxLevels.put(ench, levels.getInt(s));
		}
	}

	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
		CompoundNBT tag = pkt.getTag();
		CompoundNBT points = tag.getCompound("Points");
		for (String s : points.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.points.put(ench, points.getInt(s));
		}
		CompoundNBT levels = tag.getCompound("Levels");
		for (String s : levels.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.maxLevels.put(ench, levels.getInt(s));
		}
		this.activeContainers.forEach(EnchLibraryContainer::onChanged);
	}

	@Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		return new SUpdateTileEntityPacket(this.worldPosition, -12, this.getUpdateTag());
	}

	@Override
	public CompoundNBT getUpdateTag() {
		CompoundNBT tag = super.getUpdateTag();
		CompoundNBT points = new CompoundNBT();
		for (Object2IntMap.Entry<Enchantment> e : this.points.object2IntEntrySet()) {
			points.putInt(e.getKey().getRegistryName().toString(), e.getIntValue());
		}
		tag.put("Points", points);
		CompoundNBT levels = new CompoundNBT();
		for (Object2IntMap.Entry<Enchantment> e : this.maxLevels.object2IntEntrySet()) {
			levels.putInt(e.getKey().getRegistryName().toString(), e.getIntValue());
		}
		tag.put("Levels", levels);
		return tag;
	}

	public Object2IntMap<Enchantment> getPointsMap() {
		return this.points;
	}

	public Object2IntMap<Enchantment> getLevelsMap() {
		return this.maxLevels;
	}

	public void addListener(EnchLibraryContainer ctr) {
		this.activeContainers.add(ctr);
	}

	public void removeListener(EnchLibraryContainer ctr) {
		this.activeContainers.remove(ctr);
	}

	public int getMax(Enchantment ench) {
		return Math.min(this.maxLevel, this.maxLevels.getInt(ench));
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return this.itemHandler.cast();
		return super.getCapability(cap, side);
	}

	private class EnchLibItemHandler implements IItemHandler {

		@Override
		public int getSlots() {
			return 1;
		}

		@Override
		public ItemStack getStackInSlot(int slot) {
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
			if (stack.getItem() != Items.ENCHANTED_BOOK || stack.getCount() > 1) return stack;
			else if (!simulate) {
				EnchLibraryTile.this.depositBook(stack);
			}
			return ItemStack.EMPTY;
		}

		@Override
		public ItemStack extractItem(int slot, int amount, boolean simulate) {
			return ItemStack.EMPTY;
		}

		@Override
		public int getSlotLimit(int slot) {
			return 1;
		}

		@Override
		public boolean isItemValid(int slot, ItemStack stack) {
			return slot == 0 && stack.getItem() == Items.ENCHANTED_BOOK;
		}

	}

	public static class BasicLibraryTile extends EnchLibraryTile {

		public BasicLibraryTile() {
			super(ApotheosisObjects.ENCH_LIB_TILE, 16);
		}

	}

	public static class EnderLibraryTile extends EnchLibraryTile {

		public EnderLibraryTile() {
			super(ApotheosisObjects.ENDER_LIB_TILE, 31);
		}

	}

}
