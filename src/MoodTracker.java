import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class MoodTracker extends Application {

    public static void main(String[] args) throws Exception {
        System.out.println("MoodTracker Launched.");
        launch(args);
    }

    private Label moodLabel;
    private ObservableMap<String, MoodEntry> moodEntries = FXCollections.observableHashMap();
    private final String FILE_NAME = "mood_entries.csv";
    private int width = 300;
    private int height = 400;
    private final static String happyText = "Happy";
    private final static String sadText = "Sad";
    private final static String angryText = "Angry";
    private final static String anxiousText = "Anxious";
    private final static String calmText = "Calm";
    private final static String happyColor = "#4CAF50";
    private final static String sadColor = "#2196F3";
    private final static String angryColor = "#f44336";
    private final static String anxiousColor = "#FFC107";
    private final static String calmColor = "#8BC34A";

    @Override
    public void start(Stage primaryStage) {
        loadMoodEntries();

        moodLabel = new Label("Select your mood:");
        moodLabel.setAlignment(Pos.CENTER);

        Button happyButton = createMoodButton(happyText, happyColor);
        Button sadButton = createMoodButton(sadText, sadColor);
        Button angryButton = createMoodButton(angryText, angryColor);
        Button anxiousButton = createMoodButton(anxiousText, anxiousColor);
        Button calmButton = createMoodButton(calmText, calmColor);

        Button historyButton = new Button("View History");

        VBox.setMargin(moodLabel, new Insets(0, 0, 10, 0));
        VBox.setMargin(historyButton, new Insets(10, 0, 0, 0));

        // Layout for the main scene
        VBox root = new VBox(0); // Set spacing to #
        root.setAlignment(Pos.CENTER);
        root.getChildren().addAll(moodLabel, happyButton, sadButton, angryButton, anxiousButton, calmButton,
                historyButton);
        root.setStyle("-fx-padding: 20;");

        // Set VBox to grow the buttons to fill available space
        VBox.setVgrow(happyButton, Priority.ALWAYS);
        VBox.setVgrow(sadButton, Priority.ALWAYS);
        VBox.setVgrow(angryButton, Priority.ALWAYS);
        VBox.setVgrow(anxiousButton, Priority.ALWAYS);
        VBox.setVgrow(calmButton, Priority.ALWAYS);

        Scene mainScene = new Scene(root, width, height);
        Scene historyScene = createHistoryScene(mainScene, primaryStage);

        // Event handler for the history button
        historyButton.setOnAction(e -> primaryStage.setScene(historyScene));

        primaryStage.setTitle("Mood Tracker");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private Button createMoodButton(String mood, String color) {
        Button button = new Button(mood);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px;");
        button.setMaxWidth(Double.MAX_VALUE);
        button.setPrefHeight(50);

        button.setOnAction(e -> {
            moodLabel.setText("Current Mood: " + mood);
            storeMoodEntry(mood);
        });

        return button;
    }

    private void storeMoodEntry(String mood) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        MoodEntry newEntry = new MoodEntry(mood, date);

        moodEntries.put(date, newEntry);

        saveMoodEntries(); // Save the updated entries to file

        // Update the history scene
        Stage stage = (Stage) moodLabel.getScene().getWindow(); // Get the current stage
        Scene currentScene = stage.getScene();
        if (currentScene instanceof Scene) {
            Scene historyScene = createHistoryScene(currentScene, stage);
            stage.setScene(historyScene);
        }
    }

    private void saveMoodEntries() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (MoodEntry entry : moodEntries.values()) {
                writer.println(entry.getMood() + "," + entry.getDate());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadMoodEntries() {
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    moodEntries.put(parts[1], new MoodEntry(parts[0], parts[1])); // Use date as the key
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Scene createHistoryScene(Scene mainScene, Stage primaryStage) {
        GridPane calendarPane = createCalendar(YearMonth.now());

        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> primaryStage.setScene(mainScene));

        VBox historyLayout = new VBox(10, calendarPane, backButton);
        historyLayout.setAlignment(Pos.CENTER);
        historyLayout.setStyle("-fx-padding: 20;");
        Scene historyScene = new Scene(historyLayout, width, height);
        return historyScene;
    }

    private GridPane createCalendar(YearMonth yearMonth) {
        GridPane calendarPane = new GridPane();
        calendarPane.setAlignment(Pos.CENTER);
        calendarPane.setHgap(1);
        calendarPane.setVgap(1);

        String[] daysOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int i = 0; i < 7; i++) {
            Label dayOfWeekLabel = new Label(daysOfWeek[i]);
            dayOfWeekLabel.setStyle("-fx-font-weight: bold;");
            dayOfWeekLabel.setPrefSize(40, 60);
            dayOfWeekLabel.setAlignment(Pos.CENTER);
            calendarPane.add(dayOfWeekLabel, i, 0);
        }

        // Get the first day of the month and the number of days in the month
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int lengthOfMonth = yearMonth.lengthOfMonth();

        // Populate the calendar with day labels and mood indicators
        int row = 1;
        int col = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Adjust for Sunday being 7 instead of 0
        for (int i = 1; i <= lengthOfMonth; i++) {

            String dateKey = yearMonth.atDay(i).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            Label moodIndicator = new Label(Integer.toString(i));
            moodIndicator.setStyle("-fx-font-weight: bold;" + "-fx-border-color: black;");
            moodIndicator.setPrefSize(40, 40);
            moodIndicator.setAlignment(Pos.CENTER);
            if (moodEntries.containsKey(dateKey)) {
                MoodEntry moodEntry = moodEntries.get(dateKey);
                moodIndicator.setStyle("-fx-font-weight: bold;" + "-fx-background-color: " + moodEntry.getColor()
                        + "; -fx-border-color: black;");
            }
            calendarPane.add(moodIndicator, col, row + 1);

            col++;
            if (col == 7) {
                col = 0;
                row += 2; // Move to the next row (skipping the row for mood indicators)
            }
        }

        return calendarPane;
    }

    // MoodEntry class to store mood data
    public static class MoodEntry {
        private String mood;
        private final String date;

        public MoodEntry(String mood, String date) {
            this.mood = mood;
            this.date = date;
        }

        public String getMood() {
            return mood;
        }

        public void setMood(String mood) {
            this.mood = mood;
        }

        public String getDate() {
            return date;
        }

        // Method to get the color associated with the mood
        public String getColor() {
            switch (mood) {
                case happyText:
                    return happyColor;
                case sadText:
                    return sadColor;
                case angryText:
                    return angryColor;
                case anxiousText:
                    return anxiousColor;
                case calmText:
                    return calmColor;
                default:
                    return "transparent"; // Default color if mood is not recognized
            }
        }
    }
}
