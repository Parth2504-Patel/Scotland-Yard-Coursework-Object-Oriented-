package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {
		class MyModel implements Model {
			private Board.GameState newGameState;
			private Set<Model.Observer> allObserver;

			private MyModel() {
				this.newGameState = new MyGameStateFactory().build(setup, mrX, detectives);
				this.allObserver = new HashSet<>();
			}

			@Override public Board getCurrentBoard() {
				return this.newGameState;
			}

			@Override public void registerObserver(@Nonnull Observer observer) {
				Objects.requireNonNull(observer);
				if(allObserver.contains(observer)) throw new IllegalArgumentException("Duplicate Observer!");
				allObserver.add(observer);
			}

			@Override public void unregisterObserver(@Nonnull Observer observer) {
				Objects.requireNonNull(observer);
				if(allObserver.isEmpty()) throw new IllegalArgumentException("List Is Empty!");
				if(!allObserver.contains(observer)) throw new IllegalArgumentException("Never was an observer!");
				allObserver.remove(observer);

			}

			@Override
			public ImmutableSet<Observer> getObservers() {
				return ImmutableSet.copyOf(allObserver);
			}

			public void announce(boolean WinnerDetermined) {
				if (WinnerDetermined) allObserver.forEach(observer -> observer.onModelChanged(this.newGameState, Observer.Event.GAME_OVER));
				else allObserver.forEach(observer -> observer.onModelChanged(this.newGameState, Observer.Event.MOVE_MADE));
			}

			@Override
			public void chooseMove(@Nonnull Move move) {
				this.newGameState = newGameState.advance(move);
				announce(!newGameState.getWinner().isEmpty());
			}
		}
		return new MyModel();
	}
}