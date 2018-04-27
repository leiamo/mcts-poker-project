package projectbot.simulation;

import projectbot.nodes.Node;

public interface ISimulation {
    double simulatePlayout(Node node) throws Exception;
}
