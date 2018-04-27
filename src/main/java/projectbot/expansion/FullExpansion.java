package projectbot.expansion;

import projectbot.gamestate.GameState;
import projectbot.nodes.Node;

import java.util.LinkedList;

public class FullExpansion implements IExpansion {
    @Override
    public void expandNode(Node node) {
        LinkedList<GameState> possibleStates = node.getCurrentState().getAllPossibleGameStates();
        Node newNode;
        for (GameState state : possibleStates) {
            newNode = new Node(state, node);        // creates node with state and given parent
            node.getChildNodes().add(newNode);      // add this new node possibility for game to tree (child of given parent node)
        }
    }

}
