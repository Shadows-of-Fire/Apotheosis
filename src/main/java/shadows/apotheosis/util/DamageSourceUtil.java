package shadows.apotheosis.util;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;

public class DamageSourceUtil {

    public static EntityDamageSource copy(EntityDamageSource other) {
        EntityDamageSource nSrc = other instanceof IndirectEntityDamageSource ind ? new IndirectEntityDamageSource(ind.getMsgId(), ind.getDirectEntity(), ind.getEntity()) : new EntityDamageSource(other.getMsgId(), other.getEntity());
        ((DmgSrcCopy) nSrc).copyFrom(other);
        return nSrc;
    }

    /**
     * Be careful with this, there are identity comparisons to certain vanilla constant sources.
     *
     * @param other
     * @return
     */
    public static DamageSource copy(DamageSource other) {
        if (other instanceof EntityDamageSource eSrc) return copy(eSrc);
        DamageSource nSrc = new DamageSource(other.getMsgId());
        ((DmgSrcCopy) nSrc).copyFrom(other);
        return nSrc;
    }

    public static interface DmgSrcCopy {
        public void copyFrom(DamageSource other);
    }

}
