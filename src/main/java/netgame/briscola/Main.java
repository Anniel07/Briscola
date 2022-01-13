package netgame.briscola;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

/**
 * Main window
 * 
 * @author anniel
 *
 */
public class Main extends Application {

	public static void main(String[] args) {
		launch(args);

	}

	static final String fontFamily = "Times New Roman";
	static final int DEFAULT_PORT = 45209;
	
	RadioButton serverRd, clientRd;
	TextField listenport, connectport, computerTxt;
	Button okBtn, cancelBtn;
	Stage mainStage; // window that contain a form

	public void start(Stage primaryStage) {
		mainStage = primaryStage;
		Label welcomeLb = new Label("Welcome to Networked Briscola! ");
		welcomeLb.setFont(Font.font(fontFamily, FontWeight.BOLD, 24));

		serverRd = new RadioButton("Start a new game");
		serverRd.setFont(Font.font(fontFamily, FontWeight.BOLD, 16));
		clientRd = new RadioButton("Connect to a existing game");
		clientRd.setFont(Font.font(fontFamily, FontWeight.BOLD, 16));
		ToggleGroup toggle = new ToggleGroup();
		serverRd.setToggleGroup(toggle);
		clientRd.setToggleGroup(toggle);
		toggle.selectToggle(serverRd);
		/*
		 * Enable or disable the component depend of which selection
		 */
		toggle.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
			// System.out.println(newValue == null ); //for test
			if (newValue == serverRd) {
				computerTxt.setDisable(true);
				connectport.setDisable(true);
				listenport.setDisable(false);
			} else if (newValue == clientRd) {
				listenport.setDisable(true);
				computerTxt.setDisable(false);
				connectport.setDisable(false);
			}
		});

		listenport = new TextField("" + DEFAULT_PORT);
		listenport.setPrefColumnCount(5);
		connectport = new TextField("" + DEFAULT_PORT);
		connectport.setPrefColumnCount(5);
		connectport.setDisable(true);
		computerTxt = new TextField("localhost");
		computerTxt.setPrefColumnCount(25);
		computerTxt.setDisable(true);

		okBtn = new Button("OK");
		okBtn.setDefaultButton(true);
		okBtn.setOnAction(this::doOK);

		cancelBtn = new Button("Cancel");
		cancelBtn.setCancelButton(true);
		cancelBtn.setOnAction(this::doCancel);

		HBox buttons = new HBox(5, cancelBtn, okBtn);
		buttons.setAlignment(Pos.CENTER);
		buttons.setStyle("-fx-padding: 10px;");

		Label listenportLb = makeLb("Listen on port:");
		HBox listenRow = new HBox(5, listenportLb, listenport);
		HBox.setMargin(listenportLb, new Insets(0, 0, 0, 50));

		Label compLb = makeLb("Computer:");
		HBox computerRow = new HBox(5, compLb, computerTxt);
		HBox.setMargin(compLb, new Insets(0, 0, 0, 50));

		Label connectPortLb = makeLb("Port number:");
		HBox compPortRow = new HBox(5, connectPortLb, connectport);
		HBox.setMargin(connectPortLb, new Insets(0, 0, 0, 50));

		VBox top = new VBox(15, welcomeLb, serverRd, listenRow, clientRd, computerRow, compPortRow);

		top.setStyle("-fx-padding: 20px; -fx-border-color: black; -fx-border-width: 2px");
		BorderPane root = new BorderPane(top);
		BorderPane.setMargin(top, new Insets(15, 15, 0, 15));
		root.setBottom(buttons);

		primaryStage.setScene(new Scene(root));
		primaryStage.setTitle("Networked Briscola!");
		primaryStage.setResizable(false);
		primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/logo.png")));
		primaryStage.show();
	}

	/**
	 * Validate the form if true open a new window, if serverRd was selected create
	 * a serverSocket for wait for the second player connect; if clientRd was
	 * selected try to connect to the hub, if success set up for a new game, if not
	 * success the connection, show alert message and close the window; If the form
	 * is false show alert message for the user correct the fields
	 * 
	 * @param e
	 */
	private void doOK(ActionEvent e) {

		if (serverRd.isSelected()) {
			int port = getPort(listenport);
			if (port < 0) {
				Alert alert = Util
						.makeErrAlert("The port number should be a integer\n" + "in the range 0 and " + (1 << 16));
				alert.showAndWait();
				listenport.selectAll();
				listenport.requestFocus();
				return;
			}
			// try to create a serverSocket
			mainStage.close();
			new BriscolaWindow(port); // show the cruz cero window

		} else { // clientRd is selected
			String computer = computerTxt.getText().trim();
			if (computer.length() == 0) {
				Alert alert = Util.makeErrAlert(
						"You must enter the name or IP address\nof the computer that is hosting the game.");
				alert.showAndWait();
				computerTxt.requestFocus();
				return;
			}
			int port = getPort(connectport);
			if (port < 0) {
				Alert alert = Util
						.makeErrAlert("The port number should be a integer\n" + "in the range 0 and " + (1 << 16));
				alert.showAndWait();
				connectport.selectAll();
				connectport.requestFocus();
				return;
			}
			mainStage.close();
			// open a new window and try to connect to that computer
			new BriscolaWindow(computer, port); // show the cruz cero window
		}
	}

	private void doCancel(ActionEvent e) {
		Platform.exit();
	}

	private Label makeLb(String text) {
		Label lb = new Label(text);
		lb.setFont(Font.font(fontFamily, FontWeight.BOLD, 16));
		return lb;
	}

	/**
	 * 
	 * @param portTxt
	 * @return the port number associate to the text field, or -1 if the port is not
	 *         valid
	 */
	private int getPort(TextField portTxt) {
		String s = portTxt.getText();
		int port;
		try {
			port = Integer.parseInt(s);
			if (port < 0 || port >= (1 << 16))
				throw new NumberFormatException();
		} catch (NumberFormatException e) {
			port = -1;
		}
		return port;
	}

}
