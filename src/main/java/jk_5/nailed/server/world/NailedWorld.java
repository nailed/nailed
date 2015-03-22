package jk_5.nailed.server.world;

import com.google.common.base.MoreObjects;
import com.google.common.collect.ImmutableSet;
import jk_5.nailed.api.gamerule.DefaultGameRules;
import jk_5.nailed.api.gamerule.EditableGameRules;
import jk_5.nailed.api.map.Map;
import jk_5.nailed.api.mappack.metadata.MappackWorld;
import jk_5.nailed.api.player.Player;
import jk_5.nailed.api.world.*;
import jk_5.nailed.server.map.gamerule.WrappedEditableGameRules;
import jk_5.nailed.server.player.NailedPlayer;
import net.minecraft.network.play.server.S41PacketServerDifficulty;
import net.minecraft.world.EnumDifficulty;
import net.minecraft.world.WorldServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class NailedWorld implements World {

    private static final Logger logger = LogManager.getLogger();

    private final Set<Player> players = new HashSet<Player>();
    private final WorldContext context;
    private final WorldProvider worldProvider;
    private final EditableGameRules gameRules;

    private WorldServer wrapped;
    private Map map;

    public NailedWorld(WorldServer wrapped, WorldContext context) {
        this.wrapped = wrapped;
        this.context = context;

        if(wrapped.provider instanceof DelegatingWorldProvider){
            this.worldProvider = ((DelegatingWorldProvider) wrapped.provider).getWrapped();
        }else{
            this.worldProvider = null;
        }

        MappackWorld config = this.getConfig();
        if(config != null){
            this.setDifficulty(config.difficulty());
        }

        if(this.context != null && this.context.getConfig() != null){
            this.gameRules = new WrappedEditableGameRules(context.getConfig().gameRules());
        }else{
            this.gameRules = new WrappedEditableGameRules(DefaultGameRules.INSTANCE);
        }
    }

    @Override
    public EditableGameRules getGameRules() {
        return this.gameRules;
    }

    @Override
    public int getDimensionId() {
        return wrapped.provider.getDimensionId();
    }

    @Override
    public String getName() {
        return "world_" + getDimensionId();
    }

    @Override
    public Collection<Player> getPlayers() {
        return ImmutableSet.copyOf(players);
    }

    @Override
    public Dimension getDimension() {
        if(this.worldProvider != null){
            return this.worldProvider.getDimension();
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
        }
        return this.context.getConfig();
    }

    @Override
    public void onPlayerJoined(Player player) {
        logger.info("Player " + player.toString() + " joined world " + this.toString());
        players.add(player);
        ((NailedPlayer) player).sendPacket(new S41PacketServerDifficulty(wrapped.getDifficulty(), false));
        if(this.getConfig().resourcePackUrl() != null){
            player.loadResourcePack(this.getConfig().resourcePackUrl(), ""); //TODO: fix hash
        }
    }

    @Override
    public void onPlayerLeft(Player player) {
        logger.info("Player " + player.toString() + " left world " + this.toString());
        players.remove(player);
    }

    @Override
    public int getTime() {
        return (int) wrapped.getWorldTime();
    }

    @Override
    public void setTime(int time) {
        this.wrapped.setWorldTime(time);
    }

    @Override
    public WeatherType getWeather() {
        boolean rain = this.wrapped.isRaining();
        boolean thunder = this.wrapped.isThundering();

        if(!rain && !thunder){
            return WeatherType.DRY;
        }else if(!thunder){
            return WeatherType.RAIN;
        }else{
            return WeatherType.THUNDER;
        }
    }

    @Override
    public void setWeather(WeatherType weather) {
        if(weather == WeatherType.DRY){
            this.wrapped.getWorldInfo().setRaining(false);
            this.wrapped.getWorldInfo().setRainTime(0);
            this.wrapped.getWorldInfo().setThundering(false);
            this.wrapped.getWorldInfo().setThunderTime(0);
        }else if(weather == WeatherType.RAIN){
            this.wrapped.getWorldInfo().setRaining(true);
            this.wrapped.getWorldInfo().setThundering(false);
            this.wrapped.getWorldInfo().setThunderTime(0);
        }else if(weather == WeatherType.THUNDER){
            this.wrapped.getWorldInfo().setRaining(true);
            this.wrapped.getWorldInfo().setThundering(true);
        }
    }

    @Override
    public Difficulty getDifficulty() {
        return Difficulty.byId(wrapped.getDifficulty().getDifficultyId());
    }

    @Override
    public void setDifficulty(Difficulty difficulty) {
        EnumDifficulty diff = EnumDifficulty.getDifficultyEnum(difficulty.getId());
        this.wrapped.getWorldInfo().setDifficulty(diff);
        for (Player player : this.players) {
            ((NailedPlayer) player).sendPacket(new S41PacketServerDifficulty(diff, false));
        }
        if(difficulty == Difficulty.PEACEFUL){
            wrapped.setAllowedSpawnTypes(false, true);
        }else{
            wrapped.setAllowedSpawnTypes(true, true);
        }
    }

    public WorldServer getWrapped() {
        return wrapped;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("id", getDimensionId())
                .add("name", getName())
                .add("dimension", getDimension())
                .add("gameRules", gameRules)
                .toString();
    }
}
