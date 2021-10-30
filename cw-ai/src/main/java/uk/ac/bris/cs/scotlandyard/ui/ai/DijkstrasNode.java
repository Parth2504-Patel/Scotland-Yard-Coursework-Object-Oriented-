package uk.ac.bris.cs.scotlandyard.ui.ai;

import uk.ac.bris.cs.gamekit.graph.Node;
import uk.ac.bris.cs.gamekit.graph.Edge;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard;
import java.util.HashSet;
import java.util.Collection;
import java.util.Set;

public class DijkstrasNode {
    final private int location;
    private int danger;
    private int availability;

    public DijkstrasNode(Integer location) {
        this.location = location;
        this.danger = 100;
        this.availability = 0;
        //availability relates to how many spaces adjacent to source are free to move to.
    }

    public int getLocation(){
        return this.location;
    }
    public int getDanger() {return this.danger;}
    public int getAvailability(){
        return availability;
    }
    public void setDanger(int danger) {
        this.danger = danger;
    }
    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public void adjustDanger(int dScore) {
        if(this.danger - dScore > 0) {
            this.danger = dScore;
        }
    }

    //Returns adjacent nodes to location
    public Set<Integer> findAdjacentNodes(DijkstrasGraph graph, int location){
        return new HashSet<>(graph.getGraph().adjacentNodes(location));
    }

}
