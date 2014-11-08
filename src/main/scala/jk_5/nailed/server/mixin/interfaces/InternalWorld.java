package jk_5.nailed.server.mixin.interfaces;

import jk_5.nailed.api.world.World;
import jk_5.nailed.api.world.WorldContext;

public interface InternalWorld extends World {

    void setContext(WorldContext context);
}
