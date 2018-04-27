package projectbot.selection;

import projectbot.nodes.Node;

public interface ISelection {
    Node findBestNode(Node node);
}
