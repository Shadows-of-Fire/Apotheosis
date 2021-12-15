package shadows.apotheosis.deadly.asm;

import java.util.List;

import net.minecraft.core.NonNullList;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SmithingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.UpgradeRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.player.PlayerContainerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import shadows.apotheosis.Apotheosis;

@EventBusSubscriber(bus = Bus.FORGE, modid = Apotheosis.MODID)
public class ApothSmithingContainer extends SmithingMenu {

	protected final Level world;
	protected final List<UpgradeRecipe> recipes;

	public ApothSmithingContainer(int id, Inventory inv, ContainerLevelAccess wPos) {
		super(id, inv, wPos);
		this.world = wPos.evaluate((w, p) -> w).get();
		this.recipes = this.world.getRecipeManager().getAllRecipesFor(RecipeType.SMITHING);
	}

	@Override
	protected ItemStack onTake(Player p_230301_1_, ItemStack p_230301_2_) {
		p_230301_2_.onCraftedBy(p_230301_1_.level, p_230301_1_, p_230301_2_.getCount());
		this.resultSlots.awardUsedRecipes(p_230301_1_);
		UpgradeRecipe recipe = this.recipes.stream().filter(r -> r.matches(this.inputSlots, this.world)).findFirst().orElse(null);
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
						ServerPlayer player = (ServerPlayer) this.player;
						player.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, i, r));
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
		if (e.getPlayer() instanceof ServerPlayer && e.getContainer().getClass() == SmithingMenu.class) {
			ServerPlayer player = (ServerPlayer) e.getPlayer();
			SmithingMenu container = (SmithingMenu) e.getContainer();
			player.containerMenu = new ApothSmithingContainer(container.containerId, player.inventory, container.access);
		}
	}

}
