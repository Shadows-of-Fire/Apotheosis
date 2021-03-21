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
		this.world = wPos.apply((w, p) -> w).get();
		this.recipes = this.world.getRecipeManager().getRecipesForType(IRecipeType.SMITHING);
	}

	@Override
	protected ItemStack func_230301_a_(PlayerEntity p_230301_1_, ItemStack p_230301_2_) {
		p_230301_2_.onCrafting(p_230301_1_.world, p_230301_1_, p_230301_2_.getCount());
		this.field_234642_c_.onCrafting(p_230301_1_);
		SmithingRecipe recipe = this.recipes.stream().filter(r -> r.matches(this.field_234643_d_, this.world)).findFirst().orElse(null);
		if (recipe == null) {
			this.func_234654_d_(0);
			this.func_234654_d_(1);
		} else {
			NonNullList<ItemStack> remainder = recipe.getRemainingItems(this.field_234643_d_);
			for (int i = 0; i < remainder.size(); i++) {
				ItemStack r = remainder.get(i);
				if (!r.isEmpty()) {
					this.field_234643_d_.setInventorySlotContents(i, r);
					if (!this.world.isRemote) {
						ServerPlayerEntity player = (ServerPlayerEntity) this.field_234645_f_;
						player.connection.sendPacket(new SSetSlotPacket(this.windowId, i, r));
					}
				} else this.field_234643_d_.getStackInSlot(i).shrink(1);
			}
		}
		this.field_234644_e_.consume((p_234653_0_, p_234653_1_) -> {
			p_234653_0_.playEvent(1044, p_234653_1_, 0);
		});
		return p_230301_2_;
	}

	private void func_234654_d_(int p_234654_1_) {
		ItemStack itemstack = this.field_234643_d_.getStackInSlot(p_234654_1_);
		itemstack.shrink(1);
		this.field_234643_d_.setInventorySlotContents(p_234654_1_, itemstack);
	}

	@SubscribeEvent
	public static void containers(PlayerContainerEvent.Open e) {
		if (e.getPlayer() instanceof ServerPlayerEntity && e.getContainer().getClass() == SmithingTableContainer.class) {
			ServerPlayerEntity player = (ServerPlayerEntity) e.getPlayer();
			SmithingTableContainer container = (SmithingTableContainer) e.getContainer();
			player.openContainer = new ApothSmithingContainer(container.windowId, player.inventory, container.field_234644_e_);
		}
	}

}
