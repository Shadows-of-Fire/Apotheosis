package shadows.apotheosis.ench.table;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentData;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.EnchantmentContainer;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.stats.Stats;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.items.SlotItemHandler;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.advancements.AdvancementTriggers;
import shadows.apotheosis.ench.objects.BlockHellBookshelf;

public class EnchantmentContainerExt extends EnchantmentContainer {

	protected IWorldPosCallable wPos = super.field_217006_g;

	public EnchantmentContainerExt(int id, PlayerInventory inv) {
		super(id, inv, IWorldPosCallable.DUMMY);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new Slot(this.tableInventory, 1, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(p_75214_1_.getItem());
			}
		});
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
	}

	public EnchantmentContainerExt(int id, PlayerInventory inv, IWorldPosCallable wPos, EnchantingTableTileEntityExt te) {
		super(id, inv, wPos);
		this.inventorySlots.clear();
		this.addSlot(new Slot(this.tableInventory, 0, 15, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return true;
			}

			@Override
			public int getSlotStackLimit() {
				return 1;
			}
		});
		this.addSlot(new SlotItemHandler(te.inv, 0, 35, 47) {
			@Override
			public boolean isItemValid(ItemStack p_75214_1_) {
				return net.minecraftforge.common.Tags.Items.GEMS_LAPIS.contains(p_75214_1_.getItem());
			}
		});
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				this.addSlot(new Slot(inv, j + i * 9 + 9, 8 + j * 18, 84 + i * 18 + 31));
			}
		}
		for (int k = 0; k < 9; ++k) {
			this.addSlot(new Slot(inv, k, 8 + k * 18, 142 + 31));
		}
	}

	@Override
	public boolean enchantItem(PlayerEntity playerIn, int id) {
		int level = enchantLevels[id];
		boolean ret = super.enchantItem(playerIn, id);
		if (ret) {
			playerIn.addStat(Stats.ENCHANT_ITEM);
			if (playerIn instanceof ServerPlayerEntity) {
				AdvancementTriggers.ENCHANTED_ITEM.trigger((ServerPlayerEntity) playerIn, this.tableInventory.getStackInSlot(0), level);
			}
		}
		return ret;
	}

	/**
	 * Callback for when the crafting matrix is changed.
	 */
	@SuppressWarnings("deprecation")
	@Override
	public void onCraftMatrixChanged(IInventory inventoryIn) {
		wPos.apply((world, pos) -> {
			if (inventoryIn == this.tableInventory) {
				ItemStack itemstack = inventoryIn.getStackInSlot(0);
				if (itemstack.getCount() == 1 && itemstack.isEnchantable()) {
					float power = getEnchPower();
					this.rand.setSeed(this.xpSeed.get());

					for (int num = 0; num < 3; ++num) {
						this.enchantLevels[num] = RealEnchantmentHelper.calcItemStackEnchantability(this.rand, num, (int) power, itemstack);
						this.enchantClue[num] = -1;
						this.worldClue[num] = -1;

						if (this.enchantLevels[num] < num + 1) {
							this.enchantLevels[num] = 0;
						}
						this.enchantLevels[num] = ForgeEventFactory.onEnchantmentLevelSet(world, pos, num, (int) power, itemstack, enchantLevels[num]);
					}

					for (int j1 = 0; j1 < 3; ++j1) {
						if (this.enchantLevels[j1] > 0) {
							List<EnchantmentData> list = this.getEnchantmentList(itemstack, j1, this.enchantLevels[j1]);

							if (list != null && !list.isEmpty()) {
								EnchantmentData enchantmentdata = list.get(this.rand.nextInt(list.size()));
								this.enchantClue[j1] = Registry.ENCHANTMENT.getId(enchantmentdata.enchantment);
								this.worldClue[j1] = enchantmentdata.enchantmentLevel;
							}
						}
					}

					this.detectAndSendChanges();
				} else {
					for (int i = 0; i < 3; ++i) {
						this.enchantLevels[i] = 0;
						this.enchantClue[i] = -1;
						this.worldClue[i] = -1;
					}
				}
			}
			return this;
		});
	}

	private List<EnchantmentData> getEnchantmentList(ItemStack stack, int enchantSlot, int level) {
		this.rand.setSeed(this.xpSeed.get() + enchantSlot);
		List<EnchantmentData> list = EnchantmentHelper.buildEnchantmentList(this.rand, stack, level, false);

		if (stack.getItem() == Items.BOOK && list.size() > 1) {
			list.remove(this.rand.nextInt(list.size()));
		}

		return list;
	}

	public float getEnchPower() {
		return wPos.apply((world, pos) -> {
			float[] powers = { 0, 0 };

			for (int j = -1; j <= 1; ++j) {
				for (int k = -1; k <= 1; ++k) {
					if ((j != 0 || k != 0) && world.isAirBlock(pos.add(k, 0, j)) && world.isAirBlock(pos.add(k, 1, j))) {
						getSinglePower(powers, world, pos.add(k * 2, 0, j * 2));
						getSinglePower(powers, world, pos.add(k * 2, 1, j * 2));
						if (k != 0 && j != 0) {
							getSinglePower(powers, world, pos.add(k * 2, 0, j));
							getSinglePower(powers, world, pos.add(k * 2, 1, j));
							getSinglePower(powers, world, pos.add(k, 0, j * 2));
							getSinglePower(powers, world, pos.add(k, 1, j * 2));
						}
					}
				}
			}
			return Math.min(60, Math.min(powers[0], 30) + powers[1]);
		}).orElse(0F);
	}

	public void getSinglePower(float[] powers, World world, BlockPos pos) {
		BlockState state = world.getBlockState(pos);
		boolean hell = state.getBlock() instanceof BlockHellBookshelf;
		float power = state.getEnchantPowerBonus(world, pos);
		powers[hell ? 1 : 0] += power;
	}

	@Override
	public ContainerType<?> getType() {
		return ApotheosisObjects.ENCHANTING;
	}
}
