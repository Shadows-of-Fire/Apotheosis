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
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DeferredWorkQueue;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import shadows.apotheosis.ApotheosisObjects;
import shadows.apotheosis.ench.altar.RenderPrismaticAltar;
import shadows.apotheosis.ench.table.EnchantmentScreenExt;

@SuppressWarnings("deprecation")
public class EnchModuleClient {

	static BlockRayTraceResult res = BlockRayTraceResult.createMiss(Vec3d.ZERO, Direction.NORTH, BlockPos.ZERO);

	@SubscribeEvent
	public void tooltips(ItemTooltipEvent e) {
		Item i = e.getItemStack().getItem();
		if (i == Items.COBWEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.cobweb").applyTextStyle(TextFormatting.GRAY));
		else if (i == ApotheosisObjects.PRISMATIC_WEB) e.getToolTip().add(new TranslationTextComponent("info.apotheosis.prismatic_cobweb").applyTextStyle(TextFormatting.GRAY));
		else if (i instanceof BlockItem) {
			Block block = ((BlockItem) i).getBlock();
			World world = Minecraft.getInstance().world;
			BlockItemUseContext ctx = new BlockItemUseContext(world, Minecraft.getInstance().player, Hand.MAIN_HAND, e.getItemStack(), res) {
			};
			BlockState state = block.getStateForPlacement(ctx);
			float power = block.getEnchantPowerBonus(state, world, BlockPos.ZERO);
			if (power > 0) {
				e.getToolTip().add(new TranslationTextComponent("info.apotheosis.ench_power", String.valueOf(power).substring(0, 3)).applyTextStyle(TextFormatting.GRAY));
			}
		}
	}

	public static void init() {
		DeferredWorkQueue.runLater(() -> {
			ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ALTAR_TYPE, RenderPrismaticAltar::new);
			ClientRegistry.bindTileEntityRenderer(ApotheosisObjects.ENCHANTING_TABLE, EnchantmentTableTileEntityRenderer::new);
			ScreenManager.registerFactory(ApotheosisObjects.ENCHANTING, EnchantmentScreenExt::new);
		});
	}
}
