package game;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.GameInfo;
import com.biotools.meerkat.GameObserver;

/**
 * Extended events for a Game Observer
 */
public interface ExtendedGameObserver extends GameObserver {

    /**
     * Observer that will be informed about actions BEFORE they are processed by the game engine, immediately after the
     * action was decided by an agent
     */
    public void beforeActionEvent(int pos, Action action);

    /**
     * Same as gameOverEvent, but specifies which game is over. Useful when running multiple games simultaneously
     *
     * @param gameInfo
     */
    public void gameOverEvent(GameInfo gameInfo);


}
