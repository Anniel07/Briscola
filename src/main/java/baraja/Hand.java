package baraja;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * this class implement Serializable interface because
 * object of this type are sended by networking messages
 */
public class Hand implements Serializable {

	private ArrayList<Card> cards;

	public Hand() {
		cards = new ArrayList<Card>();
	}

	public void addCard(Card card) {
		if (card != null)
			cards.add(card);
	}
	/**
	 * INsert card at position pos
	 * @param pos
	 * @param card
	 */
	public void addCard(int pos, Card card) {
		if (card != null)
			cards.add(pos, card);
	}

	public Card getCard(int pos) {
		if (pos < 0 || pos >= cards.size())
			throw new IllegalArgumentException("Invalid card's position: " + pos);
		return cards.get(pos);
	}

	public void clear() {
		cards.clear();
	}

	public int getSize() {
		return cards.size();
	}

	public void removeCard(Card card) {
		cards.remove(card);
	}

	public void removeCard(int pos) {

		cards.remove(pos);
	}

	/**
	 * Sorts the cards in the hand so that cards of the same suit are grouped
	 * together, and within a suit the cards are sorted by value.
	 */
	public void sortBySuit() {
		// find minimun and add to the sorted list
		ArrayList<Card> sorted = new ArrayList<Card>();
		while (cards.size() > 0) {
			Card minSuit = cards.get(0);
			for (int i = 1; i < cards.size(); i++) {
				Card ci = cards.get(i);
				if (ci.getSuit().ordinal() < minSuit.getSuit().ordinal()) {
					// this has lowest suit
					minSuit = ci;
				} else if (ci.getSuit().ordinal() == minSuit.getSuit().ordinal()
						&& ci.getValueCart().ordinal() < minSuit.getValueCart().ordinal()) {
					// compare by value now
					minSuit = ci;
				}
			}
			sorted.add(minSuit);
			cards.remove(minSuit);
		}
		cards = sorted;
	}

	/**
	 * Sorts the cards in the hand so that cards are sorted into order of increasing
	 * value. Cards with the same value are sorted by suit. Note that aces are
	 * considered to have the lowest value.
	 */
	public void sortByValue() {
		ArrayList<Card> sorted = new ArrayList<Card>();
		while (cards.size() > 0) {
			Card minSuit = cards.get(0);
			for (int i = 1; i < cards.size(); i++) {
				Card ci = cards.get(i);
				if (ci.getValueCart().ordinal() < minSuit.getValueCart().ordinal()) {
					// this has lowest value
					minSuit = ci;
				} else if (ci.getValueCart().ordinal() == minSuit.getValueCart().ordinal()
						&& ci.getSuit().ordinal() < minSuit.getSuit().ordinal()) {
					// compare by suit now
					minSuit = ci;
				}
			}
			sorted.add(minSuit);
			cards.remove(minSuit);
		}
		cards = sorted;
	}
}
