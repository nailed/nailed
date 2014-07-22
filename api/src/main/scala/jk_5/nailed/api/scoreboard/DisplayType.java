package jk_5.nailed.api.scoreboard;

/**
 * Describes where an objective should be displayed on the client gui
 *
 * @author jk-5
 */
public enum DisplayType {
    /**
     * Displays the objective in the player list that is displayed when the player presses [Tab]
     */
    PLAYER_LIST(0),
    /**
     * Displays the objective in the sidebar on the right of the screen
     */
    SIDEBAR(1),
    /**
     * Displays the objective below the name of the player (Above a player's head)
     */
    BELOW_NAME(2);

    /**
     * The id of the {@link DisplayType}. This is used for vanilla compatability and networking
     */
    private final int id;

    DisplayType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }
}
