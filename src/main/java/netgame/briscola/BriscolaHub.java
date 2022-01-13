package netgame.briscola;

import java.io.IOException;

import netgame.common.Hub;

/**
 * Keep the business logic in the server. The game state is keep here
 * 
 * @author anniel
 *
 */
public class BriscolaHub extends Hub {
	private BriscolaGameState brGameState;

	public BriscolaHub(int port) throws IOException {
		super(port);

	}

	/**
	 * Recieve a message from client, apply this message to game state and send a
	 * view to each player, segun su punto de vista
	 */
	protected void messageReceived(int playerID, Object message) {

		// it's a player move
		brGameState.applyMessage(playerID, message);
		sendToOne(playerID, brGameState.toViewState(playerID));
		sendToOne(3 - playerID, brGameState.toViewState(3 - playerID));
		delay(1000); // 1s for see both cards that was played
		if (brGameState.isTwoMoved()) {// then decide what win this turn
			brGameState.makeDecision();
			sendToOne(playerID, brGameState.toViewState(playerID));
			sendToOne(3 - playerID, brGameState.toViewState(3 - playerID));

			/**
			 * Insert a delay for make a animation in BriscolaWindow. I estimate
			 * the duration time of the animation to be about 1s, because I decide
			 * use 1100ms
			 * 
			 */
			delay(1100);
			brGameState.endTurn(); //end turn
			sendToOne(playerID, brGameState.toViewState(playerID));
			sendToOne(3 - playerID, brGameState.toViewState(3 - playerID));
		}


	}

	/**
	 * When to players connect, close serverSocket, send a msg to a individual
	 * client with what symbol play, then send to both client the gameState
	 */
	protected void playerConnected(int playerID) {
		int[] playerList = getPlayerList();
		if (playerList.length == 2) {
			shutdownServerSocket();
			brGameState = new BriscolaGameState();
			setAutoreset(true); // needed because some objects transmitted more than once
			// construct a view state for each player
			sendToOne(playerList[0], brGameState.toViewState(playerList[0]));
			sendToOne(playerList[1], brGameState.toViewState(playerList[1]));

		}
	}

	private void delay(int ms) {
		try {
			Thread.sleep(ms);
		} catch (InterruptedException e) {
		}
	}

}
