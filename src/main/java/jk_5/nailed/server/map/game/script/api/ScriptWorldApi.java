package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.WeatherType;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.world.NailedWorld;

import java.util.ArrayList;
import java.util.List;

public class ScriptWorldApi {

    private final NailedWorld world;

    public ScriptWorldApi(NailedWorld world) {
        this.world = world;
    }

    public ScriptPlayerApi[] getPlayers(){
        List<ScriptPlayerApi> builder = new ArrayList<ScriptPlayerApi>();
        for (Player player : world.getPlayers()) {
            builder.add(new ScriptPlayerApi((NailedPlayer) player));
        }
        return builder.toArray(new ScriptPlayerApi[builder.size()]);
    }

    public void setTime(int time){
        world.setTime(time);
    }

    public void setWeather(WeatherType weather){
        world.setWeather(weather);
    }

    public void setDifficulty(Difficulty difficulty){
        world.setDifficulty(difficulty);
    }
}
