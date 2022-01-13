package netgame.briscola;

import java.io.Serializable;

import baraja.Card;
import baraja.Hand;

/**
 * This object is sended by the hub to each client for update it's state
 * 
 * @author anniel
 *
 */
public class BriscolaViewState implements Serializable {

	public final Hand ownHand; // own hand for a player
	public final int ownPoints; // score in this game for own player
	public final int oppPoints;// score in this game for opponent player
	public final String ownPlayerName;
	public final String oppPlayerName; // opponent player name
	public final int currentPlayer; // what it's the current player
	public final int ownwins; // how many game have won this player
	public final int oppwins;

	public final int cardsLeft; // how many card left are in deck
	public final int oppHandSize;
	public final Card arrastre; // what it's the card face up for trump

	public final Card ownCard; // the card moved by own Player
	public final Card oppCard; // the card moved by opponent Player

	public final boolean[] ownCardMissed; // the position of the card moved by own
	public final boolean[] oppCardMissed; // the position of the card moved by opponent
	public final boolean isDecision; // if the decision was taken by the hub when two players make a move, this
										// variable is used for make an animation after the two player maker his move
	public final int playerWin; //what player win this turn

	public final int pointEarned; //point earned by the player that win a turn

	public final boolean gameInProgress;
	public final int playerWhoWon; //player who won the a game
	public final boolean gameEndedInTie;

	public BriscolaViewState(Hand ownHand, int ownPoints, int oppPoints, String ownPlayerName, String oppPlayerName,
			int currentPlayer, int ownwinners, int oppwinners, int cardsLeft, int oppHandSize, Card arrastre,
			Card ownCard, Card oppCard, boolean[] ownPosCard, boolean[] oppPosCard, boolean isDecision, int playerWin,
			int pointEarned, boolean gameInProgress, int playerWhoWon, boolean gameEndedInTie) {

		this.ownHand = ownHand;
		this.ownPoints = ownPoints;
		this.oppPoints = oppPoints;
		this.ownPlayerName = ownPlayerName;
		this.oppPlayerName = oppPlayerName;
		this.currentPlayer = currentPlayer;
		this.ownwins = ownwinners;
		this.oppwins = oppwinners;
		this.cardsLeft = cardsLeft;
		this.oppHandSize = oppHandSize;
		this.arrastre = arrastre;
		this.ownCard = ownCard;
		this.oppCard = oppCard;
		this.ownCardMissed = ownPosCard;
		this.oppCardMissed = oppPosCard;
		this.isDecision = isDecision;
		this.playerWin = playerWin;
		this.pointEarned = pointEarned;
		this.gameInProgress = gameInProgress;
		this.playerWhoWon = playerWhoWon;
		this.gameEndedInTie = gameEndedInTie;
	}

}
