package projectbot.tree;

import projectbot.nodes.Node;

public class Tree {

    public Node RootNode;

    // Initialise a new tree where root node has no parent
    public Tree(){
        RootNode = new Node(null);
    }

    public Node getRootNode() {
        return RootNode;
    }

}
