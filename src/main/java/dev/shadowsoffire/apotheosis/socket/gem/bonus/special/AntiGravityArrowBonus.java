package dev.shadowsoffire.apotheosis.socket.gem.bonus.special;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import dev.shadowsoffire.apotheosis.Apoth.Attachments;
import dev.shadowsoffire.apotheosis.socket.gem.GemClass;
import dev.shadowsoffire.apotheosis.socket.gem.GemInstance;
import dev.shadowsoffire.apotheosis.socket.gem.GemView;
import dev.shadowsoffire.apotheosis.socket.gem.Purity;
import dev.shadowsoffire.apotheosis.socket.gem.bonus.GemBonus;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.neoforged.neoforge.common.util.AttributeTooltipContext;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

/**
 * Gem bonus that removes gravity from arrows fired by the socketed weapon.
 * <p>
 * This bonus has no per-purity strength; it is either active or inactive.
 * It is active for all purities at or above {@link #minPurity}.
 * <p>
 * Fired arrows are marked with {@link Attachments#ANTI_GRAVITY_ARROW_START}. Gravity is restored when the arrow hits something,
 * or by {@link #tick(AbstractArrow)} once the arrow is spent, so arrows can land and despawn normally.
 */
public class AntiGravityArrowBonus extends GemBonus {

    public static final Codec<AntiGravityArrowBonus> CODEC = RecordCodecBuilder.create(inst -> inst
        .group(
            gemClass(),
            Purity.CODEC.optionalFieldOf("min_purity", Purity.CRACKED).forGetter(a -> a.minPurity))
        .apply(inst, AntiGravityArrowBonus::new));

    protected final Purity minPurity;

    public AntiGravityArrowBonus(GemClass gemClass, Purity minPurity) {
        super(gemClass);
        this.minPurity = minPurity;
    }

    /**
     * The maximum number of ticks an arrow may fly without gravity before it is restored.
     */
    public static final int MAX_FLIGHT_TICKS = 600;

    /**
     * Squared speed (in blocks per tick) below which an arrow is considered spent, and has its gravity restored.
     */
    public static final double SPENT_SPEED_SQ = 0.01 * 0.01;

    @Override
    public void onProjectileFired(GemInstance inst, LivingEntity user, Projectile proj) {
        if (proj instanceof AbstractArrow arrow) {
            arrow.setNoGravity(true);
            arrow.setData(Attachments.ANTI_GRAVITY_ARROW_START, user.level().getGameTime());
        }
    }

    /**
     * Restores gravity on impact, so that arrows which bounce or deflect after hitting something (such as tridents) fall and land nearby
     * instead of drifting away in a straight line.
     * <p>
     * The impact event fires before the projectile's own hit handling, so gravity is already restored when the bounce is applied.
     */
    @Override
    public void onProjectileImpact(GemInstance inst, Projectile proj, HitResult res) {
        if (proj instanceof AbstractArrow arrow) {
            restoreGravity(arrow);
        }
    }

    /**
     * Restores gravity to a marked arrow once it has slowed to near-zero speed or has flown for {@link #MAX_FLIGHT_TICKS}.
     * <p>
     * Without this, an arrow with no gravity that never hits anything would hover in the air indefinitely, since arrows only despawn while in the ground.
     * <p>
     * Called from {@link EntityTickEvent.Post} on the server for arrows that have the {@link Attachments#ANTI_GRAVITY_ARROW_START} attachment.
     */
    public static void tick(AbstractArrow arrow) {
        if (!arrow.isNoGravity()) {
            arrow.removeData(Attachments.ANTI_GRAVITY_ARROW_START);
            return;
        }

        long start = arrow.getData(Attachments.ANTI_GRAVITY_ARROW_START);
        boolean spent = arrow.getDeltaMovement().lengthSqr() < SPENT_SPEED_SQ;
        boolean expired = arrow.level().getGameTime() - start >= MAX_FLIGHT_TICKS;
        if (spent || expired) {
            restoreGravity(arrow);
        }
    }

    /**
     * Re-enables gravity on an arrow and clears the {@link Attachments#ANTI_GRAVITY_ARROW_START} marker.
     */
    public static void restoreGravity(AbstractArrow arrow) {
        arrow.setNoGravity(false);
        arrow.removeData(Attachments.ANTI_GRAVITY_ARROW_START);
    }

    @Override
    public Codec<? extends GemBonus> getCodec() {
        return CODEC;
    }

    @Override
    public Component getSocketBonusTooltip(GemView gem, AttributeTooltipContext ctx) {
        return Component.translatable("bonus." + this.getTypeKey() + ".desc").withStyle(ChatFormatting.YELLOW);
    }

    @Override
    public boolean supports(Purity purity) {
        return purity.isAtLeast(this.minPurity);
    }

    public Purity getMinPurity() {
        return this.minPurity;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder extends GemBonus.Builder {

        private Purity minPurity = Purity.CRACKED;

        public Builder minPurity(Purity purity) {
            this.minPurity = purity;
            return this;
        }

        @Override
        public AntiGravityArrowBonus build(GemClass gClass) {
            return new AntiGravityArrowBonus(gClass, this.minPurity);
        }

    }

}
