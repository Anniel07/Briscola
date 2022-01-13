package netgame.briscola;

import java.io.Serializable;

import baraja.Card;
import baraja.Deck;
import baraja.Hand;
import baraja.ValueCart;

/**
 * State of the game, the hub contains the state, each time a client make a move
 * the hub update this state and a copy is sent to each client, this copy can be
 * diffrent
 * 
 * @author anniel
 *
 */
public class BriscolaGameState implements Serializable {

	static final int HAND_SIZE = 3;

	public Hand hand1; // the hand of the player with id 1
	public Hand hand2; // the hand of the player with id 2

	public Card card1; // the card moved by player with id 1, null if not moved any
	public Card card2; // the card moved by player with id 2, null if not moved any

	public int points1; // score of the player with id 1
	public int points2; // score of the player with id 2

	public int wins1; // how many wins has player with id 1
	public int wins2; // how many wins has player with id 2

//	public final String ownPlayerName;
//	public final String oppPlayerName; // opponent player name

	public boolean gameInProgress; // True while a game is being played;
									// false before first game and between games.

	public boolean gameEndedInTie; // Tells whether the game ended in a tie.

	
	public int currentPlayer; // what player is going to move, its properly change after a game end

	private Deck deck;

	public Card arrastre; // card face up, meaning trump for one game

	public boolean[] posCardMiss1; // the position of the card missed by own
	public boolean[] posCardMiss2; // the position of the card missed by opponent

	public int playerWin; // what player win 1 or 2

	public int pointEarned; // point earned in one turn

	public boolean isDecision; // true if decision was taken when two cards are face up
	private int playerWhoWon; // player who won a whole game

	BriscolaGameState() {
		deck = new Deck();
		hand1 = new Hand();
		hand2 = new Hand();
		/**
		 * the first time the first player for move is selected at random
		 */
		currentPlayer = Math.random() < 0.5 ? 1 : 2;
		this.posCardMiss1 = new boolean[HAND_SIZE];
		this.posCardMiss2 = new boolean[HAND_SIZE];
		newGame();
	}

	/**
	 * Is called for begin a new game, this include begin the first game.
	 * This reset the needed variables states for begin a new game
	 */
	public void newGame() {
		deck = new Deck();
		deck.shuffle();
		hand1.clear();
		hand2.clear();
		
		//testEmpate(); //for test game ended in tie
		// assign cards to hand1 and hand2
		for (int i = 0; i < HAND_SIZE; i++) {
			hand1.addCard(deck.dealCard());
			hand2.addCard(deck.dealCard());
			posCardMiss1[i] = posCardMiss2[i] = false;
		}
		arrastre = deck.dealCard();
		
		//just for test with less cards
//		for (int i = 0; i < 44; i++) {
//			deck.dealCard();
//		}
		card1 = card2 = null;
		points1 = points2 = 0;
		gameInProgress = true;
		gameEndedInTie = false;
		isDecision = false;
		playerWhoWon = playerWin = -1;
	}
	/**
	 * Respond to a message that was sent by one of the players to the hub. Note
	 * that illegal messages (of the wrong type or coming at an illegal time) are
	 * simply ignored. The messages that are understood are the string "newgame" for
	 * starting a new game and an MovedCardMsg for a move that the user wants to
	 * make.
	 * 
	 * @param sender  the ID number of the player who sent the message.
	 * @param message the message that was received from that player.
	 */
	public void applyMessage(int sender, Object message) {

		if (gameInProgress && message instanceof MovedCardMsg && sender == currentPlayer) {
			MovedCardMsg movedCard = (MovedCardMsg) message;
			Card card = movedCard.card;
			int posM = movedCard.pos;
			if (deck != null && deck.getLeftCards() > 0 && movedCard.change) {
				//if the user want to to change 7 by arrastre
				if (currentPlayer == 1) {
					hand1.removeCard(posM);
					hand1.addCard(posM, arrastre);
				} else {
					hand2.removeCard(posM);
					hand2.addCard(posM, arrastre);
				}
				arrastre = card;
			} else if (!movedCard.change) {
				//make a simple move
				if (currentPlayer == 1) {
					posCardMiss1[posM] = true;
					card1 = card;
					hand1.removeCard(card);
				} else {
					posCardMiss2[posM] = true;
					card2 = card;
					hand2.removeCard(card);
				}
				if (card1 != null && card2 != null) {
					return;
				}

				currentPlayer = 3 - currentPlayer; // change the turn;
			}
		} else if (!gameInProgress && message.equals("newgame")) {
			newGame();
		}
	}
//

	public boolean isTwoMoved() {
		return card1 != null && card2 != null;
	}

