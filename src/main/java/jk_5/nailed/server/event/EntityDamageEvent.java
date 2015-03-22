package jk_5.nailed.server.event;

import jk_5.eventbus.Event;
import jk_5.nailed.api.event.PlatformEvent;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.DamageSource;

@Event.Cancelable
public class EntityDamageEvent extends PlatformEvent {

    private final EntityLivingBase entity;
    private final DamageSource source;
    private float amount;

    public EntityDamageEvent(EntityLivingBase entity, DamageSource source, float amount) {
        this.entity = entity;
        this.source = source;
        this.amount = amount;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public DamageSource getSource() {
        return source;
    }

    public float getAmount() {
        return amount;
    }

    public void setAmount(float amount) {
        this.amount = amount;
    }
}
