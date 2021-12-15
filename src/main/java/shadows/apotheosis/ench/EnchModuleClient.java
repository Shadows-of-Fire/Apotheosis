package shadows.apotheosis.ench;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.renderer.blockentity.EnchantTableRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.altar.SeaAltarRenderer;
import shadows.apotheosis.ench.library.EnchLibraryScreen;
import shadows.apotheosis.ench.table.ApothEnchantScreen;
import shadows.apotheosis.ench.table.EnchantingStatManager;

@SuppressWarnings("deprecation")
public class EnchModuleClient {

	static BlockHitResult res = BlockHitResult.miss(Vec3.ZERO, Direction.NORTH, BlockPos.ZERO);

	@SubscribeEvent
	public void tooltips(ItemTooltipEvent e) {
		Item i = e.getItemStack().getItem();
		if (i == Items.COBWEB) e.getToolTip().add(new TranslatableComponent("info.apotheosis.cobweb").withStyle(ChatFormatting.GRAY));
		else if (i == ApotheosisObjects.PRISMATIC_WEB) e.getToolTip().add(new TranslatableComponent("info.apotheosis.prismatic_cobweb").withStyle(ChatFormatting.GRAY));
		else if (i instanceof BlockItem) {
			Block block = ((BlockItem) i).getBlock();
			Level world = Minecraft.getInstance().level;
			if (world == null || Minecraft.getInstance().player == null) return;
			BlockPlaceContext ctx = new BlockPlaceContext(world, Minecraft.getInstance().player, InteractionHand.MAIN_HAND, e.getItemStack(), res) {
			};
			BlockState state = null;
			try {
				state = block.getStateForPlacement(ctx);
			} catch (Exception ex) {
				EnchModule.LOGGER.debug(ex.getMessage());
				StackTraceElement[] trace = ex.getStackTrace();
				for (StackTraceElement traceElement : trace)
					EnchModule.LOGGER.debug("\tat " + traceElement);
			}

			if (state == null) state = block.defaultBlockState();
			float maxEterna = EnchantingStatManager.getMaxEterna(state, world, BlockPos.ZERO);
			float eterna = EnchantingStatManager.getEterna(state, world, BlockPos.ZERO);
			float quanta = EnchantingStatManager.getQuanta(state, world, BlockPos.ZERO);
			float arcana = EnchantingStatManager.getArcana(state, world, BlockPos.ZERO);
			if (eterna != 0 || quanta != 0 || arcana != 0) {
				e.getToolTip().add(new TranslatableComponent("info.apotheosis.ench_stats").withStyle(ChatFormatting.GOLD));
			}
			if (eterna != 0) {
				if (eterna > 0) {
					e.getToolTip().add(new TranslatableComponent("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).withStyle(ChatFormatting.GREEN));
				} else e.getToolTip().add(new TranslatableComponent("info.apotheosis.eterna", String.format("%.2f", eterna)).withStyle(ChatFormatting.GREEN));
			}
			if (quanta != 0) {
				e.getToolTip().add(new TranslatableComponent("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta * 10)).withStyle(ChatFormatting.RED));
			}
			if (arcana != 0) {
				e.getToolTip().add(new TranslatableComponent("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana * 10)).withStyle(ChatFormatting.DARK_PURPLE));
			}
		}
	}

	public static void init() {
		ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ALTAR_TYPE, SeaAltarRenderer::new);
		ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ENCHANTING_TABLE, EnchantTableRenderer::new);
		MenuScreens.register(ApotheosisObjects.ENCHANTING, ApothEnchantScreen::new);
		MenuScreens.register(ApotheosisObjects.ENCH_LIB_CON, EnchLibraryScreen::new);
	}
}