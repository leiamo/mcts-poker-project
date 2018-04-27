package projectbot.selection;

import projectbot.nodes.Node;
import java.util.Random;

// Smooth UCT Selection references http://poker.cs.ualberta.ca/publications/billings.phd.pdf p45-49
public class SmoothUCTSelection implements ISelection {

    public Node n;
    public double c1, c2, c3, c4;

    // Initialise constants for Smooth UCT
    public SmoothUCTSelection(double c1, double c2, double c3, double c4) {
        this.c1 = c1;
        this.c2 = c2;
        this.c3 = c3;
        this.c4 = c4;
    }

    @Override
    public Node findBestNode(Node node) {
        this.n = node;
        return smoothUCT();
    }

    public Node smoothUCT() {
        // Choose random probability for x
        double x = uniformDistributionProbability();

        // If this falls within probability of non-average strategy, perform regular UCT Selection
        if (x < averageStrategyProbability(c1, c2, c3)) { // best values for heads-up
            return getMaxChildNode(c4);
        }
        // If this falls within probability of average strategy profile, selection using average action
        else {
            return getAverageChild();
        }
    }

    public Node getAverageChild() {
        double probability = 0;
        double total = 0;
        // Initialise total visits for calculating action probabilities
        for (Node childNode : n.getChildNodes()) {
            total += childNode.getVisitCount();
        }
        // Iterates through child nodes to find which is most average visited
        // The highest number of visits has a higher probability of being selected
        // This means that each individual probability should add up to total = 1
        for (Node childNode : n.getChildNodes()) {
            probability += (childNode.getVisitCount() / total);
            // assign probability of child node
            double x = uniformDistributionProbability();
            if (x <= probability) {
                return childNode;
            }
        }
        return null;
    }

    public Node getMaxChildNode(double chanceConst) {
        double maxValue = Double.MIN_VALUE;
        int maxIndex = 0;
        int numChildren = n.getChildNodes().size();

        // Iterate through each child node
        for (int i = 0; i < numChildren; i++) {
            Node childNode = n.getChildNodes().get(i);

            // Assign UCT value
            double value = childNode.getExpValue() + (chanceConst * Math.sqrt(Math.log(n.getVisitCount()) / childNode.getVisitCount()));

            // If this is maximum UCT value, assign it until all nodes have been compared
            if (value >= maxValue) {
                maxValue = value;
                maxIndex = i;
            }
        }
        // After loop, return the max action child
        return n.getChildNodes().get(maxIndex);
    }

    // Returns random value x that follows a uniform distribution where 0 < x < 1
    public double uniformDistributionProbability() {
        Random rnd = new Random();
        double randomValue = rnd.nextInt(99) + 1;
        return randomValue / 100;
    }

    // Average strategy probability
    public double averageStrategyProbability(double const1, double const2, double const3) {
        double val1 = const1;
        double val2 = Math.pow(1 + const3 * (Math.sqrt(n.getVisitCount())), -1) * const2;
        return Math.max(val1, val2);
    }

}
