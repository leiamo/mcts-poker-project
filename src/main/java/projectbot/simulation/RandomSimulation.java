package projectbot.simulation;

import projectbot.gamestate.GameState;
import projectbot.nodes.Node;

public class RandomSimulation implements ISimulation {

    public double simulatePlayout(Node node) throws Exception {
        Node tempNode = node.cloneNode();
        GameState tempState;

        while (!tempNode.getCurrentState().isGameOver()) {
            tempState = tempNode.getCurrentState().playRandomMove();
            tempNode.setCurrentState(tempState);
        }

        // When end of game, find winner and return result
        return tempNode.getCurrentState().getGameResult();
    }
}
