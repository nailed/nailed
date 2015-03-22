package jk_5.nailed.server.map.gamerule;

import jk_5.nailed.api.gamerule.*;

import javax.annotation.Nonnull;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * No description given
 *
 * @author jk-5
 */
public class WrappedEditableGameRules implements EditableGameRules {

    private final Map<GameRuleKey<?>, EditableGameRule<?>> gameRuleMap = new HashMap<GameRuleKey<?>, EditableGameRule<?>>();

    public WrappedEditableGameRules(GameRules wrapped) {
        //noinspection unchecked
        for(GameRule<Object> rule : (Collection<GameRule<Object>>) wrapped.list()){
            EditableGameRule<Object> newRule = new DefaultEditableGameRule<Object>(rule.getKey());
            newRule.setValue(rule.getValue());
            gameRuleMap.put(rule.getKey(), newRule);
        }
    }

    @Nonnull
    @Override
    public <T> EditableGameRule<T> get(@Nonnull GameRuleKey<T> key) {
        //noinspection unchecked
        EditableGameRule<T> rule = (EditableGameRule<T>) gameRuleMap.get(key);
        if(rule == null){
            rule = new DefaultEditableGameRule<T>(key);
        }
        return rule;
    }

    @Nonnull
    @Override
    public Collection<EditableGameRule<?>> list() {
        return Collections.unmodifiableCollection(gameRuleMap.values());
    }

    @Override
    public String toString() {
        return "WrappedEditableGameRules{" +
                "gameRuleMap=" + gameRuleMap +
                '}';
    }
}
