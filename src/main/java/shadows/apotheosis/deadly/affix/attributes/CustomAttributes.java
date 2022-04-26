package shadows.apotheosis.deadly.affix.attributes;

import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.RegistryEvent.Register;

import static shadows.apotheosis.Apoth.Attributes.*;

public class CustomAttributes {

    public static void register(Register<Attribute> e) {
        e.getRegistry().registerAll(
                SNIPE_DAMAGE,
                DRAW_SPEED,
                CRIT_CHANCE,
                CRIT_DAMAGE,
                COLD_DAMAGE,
                FIRE_DAMAGE,
                LIFE_STEAL,
                PIERCING,
                CURRENT_HP_DAMAGE,
                OVERHEAL
        );
    }
}