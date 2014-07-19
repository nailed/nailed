package jk_5.nailed.api.chat;

public final class ClickEvent {

    /**
     * The type of action to preform on click
     */
    private final Action action;
    /**
     * Depends on action
     *
     * @see Action
     */
    private final String value;

    public ClickEvent(Action action, String value) {
        this.action = action;
        this.value = value;
    }

    public Action getAction() {
        return action;
    }

    public String getValue() {
        return value;
    }

    public enum Action {

        /**
         * Open a url at the path given by
         * {@link ClickEvent#value}
         */
        OPEN_URL,
        /**
         * Open a file at the path given by
         * {@link ClickEvent#value}
         */
        OPEN_FILE,
        /**
         * Run the command given by
         * {@link ClickEvent#value}
         */
        RUN_COMMAND,
        /**
         * Inserts the string given by
         * {@link ClickEvent#value} into the players text box
         */
        SUGGEST_COMMAND
    }
}
