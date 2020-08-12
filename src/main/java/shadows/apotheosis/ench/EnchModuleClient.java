package shadows.apotheosis.ench;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.tileentity.EnchantmentTableTileEntityRenderer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.altar.SeaAltarRenderer;
import shadows.apotheosis.ench.table.ApothEnchantScreen;
import shadows.apotheosis.ench.table.EnchantmentStatRegistry;

@SuppressWarnings("deprecation")
public class EnchModuleClient {

	static BlockRayTraceResult res = BlockRayTraceResult.createMiss(Vector3d.ZERO, Direction.NORTH, BlockPos.ZERO);

	@SubscribeEvent
	public void tooltips(ItemTooltipEvent e) {
		Item i = e.getItemStack().getItem();
		if (i == Items.COBWEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.cobweb").formatted(TextFormatting.GRAY));
		else if (i == ApotheosisObjects.PRISMATIC_WEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.prismatic_cobweb").formatted(TextFormatting.GRAY));
		else if (i instanceof BlockItem) {
			Block block = ((BlockItem) i).getBlock();
			World world = Minecraft.getInstance().world;
			if (world == null || Minecraft.getInstance().player == null) return;
			BlockItemUseContext ctx = new BlockItemUseContext(world, Minecraft.getInstance().player, Hand.MAIN_HAND, e.getItemStack(), res) {
			};
			BlockState state = block.getStateForPlacement(ctx);
			if (state == null) return;
			float maxEterna = EnchantmentStatRegistry.getMaxEterna(state, world, BlockPos.ZERO);
			float eterna = EnchantmentStatRegistry.getEterna(state, world, BlockPos.ZERO);
			float quanta = EnchantmentStatRegistry.getQuanta(state, world, BlockPos.ZERO);
			float arcana = EnchantmentStatRegistry.getArcana(state, world, BlockPos.ZERO);
			if (eterna != 0 || quanta != 0 || arcana != 0) {
				e.getToolTip().add(new TranslationTextComponent("info.apotheosis.ench_stats").formatted(TextFormatting.GOLD));
			}
			if (eterna != 0) {
				if (eterna > 0) {
					e.getToolTip().add(new TranslationTextComponent("info.apotheosis.eterna.p", String.format("%.2f", eterna), String.format("%.2f", maxEterna)).formatted(TextFormatting.GREEN));
				} else e.getToolTip().add(new TranslationTextComponent("info.apotheosis.eterna", String.format("%.2f", eterna)).formatted(TextFormatting.GREEN));
			}
			if (quanta != 0) {
				e.getToolTip().add(new TranslationTextComponent("info.apotheosis.quanta" + (quanta > 0 ? ".p" : ""), String.format("%.2f", quanta * 10)).formatted(TextFormatting.RED));
			}
			if (arcana != 0) {
				e.getToolTip().add(new TranslationTextComponent("info.apotheosis.arcana" + (arcana > 0 ? ".p" : ""), String.format("%.2f", arcana * 10)).formatted(TextFormatting.DARK_PURPLE));
			}
		}
	}

	public static void init() {
		DeferredWorkQueue.runLater(() -> {
			ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ALTAR_TYPE, SeaAltarRenderer::new);
			ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ENCHANTING_TABLE, EnchantmentTableTileEntityRenderer::new);
			ScreenManager.registerFactory(ApotheosisObjects.ENCHANTING, ApothEnchantScreen::new);
		});
	}
}
