package shadows.apotheosis.ench.library;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unimi.dsi.fastutil.objects.Object2ByteMap;
import it.unimi.dsi.fastutil.objects.Object2ByteOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2ShortMap;
import it.unimi.dsi.fastutil.objects.Object2ShortOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.ApotheosisObjects;
import shadows.placebo.recipe.VanillaPacketDispatcher;

public class EnchLibraryTile extends BlockEntity {

	protected final Object2ShortMap<Enchantment> points = new Object2ShortOpenHashMap<>();
	protected final Object2ByteMap<Enchantment> maxLevels = new Object2ByteOpenHashMap<>();
	protected final Set<EnchLibraryContainer> activeContainers = new HashSet<>();
	protected final LazyOptional<IItemHandler> itemHandler = LazyOptional.of(() -> new EnchLibItemHandler());

	public EnchLibraryTile(BlockPos pos, BlockState state) {
		super(ApotheosisObjects.ENCH_LIB_TILE, pos, state);
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
			short nVal = (short) (this.points.getShort(e.getKey()) + levelToPoints(e.getValue()));
			if (nVal < 0) nVal = Short.MAX_VALUE;
			this.points.put(e.getKey(), nVal);
			this.maxLevels.put(e.getKey(), (byte) Math.max(this.maxLevels.getByte(e.getKey()), e.getValue().byteValue()));
		}
		if (enchs.size() > 0) VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
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
		this.points.put(ench, (short) (this.points.getShort(ench) - levelToPoints(level) + levelToPoints(curLvl)));
		VanillaPacketDispatcher.dispatchTEToNearbyPlayers(this);
	}

	/**
	 * Checks if this level of an enchantment can be extracted from this library, given the current level of the enchantment on the item.
	 * @param ench The enchantment being extracted
	 * @param level The desired target level
	 * @param currentLevel The current level of this enchantment on the item being applied to.
	 * @return
	 */
	public boolean canExtract(Enchantment ench, int level, int currentLevel) {
		return this.maxLevels.getByte(ench) >= level && this.points.getShort(ench) >= levelToPoints(level) - levelToPoints(currentLevel);
	}

	/**
	 * Converts an enchantment level into the corresponding point value.
	 * @param level The level to convert.
	 * @return 2^(level - 1)
	 */
	public static short levelToPoints(int level) {
		return (short) Math.pow(2, level - 1);
	}

	@Override
	public CompoundTag save(CompoundTag tag) {
		CompoundTag points = new CompoundTag();
		for (Object2ShortMap.Entry<Enchantment> e : this.points.object2ShortEntrySet()) {
			points.putShort(e.getKey().getRegistryName().toString(), e.getShortValue());
		}
		tag.put("Points", points);
		CompoundTag levels = new CompoundTag();
		for (Object2ByteMap.Entry<Enchantment> e : this.maxLevels.object2ByteEntrySet()) {
			levels.putByte(e.getKey().getRegistryName().toString(), e.getByteValue());
		}
		tag.put("Levels", levels);
		return super.save(tag);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		CompoundTag points = tag.getCompound("Points");
		for (String s : points.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.points.put(ench, points.getShort(s));
		}
		CompoundTag levels = tag.getCompound("Levels");
		for (String s : levels.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.maxLevels.put(ench, levels.getByte(s));
		}
	}

	@Override
	public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
		CompoundTag tag = pkt.getTag();
		CompoundTag points = tag.getCompound("Points");
		for (String s : points.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.points.put(ench, points.getShort(s));
		}
		CompoundTag levels = tag.getCompound("Levels");
		for (String s : levels.getAllKeys()) {
			Enchantment ench = ForgeRegistries.ENCHANTMENTS.getValue(new ResourceLocation(s));
			if (ench == null) continue;
			this.maxLevels.put(ench, levels.getByte(s));
		}
		this.activeContainers.forEach(EnchLibraryContainer::onChanged);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag tag = super.getUpdateTag();
		CompoundTag points = new CompoundTag();
		for (Object2ShortMap.Entry<Enchantment> e : this.points.object2ShortEntrySet()) {
			points.putShort(e.getKey().getRegistryName().toString(), e.getShortValue());
		}
		tag.put("Points", points);
		CompoundTag levels = new CompoundTag();
		for (Object2ByteMap.Entry<Enchantment> e : this.maxLevels.object2ByteEntrySet()) {
			levels.putByte(e.getKey().getRegistryName().toString(), e.getByteValue());
		}
		tag.put("Levels", levels);
		return tag;
	}

	public Object2ShortMap<Enchantment> getPointsMap() {
		return this.points;
	}

	public Object2ByteMap<Enchantment> getLevelsMap() {
		return this.maxLevels;
	}

	public void addListener(EnchLibraryContainer ctr) {
		this.activeContainers.add(ctr);
	}

	public void removeListener(EnchLibraryContainer ctr) {
		this.activeContainers.remove(ctr);
	}

	public byte getMax(Enchantment ench) {
		return this.maxLevels.getByte(ench);
	}

	@Override
	public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
		if (cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) return itemHandler.cast();
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
				depositBook(stack);
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

}
