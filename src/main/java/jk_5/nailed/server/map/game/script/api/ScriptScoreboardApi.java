package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.scoreboard.DisplayType;

public class ScriptScoreboardApi {

    private final ScriptMapApi scriptMapApi;
    private final Map map;

    public ScriptScoreboardApi(ScriptMapApi scriptMapApi, Map map) {
        this.scriptMapApi = scriptMapApi;
        this.map = map;
    }

    public ScriptObjectiveApi getObjective(String name){
        return new ScriptObjectiveApi(map.getScoreboardManager().getOrCreateObjective(name));
    }

    public void setDisplay(ScriptObjectiveApi objective, DisplayType display){
        map.getScoreboardManager().setDisplay(display, map.getScoreboardManager().getObjective(objective.getObjectiveId()));
    }
}
