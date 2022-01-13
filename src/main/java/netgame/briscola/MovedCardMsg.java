package netgame.briscola;

import java.io.Serializable;

import baraja.Card;
/**
 * This class represent a message sent when a player moved a card or when 
 * change the 7 card by arrastre.
 * @author anniel
 *
 */
final class MovedCardMsg implements Serializable {
	final Card card;
	final int pos; //the position of the moved card. Can be always 0, 1 or 2 values
	final boolean change; // false when it's a move, true when a player want to change 7 by arrastre

	MovedCardMsg(Card card, int pos) {
		this(card, pos, false);
	}

	MovedCardMsg(Card card, int pos, boolean change) {
		this.card = card;
		this.pos = pos;
		this.change = change;
	}
}
