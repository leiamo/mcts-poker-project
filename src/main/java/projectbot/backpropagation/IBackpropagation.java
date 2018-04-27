package projectbot.backpropagation;

import projectbot.nodes.Node;

public interface IBackpropagation {
    void backPropagate(Node node, double scoreToUpdate);
}
