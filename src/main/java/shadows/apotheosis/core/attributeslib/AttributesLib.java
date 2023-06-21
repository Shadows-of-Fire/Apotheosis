package shadows.apotheosis.core.attributeslib;

import java.util.function.BiConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.registries.RegistryObject;
import shadows.apotheosis.core.attributeslib.api.ALAttributes;
import shadows.apotheosis.core.attributeslib.client.AttributesLibClient;
import shadows.apotheosis.core.attributeslib.impl.AttributeEvents;
import shadows.placebo.util.RegistryEvent.Register;

public class AttributesLib {

	public static final String MODID = "attributeslib";

	public AttributesLib() {
		MinecraftForge.EVENT_BUS.register(new AttributeEvents());
		if (FMLEnvironment.dist.isClient()) {
			MinecraftForge.EVENT_BUS.register(new AttributesLibClient());
		}
	}

	@SubscribeEvent
	public void attribs(Register<Attribute> e) {
		//Formatter::off
		e.getRegistry().registerAll(
			new RangedAttribute("apotheosis:draw_speed", 1.0D, 1.0D, 4.0D).setSyncable(true), "draw_speed",
			new RangedAttribute("apotheosis:crit_chance", 1.0D, 1.0D, 1024.0D).setSyncable(true), "crit_chance",
			new RangedAttribute("apotheosis:crit_damage", 1.5D, 1.0D, 1024.0D).setSyncable(true), "crit_damage",
			new RangedAttribute("apotheosis:cold_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true), "cold_damage",
			new RangedAttribute("apotheosis:fire_damage", 0.0D, 0.0D, 1024.0D).setSyncable(true), "fire_damage",
			new RangedAttribute("apotheosis:life_steal", 1.0D, 1.0D, 1024.0D).setSyncable(true), "life_steal",
			new RangedAttribute("apotheosis:piercing", 1.0D, 1.0D, 2.0D).setSyncable(true), "piercing",
			new RangedAttribute("apotheosis:current_hp_damage", 1.0D, 1.0D, 2.0D).setSyncable(true), "current_hp_damage",
			new RangedAttribute("apotheosis:overheal", 1.0D, 0.0D, 1024.0D).setSyncable(true), "overheal",
			new RangedAttribute("apotheosis:ghost_health", 0.0D, 0.0D, 1024.0D).setSyncable(true), "ghost_health",
			new RangedAttribute("apotheosis:mining_speed", 1.0D, 0.0D, 1024.0D).setSyncable(true), "mining_speed",
			new RangedAttribute("apotheosis:arrow_damage", 1.0D, 0.0D, 1024.0D).setSyncable(true), "arrow_damage",
			new RangedAttribute("apotheosis:arrow_velocity", 1.0D, 0.0D, 1024.0D).setSyncable(true), "arrow_velocity",
			new RangedAttribute("apotheosis:experience_gained", 1.0D, 0.0D, 1024.0D).setSyncable(true), "experience_gained",
			new RangedAttribute("apotheosis:healing_received", 1.0D, 0.0D, 1024.0D).setSyncable(true), "healing_received"
		);
		//Formatter::on
	}

	@SubscribeEvent
	public void applyAttribs(EntityAttributeModificationEvent e) {
		e.getTypes().forEach(type -> {
			//Formatter::off
			addAll(type, e::add,
					ALAttributes.DRAW_SPEED,
					ALAttributes.CRIT_CHANCE,
					ALAttributes.CRIT_DAMAGE,
					ALAttributes.COLD_DAMAGE,
					ALAttributes.FIRE_DAMAGE,
					ALAttributes.LIFE_STEAL,
					ALAttributes.PIERCING,
					ALAttributes.CURRENT_HP_DAMAGE,
					ALAttributes.OVERHEAL,
					ALAttributes.GHOST_HEALTH,
					ALAttributes.MINING_SPEED,
					ALAttributes.ARROW_DAMAGE,
					ALAttributes.ARROW_VELOCITY,
					ALAttributes.EXPERIENCE_GAINED,
					ALAttributes.HEALING_RECEIVED);
			//Formatter::on
		});
	}

	@SafeVarargs
	private static void addAll(EntityType<? extends LivingEntity> type, BiConsumer<EntityType<? extends LivingEntity>, Attribute> add, RegistryObject<Attribute>... attribs) {
		for (RegistryObject<Attribute> a : attribs)
			add.accept(type, a.get());
	}

	public static TooltipFlag getTooltipFlag() {
		if (FMLEnvironment.dist.isClient()) return ClientAccess.getTooltipFlag();
		return TooltipFlag.Default.NORMAL;
	}

	static class ClientAccess {
		static TooltipFlag getTooltipFlag() {
			return Minecraft.getInstance().options.advancedItemTooltips ? TooltipFlag.Default.ADVANCED : TooltipFlag.Default.NORMAL;
		}
	}
}
