package application;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

import javafx.application.Application;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import util.Hash;
import util.StringComparer;

public class Main extends Application {

	ResourceBundle locale;
	
	Parent root;
	Scene scene;
	AnchorPane rootPane;
	AnchorPane loginPane;
	AnchorPane searchPane;
	AnchorPane menuPane;
	AnchorPane passwordPane;
	AnchorPane writePane;
	AnchorPane deletePane;
	AnchorPane choosePasswordPane;
	AnchorPane chooseHintPane;
	AnchorPane securityNoticePane;
	private double xOffset = 0;
	private double yOffset = 0;
	private final String version = "v1.0";
	Hash hash;
	DataHandler dataHandler;
	private HashMap<String, String> dataMap;
	StringComparer sC;

	private boolean loggedIn = false;
	private String generalPassword = "";
	private String hint = "";
	private String salt = "4*0ou1uAXyoz*LzMsUvNbkTN";

	@Override
	public void start(Stage primaryStage) {
		try {
			hash = new Hash();
			dataHandler = new DataHandler();
			dataMap = dataHandler.getData();
			generalPassword = dataHandler.getPassword();
			hint = dataHandler.getHint();

			loadLocale(new Locale("en", "EN"));
			sC = new StringComparer();
			root = FXMLLoader.load(getClass().getClassLoader().getResource("MainGUI.fxml"), locale);
			scene = new Scene(root);
			rootPane = (AnchorPane) scene.lookup("#rootPane");

			loginPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Login.fxml"), locale);
			searchPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Search.fxml"), locale);
			menuPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Menu.fxml"), locale);
			passwordPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Password.fxml"), locale);
			deletePane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Delete.fxml"), locale);
			writePane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("Write.fxml"), locale);
			choosePasswordPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("ChoosePass.fxml"), locale);
			chooseHintPane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("ChooseHint.fxml"), locale);
			securityNoticePane = (AnchorPane) FXMLLoader.load(getClass().getClassLoader().getResource("SecurityNotice.fxml"), locale);

			if (generalPassword.equalsIgnoreCase("")) {
				rootPane.getChildren().setAll(choosePasswordPane);
			} else {
				rootPane.getChildren().setAll(loginPane);
			}

			root.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					xOffset = event.getSceneX();
					yOffset = event.getSceneY();
				}
			});
			root.setOnMouseDragged(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					primaryStage.setX(event.getScreenX() - xOffset);
					primaryStage.setY(event.getScreenY() - yOffset);
				}
			});

			scene.getStylesheets().add(getClass().getClassLoader().getResource("application.css").toExternalForm());
			primaryStage.initStyle(StageStyle.UNDECORATED);
			primaryStage.setResizable(false);
			primaryStage.setTitle("PSA");

			Label versionlabel = (Label) scene.lookup("#versionLabel");
			versionlabel.setText(version);
			PasswordField passwordTF = (PasswordField) scene.lookup("#passwordTF");

			versionlabel.setText(version);

			// KEY LISTENERS
			loginPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						tryLogin(passwordTF.getText() + salt);
					}
				}
			});
			searchPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						TextField searchTF = (TextField) searchPane.lookup("#searchTF");
						Label searchOutLabel = (Label) searchPane.lookup("#searchOutLabel");

						String key = sC.compare(dataMap, searchTF.getText());
						ArrayList<String> altKeys = sC.compareAlt(dataMap, key, searchTF.getText());
						
						if(searchTF.getText().startsWith("u:")) {
							String username = searchTF.getText().replace("u:", "");
							key = sC.findUser(dataMap, username);
							altKeys = sC.findAltUser(dataMap, key, username);
						}
						
						if (key != null) { 
							
							searchOutLabel.setText("");
							searchTF.setText("");
							String altKey = altKeys.stream().collect(Collectors.joining(", "));
							if(altKeys.isEmpty()) {
								altKey=null;
							}
							openPasswordPane(key, altKey);
							
							} else {
							String guess = sC.wordGuess(dataMap, searchTF.getText());
							if (guess != null) {
								searchOutLabel.setText("Do you mean " + guess + "?");
							} else {
								searchOutLabel.setText("No matches found.");
							}
						}
					}
				}
			});

			deletePane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						TextField deletePasswordTF = (TextField) deletePane.lookup("#deletePasswordTF");
						String key = sC.compare(dataMap, deletePasswordTF.getText());
						Label deleteOutLabel = (Label) deletePane.lookup("#deleteOutLabel");

						if (key != null) {
							dataMap.remove(key);
							dataHandler.setData(dataMap);

							deleteOutLabel.setText("The password for " + key + " was deleted.");
							deletePasswordTF.setText("");
						} else {
							deleteOutLabel.setText("");
						}
					}
				}
			});

			choosePasswordPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						TextField passwordInputTF = (TextField) choosePasswordPane.lookup("#passwordInputTF");
						if (!passwordInputTF.getText().equalsIgnoreCase("")) {
							generalPassword = Hash.sha256(passwordInputTF.getText() + salt);
							rootPane.getChildren().setAll(chooseHintPane);
						}
					}
				}
			});

			chooseHintPane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						TextField hintInputTF = (TextField) chooseHintPane.lookup("#hintInputTF");
						hint = hintInputTF.getText();

						dataHandler.setPassword(generalPassword, hint);
						loggedIn = true;
						rootPane.getChildren().setAll(securityNoticePane);
					}
				}
			});

			securityNoticePane.setOnKeyPressed(new EventHandler<KeyEvent>() {
				@Override
				public void handle(KeyEvent event) {
					if (event.getCode() == KeyCode.ENTER) {
						rootPane.getChildren().setAll(menuPane);
					}
				}
			});

			// BUTTONS
			Button backButton = (Button) root.lookup("#backButton");
			backButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					if (loggedIn) {
						rootPane.getChildren().setAll(menuPane);
					}
				}
			});

			Button searchButton = (Button) menuPane.lookup("#searchButton");
			searchButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					rootPane.getChildren().setAll(searchPane);
				}
			});

			Button writeButton = (Button) menuPane.lookup("#writeButton");
			writeButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Label writeOutLabel = (Label) writePane.lookup("#writeOutLabel");
					writeOutLabel.setText("");
					rootPane.getChildren().setAll(writePane);
				}
			});

			Button deleteButton = (Button) menuPane.lookup("#deleteButton");
			deleteButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Label deleteOutLabel = (Label) deletePane.lookup("#deleteOutLabel");
					deleteOutLabel.setText("");
					rootPane.getChildren().setAll(deletePane);
				}
			});

			Button saveEditingButton = (Button) passwordPane.lookup("#saveEditingButton");
			saveEditingButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					Label programNameLabel = (Label) passwordPane.lookup("#programNameLabel");
					TextField usernameTF = (TextField) passwordPane.lookup("#usernameTF");
					TextField passwordTF2 = (TextField) passwordPane.lookup("#passwordTF2");
					Label altMatchlabel = (Label) passwordPane.lookup("#altMatchlabel");
					String username = usernameTF.getText();
					if (username.equalsIgnoreCase("")) {
						username = "none";
					}
					altMatchlabel.setText("New data was saved.");
					dataMap.put(programNameLabel.getText(), username + ":" + passwordTF2.getText());
					dataHandler.setData(dataMap);
				}
			});

			Button saveWritingButton = (Button) writePane.lookup("#saveWritingButton");
			saveWritingButton.setOnAction(new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent e) {
					TextField programNameLabel = (TextField) writePane.lookup("#programNameWriteTF");
					TextField usernameTF = (TextField) writePane.lookup("#usernameWriteTF");
					TextField passwordTF = (TextField) writePane.lookup("#passwordWriteTF");
					String programName = programNameLabel.getText();
					String username = usernameTF.getText();
					String password = passwordTF.getText();

					Label writeOutLabel = (Label) writePane.lookup("#writeOutLabel");

					if (username.equalsIgnoreCase("")) {
						username = "none";
					}
					if (!programName.equalsIgnoreCase("") && !password.equalsIgnoreCase("")) {
						if(dataMap.containsKey(programName)) {
						writeOutLabel.setText("ID is already in use.");
						}else {
						writeOutLabel.setText("Password saved.");
						dataMap.put(programName, username + ":" + password);
						dataHandler.setData(dataMap);

						programNameLabel.setText("");
						usernameTF.setText("");
						passwordTF.setText("");
						}
					} else {
						writeOutLabel.setText("");
					}
				}
			});

			primaryStage.setScene(scene);
			primaryStage.show();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void tryLogin(String passwordInput) {
		Label loginOutLabel = (Label) loginPane.lookup("#loginOutLabel");
		Text hintOutText = (Text) loginPane.lookup("#hintOutText");

		if (generalPassword.equalsIgnoreCase(hash.sha256(passwordInput))) {
			loggedIn = true;
			loginOutLabel.setText("");
			hintOutText.setText("");
			rootPane.getChildren().setAll(menuPane);
		} else {
			loginOutLabel.setText("Wrong password.");
			if (!hint.equalsIgnoreCase("")) {
				hintOutText.setText("Hint: " + hint);
			}
		}
	}

	private void openPasswordPane(String key, String altKey) {
		rootPane.getChildren().setAll(passwordPane);
		Label programNameLabel = (Label) passwordPane.lookup("#programNameLabel");
		TextField usernameTF = (TextField) passwordPane.lookup("#usernameTF");
		TextField passwordTF2 = (TextField) passwordPane.lookup("#passwordTF2");
		Label altMatchlabel = (Label) passwordPane.lookup("#altMatchlabel");

		String username = dataMap.get(key).split(":")[0];
		String password = dataMap.get(key).split(":")[1];
		if (username.equalsIgnoreCase("none")) {
			username = "";
		}
		programNameLabel.setText(key);
		usernameTF.setText(username);
		passwordTF2.setText(password);

		if (altKey != null) {
			altMatchlabel.setText("Alternative match: " + altKey);
		} else {
			altMatchlabel.setText("");
		}
	}
	
	private void loadLocale(Locale loc) {
		locale = ResourceBundle.getBundle("LangBundle", loc);
	}

	public static void main(String[] args) {
		launch(args);
	}
}
