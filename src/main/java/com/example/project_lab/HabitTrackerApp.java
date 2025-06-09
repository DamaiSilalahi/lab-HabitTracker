package com.example.project_lab;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public class HabitTrackerApp extends Application {

    private UserService userService;
    private HabitStorageService habitStorageService;
    private User currentUser ;
    private HabitManager habitManager;

    private Stage primaryStage;
    private VBox habitDisplayBox;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.userService = new UserService();
        this.habitStorageService = new HabitStorageService();

        primaryStage.setTitle("Habit Tracker");
        showLoginScene();
        primaryStage.show();
    }

    private void showLoginScene() {
        VBox loginLayout = new VBox(15);
        loginLayout.setPadding(new Insets(20));
        loginLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Login Habit Tracker");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField usernameInput = new TextField();
        usernameInput.setPromptText("Username");
        PasswordField passwordInput = new PasswordField();
        passwordInput.setPromptText("Password");

        Button loginButton = new Button("Login");
        Button goToSignUpButton = new Button("Belum punya akun? Sign Up");

        Label errorLabel = new Label();
        errorLabel.setStyle("-fx-text-fill: red;");

        loginButton.setOnAction(e -> {
            String username = usernameInput.getText();
            String password = passwordInput.getText();
            Optional<User> userOpt = userService.signIn(username, password);
            if (userOpt.isPresent()) {
                currentUser  = userOpt.get();
                habitManager = new HabitManager(currentUser .getUsername(), habitStorageService);
                showMainAppScene();
            } else {
                errorLabel.setText("Username atau password salah.");
            }
        });

        goToSignUpButton.setOnAction(e -> showSignUpScene());

        loginLayout.getChildren().addAll(titleLabel, usernameInput, passwordInput, loginButton, goToSignUpButton, errorLabel);
        Scene loginScene = new Scene(loginLayout, 350, 300);
        primaryStage.setScene(loginScene);
        primaryStage.setTitle("Habit Tracker - Login");
    }

    private void showSignUpScene() {
        VBox signUpLayout = new VBox(15);
        signUpLayout.setPadding(new Insets(20));
        signUpLayout.setAlignment(Pos.CENTER);

        Label titleLabel = new Label("Sign Up Habit Tracker");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold;");

        TextField newUsernameInput = new TextField();
        newUsernameInput.setPromptText("Username Baru");
        PasswordField newPasswordInput = new PasswordField();
        newPasswordInput.setPromptText("Password Baru");
        PasswordField confirmPasswordInput = new PasswordField();
        confirmPasswordInput.setPromptText("Konfirmasi Password");

        Button signUpButton = new Button("Sign Up");
        Button goToLoginButton = new Button("Sudah punya akun? Login");

        Label messageLabel = new Label();

        signUpButton.setOnAction(e -> {
            String username = newUsernameInput.getText().trim();
            String password = newPasswordInput.getText();
            String confirmPassword = confirmPasswordInput.getText();

            if (username.isEmpty() || password.isEmpty()) {
                messageLabel.setText("Username dan password tidak boleh kosong.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }
            if (!password.equals(confirmPassword)) {
                messageLabel.setText("Password tidak cocok.");
                messageLabel.setStyle("-fx-text-fill: red;");
                return;
            }

            if (userService.signUp(username, password)) {
                messageLabel.setText("Akun berhasil dibuat! Silakan login.");
                messageLabel.setStyle("-fx-text-fill: green;");
                newUsernameInput.clear();
                newPasswordInput.clear();
                confirmPasswordInput.clear();
            } else {
                messageLabel.setText("Username sudah ada atau terjadi kesalahan.");
                messageLabel.setStyle("-fx-text-fill: red;");
            }
        });

        goToLoginButton.setOnAction(e -> showLoginScene());

        signUpLayout.getChildren().addAll(titleLabel, newUsernameInput, newPasswordInput, confirmPasswordInput, signUpButton, goToLoginButton, messageLabel);
        Scene signUpScene = new Scene(signUpLayout, 350, 350);
        primaryStage.setScene(signUpScene);
        primaryStage.setTitle("Habit Tracker - Sign Up");
    }

    private void showMainAppScene() {
        primaryStage.setTitle("Habit Tracker - " + currentUser .getUsername());

        TextField habitInput = new TextField();
        habitInput.setPromptText("Masukkan nama kebiasaan");

        Button addButton = new Button("Tambah Habit");
        Button calendarButton = new Button("Kalender & Riwayat");

        habitDisplayBox = new VBox(5);
        habitDisplayBox.setPadding(new Insets(10));

        addButton.setOnAction(e -> {
            String habitName = habitInput.getText().trim();
            if (!habitName.isEmpty()) {
                Habit newHabit = new Habit(habitName, LocalDate.now());
                habitManager.addHabit(newHabit);
                updateHabitDisplay();
                habitInput.clear();
            } else {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Nama kebiasaan tidak boleh kosong!");
                alert.showAndWait();
            }
        });

        calendarButton.setOnAction(e -> showCalendarScene());

        Button logoutButton = new Button("Logout");
        logoutButton.setOnAction(e -> {
            currentUser  = null;
            habitManager = null;
            showLoginScene();
        });

        HBox inputLayout = new HBox(10, habitInput, addButton, calendarButton);
        inputLayout.setPadding(new Insets(10));

        HBox topBarLayout = new HBox(10, new Label(":User  " + currentUser .getUsername()), logoutButton);
        topBarLayout.setAlignment(Pos.CENTER_RIGHT);
        topBarLayout.setPadding(new Insets(5));

        VBox mainLayout = new VBox(10, topBarLayout, inputLayout, new ScrollPane(habitDisplayBox));
        mainLayout.setPadding(new Insets(10));

        Scene scene = new Scene(mainLayout, 450, 400);
        primaryStage.setScene(scene);
        updateHabitDisplay();
    }

    private void updateHabitDisplay() {
        if (habitManager == null) return;

        habitDisplayBox.getChildren().clear();
        for (Habit habit : habitManager.getHabitList()) {
            CheckBox cb = new CheckBox(habit.getName() + " (" + habit.getDate().toString() + ")");
            cb.setSelected(habit.isCompleted());

            cb.setOnAction(e -> {
                if (!habit.getDate().isBefore(LocalDate.now()) || habit.getDate().isEqual(LocalDate.now())) {
                    habitManager.updateHabitStatus(habit, cb.isSelected());
                } else {
                    cb.setSelected(habit.isCompleted());
                    Alert alert = new Alert(Alert.AlertType.INFORMATION, "Kebiasaan dari tanggal lampau tidak dapat diubah statusnya.");
                    alert.showAndWait();
                }
            });
            habitDisplayBox.getChildren().add(cb);
        }
    }

    public static void main(String[] args) {
        launch(args);
    }

    private void showCalendarScene() {
        CalenderView calendarView = new CalenderView(habitManager);
        // Set up the calendar scene
        calendarView.getDatePicker().setOnAction(e -> {
            LocalDate selectedDate = calendarView.getDatePicker().getValue();
            calendarView.updateHabitsForSelectedDate(selectedDate);
        });
        calendarView.getSummaryTypeComboBox().setOnAction(e -> {
            calendarView.updateCalendar();
        });
        // Initialize the calendar view with the current date
        calendarView.updateHabitsForSelectedDate(calendarView.getDatePicker().getValue());
        calendarView.updateCalendar();
        Scene calendarScene = new Scene(calendarView, 400, 300);
        primaryStage.setScene(calendarScene);
        primaryStage.setTitle("Kalender & Riwayat");
    }

    public static void createHabitRows(List<Habit> habitsForDate, VBox habitListBox) {
        for (Habit habit : habitsForDate) {
            HBox habitRow = new HBox(10);
            habitRow.setPadding(new Insets(8));
            habitRow.setBackground(new Background(new BackgroundFill(
                    Color.web("#f9fafb"), new CornerRadii(8), Insets.EMPTY)));
            habitRow.setBorder(new Border(new BorderStroke(
                    Color.web("#e5e7eb"), BorderStrokeStyle.SOLID,
                    new CornerRadii(8), BorderWidths.DEFAULT)));
            habitRow.setPrefWidth(380);
            Circle statusCircle = new Circle(8);
            if (habit.isCompleted()) {
                statusCircle.setFill(Color.web("#10b981"));
            } else {
                statusCircle.setFill(Color.web("#9ca3af"));
            }
            Label habitNameLabel = new Label(habit.getName());
            habitRow.getChildren().addAll(statusCircle, habitNameLabel);
            habitListBox.getChildren().add(habitRow);
        }


    }
}
