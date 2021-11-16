package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Dijkstra {
    private final DijkstrasGraph DijkstraGraph;
    private final int source;
    private final Set<Integer> adjacentNodes;
    private ArrayList<Integer> distanceForEachAdjacentNode;

    public Dijkstra(DijkstrasGraph graph, int source){
        this.DijkstraGraph = graph;
        this.adjacentNodes = graph.getGraph().adjacentNodes(source);
        this.source = source;
        this.distanceForEachAdjacentNode = new ArrayList<>();

    }
    // The method returns the shortest distance between the source and destination provided into the method.
    private int DijkstrasAlgo(int destination, int source) {
        Set<Integer> allNodes = new HashSet<>(this.DijkstraGraph.getGraph().nodes());
        int sizeOfGraph = allNodes.size();
        ArrayList<Integer> distance = new ArrayList<>(sizeOfGraph);
        ArrayList<Integer> priorityQueue = new ArrayList<>();
        ArrayList<Integer> visitedPlaces = new ArrayList<>();

        for (int i=0;i < sizeOfGraph;i++) {distance.add(Integer.MAX_VALUE);}

        priorityQueue.add(source);
        distance.set(source-1,0);
        int currentNode;

        while (!priorityQueue.isEmpty()) {
            int IndexOfcurrentNode = findIndexOfNodeWithSmallDistance(priorityQueue,distance);
            currentNode = priorityQueue.get(IndexOfcurrentNode);
            int removed = priorityQueue.remove(IndexOfcurrentNode);
            visitedPlaces.add(currentNode);
            //Adds the currentNode to list of vistedNodes
            for (int adjacentNode : this.DijkstraGraph.getGraph().adjacentNodes(currentNode)) {
                if (!visitedPlaces.contains(adjacentNode)) {
                    int newDistance = distance.get(currentNode-1) + 1;
                    if (newDistance < distance.get(adjacentNode-1)) {
                        distance.set(adjacentNode-1,newDistance);
                        //node is only added to priortity queue if and only if a shorter distance to the node being explored
                        //is found
                        priorityQueue.add(adjacentNode);
                    }
                }
            }
        }
        ArrayList<Integer> allNodesAsList = new ArrayList<>(allNodes);
        int indexOfDestination = allNodesAsList.indexOf(destination);
        return distance.get(indexOfDestination);
    }

        public int findIndexOfNodeWithSmallDistance(ArrayList<Integer> priorityQueue,ArrayList<Integer> distances) {
            int smallestNodeDistance = Integer.MAX_VALUE;
            int smallestNodeLocation = 0;
            for (int node : priorityQueue) {
                if (distances.get(node-1) <= smallestNodeDistance) {
                    smallestNodeDistance = distances.get(node-1);
                    smallestNodeLocation = node;
                }
            }
            return priorityQueue.indexOf(smallestNodeLocation);
        }

        //This method applies dijkstra's algorithm to mrX's adjacent nodes and the detectives, calculating the total distance
        //from an adjacent node to each detective.
        public void ApplyAlgos(){
        Set<Integer> detectivePositions = new HashSet<>(this.DijkstraGraph.getAlldetectiveLocation());
        for (int adjacentNode : this.adjacentNodes) {
            int totalDistanceForOneNode = 0;
            for (int detectivePosition : detectivePositions) {
                int tempDistance = DijkstrasAlgo(adjacentNode,detectivePosition);
                if (tempDistance <= 1) this.DijkstraGraph.getNode(adjacentNode).setDanger(10000);
                //Danger set to 10000 as if mrX moves here, he will definitely be caught the next round as the detective
                //Can move to this location
                totalDistanceForOneNode += tempDistance;
            }
            this.distanceForEachAdjacentNode.add(totalDistanceForOneNode);
        }
        }

        //findDangerScore is the formula we came up with the combines all the factors the AI considers into 1 final score
        //Which is represented by a danger Score. Higher the Danger
        public int findDangerScore(DijkstrasNode location, int distance){
            return (location.getDanger() - (2*distance) - ((int) Math.floor(0.4*location.getAvailability())));
        }

        public ArrayList<Integer> getDistanceForEachAdjacentNode() { return this.distanceForEachAdjacentNode;}
    }
