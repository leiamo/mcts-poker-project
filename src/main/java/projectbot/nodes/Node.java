package projectbot.nodes;

import com.biotools.meerkat.Hand;
import projectbot.gamestate.GameState;
import projectbot.gamestate.RowAction;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class Node {

    public GameState CurrentState;
    public Node ParentNode;
    public List<Node> ChildNodes;
    public int VisitCount;
    public double ExpValue;
    public String playerName = "Abot";

    public Node(Node parentNode) {
        ParentNode = parentNode;
        ChildNodes = new LinkedList<Node>();
        VisitCount = 0;
        ExpValue = 0;
    }

    public Node(GameState state, Node parentNode){
        CurrentState = state;
        ParentNode = parentNode;
        ChildNodes = new LinkedList<Node>();
        VisitCount = 0;
        ExpValue = 0;
    }

    public Node cloneNode() {
        Node newNode = new Node(getParentNode());
        for (Node child : ChildNodes) {
            newNode.addChildNode(child.cloneNode());
        }

        GameState s = CurrentState;
        Hand newBoardCards = new Hand();
        for (int i = 0; i < s.getBoardCards().size(); i++) {
            newBoardCards.addCard(s.getBoardCards().getCard(i));
        }

        LinkedList<RowAction> newActions = new LinkedList<>();
        for (RowAction r: s.getActions()) {
            newActions.add(r);
        }

        GameState newState = new GameState(newBoardCards, s.getCard1(),s.getCard2(),
                s.getMySeat(),s.getEnemySeat(),s.getButtonSeat(),s.isEnemyTurn(),
                newActions,s.getPotSize(),s.getRound(),s.getDealer(),s.getEnemyName(),
                s.getBotWinProb(), s.classifier, s.aggression);

        newNode.setCurrentState(newState);
        newNode.VisitCount = VisitCount;
        newNode.ExpValue = ExpValue;
        return newNode;
    }

    public void updateExpValue(double update) {
        ExpValue += update;
    }

    public GameState getCurrentState() {
        return CurrentState;
    }

    public void setCurrentState(GameState currentState) {
        CurrentState = currentState;
    }

    public Node getParentNode() {
        return ParentNode;
    }

    public List<Node> getChildNodes() {
        return ChildNodes;
    }

    public double getExpValue() {
        return ExpValue;
    }

    public int getVisitCount() {
        return VisitCount;
    }

    public void incrementVisitCount(){
        VisitCount++;
    }

    public void addChildNode(Node node) {
        ChildNodes.add(node);
    }

    public Node selectRandomChildNode() {
        Random r = new Random();
        int random = r.nextInt(ChildNodes.size());
        return ChildNodes.get(random);
    }

    public Node selectChildWithMaxScore() {
        double maxScore = Integer.MIN_VALUE;
        Node maxNode = this;
        for ( Node child : this.getChildNodes() ) {
            if (child.getExpValue() >= maxScore) {
                maxNode = child;
                maxScore = child.getExpValue();
            }
        }
        return maxNode;
    }
}
