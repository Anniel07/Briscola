package netgame.briscola;

import java.io.IOException;

import baraja.Card;
import baraja.Hand;
import baraja.Suit;
import baraja.ValueCart;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;
import javafx.util.Pair;
import netgame.common.Client;

/**
 * The client connected to the hub, only two player are allowed. Player take
 * turn for make a move, the player who win a turn begin playing for the next
 * turn. If a client disconnect the game end.
 * <p>
 * Rules:
 * </p>
 * <ol>
 * <li>The player who lost a game begin playing the next game; when it's tie the
 * last player in make a move is the first player for the next game</li>
 * </ol>
 * 
 * @author anniel
 *
 */
public class BriscolaWindow extends Stage {
	static final String fontFamily = "Times New Roman";
	static final int paddX = 20;
	static final int paddY = 30;

	static final int HAND_SIZE = 3;
	static final int CARD_WIDTH = 79, CARD_HEIGTH = 123;
	static final int W = 4 * CARD_WIDTH + 5 * paddX, H = CARD_HEIGTH * 3 + 4 * paddY;

	static final double x7 = paddX + 0.5, y7 = 3 * paddY + 2 * CARD_HEIGTH + 0.5;
	/**
	 * The game area
	 */
	Canvas canvas;
	/**
	 * true if the two players are connected, otherwise false (if one player is
	 * disconnected is false too) and before established the connection between the
	 * two players
	 */
	volatile boolean twoConnected;

	Label msgLb; // message in the bottom

	String host;
	int port;
	String info; // info of the connection state, etc

	BriscolaClient briscolaClient; // connection from client to hub

	/**
	 * This variable not need be volatile because is updated in javafx thread. keep
	 * a copy of the gameState for each client. The copy is updated from the hub
	 */
	BriscolaViewState briscolaView;

	long myID; // myID 1 or 2

	Image imgCards;

	/**
	 * variables for animation in decision
	 */
	private double xxc = paddX + 2 * (CARD_WIDTH + paddX), yyc = 2 * paddY + CARD_HEIGTH;
	private final int moveSecond = 16;// move the card on right to the left
	private AnimationTimer decisionTimer;
	private double movX = 25; // for show earned point , point from
	// end animation

	public BriscolaWindow(int port) {
		this(null, port);
	}

	public BriscolaWindow(String host, int port) {

		try {
			imgCards = new Image("cards.png");
		} catch (Exception e) {
			Alert alert = Util.makeErrAlert("This program can't coninue. The file \"cards.png\" is misssing");
			System.out.println("Error: " + e);
			alert.showAndWait();
			System.exit(1);
		}
		this.host = host;
		this.port = port;
		canvas = new Canvas(W, H);
		canvas.setOnMouseClicked(this::doClick);
		draw();
		msgLb = new Label();
		msgLb.setAlignment(Pos.CENTER);
		msgLb.setMaxWidth(1000);
		msgLb.setFont(Font.font(fontFamily, FontWeight.BOLD, 16));
		msgLb.setPadding(new Insets(10));
		BorderPane root = new BorderPane(canvas);
		root.setBottom(msgLb);
		setScene(new Scene(root));
		setTitle("Networked Briscola");
		setOnHidden(e -> {
			if (twoConnected) { // disconnect a client cleanly
				twoConnected = false;
				briscolaClient.disconnect();
				try {
					Thread.sleep(500);
				} catch (InterruptedException e1) {
				}
			}
			System.exit(0); // needed for end the JVM
		});
		setResizable(false);
		setX(100 + 100 * Math.random());
		setY(100 + 50 * Math.random());
		getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
		show();
		new ConnectionHandler().start();
	}

	private void draw() {
		GraphicsContext g = canvas.getGraphicsContext2D();

		g.setFill(Color.BEIGE);
		g.fillRect(0, 0, W, H);
		g.setStroke(Color.DARKRED);
		g.setLineWidth(3);
		g.strokeRect(1.5, 1.5, W - 3, H - 3);
		if (briscolaView == null) {
			g.setFill(Color.BLACK);
			g.fillText(info, 20, 40);
		} else {
			drawBoard(g);
		}

	}

