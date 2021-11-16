package uk.ac.bris.cs.scotlandyard.ui.ai;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.annotation.Nonnull;


import io.atlassian.fugue.Pair;
import uk.ac.bris.cs.scotlandyard.model.Ai;
import uk.ac.bris.cs.scotlandyard.model.Board;
import uk.ac.bris.cs.scotlandyard.model.Move;

public class MyAi implements Ai {

	@Nonnull @Override public String name() { return "MyAi"; }
	@Nonnull @Override public Move pickMove(
			@Nonnull Board board,
			Pair<Long, TimeUnit> timeoutPair) {

		int mrXLocation = board.getAvailableMoves().iterator().next().source();
		int numOfDetectives = board.getPlayers().size()-1;
		DijkstrasGraph DijkstraGraph = new DijkstrasGraph(board);
		DijkstraPickMove pickingFinalMove = new DijkstraPickMove(DijkstraGraph,mrXLocation,board,numOfDetectives);
		return pickingFinalMove.findBestMove();

	}
}
