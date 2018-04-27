package projectbot.main;

import com.biotools.meerkat.Action;
import com.biotools.meerkat.Card;
import com.biotools.meerkat.GameInfo;
import projectbot.backpropagation.IBackpropagation;
import projectbot.enums.ActionEnum;
import projectbot.expansion.IExpansion;
import projectbot.gamestate.GameState;
import projectbot.gamestate.RowAction;
import projectbot.nodes.Node;
import projectbot.opponentModelling.ClassifierHandler;
import projectbot.selection.ISelection;
import projectbot.simulation.ISimulation;
import projectbot.tree.Tree;

import java.util.LinkedList;

// Most of this code references http://www.baeldung.com/java-monte-carlo-tree-search for basic MCTS implementation

public class MCTS {
    private double end;
    private String playerName;
    private ISelection selection;
    private IExpansion expansion;
    private ISimulation simulation;
    private IBackpropagation backpropagation;

    // Initialise MCTS with strategies
    public MCTS(int numIterations, ISelection selection, IExpansion expansion, ISimulation simulation, IBackpropagation backpropagation) {
        this.end = numIterations;
        this.playerName = "Abot";
        this.selection = selection;
        this.expansion = expansion;
        this.simulation = simulation;
        this.backpropagation = backpropagation;
    }

    // When it is the bot's turn, it runs MCTS with opponent modelling to select the optimal move
    public Action chooseNextMove(GameInfo gameInfo, Card c1, Card c2, LinkedList<RowAction> actionsMade, Double winProb, ClassifierHandler classifierHandler, double aggression) throws Exception {
        long count = 0;

        // Construct new game tree with root and initial game information
        Tree gameTree = new Tree();
        Node rootNode = gameTree.getRootNode();
        rootNode.setCurrentState(new GameState(gameInfo, c1, c2, playerName, actionsMade, winProb, classifierHandler, aggression));

        // Run MCTS process for fixed time/iterations
        while (count < end) {
            count += 1;

            // Select most promising node from root's children, and repeat selection process until leaf node reached
            Node promisingNode = selectPromisingNode(rootNode);

            // Expand leaf node if it is not a terminal state node
            if (!promisingNode.getCurrentState().isGameOver()) {
                expansion.expandNode(promisingNode);
            }

            // Choose random node from expanded children (cannot use special selection as no information available)
            Node nodeToExplore = promisingNode;
            if (promisingNode.getChildNodes().size() > 0) {
                nodeToExplore = promisingNode.selectRandomChildNode();
            }

            // Simulate game-play from the randomly chosen node until an endpoint is reached
            double playoutResult = simulation.simulatePlayout(nodeToExplore);

            // Back-propagate up the tree to update nodes with the simulated result and increment their visit count
            backpropagation.backPropagate(nodeToExplore, playoutResult);
        }

        // After MCTS process produces an incomplete game tree with weighted/preferred nodes, select optimal node
        Node optimalNode = rootNode.selectChildWithMaxScore();

        // Return optimal node's last action
        return convertToAction(optimalNode, gameInfo.getMinRaise(), actionsMade.size() == 2);
    }

    private Action convertToAction(Node optimalNode, double minRaise, boolean isFirstMove) {
        ActionEnum numberedAction = optimalNode.CurrentState.getActions().getLast().action;
        return ActionEnum.convertAction(numberedAction.getValue(), minRaise, isFirstMove);
    }

    private Node selectPromisingNode(Node node) {
        while (node.getChildNodes().size() != 0) {
            node = selection.findBestNode(node);
        }
        return node;
    }

}
