package uk.ac.bris.cs.scotlandyard.ui.ai;

import com.google.common.collect.ImmutableSet;
import com.google.common.graph.ImmutableValueGraph;
import uk.ac.bris.cs.gamekit.graph.Edge;

import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.MyGameStateFactory.*;
import uk.ac.bris.cs.scotlandyard.model.Piece;
import uk.ac.bris.cs.scotlandyard.model.Player;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import java.util.*;

public class DijkstrasGraph {
    final private ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> graph;
    final private ArrayList<DijkstrasNode> nodes = new ArrayList<>();
    final private HashSet<Integer> detectiveLocation;


    public DijkstrasGraph(Board state) {
        this.graph = state.getSetup().graph;
        HashSet<Integer> tempLocations = new HashSet<>();
        //This for loop gathers the location of the detectives into detectiveLocation
        for (Piece playerPiece: state.getPlayers()) {
            if (!playerPiece.isMrX()) {
                if (state.getDetectiveLocation((Piece.Detective) playerPiece).isPresent()) {
                    tempLocations.add(state.getDetectiveLocation((Piece.Detective) playerPiece).get());
                }
            }

        }
        this.detectiveLocation = tempLocations;
        Set<Integer> allNodes = new HashSet<>();
        allNodes = this.graph.nodes();
        for(int node : allNodes) {
            //Associates each node with a dijkstra Node
            DijkstrasNode dijkstrasNode = new DijkstrasNode(node);
            dijkstrasNode.setAvailability(adjustAvailability(dijkstrasNode.findAdjacentNodes(this,node)));
            this.nodes.add(dijkstrasNode);
        }
    }

    public int adjustAvailability(Set<Integer> adjacentNodes) {
        int detectiveNearBy = 0;
        for (int node : adjacentNodes) {
            if (this.detectiveLocation.contains(node)) {
                detectiveNearBy++;
            }
        }
        return adjacentNodes.size() - detectiveNearBy;
    }

    // Getters and Setters
    public ImmutableValueGraph<Integer, ImmutableSet<ScotlandYard.Transport>> getGraph() {
        return graph;
    }

    public DijkstrasNode getNode(int location){
        for(DijkstrasNode node : this.nodes){
            if (node.getLocation()==location){
                return node;
            }
        }
        return null;
    }
    public HashSet<Integer> getAlldetectiveLocation(){
        return this.detectiveLocation;
    }
}