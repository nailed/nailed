package jk_5.nailed.server.map.gamerule;

import com.google.common.collect.MapMaker;
import jk_5.nailed.api.gamerule.DefaultGameRuleKey;
import jk_5.nailed.api.gamerule.EditableGameRules;
import jk_5.nailed.api.gamerule.GameRuleKey;
import jk_5.nailed.api.gamerule.GameRules;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Map;
import java.util.Set;

/**
 * No description given
 *
 * @author jk-5
 */
public class DelegatingEditableGameRules extends DelegatingGameRules {

    private static final Logger logger = LogManager.getLogger();
    private static final Map<GameRules, net.minecraft.world.GameRules> cache = new MapMaker().weakKeys().makeMap();

    private final EditableGameRules wrapped;

    public DelegatingEditableGameRules(EditableGameRules wrapped) {
        //noinspection unchecked
        super((GameRules) wrapped);
        this.wrapped = wrapped;
    }

    @Override
    public void readGameRulesFromNBT(NBTTagCompound nbt) {
        //noinspection unchecked
        Set<String> keys = (Set<String>) nbt.getKeySet();
        for(String key : keys){
            this.setOrCreateGameRule(key, nbt.getString(key));
        }
    }

    @Override
    public void addGameRule(String key, String value, ValueType type) {
        logger.warn("Tried to create gamerule {} (value: {}, type: {}). Ignoring", key, value, type);
    }

    @Override
    public void setOrCreateGameRule(String key, String value) {
        GameRuleKey<?> ruleKey = DefaultGameRuleKey.getByName(key);
        if(ruleKey == null){
            logger.warn("Tried to set gamerule {} to {} but it does not exist. Ignoring", key, value);
            return;
        }
        if(ruleKey.getType() == GameRuleKey.Type.BOOL){
            //noinspection unchecked
            wrapped.get((GameRuleKey<Boolean>) ruleKey).setValue(value.equals("true"));
        }else if(ruleKey.getType() == GameRuleKey.Type.INTEGER){
            //noinspection unchecked
            wrapped.get((GameRuleKey<Integer>) ruleKey).setValue(Integer.parseInt(value));
        }
    }

    public static net.minecraft.world.GameRules get(GameRules original){
        if(!cache.containsKey(original)){
            DelegatingGameRules v;
            if(original instanceof EditableGameRules){
                v = new DelegatingEditableGameRules((EditableGameRules) original);
            }else{
                //noinspection unchecked
                v = new DelegatingGameRules(original);
            }
            cache.put(original, v);
            return v;
        }else{
            return cache.get(original);
        }
    }

    @Override
    public String toString() {
        return "DelegatingEditableGameRules{" +
                "wrapped=" + wrapped +
                '}';
    }
}
