package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.scoreboard.Objective;

public class ScriptObjectiveApi {

    private final Objective objective;

    public ScriptObjectiveApi(Objective objective) {
        this.objective = objective;
    }

    public void setDisplayName(String name){
        objective.setDisplayName(name);
    }

    public void set(String score, int value){
        objective.getScore(score).setValue(value);
    }

    public void add(String score, int amount){
        objective.getScore(score).addValue(amount);
    }

    public int get(String score){
        return objective.getScore(score).getValue();
    }

    public String getObjectiveId(){
        return objective.getId();
    }
}