	/**
	 * @Precondition card1!=null && card2!= null. 
	 * Decide what player wins this turn. Set pointEarned
	 */
	public void makeDecision() {
		isDecision = true;
		// decide quien se lleva la jugada
		Card serveCard, lastCard;
		if (currentPlayer == 1) {
			// el ultimo en jugar fue el player 1
			serveCard = card2;
			lastCard = card1;
		} else {
			serveCard = card1;
			lastCard = card2;
		}

		pointEarned = getBriscolaValue(lastCard) + getBriscolaValue(serveCard);
		
		if ((lastCard.getSuit().equals(serveCard.getSuit()) && valCard(lastCard) > valCard(serveCard))
				|| (!isArrastre(serveCard) && isArrastre(lastCard))) {
			// wins player that moved lastCard
			playerWin = currentPlayer;
		} else {
			// wins player that moved firstCard
			playerWin = 3 - currentPlayer;
		}

	}
	/**
	 * End the turn. And check if the game is Over
	 */
	public void endTurn() {
		if (playerWin == 1) {
			points1 += pointEarned;
			addCardinHole(hand1, posCardMiss1);
			addCardinHole(hand2, posCardMiss2);
		} else {
			points2 += pointEarned;
			addCardinHole(hand2, posCardMiss2);
			addCardinHole(hand1, posCardMiss1);
		}
		
		if (hand1.getSize() + hand2.getSize() == 0/*points1 + points2 == 120*/) {
			// the game end
			gameInProgress = false;
			// decide who won and who begin the new game, nota: el q gana empieza de sgundo
			if (points1 > points2) {
				wins1++;
				playerWhoWon = 1;
				currentPlayer = 2;
			} else if (points2 > points1) {
				wins2++;
				playerWhoWon = 2;
				currentPlayer = 1;
			} else {//ended in tie
				// el q termina empieza el proximo juego en caso de empate
				gameEndedInTie = true;
				playerWhoWon = -1;// no winner
				//note: when it's tie the last player in make a move is the current player for the next game
			}

		} else {
			currentPlayer = playerWin; // el q gana un turno la proxima vez es el q empieza
		}
		isDecision = false;
		card1 = card2 = null;

	}

	/**
	 * Create a view state for each player
	 * 
	 * @param playerID
	 * @return
	 */
	public BriscolaViewState toViewState(int playerID) {
		Hand ownHand;
		int ownPoints, oppPoints;
		String ownPlayerName, oppPlayerName;
		int ownwins, oppwins;
		int cardsLeft = deck != null ? deck.getLeftCards() : 0;
		int oppHandSize;
		Card ownCard, oppCard;
		boolean[] ownCardMissed, oppCardMissed;
		// players has id 1 or 2
		if (playerID == 1) {
			ownHand = hand1;
			ownPoints = points1;
			oppPoints = points2;
			ownPlayerName = "You";
			oppPlayerName = "Opponent";
			ownwins = wins1;
			oppwins = wins2;
			oppHandSize = hand2.getSize();
			ownCard = card1;
			oppCard = card2;
			ownCardMissed = posCardMiss1;
			oppCardMissed = posCardMiss2;
		} else {
			ownHand = hand2;
			ownPoints = points2;
			oppPoints = points1;
			ownPlayerName = "You";
			oppPlayerName = "Opponent";
			ownwins = wins2;
			oppwins = wins1;
			oppHandSize = hand1.getSize();
			ownCard = card2;
			oppCard = card1;
			ownCardMissed = posCardMiss2;
			oppCardMissed = posCardMiss1;
		}

		return new BriscolaViewState(ownHand, ownPoints, oppPoints, ownPlayerName, oppPlayerName, currentPlayer,
				ownwins, oppwins, cardsLeft, oppHandSize, arrastre, ownCard, oppCard, ownCardMissed, oppCardMissed,
				isDecision, playerWin, pointEarned, gameInProgress, playerWhoWon, gameEndedInTie);
	}
	
	/**
	 * Add a card when a turn end
	 * @param hand
	 * @param missing
	 */
	private void addCardinHole(Hand hand, boolean[] missing) {
		if (deck == null)
			return;
		Card card;
		if (deck.getLeftCards() == 0) {
			card = arrastre;
			deck = null;
		} else {
			card = deck.dealCard();
		}
		for (int i = 0; i < missing.length; i++) {
			if (missing[i]) {
				missing[i] = false;
				hand.addCard(i, card);
				return;
			}
		}
	}
	/**
	 * FOr get the point earned by that represent a card
	 * @param card
	 * @return
	 */
	private int getBriscolaValue(Card card) {
		switch (card.getValueCart()) {
		case JACK :return 2;
		case QUEEN :return 3;
		case KING :return 4;
		case THREE :return 10;
		case AS :return 11;
		default :return 0;
		}
	}

	private boolean isArrastre(Card card) {
		return card.getSuit().equals(arrastre.getSuit());
	}
	/**
	 * For get the value of a card in briscola game
	 * @param card
	 * @return
	 */
	private int valCard(Card card) {
		switch (card.getValueCart()) {
		case TWO :return 2;
		case FOR :return 4;
		case FIVE :return 5;
		case SIX :return 6;
		case SEVEN :return 7;
		case EIGTH :return 8;
		case NINE :return 9;
		case TEN :return 10;
		case JACK :return 11;
		case QUEEN :return 12;
		case KING :return 13;
		case THREE :return 14;
		case AS :return 15;
		default :return 0; // no default case cae aki
		}
	}

	/**
	 * Only for test
	 * This method always fill both hand with card that its earned points is 0,
	 * this propose is for test situation where the game ended in tie
	 * Nota: solo puede q cuando se reparta una vez cartas aparezca una carta con valor,
	 * pero generalmente las partidas terminan en empate
	 */
	private void testEmpate() {
		for (int i = 0; i < HAND_SIZE; i++) {
			hand1.addCard(assign());
			hand2.addCard(assign());
			posCardMiss1[i] = posCardMiss2[i] = false;
		}
		arrastre = assign();
		//skip card for only one distribution
		while(deck.getLeftCards() > 1) {
			deck.dealCard();
		}
	}

	/**
	 * Only for test
	 * @return
	 */
	private Card assign() {
		while(true) {
			Card c = deck.dealCard();
			if(c.getValueCart().ordinal() > ValueCart.TEN.ordinal() || c.getValueCart().equals(ValueCart.THREE)
					|| c.getValueCart().equals(ValueCart.AS))
				continue;
			return c;
		}
	}
}
