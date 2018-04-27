package projectbot.selection;

import projectbot.nodes.Node;
import java.util.Collections;
import java.util.Comparator;

// References http://www.baeldung.com/java-monte-carlo-tree-search for UCT Selection
public class UCTSelection implements ISelection {

    public double const1;

    public UCTSelection(double c1) {
        const1 = c1;
    }

    public double uctValue(int totalVisit, double nodeWinScore, int nodeVisit) {
        if (nodeVisit == 0) {
            return Integer.MAX_VALUE;
        }
        return (nodeWinScore / (double) nodeVisit) + (const1 * Math.sqrt(Math.log(totalVisit) / (double) nodeVisit));
    }

    @Override
    public Node findBestNode(Node node) {
        int parentVisit = node.getVisitCount();
        return Collections.max(node.getChildNodes(),
                Comparator.comparing(c -> uctValue(parentVisit, c.getExpValue(), c.getVisitCount())));
    }
}