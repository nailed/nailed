package jk_5.nailed.server.event;

import jk_5.eventbus.Event;
import net.minecraft.entity.EntityLivingBase;

@Event.Cancelable
public class EntityFallEvent extends Event {

    private final EntityLivingBase entity;
    private float distance;

    public EntityFallEvent(EntityLivingBase entity, float distance) {
        this.entity = entity;
        this.distance = distance;
    }

    public EntityLivingBase getEntity() {
        return entity;
    }

    public float getDistance() {
        return distance;
    }

    public void setDistance(float distance) {
        this.distance = distance;
    }
}
