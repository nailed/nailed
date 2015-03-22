package jk_5.nailed.server.map.gamerule;

import jk_5.nailed.api.gamerule.EditableGameRule;
import jk_5.nailed.api.gamerule.GameRuleKey;
import jk_5.nailed.api.util.Checks;

import javax.annotation.Nonnull;

/**
 * No description given
 *
 * @author jk-5
 */
public class DefaultEditableGameRule<T> implements EditableGameRule<T> {

    private final GameRuleKey<T> key;
    private T value;

    public DefaultEditableGameRule(@Nonnull GameRuleKey<T> key) {
        Checks.notNull(key, "key");
        this.key = key;
        this.value = key.getDefaultValue();
    }

    @Override
    public void setValue(@Nonnull T value) {
        this.value = value;
    }

    @Nonnull
    @Override
    public GameRuleKey<T> getKey() {
        return this.key;
    }

    @Nonnull
    @Override
    public T getValue() {
        if(this.key.getType() == GameRuleKey.Type.BOOL && this.value instanceof String){
            //noinspection unchecked
            return (T) Boolean.valueOf((String) this.value);
        }
        return this.value;
    }

    @Override
    public String toString() {
        return "DefaultEditableGameRule{" +
                "key=" + key +
                ", value=" + value +
                '}';
    }
}
