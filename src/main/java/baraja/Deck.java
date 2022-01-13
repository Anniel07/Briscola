package baraja;

import java.util.EnumSet;

public class Deck {
	private Card[] cards;
	private int usedCards; // position available for next card
	private final boolean hasJoker;

	public Deck() {
		this(false);
	}

	
	/**
	 * Add all cart of the deck
	 */
	public Deck(boolean hasJoker) {
		this.hasJoker = hasJoker;
		if (hasJoker) {
			cards = new Card[54]; // add 2 jokers
			cards[cards.length - 2] = new Card(Suit.DIAMONDS, ValueCart.JOKER);
			cards[cards.length - 1] = new Card(Suit.HEARTS, ValueCart.JOKER);
		} else {
			cards = new Card[52];
		}

		usedCards = 0;
		for (Suit suit : Suit.values()) {
			EnumSet<ValueCart> set = EnumSet.range(ValueCart.AS, ValueCart.KING);
			for (ValueCart vc : set) {
				cards[usedCards++] = new Card(suit, vc);
			}
		}
		usedCards = 0;// the next available card in the deck
	}

	/**
	 * Get the next card from the deck
	 * 
	 * @param pos
	 * @return
	 * @throws IllegalArgumentException if the position of the cart if not in it's
	 *                                  range
	 */

	public Card dealCard() {
		if (usedCards >= cards.length) {
			throw new IllegalStateException("The deck has not more cards");
		}
		Card card = cards[usedCards];
		usedCards++;
		return card;
	}

	/**
	 * Shuffle the deck, and set usedcards to 0
	 */
	public void shuffle() {
		for (int i = cards.length - 1; i > 0; i--) {
			int rand = (int) (Math.random() * (i + 1));
			Card temp = cards[rand];
			cards[rand] = cards[i];
			cards[i] = temp;
		}
		usedCards = 0;
	}

	public int getDeckSize() {
		return cards.length;
	}

	public int getLeftCards() {
		return getDeckSize() - usedCards;
	}

	public boolean hasJoker() {
		return hasJoker;
	}
}
