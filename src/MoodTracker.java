import java.io.BufferedReader;
import java.io.File;
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

/**
 * MoodTracker Application that lets you pick from 5 options -
 * Happy, Sad, Angry, Anxious, Calm - and keeps track over time
 * your choices.
 * 
 * @author LoomisFire
 */
public class MoodTracker extends Application {

    /* The label for each text button */
    private Label moodLabel;

    /* Map for mood entries */
    private ObservableMap<String, MoodEntry> moodEntries = FXCollections.observableHashMap();

    /* File location for saving mood entries */
    private final String ENTRIES_FILE_NAME = "mood_entries.csv";

    /* Application Dimensions */
    private int width = 300;
    private int height = 400;

    /* Individual text for each button */
    private static final String HAPPY_TEXT = "Happy";
    private static final String SAD_TEXT = "Sad";
    private static final String ANGRY_TEXT = "Angry";
    private static final String ANXIOUS_TEXT = "Anxious";
    private static final String CALM_TEXT = "Calm";

    /* Individual background colors for each button */
    private static final String HAPPY_COLOR = "#4CAF50";
    private static final String SAD_COLOR = "#2196F3";
    private static final String ANGRY_COLOR = "#f44336";
    private static final String ANXIOUS_COLOR = "#FFC107";
    private static final String CALM_COLOR = "#8BC34A";

    /**
     * Main method to launch the JavaFX application.
     * 
     * @param args the command line arguments
     * @throws Exception if there is an issue launching the application
     */
    public static void main(String[] args) throws Exception {
        System.out.println("MoodTracker Launched.");
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        loadMoodEntries();

        moodLabel = new Label("Select your mood:");
        moodLabel.setAlignment(Pos.CENTER);

        Button happyButton = createMoodButton(HAPPY_TEXT, HAPPY_COLOR);
        Button sadButton = createMoodButton(SAD_TEXT, SAD_COLOR);
        Button angryButton = createMoodButton(ANGRY_TEXT, ANGRY_COLOR);
        Button anxiousButton = createMoodButton(ANXIOUS_TEXT, ANXIOUS_COLOR);
        Button calmButton = createMoodButton(CALM_TEXT, CALM_COLOR);

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
        Scene historyScene = createHistoryScene(primaryStage, mainScene);

        // Event handler for the history button
        historyButton.setOnAction(e -> primaryStage.setScene(historyScene));

        primaryStage.setTitle("Mood Tracker");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    /**
     * Creates a mood button with the specified mood text and color.
     * 
     * @param mood  the text to display on the button
     * @param color the background color of the button
     * @return the created Button
     */
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

    /**
     * Stores the mood entry with the current date.
     * 
     * @param mood the mood to store
     */
    private void storeMoodEntry(String mood) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        MoodEntry newEntry = new MoodEntry(mood, date);

        moodEntries.put(date, newEntry);

        saveMoodEntries(); // Save the updated entries to file

        // Update an switch to the history scene
        Stage stage = (Stage) moodLabel.getScene().getWindow();
        Scene currentScene = stage.getScene();
        if (currentScene != null) {
            Scene historyScene = createHistoryScene(stage, currentScene);
            stage.setScene(historyScene);
        }
    }

