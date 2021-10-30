package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;

public final class MyGameStateFactory implements Factory<GameState> {
	private final class MyGameState implements GameState {
		final private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		final private Player mrX;
		final private List<Player> detectives;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		public int currentRound;

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives,
				int currentRound) {

			ArrayList<String> overlapColours = new ArrayList<>();
			ArrayList<Integer> overlapLocations = new ArrayList<>();

			for (Player potentialPlayer : detectives) {
				// Checks if any of the detectives are MrX
				if (potentialPlayer.isMrX()){ throw new IllegalArgumentException(); }
				// Checks if there are any duplicate locations or colours
				if (overlapColours.contains(potentialPlayer.piece().webColour())) {throw new IllegalArgumentException();}
				overlapColours.add(potentialPlayer.piece().webColour());
				if (overlapLocations.contains(potentialPlayer.location())) { throw new IllegalArgumentException();}
				overlapLocations.add(potentialPlayer.location());
				//Checks ticket validity
				if (potentialPlayer.has(Ticket.SECRET) || potentialPlayer.has(Ticket.DOUBLE)) {throw new IllegalArgumentException();}

			}

			// Checks if rounds and graphs are empty
			if(setup.rounds.isEmpty()) {throw new IllegalArgumentException("Rounds is empty!");}
			if(setup.graph.nodes().isEmpty()) {throw new IllegalArgumentException("Graph is empty");}
			// Checks players are non null
			if(detectives.isEmpty()) {throw new NullPointerException("Detectives is empty!");}
			if (mrX.piece() == null) {throw new NullPointerException("No MrX piece");}
			// Check MrX should be black
			if (!mrX.piece().webColour().equals("#000")) {throw new IllegalArgumentException("MrX character is wrong");}

			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.winner = ImmutableSet.of();
			List<Player> tempPlayers = new ArrayList<>(detectives);
			tempPlayers.add(mrX);
			this.everyone = ImmutableList.copyOf(tempPlayers);
			this.currentRound = currentRound;

			ArrayList<Move> bigListOfAllMoves = new ArrayList<Move>();
			remaining.forEach(playablePiece -> {
				if (remaining.contains(mrX.piece())) {
					bigListOfAllMoves.addAll(makeSingleMoves(setup,detectives,mrX,mrX.location()));
					bigListOfAllMoves.addAll(makeDoubleMoves(setup,detectives,mrX,mrX.location()));

				}
				else {
					Player playablePlayer = checkPieceToPlayer(playablePiece);
					bigListOfAllMoves.addAll(makeSingleMoves(setup, detectives, playablePlayer, playablePlayer.location()));
				}
			});
			this.moves = ImmutableSet.copyOf(bigListOfAllMoves);
			winnerChecks();
		}

		public void winnerChecks() {
			boolean playersHaveNoTickets = true;
			boolean mrXCaught = false;

			for (Player detective : detectives ) {
				if (!makeSingleMoves(setup,detectives,detective,detective.location()).isEmpty()) {
					playersHaveNoTickets = false;
				}
				if (mrX.location() == detective.location()) {
					mrXCaught = true;
				}
			}

			if (currentRound == setup.rounds.size()) {
				if (!mrXCaught)  setMrXAsWinner();
				else setDetectivesAsWinner();
				setMrXAsWinner();
			}

			boolean mrXCornered = true;
			ArrayList<Integer> locationsOfDetectives = new ArrayList<>();
			for (Player p : detectives) {
				locationsOfDetectives.add(p.location());
			}

			for (int adjacentNodeLocation : setup.graph.adjacentNodes(mrX.location())) {
				if (!locationsOfDetectives.contains(adjacentNodeLocation)) mrXCornered = false;
			}

			if (currentRound == setup.rounds.size()) {
				if (!mrXCaught)  setMrXAsWinner();
				else setDetectivesAsWinner();
				setMrXAsWinner();
			}
			else {
				if (remaining.asList().get(0).isMrX() && this.getAvailableMoves().isEmpty()) setDetectivesAsWinner();
				if (playersHaveNoTickets) setMrXAsWinner();
				if (mrXCaught || mrXCornered) setDetectivesAsWinner();
			}
		}

