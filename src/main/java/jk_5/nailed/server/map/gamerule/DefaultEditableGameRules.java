package jk_5.nailed.server.map.gamerule;

import jk_5.nailed.api.gamerule.DefaultGameRuleKey;
import jk_5.nailed.api.gamerule.EditableGameRule;
import jk_5.nailed.api.gamerule.EditableGameRules;
import jk_5.nailed.api.gamerule.GameRuleKey;

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
public class DefaultEditableGameRules implements EditableGameRules {

    private final Map<GameRuleKey<?>, EditableGameRule<?>> gameRuleMap = new HashMap<GameRuleKey<?>, EditableGameRule<?>>();

    public DefaultEditableGameRules() {
        for(GameRuleKey<?> key : DefaultGameRuleKey.list()){
            //noinspection unchecked
            gameRuleMap.put(key, new DefaultEditableGameRule(key));
        }
    }

    @Nonnull
    @Override
    public <T> EditableGameRule<T> get(@Nonnull GameRuleKey<T> key) {
        //noinspection unchecked
        return (EditableGameRule<T>) gameRuleMap.get(key);
    }

    @Nonnull
    @Override
    public Collection<EditableGameRule<?>> list() {
        return Collections.unmodifiableCollection(gameRuleMap.values());
    }

    @Override
    public String toString() {
        return "DefaultEditableGameRules{" +
                "gameRuleMap=" + gameRuleMap +
                '}';
    }
}
