package application;

import javafx.fxml.FXML;

import javafx.scene.control.Button;

import javafx.event.ActionEvent;

import javafx.scene.control.Label;

import javafx.scene.layout.AnchorPane;

public class MainGUIController {
	@FXML
	private Label psaLabel;
	@FXML
	private Label versionLabel;
	@FXML
	private Button exitButton;
	@FXML
	private AnchorPane rootPane;

	@FXML
	private void exitApp(ActionEvent event) {
		event.consume();
		System.exit(0);
	}
}
