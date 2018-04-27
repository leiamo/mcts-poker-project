package projectbot.selection;

import projectbot.nodes.Node;
import java.util.Random;

public class RandomSelection implements ISelection {
    @Override
    public Node findBestNode(Node node) {
        Random rnd = new Random();
        int randomChoice = rnd.nextInt(node.ChildNodes.size());
        return node.getChildNodes().get(randomChoice);
    }
}