    /**
     * Saves mood entries to the CSV file.
     */
    private void saveMoodEntries() {
        try (PrintWriter writer = new PrintWriter(new FileWriter(ENTRIES_FILE_NAME))) {
            for (MoodEntry entry : moodEntries.values()) {
                writer.println(entry.getMood() + "," + entry.getDate());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads mood entries from the CSV file or create file if it does not exist.
     */
    private void loadMoodEntries() {
        File file = new File(ENTRIES_FILE_NAME);
        if (!file.exists()) {
            try (PrintWriter writer = new PrintWriter(new FileWriter(ENTRIES_FILE_NAME))) {
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        try (BufferedReader reader = new BufferedReader(new FileReader(ENTRIES_FILE_NAME))) {
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

    /**
     * Creates the history scene to display the calendar.
     * 
     * @param primaryStage the primary stage of the application
     * @param mainScene    the main scene to return to
     * @return the created history scene
     */
    private Scene createHistoryScene(Stage primaryStage, Scene mainScene) {
        GridPane calendarPane = createCalendar(YearMonth.now());

        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> primaryStage.setScene(mainScene));

        VBox historyLayout = new VBox(10, calendarPane, backButton);
        historyLayout.setAlignment(Pos.CENTER);
        historyLayout.setStyle("-fx-padding: 20;");
        Scene historyScene = new Scene(historyLayout, width, height);
        return historyScene;
    }

    /**
     * Creates a calendar for the specified month and year.
     * 
     * @param yearMonth the year and month to display
     * @return the created GridPane representing the calendar
     */
    private GridPane createCalendar(YearMonth yearMonth) {
        GridPane calendarPane = new GridPane();
        calendarPane.setAlignment(Pos.CENTER);
        calendarPane.setHgap(1);
        calendarPane.setVgap(1);

        // Add label for month and year
        Label monthYearLabel = new Label(yearMonth.getMonth().toString() + " " + yearMonth.getYear());
        monthYearLabel.setStyle("-fx-font-weight: bold;");
        // monthYearLabel.setPrefWidth(280); // Set width to match calendar width
        monthYearLabel.setPrefSize(280, 40); // Set width to match calendar width
        monthYearLabel.setAlignment(Pos.CENTER);
        calendarPane.add(monthYearLabel, 0, 0, 7, 1); // Span across all columns

        // Add left arrow button
        Button leftArrowButton = new Button("<");
        leftArrowButton.setPrefSize(40, 40);
        leftArrowButton.setAlignment(Pos.CENTER);
        leftArrowButton.setOnAction(e -> {
            YearMonth previousMonth = yearMonth.minusMonths(1);
            calendarPane.getChildren().clear();
            calendarPane.add(createCalendar(previousMonth), 0, 0, 7, 1);
        });
        calendarPane.add(leftArrowButton, 0, 1);

        // Add right arrow button
        Button rightArrowButton = new Button(">");
        rightArrowButton.setAlignment(Pos.CENTER);
        rightArrowButton.setPrefSize(40, 40);
        rightArrowButton.setOnAction(e -> {
            YearMonth nextMonth = yearMonth.plusMonths(1);
            calendarPane.getChildren().clear();
            calendarPane.add(createCalendar(nextMonth), 0, 0, 7, 1);
        });
        calendarPane.add(rightArrowButton, 6, 1);

        // Add days of the week
        String[] daysOfWeek = { "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
        for (int i = 0; i < 7; i++) {
            Label dayOfWeekLabel = new Label(daysOfWeek[i]);
            dayOfWeekLabel.setStyle("-fx-font-weight: bold;");
            dayOfWeekLabel.setPrefSize(40, 40);
            dayOfWeekLabel.setAlignment(Pos.CENTER);
            calendarPane.add(dayOfWeekLabel, i, 2);
        }

        // Get the first day of the month and the number of days in the month
        LocalDate firstDayOfMonth = yearMonth.atDay(1);
        int lengthOfMonth = yearMonth.lengthOfMonth();

        // Populate the calendar with day labels and mood indicators
        int row = 1;
        int col = firstDayOfMonth.getDayOfWeek().getValue() % 7; // Adjust for Sunday being 7 (Java's DayOfWeek enum
                                                                 // starts with Monday as 1)
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
            calendarPane.add(moodIndicator, col, row + 2);

            col++;
            if (col == 7) {
                col = 0;
                row += 2; // Move to the next row (skipping the row for mood indicators)
            }
        }

        return calendarPane;
    }

    /**
     * MoodEntry class to store mood data.
     */
    public static class MoodEntry {
        private String mood;
        private final String date;

        /**
         * Constructs a MoodEntry with the specified mood and date.
         * 
         * @param mood the mood
         * @param date the date
         */
        public MoodEntry(String mood, String date) {
            this.mood = mood;
            this.date = date;
        }

        /**
         * Gets the mood.
         * 
         * @return the mood
         */
        public String getMood() {
            return mood;
        }

        /**
         * Sets the mood.
         * 
         * @param mood the mood to set
         */
        public void setMood(String mood) {
            this.mood = mood;
        }

        /**
         * Gets the date.
         * 
         * @return the date
         */
        public String getDate() {
            return date;
        }

        /**
         * Gets the color associated with the mood.
         * 
         * @return the color as a string
         */
        public String getColor() {
            switch (mood) {
                case HAPPY_TEXT:
                    return HAPPY_COLOR;
                case SAD_TEXT:
                    return SAD_COLOR;
                case ANGRY_TEXT:
                    return ANGRY_COLOR;
                case ANXIOUS_TEXT:
                    return ANXIOUS_COLOR;
                case CALM_TEXT:
                    return CALM_COLOR;
                default:
                    return "transparent"; // Default color if mood is not recognized
            }
        }
    }
}