		public void setDetectivesAsWinner() {
			Set<Piece> detectiveWinnerList = new HashSet<>();
			for (Player detective : detectives) detectiveWinnerList.add(detective.piece());
			this.winner = ImmutableSet.copyOf(detectiveWinnerList);
			this.moves = ImmutableSet.of();
		}

		public void setMrXAsWinner() {
			this.winner = ImmutableSet.of(mrX.piece());
			this.moves = ImmutableSet.of();
		}

		@Nonnull @Override public GameSetup getSetup() {
			return setup;
		}

		@Nonnull @Override public ImmutableSet<Piece> getPlayers() {
			Set<Piece> playerPieces = new HashSet<>();
			for (Player p : everyone) {
				playerPieces.add(p.piece());
			}
			return ImmutableSet.copyOf(playerPieces);
		}

		@Nonnull @Override public Optional<Integer> getDetectiveLocation(Piece.Detective detective){
			for (Player potentialDetective : detectives ) {
				if (potentialDetective.piece().webColour().equals(detective.webColour())) return Optional.of(potentialDetective.location());
			}
			return Optional.empty();
		}

		//gets the corresponding Player given a Piece
		public Player checkPieceToPlayer (Piece piece) {
			for (Player potentialPlayer : everyone) {
				if (potentialPlayer.piece().webColour().equals(piece.webColour())) return potentialPlayer;
			}
			//Exception thrown if Piece provided isn't a player of the game
			throw new NullPointerException();
		}

