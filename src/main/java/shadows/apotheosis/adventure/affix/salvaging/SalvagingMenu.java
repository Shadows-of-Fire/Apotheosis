package shadows.apotheosis.adventure.affix.salvaging;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.annotation.Nullable;

import com.google.common.base.Predicates;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.wrapper.RecipeWrapper;
import shadows.apotheosis.Apoth;
import shadows.apotheosis.Apoth.RecipeTypes;
import shadows.apotheosis.adventure.affix.salvaging.SalvagingRecipe.OutputData;
import shadows.placebo.cap.InternalItemHandler;
import shadows.placebo.container.BlockEntityContainer;
import shadows.placebo.container.FilteredSlot;

public class SalvagingMenu extends BlockEntityContainer<SalvagingTableTile> {

	protected final Player player;
	protected final InternalItemHandler inputInv = new InternalItemHandler(15) {
		@Override
		protected void onContentsChanged(int slot) {
			if (SalvagingMenu.this.updateCallback != null) SalvagingMenu.this.updateCallback.run();
		}
	};
	protected Runnable updateCallback;

	public SalvagingMenu(int id, Inventory inv, BlockPos pos) {
		super(Apoth.Menus.SALVAGE.get(), id, inv, pos);
		this.player = inv.player;
		for (int i = 0; i < 15; i++) {
			this.addSlot(new FilteredSlot(this.inputInv, i, 8 + i % 5 * 18, 17 + i / 5 * 18, s -> findMatch(level, s) != null) {

				@Override
				public int getMaxStackSize() {
					return 1;
				}

				@Override
				public int getMaxStackSize(ItemStack stack) {
					return 1;
				}
			});
		}

		for (int i = 0; i < 6; i++) {
			this.addSlot(new FilteredSlot(this.tile.output, i, 134 + i % 2 * 18, 17 + i / 2 * 18, Predicates.alwaysFalse()));
		}

		this.addPlayerSlots(inv, 8, 84);
		this.mover.registerRule((stack, slot) -> slot >= this.playerInvStart && findMatch(level, stack) != null, 0, 15);
		this.mover.registerRule((stack, slot) -> slot < this.playerInvStart, this.playerInvStart, this.hotbarStart + 9);
		this.registerInvShuffleRules();
	}

	public void setCallback(Runnable r) {
		this.updateCallback = r;
	}

	@Override
	public boolean stillValid(Player player) {
		if (level.isClientSide) return true;
		return level.getBlockState(pos).getBlock() == Apoth.Blocks.SALVAGING_TABLE.get();
	}

	@Override
	public void removed(Player pPlayer) {
		super.removed(pPlayer);
		if (!this.level.isClientSide) {
			this.clearContainer(pPlayer, new RecipeWrapper(this.inputInv));
		}
	}

	@Override
	public boolean clickMenuButton(Player player, int id) {
		if (id == 0) {
			salvageAll();
			player.level.playSound(null, player.blockPosition(), SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.99F, this.level.random.nextFloat() * 0.25F + 1F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.AMETHYST_CLUSTER_STEP, SoundSource.BLOCKS, 0.34F, this.level.random.nextFloat() * 0.2F + 0.8F);
			player.level.playSound(null, player.blockPosition(), SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 0.45F, this.level.random.nextFloat() * 0.5F + 0.75F);
			return true;
		}
		return super.clickMenuButton(player, id);
	}

	protected void giveItem(Player player, ItemStack stack) {
		if (!player.isAlive() || player instanceof ServerPlayer && ((ServerPlayer) player).hasDisconnected()) {
			player.drop(stack, false);
		} else {
			Inventory inventory = player.getInventory();
			if (inventory.player instanceof ServerPlayer) {
				inventory.placeItemBackInInventory(stack);
			}
		}
	}

	protected void salvageAll() {
		for (int inSlot = 0; inSlot < 15; inSlot++) {
			Slot s = this.getSlot(inSlot);
			ItemStack stack = s.getItem();
			List<ItemStack> outputs = salvageItem(this.level, stack);
			s.set(ItemStack.EMPTY);
			for (ItemStack out : outputs) {
				for (int outSlot = 0; outSlot < 6; outSlot++) {
					if (out.isEmpty()) break;
					out = this.tile.output.insertItem(outSlot, out, false);
				}
				if (!out.isEmpty()) giveItem(this.player, out);
			}
		}
	}

	public static int getSalvageCount(OutputData output, ItemStack stack, RandomSource rand) {
		int[] counts = getSalvageCounts(output, stack);
		return rand.nextInt(counts[0], counts[1] + 1);
	}

	public static int[] getSalvageCounts(OutputData output, ItemStack stack) {
		int[] out = new int[] { output.min, output.max };
		if (stack.isDamageableItem()) {
			out[1] = Math.max(out[0], Math.round(out[1] * (stack.getMaxDamage() - stack.getDamageValue()) / stack.getMaxDamage()));
		}
		return out;
	}

	public static List<ItemStack> salvageItem(Level level, ItemStack stack) {
		var recipe = findMatch(level, stack);
		if (recipe == null) return Collections.emptyList();
		List<ItemStack> outputs = new ArrayList<>();
		for (OutputData d : recipe.getOutputs()) {
			ItemStack out = d.stack.copy();
			out.setCount(getSalvageCount(d, stack, level.random));
			outputs.add(out);
		}
		return outputs;
	}

	public static List<ItemStack> getBestPossibleSalvageResults(Level level, ItemStack stack) {
		var recipe = findMatch(level, stack);
		if (recipe == null) return Collections.emptyList();
		List<ItemStack> outputs = new ArrayList<>();
		for (OutputData d : recipe.getOutputs()) {
			ItemStack out = d.stack.copy();
			out.setCount(getSalvageCounts(d, stack)[1]);
			outputs.add(out);
		}
		return outputs;
	}

	@Nullable
	public static SalvagingRecipe findMatch(Level level, ItemStack stack) {
		for (var recipe : level.getRecipeManager().getAllRecipesFor(RecipeTypes.SALVAGING)) {
			if (recipe.matches(stack)) return recipe;
		}
		return null;
	}

}
