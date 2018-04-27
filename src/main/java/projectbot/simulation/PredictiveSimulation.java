package projectbot.simulation;

import projectbot.gamestate.GameState;
import projectbot.nodes.Node;

public class PredictiveSimulation implements ISimulation {
    @Override

    public double simulatePlayout(Node node) throws Exception {
        Node tempNode = node.cloneNode();
        GameState tempState;
        while (!tempNode.getCurrentState().isGameOver()) {
            tempState = tempNode.getCurrentState().playMove();
            tempNode.setCurrentState(tempState);
        }

        // When end of game, find winner and return result
        return tempNode.getCurrentState().getGameResult();
    }

}
