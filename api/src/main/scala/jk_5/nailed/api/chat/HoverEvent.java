package jk_5.nailed.api.chat;

final public class HoverEvent {

    private final Action action;
    private final BaseComponent[] value;

    public HoverEvent(Action action, BaseComponent[] value) {
        this.action = action;
        this.value = value;
    }

    public BaseComponent[] getValue() {
        return value;
    }

    public Action getAction() {
        return action;
    }

    public enum Action {

        SHOW_TEXT,
        SHOW_ACHIEVEMENT,
        SHOW_ITEM
    }
}