		@Nonnull @Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			class playerTicketBoard implements TicketBoard {
				Player player;
				ImmutableMap<ScotlandYard.Ticket, Integer> tickets;
				playerTicketBoard(Player player) {
					this.player = player;
					this.tickets = player.tickets();
				}

				@Override
				public int getCount(@Nonnull Ticket ticket) {
					return this.tickets.get(ticket);
				}
			}
			for (Player maybePlayer : everyone) {
				if (maybePlayer.piece().equals(piece)) {
					playerTicketBoard TicketBoardOfPlayer = new playerTicketBoard( checkPieceToPlayer(piece) );
					return Optional.of(TicketBoardOfPlayer);
				}
			}
			return Optional.empty();
		}

		@Nonnull @Override public ImmutableList<LogEntry> getMrXTravelLog() {
			return log;
		}
		@Nonnull @Override public ImmutableSet<Piece> getWinner() {
			return winner;
		}

		private ImmutableSet<Move.SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source) {
			ArrayList<SingleMove> singleMoves = new ArrayList<>();
			ArrayList<Integer> locationsOfDetectives = new ArrayList<>();
			for (Player p : detectives) {
				locationsOfDetectives.add(p.location());
			}
			for (int destination : setup.graph.adjacentNodes(source)) {
				for(Transport t : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
					if (!(locationsOfDetectives.contains(destination))) {
						if (player.has(t.requiredTicket())) {
							SingleMove tempSingleMove = new SingleMove(player.piece(),source,t.requiredTicket(),destination);
							singleMoves.add(tempSingleMove);
						}
						if (!(detectives.contains(player)) && (player.has(Ticket.SECRET)) ) {
							SingleMove tempSingleMove = new SingleMove(player.piece(),source,Ticket.SECRET,destination);
							singleMoves.add(tempSingleMove);
						}
					}
				}
			}
			return ImmutableSet.copyOf(singleMoves);
		}

		private ImmutableSet<Move.DoubleMove> makeDoubleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source) {
			ArrayList<Move.DoubleMove> doubleMoves = new ArrayList<>();
			if (player.has(Ticket.DOUBLE) && !setup.rounds.equals(ImmutableList.of(true))) {
				ArrayList<Move.SingleMove> firstMoves = new ArrayList<>(makeSingleMoves(setup, detectives, player, source));
				for (Move.SingleMove firstMove : Collections.unmodifiableList(firstMoves)) {
					HashSet<Move.SingleMove> secondMoves = new HashSet<>(makeSingleMoves(setup, detectives, player, firstMove.destination));
					for (Move.SingleMove secondMove : secondMoves) {
						if (!firstMove.ticket.equals(secondMove.ticket) || player.hasAtLeast(secondMove.ticket,2)) doubleMoves.add(new DoubleMove(mrX.piece(), source, firstMove.ticket, firstMove.destination, secondMove.ticket, secondMove.destination));
					}
				}
			}
			return ImmutableSet.copyOf(doubleMoves);
		}

		@Nonnull @Override public ImmutableSet<Move> getAvailableMoves() {return moves;}

		public void updateRemainingState(Player player, boolean isDoubleMove) {
			HashSet<Piece> temporaryRemaining = new HashSet<>();
			if (player.isMrX() && !isDoubleMove) {
				for (Player playableDetectives : detectives) {
					if (!makeSingleMoves(setup,detectives,playableDetectives,playableDetectives.location()).isEmpty()) {
						temporaryRemaining.add(playableDetectives.piece());
					}
				}
			}
			else if (player.isDetective()){
				if (remaining.size() == 1)  {
					if (currentRound < setup.rounds.size()) {
						temporaryRemaining.add(mrX.piece());
						this.currentRound = currentRound + 1;
					}
				}
				else {
					for (Piece currentPiece : remaining) {
						if (!currentPiece.equals(player.piece())) temporaryRemaining.add(currentPiece);
					}
				}
			}
			this.remaining = ImmutableSet.copyOf(temporaryRemaining);
		}

		public void updateMrXLog(SingleMove singleMove) {
			// Reveal round
			if (setup.rounds.get(this.currentRound).equals(true)) {
				LogEntry temporaryLogEntry = LogEntry.reveal(singleMove.ticket,singleMove.destination);
				ArrayList<LogEntry> logEntries = new ArrayList<>(this.log);
				logEntries.add(temporaryLogEntry);
				this.log = ImmutableList.copyOf(logEntries);
			}
			// Hidden round
			else {
				LogEntry hiddenTicket = LogEntry.hidden(singleMove.ticket);
				ArrayList<LogEntry> logEntries = new ArrayList<>(this.log);
				logEntries.add(hiddenTicket);
				this.log = ImmutableList.copyOf(logEntries);
			}
			updateRemainingState(mrX,false);
		}

		@Nonnull @Override public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+ move);
			// Implementation of Visitor Pattern
			FunctionalVisitor<MyGameState> visitorObject = new FunctionalVisitor<MyGameState>(singleMove -> {
				Player currentPlayer = checkPieceToPlayer(singleMove.commencedBy());
				if (currentPlayer.isMrX()) {
					updateMrXLog(singleMove);
					return new MyGameState(setup, remaining, log, (mrX.use(singleMove.ticket).at(singleMove.destination)), detectives, currentRound);
				}
				else {
					int currentIndexOfPlayer = detectives.indexOf(currentPlayer);
					ArrayList<Player> tempList = new ArrayList<>(this.detectives);
					tempList.remove(currentPlayer);

					currentPlayer = currentPlayer.use(singleMove.ticket);
					if (currentPlayer.isDetective()) updateRemainingState(currentPlayer, false);

					currentPlayer = currentPlayer.at(singleMove.destination);
					tempList.add(currentIndexOfPlayer, currentPlayer);
					return new MyGameState(setup, remaining, log, mrX.give(singleMove.ticket), tempList, currentRound);
				}
			}, doubleMove -> {
				SingleMove SingleMove1 = new SingleMove(mrX.piece(), mrX.location(), doubleMove.ticket1, doubleMove.destination1);
				SingleMove SingleMove2 = new SingleMove(mrX.piece(), mrX.location(), doubleMove.ticket2, doubleMove.destination2);
				updateMrXLog(SingleMove1);
				MyGameState t = new MyGameState(setup, remaining, log, mrX.use(SingleMove1.ticket).at(SingleMove1.destination), detectives, currentRound + 1);
				t.updateMrXLog(SingleMove2);

				return new MyGameState(this.setup, this.remaining, t.log, t.mrX.use(SingleMove2.ticket).use(Ticket.DOUBLE).at(SingleMove2.destination), t.detectives, currentRound);
			} );
			return move.visit(visitorObject); //return type depending on what FunctionalVisitor type is
		}
	}

	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		// TODO
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives,0);
	}

}