	/**
	 * Draw the board. On top of the window is draw the data of opponent player, on
	 * the bottom, "you" player
	 * 
	 * @param g
	 */
	private void drawBoard(GraphicsContext g) {
		if (briscolaView.gameInProgress) {
			//g.save();
			// draw arrastre card
			Card arrastre = briscolaView.arrastre;
			int xt = 3 * (paddX + CARD_WIDTH / 2);
			int yt = 2 * paddY + CARD_HEIGTH + (CARD_HEIGTH - CARD_WIDTH) / 2;
			g.translate(xt, yt);
			g.rotate(90);
			drawCard(arrastre, g, 0, 0);
			if (briscolaView.cardsLeft == 0) { // all card are distributed. Draw arrastre in gray color
				g.setFill(Color.rgb(0, 0, 0, 0.4));
				g.fillRect(0, 0, CARD_WIDTH, CARD_HEIGTH);
			}

			g.rotate(-90);
			g.translate(-xt, -yt);
			//g.restore();
		}
		// draw deck face down
		if (briscolaView.cardsLeft > 0) {
			double y = 2 * paddY + CARD_HEIGTH;
			double x = paddX;
			drawCard(null, g, x, y);
			// draw the number of remaining cards in the deck
			g.setFill(Color.rgb(100, 0, 0));
			g.setFont(Font.font("Arial", FontWeight.BOLD, 20));
			g.fillText("" + (briscolaView.cardsLeft + 1), x + 50, y + 25);
		}
		/**
		 * 123 * 3 + 4 * 20 = H 4 * 79 + 5 * 20 = A
		 */

		// draw opponent hand, face down
		for (int i = 0; i < HAND_SIZE; i++) {
			// drawCard(null , g, 2*padd + CARD_WIDTH + i * (CARD_WIDTH + padd) , padd);
			if (!briscolaView.oppCardMissed[i]) {
				drawCard(null, g, paddX + (i + 1) * (CARD_WIDTH + paddX), paddY);

			}

		}

		// and 3 face up, card dim 79 x 123
		Hand ownHand = briscolaView.ownHand;
		for (int i = 0, pos = 0; i < HAND_SIZE; i++) {
			if (!briscolaView.ownCardMissed[i]) {
				Card card = ownHand.getCard(pos);
				drawCard(card, g, paddX + (i + 1) * (CARD_WIDTH + paddX), H - paddY - CARD_HEIGTH);
				pos++;
			}
		}

		if (briscolaView.isDecision && decisionTimer == null) {
			// begin animation for moved the card to who win the turn and show the earned
			// points
			doDecisionAnim();
		} else if (briscolaView.isDecision && decisionTimer != null) {
			// make the animation on a different frame
			drawCard(briscolaView.ownCard, g, xxc, yyc);
			drawCard(briscolaView.oppCard, g, xxc + moveSecond, yyc);

			g.setFill(Color.GREEN);
			String text = "+" + briscolaView.pointEarned;
			if (briscolaView.playerWin == myID) {
				g.fillText(text, movX, 2 * (paddY + CARD_HEIGTH) + 20);

			} else {
				g.fillText(text, movX, paddY + CARD_HEIGTH + 20);
			}
		} else {
			// draw cards played in the game
			// draw opponent card played, always on right
			if (briscolaView.oppCard != null)
				drawCard(briscolaView.oppCard, g, paddX + 3 * (CARD_WIDTH + paddX), 2 * paddY + CARD_HEIGTH);

			// draw your card moved, always on left
			if (briscolaView.ownCard != null)
				drawCard(briscolaView.ownCard, g, paddX + 2 * (CARD_WIDTH + paddX), 2 * paddY + CARD_HEIGTH);
		}
		g.setFont(Font.font(null, FontWeight.BOLD, 16));
		// draw name of both players as well points and winners
		int topY = paddY - 8;
		int bottomY = H - 10;
		int middle = W / 2 - 20;
		int rigth = W - 68 - paddX;
		g.setFill(Color.GREEN);
		g.fillText(briscolaView.oppPlayerName, paddX + 20, topY);
		g.fillText(briscolaView.ownPlayerName, paddX + 20, bottomY);

		g.fillText("Points: " + briscolaView.oppPoints, middle, topY);
		g.fillText("Points: " + briscolaView.ownPoints, middle, bottomY);

		g.fillText("Wins: " + briscolaView.oppwins, rigth, topY);
		g.fillText("Wins: " + briscolaView.ownwins, rigth, bottomY);

		if (briscolaView.gameInProgress) {
			g.setFill(Color.rgb(100, 0, 0));
			// select opponent player in turn
			if (briscolaView.currentPlayer == myID) {
				// select own player in turn
				g.fillRect(7, H - 12 - 10, 20, 10);
				g.fillPolygon(new double[] { 27, 36, 27 }, new double[] { H - 7, H - 17, H - 27 }, 3);

			} else { // select opponent player
				g.fillRect(7, 12, 20, 10);
				g.fillPolygon(new double[] { 27, 36, 27 }, new double[] { 7, 17, 27 }, 3);
			}
		}
		// if hand contains 7 of arrastre, then paint a rectangle that will serve as
		// button by change the seven by arrastre
		if (myID == briscolaView.currentPlayer && briscolaView.cardsLeft > 0 && haveSeven(briscolaView.ownHand) >= 0) {
			g.setStroke(Color.BLACK);
			g.strokeRect(x7, y7, CARD_WIDTH, 30);
			g.setFill(Color.DARKRED);
			g.fillText("Change 7", x7 + 5, y7 + 20);
		}
	}

