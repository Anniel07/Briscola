package baraja;

import java.io.Serializable;
/**
 * Identify a card by Suit and valueCard, this class implement Serializable interface because
 * object of this type are sended by networking messages
 * @author anniel
 *
 */
public class Card implements Serializable {
	private final Suit suit;
	private final ValueCart valueCart;

	public Card(Suit suit, ValueCart valueCart) {
		this.suit = suit;
		this.valueCart = valueCart;
	}

	public Suit getSuit() {
		return suit;
	}

	public ValueCart getValueCart() {
		return valueCart;
	}

	
	public String getSuitAsString() {
		switch (suit) {
		case DIAMONDS: return "Diamonds";
		case HEARTS: return "Hearts";
		case CLUBS: return "Clubs";
		case SPADES: return "Spades";
		default:
			throw new IllegalArgumentException("Unknow value for: " + suit);
		}
		
	}

	public String getValueAsString() {

		switch (valueCart) {
		case AS : return "As";
		case JACK : return "Jack";
		case QUEEN : return "Queen";
		case KING : return "King";
		case JOKER : return "Joker";
		default : return String.valueOf(valueCart.ordinal() + 1);
		}

	}

	public String toString() {
		return getValueAsString() + " of " + getSuitAsString();
	}

	@Override
	public boolean equals(Object other) {
		if (other instanceof Card) {
			Card otherC = (Card) other;
			return this.suit == otherC.suit && this.valueCart == otherC.valueCart;
		}
		return false;

	}
}
