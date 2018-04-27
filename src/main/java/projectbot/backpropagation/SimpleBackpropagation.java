package projectbot.backpropagation;

import projectbot.nodes.Node;

public class SimpleBackpropagation implements IBackpropagation {
    public void backPropagate(Node node, double scoreToUpdate) {
        Node tempNode = node;
        while (tempNode != null) {
            tempNode.incrementVisitCount();
            tempNode.updateExpValue(
                    tempNode.getExpValue() + scoreToUpdate
            );
            tempNode = tempNode.getParentNode();
        }
    }
}