	private void drawCard(Card card, GraphicsContext g, double dx, double dy) {
		double sx, sy;
		if (card == null) {// draw a face down card
			sx = 2 * CARD_WIDTH;
			sy = 4 * CARD_HEIGTH;
		} else if (card.getValueCart() == ValueCart.JOKER) {
			if (card.getSuit() == Suit.HEARTS) {
				sx = 1 * CARD_WIDTH;
				sy = 4 * CARD_HEIGTH;
			} else { // is corazon Negro
				sx = 0;
				sy = 4 * CARD_HEIGTH;
			}
		} else {
			sx = card.getValueCart().ordinal() * CARD_WIDTH;
			sy = card.getSuit().ordinal() * CARD_HEIGTH;
		}
		g.drawImage(imgCards, sx, sy, CARD_WIDTH, CARD_HEIGTH, dx, dy, CARD_WIDTH, CARD_HEIGTH);

	}

	/**
	 * begin a animation for
	 */
	private void doDecisionAnim() {
		decisionTimer = new AnimationTimer() {
			private int dy = 6;
			private boolean endAnim;
			private long prevTime;

			@Override
			public void handle(long now) {
				if (now - prevTime > 0.95e9 / 60) {
					draw();
					movX += 7.6;

					if (briscolaView.playerWin == myID) {
						yyc += dy;
						if (yyc > H) {
							endAnim = true;
						}
					} else {
						yyc -= dy;
						if (yyc + CARD_HEIGTH < 0) {
							endAnim = true;
						}
					}
					if (endAnim) {
						// reset this variables
						endAnim = false;
						xxc = paddX + 2 * (CARD_WIDTH + paddX);
						yyc = 2 * paddY + CARD_HEIGTH;
						movX = 25;
						// end timer
						stop();
						decisionTimer = null;
					}
					prevTime = now;
				}
			}
		};
		decisionTimer.start();
	}

	/**
	 * Respond to a click event. Send msg to the Hub
	 * 
	 * @param e
	 */
	private void doClick(MouseEvent e) {
		if (briscolaView == null || !twoConnected) {
			return;
		}

		// System.out.println("Clicked: "); //test
		if (briscolaView.gameInProgress && myID == briscolaView.currentPlayer) {
			double x = e.getX(), y = e.getY();
			var pair = getCardIndex(x, y);
			if (pair != null && briscolaView.ownCard == null) {
				// System.out.println("Sended: ");//test
				briscolaClient.send(new MovedCardMsg(pair.getKey(), pair.getValue()));
				//System.out.println(pair.getKey() + " index: " + pair.getValue());
			} else if (briscolaView.cardsLeft > 0 && x > x7 && x < x7 + CARD_WIDTH && y > y7 && y < y7 + 30) {
				// check the area of the rectangle
				int p = haveSeven(briscolaView.ownHand);
				if (p >= 0)
					briscolaClient.send(new MovedCardMsg(briscolaView.ownHand.getCard(p), p, true));
			}
		}

		// begin a new game acorde to the rules #see BriscolaWindow
		else if (!briscolaView.gameInProgress ) {
			if( (briscolaView.gameEndedInTie && myID == briscolaView.currentPlayer) 
					|| (!briscolaView.gameEndedInTie && myID != briscolaView.playerWhoWon))
			briscolaClient.send("newgame");
		}

	}

	/**
	 * Get the position in the hand, when a player move a card
	 * 
	 * @param x
	 * @param y
	 * @return
	 */
	private Pair<Card, Integer> getCardIndex(double x, double y) {
		double xC;
		double yC = H - paddY - CARD_HEIGTH;
		for (int i = 0, pos = 0; i < HAND_SIZE; i++) {
			if (!briscolaView.ownCardMissed[i]) {
				xC = paddX + (i + 1) * (CARD_WIDTH + paddX);
				// check for rectangle area
				if (x >= xC && x <= xC + CARD_WIDTH && y >= yC && y <= yC + CARD_HEIGTH)
					return new Pair<Card, Integer>(briscolaView.ownHand.getCard(pos), i);
				pos++;
			}
		}
		return null;
	}

