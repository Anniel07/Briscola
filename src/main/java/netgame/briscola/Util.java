package netgame.briscola;

import javafx.scene.control.Alert;

public class Util {
	public static Alert makeErrAlert(String content) {
		Alert alert = new Alert(Alert.AlertType.ERROR);
		alert.setTitle("Error");
		alert.setHeaderText("Error");
		alert.setContentText(content);
		return alert;
	}
}
