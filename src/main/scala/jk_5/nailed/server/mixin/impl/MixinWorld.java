package jk_5.nailed.server.mixin.impl;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import net.minecraft.profiler.Profiler;
import net.minecraft.util.IThreadListener;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldProvider;
import net.minecraft.world.WorldServer;
import net.minecraft.world.storage.ISaveHandler;
import net.minecraft.world.storage.WorldInfo;

import jk_5.nailed.api.gamerule.DefaultGameRules;
import jk_5.nailed.api.gamerule.EditableGameRules;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.mappack.metadata.MappackWorld;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.world.Difficulty;
import jk_5.nailed.api.world.Dimension;
import jk_5.nailed.api.world.WeatherType;
import jk_5.nailed.api.world.World;
import jk_5.nailed.api.world.WorldContext;
import jk_5.nailed.server.map.gamerule.WrappedEditableGameRules;
import jk_5.nailed.server.mixin.interfaces.InternalWorld;
import jk_5.nailed.server.tweaker.mixin.Mixin;
import jk_5.nailed.server.world.DelegatingWorldProvider;

@Mixin(WorldServer.class)
public abstract class MixinWorld extends net.minecraft.world.World implements World, InternalWorld, IThreadListener {

    protected MixinWorld(ISaveHandler saveHandlerIn, WorldInfo info, WorldProvider providerIn, Profiler profilerIn, boolean client) {
        super(saveHandlerIn, info, providerIn, profilerIn, client);
    }

    protected Set<Player> players;
    private static final Logger mixinLogger = LogManager.getLogger();
    protected WorldContext context;
    protected Map map = null;
    protected jk_5.nailed.api.world.WorldProvider nailedProvider;
    protected EditableGameRules gameRules;

    @Override
    public void onPlayerJoined(Player player) {
        if(this.players == null) this.players = new HashSet<Player>();
        mixinLogger.info("Player {} joined world {}", player.getName(), this.toString());
        players.add(player);
    }

    @Override
    public void onPlayerLeft(Player player) {
        if(this.players == null) this.players = new HashSet<Player>();
        mixinLogger.info("Player {} left world {}", player.getName(), this.toString());
        players.remove(player);
    }

    @Override
    public Collection<Player> getPlayers() {
        if(this.players == null) this.players = new HashSet<Player>();
        return players;
    }

    @Override
    public int getDimensionId() {
        return provider.getDimensionId();
    }

    @Override
    public String getName() {
        return "world_" + provider.getDimensionId();
    }

    @Override
    public void setTime(int time) {
        this.setWorldTime(time);
    }

    @Override
    public int getTime() {
        return (int) this.getWorldTime();
    }

    @Override
    public WeatherType getWeather() {
        boolean rain = this.isRaining();
        boolean thunder = this.isThundering();

        if(!rain && !thunder) return WeatherType.DRY;
        else if(!thunder) return WeatherType.RAIN;
        else return WeatherType.THUNDER;
    }

    @Override
    public void setWeather(WeatherType weather) {
        WorldInfo info = getWorldInfo();
        if(weather == WeatherType.DRY){
            info.setRaining(false);
            info.setRainTime(0);
            info.setThundering(false);
            info.setThunderTime(0);
        }else if(weather == WeatherType.RAIN){
            info.setRaining(true);
            info.setThundering(false);
            info.setThunderTime(0);
        }else if(weather == WeatherType.THUNDER){
            info.setRaining(true);
            info.setThundering(true);
        }
    }

    @Override
    public Difficulty getDifficultyValue() {
        return Difficulty.byId(getDifficulty().getDifficultyId());
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        getWorldInfo().setDifficulty(EnumDifficulty.getDifficultyEnum(difficulty.getId()));
        if(difficulty == Difficulty.HARD || difficulty == Difficulty.NORMAL || difficulty == Difficulty.EASY){
            setAllowedSpawnTypes(true, true);
        }else if(difficulty == Difficulty.PEACEFUL){
            setAllowedSpawnTypes(false, true);
        }
    }

    @Override
    public Dimension getDimension() {
        if(this.nailedProvider != null){
            return this.nailedProvider.getDimension();
        }else{
            return Dimension.OVERWORLD;
        }
    }

    @Override
    public void setMap(Map map) {
        this.map = map;
    }

    @Override
    public Map getMap() {
        return this.map;
    }

    @Override
    public MappackWorld getConfig() {
        if(this.context == null){
            return null;
        }else{
            return this.context.getConfig();
        }
    }

    @Override
    public EditableGameRules getGamerules() {
        if(this.gameRules == null){
            this.gameRules = new WrappedEditableGameRules(DefaultGameRules.INSTANCE);
        }
        return gameRules;
    }

    @Override
    public void setContext(WorldContext context) {
        this.context = context;
        if(context.getConfig() != null){
            this.setDifficulty(context.getConfig().difficulty());
            this.gameRules = new WrappedEditableGameRules(context.getConfig().gameRules());
        }
        if(provider instanceof DelegatingWorldProvider){
            nailedProvider = ((DelegatingWorldProvider) provider).wrapped();
        }
    }

    @Override
    public String toString() {
        return "MixinWorld{id=" + provider.getDimensionId() + ",name=" + getName() + ",dimension=" + getDimension() + ",gameRules=" + getGamerules() + '}';
    }
}
