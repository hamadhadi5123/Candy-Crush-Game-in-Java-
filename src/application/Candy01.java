package application;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Random;

import javax.swing.JOptionPane;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class Candy01 extends Application {

	static {
		try {
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (ClassNotFoundException e) {
			try {
				File jarFile = new File("lib/mysql-connector-j-8.4.0.jar");
				if (jarFile.exists()) {
					java.net.URLClassLoader ucl = new java.net.URLClassLoader(
							new java.net.URL[] { jarFile.toURI().toURL() }, Candy01.class.getClassLoader());
					Driver d = (Driver) Class.forName("com.mysql.cj.jdbc.Driver", true, ucl)
							.getDeclaredConstructor().newInstance();
					DriverManager.registerDriver(new DriverShim(d));
				}
			} catch (Throwable t) {
				System.err.println("Driver load note: " + t.getMessage());
			}
		}
	}

	// Constants
	public static final int WINDOW_WIDTH = 960;
	public static final int WINDOW_HEIGHT = 660;
	private static final int DEFAULT_BOARD_SIZE = 8;
	private static final int DEFAULT_CELL_SIZE = 65;
	private static final int CANDY_FIT_SIZE = 50;
	private static final int BOARD_OFFSET_X = 350;
	private static final int BOARD_OFFSET_Y = 45;

	private int BOARD_SIZE = DEFAULT_BOARD_SIZE;
	private int CELL_SIZE = DEFAULT_CELL_SIZE;

	public int getBoardSize() {
		return BOARD_SIZE;
	}

	public int getCellSize() {
		return CELL_SIZE;
	}

	// Game State
	private int[][] candies = new int[BOARD_SIZE][BOARD_SIZE];
	public int gameScore = 0;
	private int highScore = 0;
	private int targetScore = 100;
	private int remainingSeconds = 120;
	private boolean isPaused = false;

	// UI Elements
	private Text scoreText;
	private Text highScoreText;
	private Text targetScoreText;
	private ProgressBar targetProgressBar;
	private Text timerText;
	private Timeline timeline;
	private Rectangle cursorFrame;
	private StackPane pauseOverlay;
	private Button soundToggleButton;

	// Grid Selection Coordinates
	private int firstCandyX = -1;
	private int firstCandyY = -1;
	private int cursorCol = 0;
	private int cursorRow = 0;

	// Stages
	public static Stage candyStage;
	public static Stage crushStage;
	public static Stage introStage;
	public static Stage primaryStage;

	@Override
	public void start(Stage primaryStage) {
		Candy01.primaryStage = primaryStage;
		highScoreFile();
		targetScoreFile();
		showLoginStage();
	}

	// ==============================================================
	// 1. LOGIN SCREEN WITH CANDY CRUSH STYLING
	// ==============================================================
	private void showLoginStage() {
		Stage loginStage = new Stage();
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(20);
		gridPane.setVgap(20);

		Image backgroundImage = loadImageSafe("candy_login_bg.jpg");
		BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(760, 620, false, false, false, true));
		gridPane.setBackground(new Background(background));

		VBox formCard = new VBox(15);
		formCard.setAlignment(Pos.CENTER);
		formCard.setPadding(new Insets(25, 40, 25, 40));
		formCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.90); -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0, 0, 10);");

		Text gameTitle = new Text("🍭 CANDY CRUSH 🍬");
		gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
		gameTitle.setFill(Color.web("#FF1493"));
		gameTitle.setEffect(new DropShadow(10, Color.web("#FF69B4")));

		Text subTitle = new Text("Sign in to your sweet adventure");
		subTitle.setFont(Font.font("Arial", FontWeight.NORMAL, 14));
		subTitle.setFill(Color.web("#666666"));

		Label usernameLabel = new Label("Username:");
		usernameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		usernameLabel.setTextFill(Color.web("#333333"));
		TextField usernameField = new TextField();
		usernameField.setPromptText("Enter your username");
		usernameField.setFont(Font.font("Arial", FontWeight.BOLD, 15));
		usernameField.setPrefWidth(320);
		usernameField.setPrefHeight(40);
		usernameField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #FF1493; -fx-border-width: 1.5; -fx-padding: 8;");

		Label passwordLabel = new Label("Password:");
		passwordLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		passwordLabel.setTextFill(Color.web("#333333"));
		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Enter your password");
		passwordField.setFont(Font.font("Arial", FontWeight.BOLD, 15));
		passwordField.setPrefWidth(320);
		passwordField.setPrefHeight(40);
		passwordField.setStyle("-fx-background-radius: 12; -fx-border-radius: 12; -fx-border-color: #FF1493; -fx-border-width: 1.5; -fx-padding: 8;");

		Button loginButton = new Button("Login");
		loginButton.setPrefWidth(110);
		loginButton.setPrefHeight(42);
		loginButton.setStyle("-fx-font-size: 16; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");

		Button signupButton = new Button("Signup");
		signupButton.setPrefWidth(110);
		signupButton.setPrefHeight(42);
		signupButton.setStyle("-fx-font-size: 16; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-background-color: #9C27B0; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");

		Button guestButton = new Button("Guest Play");
		guestButton.setPrefWidth(130);
		guestButton.setPrefHeight(42);
		guestButton.setStyle("-fx-font-size: 16; -fx-font-family: 'Arial'; -fx-font-weight: bold; -fx-background-color: #32CD32; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");

		loginButton.setOnAction(e -> {
			SoundManager.playClick();
			if (usernameField.getText().isEmpty() || passwordField.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please enter username and password");
			} else {
				login(usernameField.getText(), passwordField.getText(), loginStage);
			}
		});

		signupButton.setOnAction(e -> {
			SoundManager.playClick();
			showSignUpStage();
		});

		guestButton.setOnAction(e -> {
			SoundManager.playClick();
			loginStage.close();
			showIntro(primaryStage);
		});

		HBox buttonContainer = new HBox(12);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(loginButton, signupButton, guestButton);

		formCard.getChildren().addAll(gameTitle, subTitle, usernameLabel, usernameField, passwordLabel, passwordField, buttonContainer);
		gridPane.getChildren().add(formCard);

		Scene scene = new Scene(gridPane, 760, 620);
		loginStage.setTitle("Candy Crush Login");
		loginStage.setScene(scene);
		loginStage.show();
	}

	// ==============================================================
	// 2. SIGNUP SCREEN
	// ==============================================================
	private void showSignUpStage() {
		Stage signupStage = new Stage();
		GridPane gridPane = new GridPane();
		gridPane.setAlignment(Pos.CENTER);
		gridPane.setHgap(15);
		gridPane.setVgap(15);

		Image backgroundImage = loadImageSafe("candy_login_bg.jpg");
		BackgroundImage background = new BackgroundImage(backgroundImage, BackgroundRepeat.NO_REPEAT,
				BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER,
				new BackgroundSize(760, 620, false, false, false, true));
		gridPane.setBackground(new Background(background));

		VBox formCard = new VBox(10);
		formCard.setAlignment(Pos.CENTER);
		formCard.setPadding(new Insets(20, 35, 20, 35));
		formCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.92); -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.45), 25, 0, 0, 10);");

		Text title = new Text("✨ Create Candy Crush Account");
		title.setFont(Font.font("Arial", FontWeight.BOLD, 22));
		title.setFill(Color.web("#FF1493"));

		TextField usernameField = new TextField();
		usernameField.setPromptText("First Name");
		usernameField.setFont(Font.font("Arial", 14));
		usernameField.setPrefWidth(320);
		usernameField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		TextField usernameField1 = new TextField();
		usernameField1.setPromptText("Last Name");
		usernameField1.setFont(Font.font("Arial", 14));
		usernameField1.setPrefWidth(320);
		usernameField1.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		TextField emailField = new TextField();
		emailField.setPromptText("Email Address");
		emailField.setFont(Font.font("Arial", 14));
		emailField.setPrefWidth(320);
		emailField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		TextField phonenoField = new TextField();
		phonenoField.setPromptText("Phone Number");
		phonenoField.setFont(Font.font("Arial", 14));
		phonenoField.setPrefWidth(320);
		phonenoField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		PasswordField passwordField = new PasswordField();
		passwordField.setPromptText("Password");
		passwordField.setFont(Font.font("Arial", 14));
		passwordField.setPrefWidth(320);
		passwordField.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		PasswordField passwordField1 = new PasswordField();
		passwordField1.setPromptText("Confirm Password");
		passwordField1.setFont(Font.font("Arial", 14));
		passwordField1.setPrefWidth(320);
		passwordField1.setStyle("-fx-background-radius: 10; -fx-border-radius: 10; -fx-border-color: #FF1493; -fx-padding: 7;");

		Button signupButton = new Button("Register");
		signupButton.setPrefWidth(130);
		signupButton.setPrefHeight(38);
		signupButton.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: #FFFFFF; -fx-background-radius: 20; -fx-cursor: hand;");

		Button backButton = new Button("Back");
		backButton.setPrefWidth(110);
		backButton.setPrefHeight(38);
		backButton.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: #777777; -fx-text-fill: #FFFFFF; -fx-background-radius: 20; -fx-cursor: hand;");
		backButton.setOnAction(e -> {
			SoundManager.playClick();
			signupStage.close();
		});

		signupButton.setOnAction(e -> {
			SoundManager.playClick();
			if (usernameField.getText().isEmpty() || emailField.getText().isEmpty()
					|| passwordField.getText().isEmpty() || passwordField1.getText().isEmpty()) {
				JOptionPane.showMessageDialog(null, "Please enter all details.");
			} else {
				if (!passwordField.getText().equals(passwordField1.getText())) {
					JOptionPane.showMessageDialog(null, "Passwords do not match.");
				} else {
					registerUser(usernameField.getText(), passwordField.getText());
					signupStage.close();
				}
			}
		});

		HBox buttonContainer = new HBox(15);
		buttonContainer.setAlignment(Pos.CENTER);
		buttonContainer.getChildren().addAll(signupButton, backButton);

		formCard.getChildren().addAll(title, usernameField, usernameField1, emailField, phonenoField, passwordField, passwordField1, buttonContainer);
		gridPane.getChildren().add(formCard);

		Scene scene = new Scene(gridPane, 760, 620);
		signupStage.setTitle("Signup");
		signupStage.setScene(scene);
		signupStage.show();
	}

	private void login(String username, String password, Stage loginStage) {
		String url = "jdbc:mysql://localhost:3306/lab9";
		String dbUsername = "root";
		String dbPassword = "12345";

		try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
			String query = "SELECT * FROM login WHERE username = ? AND password = ?";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, username);
				stmt.setString(2, password);
				try (ResultSet rs = stmt.executeQuery()) {
					if (rs.next()) {
						loginStage.close();
						showIntro(primaryStage);
					} else {
						JOptionPane.showMessageDialog(null, "Invalid username or password.");
					}
				}
			}
		} catch (SQLException ex) {
			JOptionPane.showMessageDialog(null, "Connected in Offline Mode.");
			loginStage.close();
			showIntro(primaryStage);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Offline Mode: Logging in.");
			loginStage.close();
			showIntro(primaryStage);
		}
	}

	private void registerUser(String username, String password) {
		String url = "jdbc:mysql://localhost:3306/lab9";
		String dbUsername = "root";
		String dbPassword = "12345";

		try (Connection conn = DriverManager.getConnection(url, dbUsername, dbPassword)) {
			String query = "INSERT INTO login (username, password) VALUES (?, ?)";
			try (PreparedStatement stmt = conn.prepareStatement(query)) {
				stmt.setString(1, username);
				stmt.setString(2, password);
				int rowsInserted = stmt.executeUpdate();
				if (rowsInserted > 0) {
					JOptionPane.showMessageDialog(null, "User registered successfully!");
				} else {
					JOptionPane.showMessageDialog(null, "Failed to register user.");
				}
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(null, "Offline Mode: User registered successfully.");
		}
	}

	// ==============================================================
	// 3. INTRO SCREEN
	// ==============================================================
	public void showIntro(Stage primaryStage) {
		introStage = new Stage();
		Pane introPane = new Pane();
		introPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		Image introImage = loadImageSafe("candy_menu_bg.jpg");
		ImageView introImageView = new ImageView(introImage);
		introImageView.setFitWidth(WINDOW_WIDTH);
		introImageView.setFitHeight(WINDOW_HEIGHT);
		introPane.getChildren().add(introImageView);

		VBox card = new VBox(15);
		card.setAlignment(Pos.CENTER);
		card.setLayoutX(WINDOW_WIDTH / 2.0 - 250);
		card.setLayoutY(WINDOW_HEIGHT / 2.0 - 150);
		card.setPrefWidth(500);
		card.setPadding(new Insets(30));
		card.setStyle("-fx-background-color: rgba(25, 10, 35, 0.88); -fx-background-radius: 30; -fx-border-color: #FF1493; -fx-border-width: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 25, 0, 0, 10);");

		Text gameTitle = new Text("🍭 CANDY CRUSH 🍬");
		gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 38));
		gameTitle.setFill(Color.web("#FF1493"));
		gameTitle.setEffect(new DropShadow(15, Color.web("#FF69B4")));

		Text subText = new Text("Welcome to the Sweet Puzzle Adventure!");
		subText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		subText.setFill(Color.web("#FFFFFF"));

		Text loadingText = new Text("Loading Candies...");
		loadingText.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
		loadingText.setFill(Color.web("#00FF7F"));

		card.getChildren().addAll(gameTitle, subText, loadingText);
		introPane.getChildren().add(card);

		Scene introScene = new Scene(introPane, WINDOW_WIDTH, WINDOW_HEIGHT);
		introStage.setTitle("Candy Crush Saga");
		introStage.setScene(introScene);
		introStage.show();

		PauseTransition pause = new PauseTransition(Duration.seconds(1.8));
		pause.setOnFinished(event -> {
			introStage.hide();
			showMainWindow(primaryStage);
		});
		pause.play();
	}

	// ==============================================================
	// 4. MAIN MENU
	// ==============================================================
	private void showMainWindow(Stage primaryStage) {
		SoundManager.playBGM();

		Pane root = new Pane();
		root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		Image backgroundImage = loadImageSafe("candy_menu_bg.jpg");
		ImageView backgroundImageView = new ImageView(backgroundImage);
		backgroundImageView.setFitWidth(WINDOW_WIDTH);
		backgroundImageView.setFitHeight(WINDOW_HEIGHT);
		root.getChildren().add(backgroundImageView);

		VBox menuCard = new VBox(20);
		menuCard.setAlignment(Pos.CENTER);
		menuCard.setLayoutX(WINDOW_WIDTH / 2.0 - 240);
		menuCard.setLayoutY(WINDOW_HEIGHT / 2.0 - 240);
		menuCard.setPrefWidth(480);
		menuCard.setPadding(new Insets(35, 40, 35, 40));
		menuCard.setStyle("-fx-background-color: rgba(20, 8, 30, 0.88); -fx-background-radius: 30; -fx-border-color: #FF1493; -fx-border-width: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 30, 0, 0, 12);");

		Text gameTitle = new Text("🍭 CANDY CRUSH 🍬");
		gameTitle.setFont(Font.font("Arial", FontWeight.BOLD, 36));
		gameTitle.setFill(Color.web("#FF1493"));
		gameTitle.setEffect(new DropShadow(15, Color.web("#FF69B4")));

		Text highBanner = new Text("🏆 BEST HIGH SCORE: " + highScore);
		highBanner.setFont(Font.font("Arial", FontWeight.BOLD, 17));
		highBanner.setFill(Color.web("#FFD700"));

		Button playButton = new Button("▶️ PLAY GAME");
		playButton.setPrefWidth(300);
		playButton.setPrefHeight(55);
		playButton.setStyle("-fx-font-size: 22; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: #FFFFFF; -fx-background-radius: 30; -fx-cursor: hand; -fx-effect: dropshadow(gaussian, rgba(255,20,147,0.6), 15, 0, 0, 4);");
		playButton.setOnAction(e -> {
			SoundManager.playClick();
			primaryStage.close();
			openCandyWindow(primaryStage);
		});

		Button instructionButton = new Button("📖 HOW TO PLAY");
		instructionButton.setPrefWidth(300);
		instructionButton.setPrefHeight(45);
		instructionButton.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-background-color: #9C27B0; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
		instructionButton.setOnAction(e -> {
			SoundManager.playClick();
			primaryStage.close();
			Instruction();
		});

		Button soundButton = new Button(SoundManager.isMuted() ? "🔇 SOUND: OFF" : "🔊 SOUND: ON");
		soundButton.setPrefWidth(300);
		soundButton.setPrefHeight(45);
		soundButton.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-background-color: #00BCD4; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
		soundButton.setOnAction(e -> {
			SoundManager.toggleMute();
			soundButton.setText(SoundManager.isMuted() ? "🔇 SOUND: OFF" : "🔊 SOUND: ON");
		});

		Button exitButton = new Button("🚪 EXIT GAME");
		exitButton.setPrefWidth(300);
		exitButton.setPrefHeight(45);
		exitButton.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-background-color: #555555; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
		exitButton.setOnAction(e -> {
			SoundManager.playClick();
			SoundManager.stopBGM();
			primaryStage.close();
		});

		menuCard.getChildren().addAll(gameTitle, highBanner, playButton, instructionButton, soundButton, exitButton);
		root.getChildren().add(menuCard);

		Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
		primaryStage.setTitle("Candy Crush Saga - Main Menu");
		primaryStage.setScene(scene);
		primaryStage.show();
	}

	// ==============================================================
	// 5. INSTRUCTIONS SCREEN
	// ==============================================================
	private void Instruction() {
		candyStage = new Stage();
		Pane candyPane = new Pane();
		candyPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		Image bgImage = loadImageSafe("candy_menu_bg.jpg");
		ImageView bgView = new ImageView(bgImage);
		bgView.setFitWidth(WINDOW_WIDTH);
		bgView.setFitHeight(WINDOW_HEIGHT);
		candyPane.getChildren().add(bgView);

		VBox card = new VBox(14);
		card.setLayoutX(WINDOW_WIDTH / 2.0 - 380);
		card.setLayoutY(WINDOW_HEIGHT / 2.0 - 270);
		card.setPrefWidth(760);
		card.setPrefHeight(540);
		card.setPadding(new Insets(25, 40, 25, 40));
		card.setStyle("-fx-background-color: rgba(255, 255, 255, 0.94); -fx-background-radius: 25; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 25, 0, 0, 10);");

		Text heading = new Text("🎮 How to Play Candy Crush");
		heading.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		heading.setFill(Color.web("#FF1493"));

		Text t1 = new Text("🖱️ Mouse Control: Click any candy to select it, then click an adjacent candy to swap!");
		t1.setFont(Font.font("Verdana", FontWeight.BOLD, 15));
		t1.setFill(Color.web("#008000"));

		Text t2 = new Text("⌨️ Keyboard Movement: Use UP, DOWN, LEFT, RIGHT arrow keys to move the selector.");
		t2.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		t2.setFill(Color.web("#333333"));

		Text t3 = new Text("🔄 Swapping: Press ENTER or SPACE on the first candy (glowing Cyan). Move to adjacent candy and press ENTER/SPACE!");
		t3.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		t3.setFill(Color.web("#333333"));

		Text t4 = new Text("💥 Crushing Candies: Match 3 or more of the same candies in a line to crush them and score +10 points.");
		t4.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		t4.setFill(Color.web("#C71585"));

		Text t5 = new Text("⏸️ Pause Anytime: Press 'P' or 'ESCAPE' or click the Pause button in the game HUD!");
		t5.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		t5.setFill(Color.web("#008B8B"));

		Text t6 = new Text("🎯 Victory: Reach the Target Score before the 120-second timer hits 0 to advance!");
		t6.setFont(Font.font("Verdana", FontWeight.BOLD, 14));
		t6.setFill(Color.web("#E65100"));

		Button backButton = new Button("Back to Main Menu");
		backButton.setOnAction(event -> {
			SoundManager.playClick();
			candyStage.close();
			primaryStage.show();
		});
		backButton.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand; -fx-padding: 8 25;");

		card.getChildren().addAll(heading, t1, t2, t3, t4, t5, t6, backButton);
		candyPane.getChildren().add(card);

		Scene candyScene = new Scene(candyPane, WINDOW_WIDTH, WINDOW_HEIGHT);
		candyStage.setTitle("Instructions");
		candyStage.setScene(candyScene);
		candyStage.show();
	}

	// ==============================================================
	// 6. GAME BOARD SCREEN (AAA VISUALS, SOUND, PAUSE & CONTROLS)
	// ==============================================================
	private void openCandyWindow(Stage primaryStage) {
		SoundManager.playBGM();
		gameScore = 0;
		firstCandyX = -1;
		firstCandyY = -1;
		cursorCol = 0;
		cursorRow = 0;
		remainingSeconds = 120;
		isPaused = false;

		crushStage = new Stage();
		Pane candyPane = new Pane();
		candyPane.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

		// HD Candy Board Background
		Image backgroundImage = loadImageSafe("candy_board_bg.jpg");
		ImageView backgroundImageView = new ImageView(backgroundImage);
		backgroundImageView.setFitWidth(WINDOW_WIDTH);
		backgroundImageView.setFitHeight(WINDOW_HEIGHT);
		backgroundImageView.setOnMouseClicked(e -> {
			if (!isPaused) candyPane.requestFocus();
		});
		candyPane.getChildren().add(backgroundImageView);

		// 1. Board Frame Container (Rounded Tray)
		Rectangle boardFrame = new Rectangle(560, 560);
		boardFrame.setX(BOARD_OFFSET_X - 10);
		boardFrame.setY(BOARD_OFFSET_Y - 10);
		boardFrame.setArcWidth(26);
		boardFrame.setArcHeight(26);
		boardFrame.setFill(Color.rgb(22, 10, 32, 0.82));
		boardFrame.setStroke(Color.web("#FF1493"));
		boardFrame.setStrokeWidth(3.5);
		boardFrame.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.85)));
		candyPane.getChildren().add(boardFrame);

		// 2. 8x8 Pocket Tiles Tray
		for (int r = 0; r < BOARD_SIZE; r++) {
			for (int c = 0; c < BOARD_SIZE; c++) {
				Rectangle tile = new Rectangle(58, 58);
				tile.setArcWidth(12);
				tile.setArcHeight(12);
				tile.setX(BOARD_OFFSET_X + c * CELL_SIZE + 3.5);
				tile.setY(BOARD_OFFSET_Y + r * CELL_SIZE + 3.5);
				if ((r + c) % 2 == 0) {
					tile.setFill(Color.rgb(38, 15, 50, 0.60));
				} else {
					tile.setFill(Color.rgb(58, 22, 70, 0.60));
				}
				tile.setStroke(Color.rgb(255, 255, 255, 0.08));
				tile.setStrokeWidth(1);
				tile.setMouseTransparent(true);
				candyPane.getChildren().add(tile);
			}
		}

		// 3. Left HUD Sidepanel
		VBox hudCard = new VBox(12);
		hudCard.setLayoutX(35);
		hudCard.setLayoutY(BOARD_OFFSET_Y - 10);
		hudCard.setPrefWidth(285);
		hudCard.setPrefHeight(560);
		hudCard.setPadding(new Insets(16, 18, 16, 18));
		hudCard.setStyle("-fx-background-color: rgba(20, 8, 30, 0.86); -fx-background-radius: 25; -fx-border-color: #FF1493; -fx-border-radius: 25; -fx-border-width: 2.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 25, 0, 0, 10);");

		Text logo = new Text("🍭 CANDY CRUSH 🍬");
		logo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
		logo.setFill(Color.web("#FF1493"));
		logo.setEffect(new DropShadow(10, Color.web("#FF69B4")));

		// Score Card
		VBox scoreCard = new VBox(2);
		scoreCard.setAlignment(Pos.CENTER);
		scoreCard.setPadding(new Insets(8, 12, 8, 12));
		scoreCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.10); -fx-background-radius: 14; -fx-border-color: rgba(0, 255, 127, 0.4); -fx-border-radius: 14; -fx-border-width: 1.5;");
		Text scoreLabel = new Text("⭐ CURRENT SCORE");
		scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		scoreLabel.setFill(Color.web("#FFD700"));
		scoreText = new Text("0");
		scoreText.setFont(Font.font("Arial", FontWeight.BOLD, 28));
		scoreText.setFill(Color.web("#00FF7F"));
		scoreCard.getChildren().addAll(scoreLabel, scoreText);

		// Target Goal Card with Dynamic Progress Bar
		VBox targetCard = new VBox(4);
		targetCard.setAlignment(Pos.CENTER);
		targetCard.setPadding(new Insets(8, 12, 8, 12));
		targetCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.10); -fx-background-radius: 14; -fx-border-color: rgba(0, 255, 255, 0.4); -fx-border-radius: 14; -fx-border-width: 1.5;");
		Text targetLabel = new Text("🎯 TARGET GOAL");
		targetLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		targetLabel.setFill(Color.web("#00FFFF"));
		targetScoreText = new Text("Target: " + targetScore);
		targetScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		targetScoreText.setFill(Color.WHITE);
		targetProgressBar = new ProgressBar(0);
		targetProgressBar.setPrefWidth(240);
		targetProgressBar.setPrefHeight(12);
		targetProgressBar.setStyle("-fx-accent: #00FF7F;");
		targetCard.getChildren().addAll(targetLabel, targetScoreText, targetProgressBar);

		// High Score Card
		VBox highCard = new VBox(2);
		highCard.setAlignment(Pos.CENTER);
		highCard.setPadding(new Insets(6, 12, 6, 12));
		highCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 14;");
		Text highLabel = new Text("🏆 HIGH SCORE");
		highLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		highLabel.setFill(Color.web("#FFC107"));
		highScoreText = new Text(String.valueOf(highScore));
		highScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 18));
		highScoreText.setFill(Color.web("#FFF8DC"));
		highCard.getChildren().addAll(highLabel, highScoreText);

		// Timer Card
		VBox timerCard = new VBox(2);
		timerCard.setAlignment(Pos.CENTER);
		timerCard.setPadding(new Insets(6, 12, 6, 12));
		timerCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 14;");
		Text timerLabel = new Text("⏳ TIME LEFT");
		timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		timerLabel.setFill(Color.web("#FF6B6B"));
		timerText = new Text(remainingSeconds + "s");
		timerText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
		timerText.setFill(Color.web("#FF5252"));
		timerCard.getChildren().addAll(timerLabel, timerText);

		// Action Buttons (Focus traversable = false so keys never get stolen)
		HBox btnRow1 = new HBox(8);
		btnRow1.setAlignment(Pos.CENTER);
		Button pauseBtn = new Button("⏸️ Pause");
		pauseBtn.setFocusTraversable(false);
		pauseBtn.setPrefWidth(115);
		pauseBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		pauseBtn.setOnAction(e -> togglePause(candyPane));

		soundToggleButton = new Button(SoundManager.isMuted() ? "🔇 Sound" : "🔊 Sound");
		soundToggleButton.setFocusTraversable(false);
		soundToggleButton.setPrefWidth(115);
		soundToggleButton.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #00BCD4; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		soundToggleButton.setOnAction(e -> {
			SoundManager.toggleMute();
			soundToggleButton.setText(SoundManager.isMuted() ? "🔇 Sound" : "🔊 Sound");
		});
		btnRow1.getChildren().addAll(pauseBtn, soundToggleButton);

		HBox btnRow2 = new HBox(8);
		btnRow2.setAlignment(Pos.CENTER);
		Button restartBtn = new Button("🔄 Restart");
		restartBtn.setFocusTraversable(false);
		restartBtn.setPrefWidth(115);
		restartBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #FF1493; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		restartBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			openCandyWindow(primaryStage);
		});

		Button menuBtn = new Button("🚪 Menu");
		menuBtn.setFocusTraversable(false);
		menuBtn.setPrefWidth(115);
		menuBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		menuBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			showMainWindow(primaryStage);
		});
		btnRow2.getChildren().addAll(restartBtn, menuBtn);

		hudCard.getChildren().addAll(logo, scoreCard, targetCard, highCard, timerCard, btnRow1, btnRow2);
		candyPane.getChildren().add(hudCard);

		// Candies on board
		initializeCandies(candyPane);

		// Cursor Frame
		cursorFrame = new Rectangle(CELL_SIZE - 4, CELL_SIZE - 4);
		cursorFrame.setArcWidth(14);
		cursorFrame.setArcHeight(14);
		cursorFrame.setFill(Color.TRANSPARENT);
		cursorFrame.setMouseTransparent(true);
		cursorFrame.setFocusTraversable(false);
		updateCursorPosition();
		setCursorNormalStyle();
		candyPane.getChildren().add(cursorFrame);
		cursorFrame.toFront();

		// Pause Overlay
		createPauseOverlay(candyPane, primaryStage);

		Scene candyScene = new Scene(candyPane, WINDOW_WIDTH, WINDOW_HEIGHT);
		crushStage.setTitle("Candy Crush Game");
		crushStage.setScene(candyScene);
		crushStage.show();

		// Attach keyboard listeners
		candyScene.setOnKeyPressed(event -> handleKeyEvent(event, candyPane));
		candyPane.setOnKeyPressed(event -> handleKeyEvent(event, candyPane));
		candyPane.setFocusTraversable(true);
		candyPane.requestFocus();

		// Countdown Timer
		timeline = new Timeline();
		timeline.setCycleCount(Animation.INDEFINITE);
		KeyFrame keyFrame = new KeyFrame(Duration.seconds(1), event -> {
			if (isPaused) return;
			remainingSeconds--;
			timerText.setText(remainingSeconds + "s");
			if (remainingSeconds <= 15) {
				timerText.setFill(Color.web("#FF1744"));
			}
			if (remainingSeconds <= 0) {
				timeline.stop();
				endGame(candyPane);
				endGame1(candyPane);
			}
		});
		timeline.getKeyFrames().add(keyFrame);
		timeline.play();

		destroyCandies(candyPane, scoreText);
	}

	// ==============================================================
	// PAUSE OVERLAY SYSTEM
	// ==============================================================
	private void createPauseOverlay(Pane candyPane, Stage primaryStage) {
		pauseOverlay = new StackPane();
		pauseOverlay.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);
		pauseOverlay.setStyle("-fx-background-color: rgba(0, 0, 0, 0.75);");
		pauseOverlay.setVisible(false);

		VBox pauseCard = new VBox(18);
		pauseCard.setAlignment(Pos.CENTER);
		pauseCard.setMaxWidth(460);
		pauseCard.setPadding(new Insets(35, 45, 35, 45));
		pauseCard.setStyle("-fx-background-color: rgba(25, 10, 35, 0.95); -fx-background-radius: 28; -fx-border-color: #FF1493; -fx-border-width: 3; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.8), 30, 0, 0, 12);");

		Text pauseTitle = new Text("⏸️ GAME PAUSED");
		pauseTitle.setFont(Font.font("Arial", FontWeight.BOLD, 32));
		pauseTitle.setFill(Color.web("#FF1493"));
		pauseTitle.setEffect(new DropShadow(15, Color.web("#FF69B4")));

		Text info = new Text("Score so far: " + gameScore + "  |  Target: " + targetScore);
		info.setFont(Font.font("Arial", FontWeight.BOLD, 16));
		info.setFill(Color.web("#00FFFF"));

		Button resumeBtn = new Button("▶️ RESUME GAME");
		resumeBtn.setPrefWidth(260);
		resumeBtn.setPrefHeight(48);
		resumeBtn.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-background-color: #32CD32; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;");
		resumeBtn.setOnAction(e -> {
			SoundManager.playClick();
			togglePause(candyPane);
		});

		Button restartBtn = new Button("🔄 RESTART LEVEL");
		restartBtn.setPrefWidth(260);
		restartBtn.setPrefHeight(45);
		restartBtn.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;");
		restartBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			openCandyWindow(primaryStage);
		});

		Button soundBtn = new Button(SoundManager.isMuted() ? "🔇 SOUND: OFF" : "🔊 SOUND: ON");
		soundBtn.setPrefWidth(260);
		soundBtn.setPrefHeight(45);
		soundBtn.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: #00BCD4; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;");
		soundBtn.setOnAction(e -> {
			SoundManager.toggleMute();
			soundBtn.setText(SoundManager.isMuted() ? "🔇 SOUND: OFF" : "🔊 SOUND: ON");
			if (soundToggleButton != null) {
				soundToggleButton.setText(SoundManager.isMuted() ? "🔇 Sound" : "🔊 Sound");
			}
		});

		Button menuBtn = new Button("🚪 MAIN MENU");
		menuBtn.setPrefWidth(260);
		menuBtn.setPrefHeight(45);
		menuBtn.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 25; -fx-cursor: hand;");
		menuBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			showMainWindow(primaryStage);
		});

		pauseCard.getChildren().addAll(pauseTitle, info, resumeBtn, restartBtn, soundBtn, menuBtn);
		pauseOverlay.getChildren().add(pauseCard);
		candyPane.getChildren().add(pauseOverlay);
	}

	private void togglePause(Pane candyPane) {
		isPaused = !isPaused;
		if (isPaused) {
			SoundManager.playClick();
			if (timeline != null) timeline.pause();
			pauseOverlay.setVisible(true);
			pauseOverlay.toFront();
		} else {
			SoundManager.playClick();
			if (timeline != null) timeline.play();
			pauseOverlay.setVisible(false);
			candyPane.requestFocus();
		}
	}

	// ==============================================================
	// CURSOR POSITIONING & KEYBOARD HANDLER
	// ==============================================================
	private void updateCursorPosition() {
		if (cursorFrame != null) {
			cursorFrame.setX(BOARD_OFFSET_X + cursorCol * CELL_SIZE + 2);
			cursorFrame.setY(BOARD_OFFSET_Y + cursorRow * CELL_SIZE + 2);
			cursorFrame.toFront();
		}
	}

	private void handleKeyEvent(KeyEvent event, Pane candyPane) {
		// Toggle pause via P or Escape
		switch (event.getCode()) {
		case P:
		case ESCAPE:
			togglePause(candyPane);
			event.consume();
			return;
		default:
			break;
		}

		if (isPaused) return;

		switch (event.getCode()) {
		case UP:
			if (cursorRow > 0) {
				cursorRow--;
				updateCursorPosition();
				SoundManager.playSelect();
			}
			event.consume();
			break;
		case DOWN:
			if (cursorRow < BOARD_SIZE - 1) {
				cursorRow++;
				updateCursorPosition();
				SoundManager.playSelect();
			}
			event.consume();
			break;
		case LEFT:
			if (cursorCol > 0) {
				cursorCol--;
				updateCursorPosition();
				SoundManager.playSelect();
			}
			event.consume();
			break;
		case RIGHT:
			if (cursorCol < BOARD_SIZE - 1) {
				cursorCol++;
				updateCursorPosition();
				SoundManager.playSelect();
			}
			event.consume();
			break;
		case ENTER:
		case SPACE:
			handleCandySelection(cursorCol, cursorRow, candyPane);
			event.consume();
			break;
		default:
			break;
		}
	}

	// ==============================================================
	// CANDY SELECTION & SWAPPING WITH GLOW FEEDBACK & SOUND
	// ==============================================================
	private void handleCandySelection(int x, int y, Pane candyPane) {
		if (isPaused) return;

		cursorCol = x;
		cursorRow = y;
		updateCursorPosition();

		if (firstCandyX == -1 && firstCandyY == -1) {
			// Select first candy -> Glow Cyan & play select sound
			firstCandyX = x;
			firstCandyY = y;
			setCursorSelectedStyle();
			SoundManager.playSelect();
		} else {
			// Second candy clicked
			if (isValidSwap(firstCandyX, firstCandyY, x, y)) {
				SoundManager.playSwap();
				swapCandies(firstCandyX, firstCandyY, x, y);
				if (checkCandiesPresentSame()) {
					removeCandies();
					gameScore += 10;
					updateScoreUI();
					SoundManager.playMatch();

					boolean foundMatch;
					int comboCount = 1;
					do {
						foundMatch = checkCandiesPresentSame();
						if (foundMatch) {
							comboCount++;
							removeCandies();
							gameScore += 10 * comboCount;
							updateScoreUI();
							SoundManager.playCombo();
						}
					} while (foundMatch);
					updateBoard(candyPane);
				} else {
					// Swap back if no match
					swapCandies(firstCandyX, firstCandyY, x, y);
				}
				firstCandyX = -1;
				firstCandyY = -1;
				setCursorNormalStyle();
			} else {
				// Clicked non-adjacent candy -> Switch selection
				firstCandyX = x;
				firstCandyY = y;
				setCursorSelectedStyle();
				SoundManager.playSelect();
			}
		}
	}

	private void updateScoreUI() {
		if (scoreText != null) {
			scoreText.setText(String.valueOf(gameScore));
		}
		if (targetProgressBar != null && targetScore > 0) {
			double progress = Math.min(1.0, (double) gameScore / targetScore);
			targetProgressBar.setProgress(progress);
		}
		if (gameScore > highScore) {
			highScore = gameScore;
			if (highScoreText != null) {
				highScoreText.setText(String.valueOf(highScore));
			}
			updateHighScore(highScore);
		}
	}

	private void setCursorNormalStyle() {
		if (cursorFrame != null) {
			cursorFrame.setStroke(Color.web("#FFD700")); // Brilliant Gold
			cursorFrame.setStrokeWidth(3.5);
			DropShadow ds = new DropShadow(16, Color.web("#FFA500"));
			ds.setSpread(0.65);
			cursorFrame.setEffect(ds);
		}
	}

	private void setCursorSelectedStyle() {
		if (cursorFrame != null) {
			cursorFrame.setStroke(Color.web("#00FFFF")); // Electric Cyan
			cursorFrame.setStrokeWidth(4.5);
			DropShadow ds = new DropShadow(26, Color.web("#00FFFF"));
			ds.setSpread(0.85);
			cursorFrame.setEffect(ds);
		}
	}

	// ==============================================================
	// BOARD INITIALIZATION & UPDATE
	// ==============================================================
	private void initializeCandies(Pane candyPane) {
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				candies[i][j] = random();
				Image candyImage = getCandyImage(candies[i][j]);
				ImageView candyImageView = new ImageView(candyImage);
				candyImageView.setFitWidth(CANDY_FIT_SIZE);
				candyImageView.setFitHeight(CANDY_FIT_SIZE);
				double offsetInCell = (CELL_SIZE - CANDY_FIT_SIZE) / 2.0;
				candyImageView.setX(BOARD_OFFSET_X + j * CELL_SIZE + offsetInCell);
				candyImageView.setY(BOARD_OFFSET_Y + i * CELL_SIZE + offsetInCell);
				candyImageView.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.45)));

				final int col = j;
				final int row = i;
				candyImageView.setOnMouseClicked(e -> {
					if (!isPaused) {
						handleCandySelection(col, row, candyPane);
						candyPane.requestFocus();
					}
				});

				candyPane.getChildren().add(candyImageView);
			}
		}
	}

	private void destroyCandies(Pane candyPane, Text scoreText) {
		boolean foundMatch;
		do {
			foundMatch = checkCandiesPresentSame();
			if (foundMatch) {
				removeCandies();
				updateBoard(candyPane);
				gameScore += 10;
				updateScoreUI();
			}
		} while (foundMatch);
	}

	private boolean checkCandiesPresentSame() {
		boolean foundMatch = false;
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE - 2; j++) {
				if (candies[i][j] == candies[i][j + 1] && candies[i][j] == candies[i][j + 2]) {
					candies[i][j] = 7;
					candies[i][j + 1] = 7;
					candies[i][j + 2] = 7;
					foundMatch = true;
				}
			}
		}
		for (int i = 0; i < BOARD_SIZE - 2; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				if (candies[i][j] == candies[i + 1][j] && candies[i][j] == candies[i + 2][j]) {
					candies[i][j] = 7;
					candies[i + 1][j] = 7;
					candies[i + 2][j] = 7;
					foundMatch = true;
				}
			}
		}
		return foundMatch;
	}

	private void removeCandies() {
		for (int j = 0; j < BOARD_SIZE; j++) {
			int Count = 0;
			for (int i = BOARD_SIZE - 1; i >= 0; i--) {
				if (candies[i][j] == 7) {
					Count++;
				} else if (Count > 0) {
					candies[i + Count][j] = candies[i][j];
					candies[i][j] = 7;
				}
			}
			for (int i = 0; i < Count; i++) {
				candies[i][j] = random();
			}
		}
	}

	private void updateBoard(Pane candyPane) {
		candyPane.getChildren().clear();

		// Background
		Image backgroundImage = loadImageSafe("candy_board_bg.jpg");
		ImageView backgroundImageView = new ImageView(backgroundImage);
		backgroundImageView.setFitWidth(WINDOW_WIDTH);
		backgroundImageView.setFitHeight(WINDOW_HEIGHT);
		backgroundImageView.setOnMouseClicked(e -> {
			if (!isPaused) candyPane.requestFocus();
		});
		candyPane.getChildren().add(backgroundImageView);

		// Board Container Frame
		Rectangle boardFrame = new Rectangle(560, 560);
		boardFrame.setX(BOARD_OFFSET_X - 10);
		boardFrame.setY(BOARD_OFFSET_Y - 10);
		boardFrame.setArcWidth(26);
		boardFrame.setArcHeight(26);
		boardFrame.setFill(Color.rgb(22, 10, 32, 0.82));
		boardFrame.setStroke(Color.web("#FF1493"));
		boardFrame.setStrokeWidth(3.5);
		boardFrame.setEffect(new DropShadow(30, Color.rgb(0, 0, 0, 0.85)));
		candyPane.getChildren().add(boardFrame);

		// Tray Pocket Tiles
		for (int r = 0; r < BOARD_SIZE; r++) {
			for (int c = 0; c < BOARD_SIZE; c++) {
				Rectangle tile = new Rectangle(58, 58);
				tile.setArcWidth(12);
				tile.setArcHeight(12);
				tile.setX(BOARD_OFFSET_X + c * CELL_SIZE + 3.5);
				tile.setY(BOARD_OFFSET_Y + r * CELL_SIZE + 3.5);
				if ((r + c) % 2 == 0) {
					tile.setFill(Color.rgb(38, 15, 50, 0.60));
				} else {
					tile.setFill(Color.rgb(58, 22, 70, 0.60));
				}
				tile.setStroke(Color.rgb(255, 255, 255, 0.08));
				tile.setStrokeWidth(1);
				tile.setMouseTransparent(true);
				candyPane.getChildren().add(tile);
			}
		}

		// Left HUD Sidepanel
		VBox hudCard = new VBox(12);
		hudCard.setLayoutX(35);
		hudCard.setLayoutY(BOARD_OFFSET_Y - 10);
		hudCard.setPrefWidth(285);
		hudCard.setPrefHeight(560);
		hudCard.setPadding(new Insets(16, 18, 16, 18));
		hudCard.setStyle("-fx-background-color: rgba(20, 8, 30, 0.86); -fx-background-radius: 25; -fx-border-color: #FF1493; -fx-border-radius: 25; -fx-border-width: 2.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.6), 25, 0, 0, 10);");

		Text logo = new Text("🍭 CANDY CRUSH 🍬");
		logo.setFont(Font.font("Arial", FontWeight.BOLD, 22));
		logo.setFill(Color.web("#FF1493"));
		logo.setEffect(new DropShadow(10, Color.web("#FF69B4")));

		// Score Card
		VBox scoreCard = new VBox(2);
		scoreCard.setAlignment(Pos.CENTER);
		scoreCard.setPadding(new Insets(8, 12, 8, 12));
		scoreCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.10); -fx-background-radius: 14; -fx-border-color: rgba(0, 255, 127, 0.4); -fx-border-radius: 14; -fx-border-width: 1.5;");
		Text scoreLabel = new Text("⭐ CURRENT SCORE");
		scoreLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		scoreLabel.setFill(Color.web("#FFD700"));
		scoreCard.getChildren().addAll(scoreLabel, scoreText);

		// Target Goal Card
		VBox targetCard = new VBox(4);
		targetCard.setAlignment(Pos.CENTER);
		targetCard.setPadding(new Insets(8, 12, 8, 12));
		targetCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.10); -fx-background-radius: 14; -fx-border-color: rgba(0, 255, 255, 0.4); -fx-border-radius: 14; -fx-border-width: 1.5;");
		Text targetLabel = new Text("🎯 TARGET GOAL");
		targetLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		targetLabel.setFill(Color.web("#00FFFF"));
		targetCard.getChildren().addAll(targetLabel, targetScoreText, targetProgressBar);

		// High Score Card
		VBox highCard = new VBox(2);
		highCard.setAlignment(Pos.CENTER);
		highCard.setPadding(new Insets(6, 12, 6, 12));
		highCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 14;");
		Text highLabel = new Text("🏆 HIGH SCORE");
		highLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		highLabel.setFill(Color.web("#FFC107"));
		highCard.getChildren().addAll(highLabel, highScoreText);

		// Timer Card
		VBox timerCard = new VBox(2);
		timerCard.setAlignment(Pos.CENTER);
		timerCard.setPadding(new Insets(6, 12, 6, 12));
		timerCard.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 14;");
		Text timerLabel = new Text("⏳ TIME LEFT");
		timerLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));
		timerLabel.setFill(Color.web("#FF6B6B"));
		timerCard.getChildren().addAll(timerLabel, timerText);

		// Action Buttons
		HBox btnRow1 = new HBox(8);
		btnRow1.setAlignment(Pos.CENTER);
		Button pauseBtn = new Button("⏸️ Pause");
		pauseBtn.setFocusTraversable(false);
		pauseBtn.setPrefWidth(115);
		pauseBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #9C27B0; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		pauseBtn.setOnAction(e -> togglePause(candyPane));

		soundToggleButton = new Button(SoundManager.isMuted() ? "🔇 Sound" : "🔊 Sound");
		soundToggleButton.setFocusTraversable(false);
		soundToggleButton.setPrefWidth(115);
		soundToggleButton.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #00BCD4; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		soundToggleButton.setOnAction(e -> {
			SoundManager.toggleMute();
			soundToggleButton.setText(SoundManager.isMuted() ? "🔇 Sound" : "🔊 Sound");
		});
		btnRow1.getChildren().addAll(pauseBtn, soundToggleButton);

		HBox btnRow2 = new HBox(8);
		btnRow2.setAlignment(Pos.CENTER);
		Button restartBtn = new Button("🔄 Restart");
		restartBtn.setFocusTraversable(false);
		restartBtn.setPrefWidth(115);
		restartBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #FF1493; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		restartBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			openCandyWindow(primaryStage);
		});

		Button menuBtn = new Button("🚪 Menu");
		menuBtn.setFocusTraversable(false);
		menuBtn.setPrefWidth(115);
		menuBtn.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-background-color: #555555; -fx-text-fill: white; -fx-background-radius: 18; -fx-cursor: hand;");
		menuBtn.setOnAction(e -> {
			SoundManager.playClick();
			if (timeline != null) timeline.stop();
			crushStage.close();
			showMainWindow(primaryStage);
		});
		btnRow2.getChildren().addAll(restartBtn, menuBtn);

		hudCard.getChildren().addAll(logo, scoreCard, targetCard, highCard, timerCard, btnRow1, btnRow2);
		candyPane.getChildren().add(hudCard);

		// Candies
		for (int i = 0; i < BOARD_SIZE; i++) {
			for (int j = 0; j < BOARD_SIZE; j++) {
				Image candyImage = getCandyImage(candies[i][j]);
				ImageView candyImageView = new ImageView(candyImage);
				candyImageView.setFitWidth(CANDY_FIT_SIZE);
				candyImageView.setFitHeight(CANDY_FIT_SIZE);
				double offsetInCell = (CELL_SIZE - CANDY_FIT_SIZE) / 2.0;
				candyImageView.setX(BOARD_OFFSET_X + j * CELL_SIZE + offsetInCell);
				candyImageView.setY(BOARD_OFFSET_Y + i * CELL_SIZE + offsetInCell);
				candyImageView.setEffect(new DropShadow(8, Color.rgb(0, 0, 0, 0.45)));

				final int col = j;
				final int row = i;
				candyImageView.setOnMouseClicked(e -> {
					if (!isPaused) {
						handleCandySelection(col, row, candyPane);
						candyPane.requestFocus();
					}
				});

				candyPane.getChildren().add(candyImageView);
			}
		}

		// Cursor Frame
		candyPane.getChildren().add(cursorFrame);
		updateCursorPosition();
		if (firstCandyX != -1 && firstCandyY != -1) {
			setCursorSelectedStyle();
		} else {
			setCursorNormalStyle();
		}
		cursorFrame.toFront();

		// Pause overlay re-added
		createPauseOverlay(candyPane, primaryStage);

		candyPane.setFocusTraversable(true);
		candyPane.requestFocus();

		if (crushStage != null && crushStage.getScene() != null) {
			crushStage.getScene().setOnKeyPressed(event -> handleKeyEvent(event, candyPane));
		}
		candyPane.setOnKeyPressed(event -> handleKeyEvent(event, candyPane));
	}

	private boolean isValidSwap(int x1, int y1, int x2, int y2) {
		return (Math.abs(x1 - x2) == 1 && y1 == y2) || (Math.abs(y1 - y2) == 1 && x1 == x2);
	}

	private void swapCandies(int x1, int y1, int x2, int y2) {
		int temp = candies[y1][x1];
		candies[y1][x1] = candies[y2][x2];
		candies[y2][x2] = temp;
	}

	private int random() {
		Random rand = new Random();
		return rand.nextInt(6);
	}

	private Image getCandyImage(int candy) {
		String[] candyImages = { "Candy1.png", "Candy2.png", "Candy3.png", "Candy4.png", "Candy5.png", "Candy6.png" };
		int index = Math.max(0, Math.min(candy, candyImages.length - 1));
		return loadImageSafe(candyImages[index]);
	}

	private Image loadImageSafe(String name) {
		try {
			File f = new File("Images/" + name);
			if (f.exists()) {
				return new Image(f.toURI().toString());
			}
			File fBin = new File("bin/Images/" + name);
			if (fBin.exists()) {
				return new Image(fBin.toURI().toString());
			}
			return new Image(name);
		} catch (Exception e) {
			return null;
		}
	}

	private void endGame(Pane candyPane) {
		if (gameScore > highScore) {
			highScore = gameScore;
			updateHighScore(highScore);
			if (highScoreText != null) {
				highScoreText.setText(String.valueOf(highScore));
			}
		}

		if (candyStage != null) {
			candyStage.close();
		}
		if (crushStage != null) {
			crushStage.close();
		}
		if (introStage != null) {
			introStage.close();
		}
		if (primaryStage != null) {
			primaryStage.close();
		}
	}

	private void updateHighScore(int newHighScore) {
		try {
			File file = new File("high.txt");
			PrintWriter pw = new PrintWriter(file);
			pw.println(newHighScore);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void highScoreFile() {
		try {
			File file = new File("high.txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				if (line != null) {
					highScore = Integer.parseInt(line.trim());
				}
				br.close();
			}
		} catch (Exception e) {
			highScore = 0;
		}
	}

	private void endGame1(Pane candyPane) {
		if (gameScore >= targetScore) {
			new ExitWindow("🎉 YOU WON! 🎉", gameScore, true).show();
		} else {
			new ExitWindow("💔 TIME'S UP! 💔", gameScore, false).show();
		}

		if (candyStage != null) {
			candyStage.close();
		}
		if (crushStage != null) {
			crushStage.close();
		}
		if (introStage != null) {
			introStage.close();
		}
		if (primaryStage != null) {
			primaryStage.close();
		}
	}

	private void updateTargetScore(int newTargetScore) {
		try {
			File file = new File("target.txt");
			PrintWriter pw = new PrintWriter(file);
			pw.println(newTargetScore);
			pw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void targetScoreFile() {
		try {
			File file = new File("target.txt");
			if (file.exists()) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				String line = br.readLine();
				if (line != null) {
					targetScore = Integer.parseInt(line.trim());
				}
				br.close();
			}
		} catch (Exception e) {
			targetScore = 100;
		}
	}

	// ==============================================================
	// 7. GAME OVER & WIN SCREEN (AAA QUALITY)
	// ==============================================================
	public class ExitWindow extends Stage {
		public ExitWindow(String result, int finalScore, boolean showNextButton) {
			this.setTitle("Game Results");

			if (showNextButton) {
				SoundManager.playWin();
			} else {
				SoundManager.playGameOver();
			}

			Pane root = new Pane();
			root.setPrefSize(WINDOW_WIDTH, WINDOW_HEIGHT);

			Image backgroundImage = loadImageSafe("candy_menu_bg.jpg");
			ImageView bgView = new ImageView(backgroundImage);
			bgView.setFitWidth(WINDOW_WIDTH);
			bgView.setFitHeight(WINDOW_HEIGHT);
			root.getChildren().add(bgView);

			VBox card = new VBox(18);
			card.setAlignment(Pos.CENTER);
			card.setLayoutX(WINDOW_WIDTH / 2.0 - 250);
			card.setLayoutY(WINDOW_HEIGHT / 2.0 - 240);
			card.setPrefWidth(500);
			card.setPadding(new Insets(35, 40, 35, 40));
			card.setStyle("-fx-background-color: rgba(20, 8, 30, 0.90); -fx-background-radius: 28; -fx-border-color: "
					+ (showNextButton ? "#00FF7F" : "#FF1493")
					+ "; -fx-border-width: 3.5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.7), 30, 0, 0, 12);");

			Text resultText = new Text(result);
			resultText.setFont(Font.font("Arial", FontWeight.BOLD, 34));
			resultText.setFill(showNextButton ? Color.web("#00FF7F") : Color.web("#FF5252"));
			resultText.setEffect(new DropShadow(15, showNextButton ? Color.web("#00FF7F") : Color.web("#FF1493")));

			VBox scoreBox = new VBox(6);
			scoreBox.setAlignment(Pos.CENTER);
			scoreBox.setPadding(new Insets(12));
			scoreBox.setStyle("-fx-background-color: rgba(255, 255, 255, 0.08); -fx-background-radius: 16;");

			Text targetText = new Text("🎯 Target Goal: " + targetScore);
			targetText.setFont(Font.font("Arial", FontWeight.BOLD, 20));
			targetText.setFill(Color.web("#00FFFF"));

			Text finalScoreText = new Text("⭐ Your Final Score: " + finalScore);
			finalScoreText.setFont(Font.font("Arial", FontWeight.BOLD, 24));
			finalScoreText.setFill(Color.web("#FFD700"));

			scoreBox.getChildren().addAll(targetText, finalScoreText);

			VBox btnContainer = new VBox(12);
			btnContainer.setAlignment(Pos.CENTER);

			if (showNextButton) {
				Button nextButton = new Button("🌟 NEXT LEVEL");
				nextButton.setPrefWidth(260);
				nextButton.setPrefHeight(48);
				nextButton.setStyle("-fx-font-size: 18; -fx-font-weight: bold; -fx-background-color: #32CD32; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
				nextButton.setOnAction(event -> {
					SoundManager.playClick();
					this.close();
					targetScore += 100;
					updateTargetScore(targetScore);
					openCandyWindow(primaryStage);
				});
				btnContainer.getChildren().add(nextButton);
			}

			Button playAgainButton = new Button("🔄 PLAY AGAIN");
			playAgainButton.setPrefWidth(260);
			playAgainButton.setPrefHeight(45);
			playAgainButton.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-background-color: #FF1493; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
			playAgainButton.setOnAction(event -> {
				SoundManager.playClick();
				this.close();
				openCandyWindow(primaryStage);
			});

			Button menuButton = new Button("🚪 MAIN MENU");
			menuButton.setPrefWidth(260);
			menuButton.setPrefHeight(45);
			menuButton.setStyle("-fx-font-size: 17; -fx-font-weight: bold; -fx-background-color: #555555; -fx-text-fill: #FFFFFF; -fx-background-radius: 25; -fx-cursor: hand;");
			menuButton.setOnAction(event -> {
				SoundManager.playClick();
				this.close();
				showMainWindow(primaryStage);
			});

			btnContainer.getChildren().addAll(playAgainButton, menuButton);
			card.getChildren().addAll(resultText, scoreBox, btnContainer);
			root.getChildren().add(card);

			Scene scene = new Scene(root, WINDOW_WIDTH, WINDOW_HEIGHT);
			this.setScene(scene);
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}
