package jk_5.nailed.server.map.game.script.api;

import jk_5.nailed.api.GameMode;
import jk_5.nailed.api.chat.BaseComponent;
import jk_5.nailed.api.chat.TextComponent;
import jk_5.nailed.server.player.NailedPlayer;
import jk_5.nailed.server.world.NailedWorld;

public class ScriptPlayerApi {

    private final NailedPlayer player;

    public ScriptPlayerApi(NailedPlayer player) {
        this.player = player;
    }

    public void sendChat(String msg){
        player.sendMessage(new TextComponent(msg));
    }

    public void sendChat(BaseComponent ... comp){
        player.sendMessage(comp);
    }

    public void displaySubtitle(String msg){
        player.displaySubtitle(new TextComponent(msg));
    }

    public void displaySubtitle(BaseComponent ... comp){
        player.displaySubtitle(comp);
    }

    public void setSubtitle(String msg){
        player.setSubtitle(new TextComponent(msg));
    }

    public void setSubtitle(BaseComponent ... comp){
        player.setSubtitle(comp);
    }

    public void clearSubtitle(){
        player.clearSubtitle();
    }

    public void clearInventory(){
        player.clearInventory();
    }

    public void heal(double amount) {
        player.heal(amount);
    }

    public double getMaxHealth() {
        return player.getMaxHealth();
    }

    public void resetMaxHealth() {
        player.resetMaxHealth();
    }

    public void setMaxHealth(double maxHealth) {
        player.setMaxHealth(maxHealth);
    }

    public void setBurnDuration(int ticks) {
        player.setBurnDuration(ticks);
    }

    public int getBurnDuration() {
        return player.getBurnDuration();
    }

    public boolean isBurning() {
        return player.isBurning();
    }

    public double getExperience() {
        return player.getExperience();
    }

    public void setExperience(double experience) {
        player.setExperience(experience);
    }

    public int getLevel() {
        return player.getLevel();
    }

    public void setLevel(int level) {
        player.setLevel(level);
    }

    public void damage(double amount) {
        player.damage(amount);
    }

    public void setHealth(double health) {
        player.setHealth(health);
    }

    public double getHealth() {
        return player.getHealth();
    }

    public void setHunger(double hunger) {
        player.setHunger(hunger);
    }

    public double getHunger() {
        return player.getHunger();
    }

    public ScriptWorldApi getWorld() {
        return new ScriptWorldApi((NailedWorld) player.getWorld());
    }

    public GameMode getGameMode() {
        return player.getGameMode();
    }

    public void setGameMode(GameMode gm) {
        player.setGameMode(gm);
    }

    public void setAllowedToFly(boolean allowed) {
        player.setAllowedToFly(allowed);
    }

    public void clearTitle() {
        player.clearTitle();
    }

    public String getName() {
        return player.getName();
    }
}
