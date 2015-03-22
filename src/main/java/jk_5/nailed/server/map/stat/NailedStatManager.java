package jk_5.nailed.server.map.stat;

import com.google.common.collect.ImmutableList;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.map.stat.*;
import jk_5.nailed.api.mappack.metadata.StatConfig;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;

public class NailedStatManager implements StatManager {

    private final Map map;
    private final List<Stat> stats;

    public NailedStatManager(Map map) {
        this.map = map;
        if(map.mappack() != null){
            ImmutableList.Builder<Stat> builder = ImmutableList.builder();
            for (StatConfig config : map.mappack().getMetadata().stats()) {
                if(config.track() == null || config.track().isEmpty()){
                    builder.add(new ModifiableStat(config.name()));
                }else{
                    builder.add(new SubscribedStat(config.name(), config.track(), config.attributes()));
                }
            }
            stats = builder.build();
        }else{
            this.stats = Collections.emptyList();
        }
    }

    public void fireEvent(StatEvent event){
        for (Stat stat : stats) {
            if(stat instanceof SubscribedStat){
                SubscribedStat s = (SubscribedStat) stat;
                if(s.getTrack().equals(event.getName())){
                    boolean matches = true;
                    for (java.util.Map.Entry<String, String> entry : event.getAttributes().entrySet()) {
                        if(!s.getAttributes().get(entry.getKey()).equals(entry.getValue())){
                            matches = false;
                            break;
                        }
                    }
                    if(!matches){ //TODO: this check was missing. Is it really needed?
                        s.onEvent(event);
                    }
                }
            }
        }
    }

    @Nullable
    public Stat getStat(@Nonnull String name){
        for (Stat stat : this.stats) {
            if(stat.getName().equals(name)){
                return stat;
            }
        }
        return null;
    }
}
