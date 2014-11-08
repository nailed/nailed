package jk_5.nailed.server.mappack.metadata.xml;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.annotation.Nonnull;

import org.jdom2.Element;

import jk_5.nailed.api.gamerule.DefaultGameRule;
import jk_5.nailed.api.gamerule.DefaultGameRuleKey;
import jk_5.nailed.api.gamerule.DefaultGameRules;
import jk_5.nailed.api.gamerule.GameRule;
import jk_5.nailed.api.gamerule.GameRuleKey;
import jk_5.nailed.api.gamerule.GameRules;
import jk_5.nailed.api.mappack.MappackConfigurationException;

/**
 * No description given
 *
 * @author jk-5
 */
public class ImmutableXmlGameRules implements GameRules<GameRule<?>> {

    private final Map<GameRuleKey<?>, GameRule<?>> gameRuleMap = new HashMap<GameRuleKey<?>, GameRule<?>>();

    public ImmutableXmlGameRules(Element e) throws MappackConfigurationException {
        this(e, DefaultGameRules.INSTANCE);
    }

    public ImmutableXmlGameRules(Element e, GameRules<GameRule<?>> parent) throws MappackConfigurationException {
        for(GameRule<?> r : parent.list()){
            //noinspection unchecked
            gameRuleMap.put(r.getKey(), r);
        }
        for(Element element : e.getChildren()){
            GameRuleKey key = DefaultGameRuleKey.getByName(element.getName());
            gameRuleMap.put(key, new DefaultGameRule<Object>(key, element.getText()));
        }
    }

    @Nonnull
    @Override
    public <T> GameRule<T> get(@Nonnull GameRuleKey<T> key) {
        //noinspection unchecked
        return (GameRule<T>) gameRuleMap.get(key);
    }

    @Nonnull
    @Override
    public Collection<GameRule<?>> list() {
        return Collections.unmodifiableCollection(gameRuleMap.values());
    }
}
