package shadows.apotheosis.deadly.asm;

import java.util.List;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.SmithingTableContainer;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipeType;
import net.minecraft.item.crafting.SmithingRecipe;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.util.IWorldPosCallable;
import net.minecraft.util.NonNullList;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import shadows.apotheosis.Apotheosis;

@EventBusSubscriber(bus = Bus.FORGE, modid = Apotheosis.MODID)
public class ApothSmithingContainer extends SmithingTableContainer {

	protected final World world;
	protected final List<SmithingRecipe> recipes;

	public ApothSmithingContainer(int id, PlayerInventory inv, IWorldPosCallable wPos) {
		super(id, inv, wPos);
		this.world = wPos.evaluate((w, p) -> w).get();
		this.recipes = this.world.getRecipeManager().getAllRecipesFor(IRecipeType.SMITHING);
	}

	@Override
	protected ItemStack onTake(PlayerEntity p_230301_1_, ItemStack p_230301_2_) {
		p_230301_2_.onCraftedBy(p_230301_1_.level, p_230301_1_, p_230301_2_.getCount());
		this.resultSlots.awardUsedRecipes(p_230301_1_);
		SmithingRecipe recipe = this.recipes.stream().filter(r -> r.matches(this.inputSlots, this.world)).findFirst().orElse(null);
		if (recipe == null) {
			this.shrinkStackInSlot(0);
			this.shrinkStackInSlot(1);
		} else {
			NonNullList<ItemStack> remainder = recipe.getRemainingItems(this.inputSlots);
			for (int i = 0; i < remainder.size(); i++) {
				ItemStack r = remainder.get(i);
				if (!r.isEmpty()) {
					this.inputSlots.setItem(i, r);
					if (!this.world.isClientSide) {
						ServerPlayerEntity player = (ServerPlayerEntity) this.player;
						player.connection.send(new SSetSlotPacket(this.containerId, i, r));
					}
				} else this.inputSlots.getItem(i).shrink(1);
			}
		}
		this.access.execute((p_234653_0_, p_234653_1_) -> {
			p_234653_0_.levelEvent(1044, p_234653_1_, 0);
		});
		return p_230301_2_;
	}

	private void shrinkStackInSlot(int p_234654_1_) {
		ItemStack itemstack = this.inputSlots.getItem(p_234654_1_);
		itemstack.shrink(1);
		this.inputSlots.setItem(p_234654_1_, itemstack);
	}

	@SubscribeEvent
	public static void containers(PlayerContainerEvent.Open e) {
		if (e.getPlayer() instanceof ServerPlayerEntity && e.getContainer().getClass() == SmithingTableContainer.class) {
			ServerPlayerEntity player = (ServerPlayerEntity) e.getPlayer();
			SmithingTableContainer container = (SmithingTableContainer) e.getContainer();
			player.containerMenu = new ApothSmithingContainer(container.containerId, player.inventory, container.access);
		}
	}

}