	/**
	 * Method that check if the hand contains 7 of arrastre. For change this card by
	 * arrastre
	 * 
	 * @param ownHand
	 * @return
	 */
	private int haveSeven(Hand ownHand) {
		for (int i = 0; i < ownHand.getSize(); i++) {
			Card card = ownHand.getCard(i);
			if (card.getValueCart().equals(ValueCart.SEVEN) && card.getSuit().equals(briscolaView.arrastre.getSuit())) {
				return i;
			}
		}
		return -1;
	}

	/**
	 * This method is called by BriscolaCLient object, this method run always on
	 * Javafx thread. Update the state of the game for a player point of view
	 */
	private void newState(BriscolaViewState briscolaView) {
		this.briscolaView = briscolaView;

		if (!briscolaView.gameInProgress) { //define when the game end
			setTitle("Game Over");
			if (briscolaView.gameEndedInTie) {
				if (briscolaView.currentPlayer == myID)
					msgLb.setText("Game ended in tie. Click to start again.");
				else
					msgLb.setText("Game ended in tie. Waiting for new game...");
			} else if (briscolaView.playerWhoWon == myID) {
				msgLb.setText("You won!  Waiting for new game...");
			} else {
				msgLb.setText("You lost. Click to start a new game.");
			}

		} else {
			setTitle("Networked Briscola");
			if (briscolaView.currentPlayer == myID) {
				msgLb.setText("Make your move");
			} else {
				msgLb.setText("Waiting for opponent's move");
			}
		}
		draw();

	}

	/**
	 * if this program is running as server, first create the server Hub. Then
	 * create the client to connect to the hub. After established the connection the
	 * thread end
	 * 
	 * @author anniel
	 *
	 */
	class ConnectionHandler extends Thread {
		public void run() {
			if (host == null) { // this window is running as a server
				host = "localhost";
				try {
					new BriscolaHub(port);
				} catch (IOException e) {
					Platform.runLater(() -> {
						Alert alert = Util.makeErrAlert(
								"Sorry, could not connect to\n" + host + " on port " + port + "\nShutting down.");
						alert.showAndWait();
						Platform.exit();
					});
					return;
				}

			}
			info = "Connecting...\n";
			Platform.runLater(() -> {
				msgLb.setText("Waiting for connection...");
				draw();
			});
			BriscolaClient conn;
			try {
				conn = new BriscolaClient(host, port);
			} catch (IOException e) {
				Platform.runLater(() -> {
					Alert alert = Util.makeErrAlert(
							"Sorry, could not connect to\n" + host + " on port " + port + "\nShutting down.");
					alert.showAndWait();
					Platform.exit();
				});
				return;
			}
			int id = conn.getID();
			Platform.runLater(() -> {
				briscolaClient = conn;
				myID = id;
				msgLb.setText("Waiting for two players to connect.");
				info = "Starting up.";
				draw(); // draw the board
			});

		}
	}

	/**
	 * Class for make the connection to the hub.
	 *
	 */
	class BriscolaClient extends Client {

		public BriscolaClient(String hubHostName, int hubPort) throws IOException {
			super(hubHostName, hubPort);

		}

		/**
		 * Process a message from the hub. A msg is of type BriscolaViewState for update
		 * in each client the state of the game
		 */
		@Override
		protected void messageReceived(Object message) {
			if (message instanceof BriscolaViewState) {
				twoConnected = true; // if a player receive a BriscolaViewState message is because the two players
										// are connected
				Platform.runLater(() -> newState((BriscolaViewState) message));
			}

		}

		protected void playerDisconnected(int departingPlayerID) {
			twoConnected = false;
			briscolaClient.disconnect(); // disconnect this client cleanly
			Platform.runLater(() -> {
				Alert alert = Util.makeErrAlert("Your opponent has disconnected.\nThe game is ended.");
				alert.showAndWait();
				System.exit(0);
			});
		}

		protected void connectionClosedByError(String message) {
			if (!twoConnected)
				return;
			twoConnected = false;
			Platform.runLater(() -> {
				Alert alert = Util.makeErrAlert("Connection lost.\nThe game is ended.");
				alert.showAndWait();
				System.exit(0);
			});
		}
	}
}
