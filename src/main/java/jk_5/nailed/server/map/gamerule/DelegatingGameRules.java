package jk_5.nailed.server.map.gamerule;

import jk_5.nailed.api.gamerule.DefaultGameRuleKey;
import jk_5.nailed.api.gamerule.GameRule;
import jk_5.nailed.api.gamerule.GameRuleKey;
import jk_5.nailed.api.gamerule.GameRules;
import net.minecraft.nbt.NBTTagCompound;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * No description given
 *
 * @author jk-5
 */
public class DelegatingGameRules extends net.minecraft.world.GameRules {

    private static final Logger logger = LogManager.getLogger();
    private final GameRules<GameRule<?>> wrapped;
    private boolean allowCreation = true;

    public DelegatingGameRules(GameRules<GameRule<?>> wrapped) {
        super();
        this.allowCreation = false; //Creation will be done in the super constructor. Don't log messages when it happens
        this.wrapped = wrapped;
    }

    @Override
    public void addGameRule(String key, String value, ValueType type) {
        if(!allowCreation) logger.warn("Tried to add gamerule {} (value: {}, type: {}) to immutable gamerules object. Ignoring", key, value, type.name());
    }

    @Override
    public void setOrCreateGameRule(String key, String value) {
        logger.warn("Tried to add gamerule {} (value: {}) to immutable gamerules object. Ignoring", key, value);
    }

    @Override
    public String getGameRuleStringValue(String name) {
        return wrapped.get(DefaultGameRuleKey.getByName(name)).toString();
    }

    @Override
    public boolean getGameRuleBooleanValue(String name) {
        GameRuleKey<?> key = DefaultGameRuleKey.getByName(name);
        if(key == null){
            logger.warn("Could not find gamerule key for rule " + name);
            return false;
        }
        if(key.getType() == GameRuleKey.Type.BOOL){
            //noinspection unchecked
            return wrapped.get((GameRuleKey<Boolean>) key).getValue();
        }else{
            logger.warn("Requested boolean gamerule with name {} but it was of type {}", name, key.getType());
            return false;
        }
    }

    @Override
    public int getInt(String name) {
        GameRuleKey<?> key = DefaultGameRuleKey.getByName(name);
        if(key.getType() == GameRuleKey.Type.INTEGER){
            //noinspection unchecked
            return wrapped.get((GameRuleKey<Integer>) key).getValue();
        }else{
            logger.warn("Requested integer gamerule with name {} but it was of type {}", name, key.getType());
            return 0;
        }
    }

    @Override
    public NBTTagCompound writeGameRulesToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        for(GameRule<?> rule : wrapped.list()){
            nbt.setString(rule.getKey().getName(), rule.getValue().toString());
        }
        return nbt;
    }

    @Override
    public void readGameRulesFromNBT(NBTTagCompound nbt) {
        logger.warn("Tried to read immutable gamerules object from nbt. Ignoring");
    }

    @Override
    public String[] getRules() {
        List<String> ret = new ArrayList<String>(this.wrapped.list().size());
        for(GameRule<?> rule : this.wrapped.list()){
            ret.add(rule.getKey().getName());
        }
        return ret.toArray(new String[ret.size()]);
    }

    @Override
    public boolean hasRule(String name) {
        for(GameRule<?> rule : this.wrapped.list()){
            if(rule.getKey().getName().equalsIgnoreCase(name)){
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean areSameType(String key, ValueType type) {
        if(type == ValueType.ANY_VALUE) return true;
        GameRuleKey<?> ruleKey = null;
        for(GameRule<?> rule : this.wrapped.list()){
            if(rule.getKey().getName().equalsIgnoreCase(key)){
                ruleKey = rule.getKey();
            }
        }
        if(ruleKey == null) return false;
        return (ruleKey.getType() == GameRuleKey.Type.BOOL && type == ValueType.BOOLEAN_VALUE) || (ruleKey.getType() == GameRuleKey.Type.INTEGER && type == ValueType.NUMERICAL_VALUE);
    }

    @Override
    public String toString() {
        return "DelegatingGameRules{" +
                "wrapped=" + wrapped +
                '}';
    }
}
