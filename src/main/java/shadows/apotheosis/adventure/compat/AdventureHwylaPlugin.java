package shadows.apotheosis.adventure.compat;

import com.google.common.base.Predicates;

import mcp.mobius.waila.api.EntityAccessor;
import mcp.mobius.waila.api.IEntityComponentProvider;
import mcp.mobius.waila.api.IServerDataProvider;
import mcp.mobius.waila.api.ITooltip;
import mcp.mobius.waila.api.IWailaClientRegistration;
import mcp.mobius.waila.api.IWailaCommonRegistration;
import mcp.mobius.waila.api.IWailaPlugin;
import mcp.mobius.waila.api.TooltipPosition;
import mcp.mobius.waila.api.WailaPlugin;
import mcp.mobius.waila.api.config.IPluginConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import shadows.apotheosis.util.CommonTooltipUtil;

@WailaPlugin
public class AdventureHwylaPlugin implements IWailaPlugin, IEntityComponentProvider, IServerDataProvider<Entity> {

	@Override
	public void register(IWailaCommonRegistration reg) {
		reg.registerEntityDataProvider(this, LivingEntity.class);
	}

	@Override
	public void registerClient(IWailaClientRegistration reg) {
		reg.registerComponentProvider(this, TooltipPosition.BODY, Entity.class);
	}

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor accessor, IPluginConfig config) {
		if (accessor.getEntity() instanceof LivingEntity living && accessor.getServerData().getBoolean("apoth.boss")) {
			ListTag bossAttribs = accessor.getServerData().getList("apoth.modifiers", Tag.TAG_COMPOUND);
			AttributeMap map = living.getAttributes();
			for (Tag t : bossAttribs) {
				CompoundTag tag = (CompoundTag) t;
				Attribute attrib = ForgeRegistries.ATTRIBUTES.getValue(new ResourceLocation(tag.getString("Name")));
				map.getInstance(attrib).load(tag);
			}
			accessor.getServerData().remove("apoth.modifiers");
			living.getPersistentData().merge(accessor.getServerData());
			CommonTooltipUtil.appendBossData(living.level, living, tooltip::add);
		}
	}

	@Override
	public void appendServerData(CompoundTag tag, ServerPlayer player, Level world, Entity entity, boolean something) {
		if (entity instanceof LivingEntity living && living.getPersistentData().getBoolean("apoth.boss")) {
			tag.putBoolean("apoth.boss", true);
			tag.putString("apoth.rarity", living.getPersistentData().getString("apoth.rarity"));
			AttributeMap map = living.getAttributes();
			ListTag bossAttribs = new ListTag();
			ForgeRegistries.ATTRIBUTES.getValues().stream().map(map::getInstance).filter(Predicates.notNull()).forEach(inst -> {
				for (AttributeModifier modif : inst.getModifiers()) {
					if (modif.getName().startsWith("placebo_random_modifier_")) {
						bossAttribs.add(inst.save());
						continue;
					}
				}
			});
			tag.put("apoth.modifiers", bossAttribs);
		}
	}

}