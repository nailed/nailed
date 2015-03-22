package jk_5.nailed.server.mappack.metadata.xml;

import jk_5.nailed.api.gamerule.*;
import jk_5.nailed.api.mappack.MappackConfigurationException;
import org.jdom2.Element;

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
