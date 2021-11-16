package uk.ac.bris.cs.scotlandyard.ui.ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;
import uk.ac.bris.cs.scotlandyard.model.Piece;

import java.util.*;

public class DijkstraPickMove {
    final private DijkstrasGraph DijkstraGraph;
    final private int mrXLocation;
    final private Board board;
    final private int numberOfDetectives;

    public DijkstraPickMove(DijkstrasGraph graph,int mrXLocation, Board board,int numOfDetectives) {
        this.DijkstraGraph = graph;
        this.mrXLocation = mrXLocation;
        this.board = board;
        this.numberOfDetectives = numOfDetectives;
    }

    public Move findBestMove() {
        Dijkstra DijkstraAlgo = new Dijkstra(this.DijkstraGraph,mrXLocation);
        int destinationOfBestMove = findBestDestination(mrXLocation,DijkstraAlgo,this.DijkstraGraph);
        Move FinalMove = null;
        final boolean TimeForDoubleMove = this.DijkstraGraph.getNode(destinationOfBestMove).getDanger() > (100 - (numberOfDetectives*6));
        //Need to reset graph in case mrX goes back to some locations and in that case need to reexamine the dangers from 100
        resetGraph(this.DijkstraGraph);
        Dijkstra DijkstraAlgoForDoubleMove = new Dijkstra(this.DijkstraGraph,destinationOfBestMove);
        final int destination2OfBestMove = findBestDestination(destinationOfBestMove,DijkstraAlgoForDoubleMove,this.DijkstraGraph);
        resetGraph(this.DijkstraGraph);
        Move.FunctionalVisitor<Move> moveVisitor = new Move.FunctionalVisitor<Move>(singleMove -> {
            if (!TimeForDoubleMove) {
                if (destinationOfBestMove == singleMove.destination) return singleMove;
            }
            return null;

        }, doubleMove -> {
            if (TimeForDoubleMove) {
                if (destinationOfBestMove == doubleMove.destination1) {
                    if (destination2OfBestMove == doubleMove.destination2 && (destination2OfBestMove != mrXLocation)) {
                        return doubleMove;
                    }
                }
            }
            return null;
        });

        Iterator<Move> moveIterator = this.board.getAvailableMoves().iterator();
        while (moveIterator.hasNext()) {
            Move move = moveIterator.next();
            if (move.visit(moveVisitor) != null) {
                FinalMove = move;
                break;
            }
        }
        //This visitor has less strict conditions when picking the move where which is only used if the first visitor checks
        //are not fulfilled. Here it gets a move with at least the first move destination being the best First detination mrX can move to.
        Move.FunctionalVisitor<Move> lastResortVisitor = new Move.FunctionalVisitor<Move>(singleMove ->{
            if (singleMove.destination == destinationOfBestMove) return singleMove;
            return null;
        }, doubleMove -> null);

        if (FinalMove == null) {
            for (Move move : this.board.getAvailableMoves()) {
                if (move.visit(lastResortVisitor) != null) {
                    FinalMove = move;
                    break;
                }
            }
            //This check is done incase if even the less strict conditions are not met and thus to not return a null move
            //the first move from getAvailableMoves is returned.
            if (FinalMove == null) FinalMove = this.board.getAvailableMoves().iterator().next();
        }
        return FinalMove;
    }

    public ArrayList<Integer> calculateTotalDistances (int source, Dijkstra DijkstraObj, DijkstrasGraph DijkstraGraph) {
        DijkstraObj.ApplyAlgos();
        int i =0;
        ArrayList<Integer> DijkstraDangerScoreList = new ArrayList<>();
        for (int adjacentNode : DijkstraGraph.getGraph().adjacentNodes(source)) {
            DijkstrasNode currentNode = DijkstraGraph.getNode(adjacentNode);
            if (!(currentNode.getDanger() >= 10000))currentNode.adjustDanger(DijkstraObj.findDangerScore(currentNode,DijkstraObj.getDistanceForEachAdjacentNode().get(i)));
            DijkstraDangerScoreList.add(DijkstraGraph.getNode(adjacentNode).getDanger());
            i++;
        }
        return  DijkstraDangerScoreList;
    }
    public int findBestDestination(int source, Dijkstra DijkstraObj, DijkstrasGraph DijkstraGraph) {
        ArrayList<Integer> DijkstraDangerList = calculateTotalDistances(source, DijkstraObj, DijkstraGraph);
        int indexOfSmallestDanger = DijkstraDangerList.indexOf(Collections.min(DijkstraDangerList));
        ArrayList<Integer> adjacentNodesAsList = new ArrayList<>(DijkstraGraph.getGraph().adjacentNodes(source));
        return adjacentNodesAsList.get(indexOfSmallestDanger);
    }
    public void resetGraph(DijkstrasGraph DijkstraGraph){
        for (int node : DijkstraGraph.getGraph().nodes()) {
            if (!(DijkstraGraph.getNode(node).getDanger() >=10000)) DijkstraGraph.getNode(node).setDanger(100);
        }
    }
}

