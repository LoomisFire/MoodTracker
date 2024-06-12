import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import javafx.application.Application;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
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
    private TableView<MoodEntry> tableView;
    private final String FILE_NAME = "mood_entries.csv";

    @Override
    public void start(Stage primaryStage) {
        loadMoodEntries(); // Load mood entries from file

        // Initialize the mood label
        moodLabel = new Label("Select your mood:");
        moodLabel.setAlignment(Pos.CENTER);

        // Create buttons for different moods
        Button happyButton = createMoodButton("Happy", "#4CAF50");
        Button sadButton = createMoodButton("Sad", "#2196F3");
        Button angryButton = createMoodButton("Angry", "#f44336");
        Button anxiousButton = createMoodButton("Anxious", "#FFC107");
        Button calmButton = createMoodButton("Calm", "#8BC34A");
        
        Button historyButton = new Button("View History");

        // Add margins to moodLabel and historyButton
        VBox.setMargin(moodLabel, new Insets(0, 0, 10, 0));
        VBox.setMargin(historyButton, new Insets(10, 0, 0, 0));

        // Layout for the main scene
        VBox root = new VBox(0); // Set spacing to #
        root.setAlignment(Pos.CENTER); // Center align all elements
        root.getChildren().addAll(moodLabel, happyButton, sadButton, angryButton, anxiousButton, calmButton,
                historyButton);
        root.setStyle("-fx-padding: 20;");

        // Set VBox to grow the buttons to fill available space
        VBox.setVgrow(happyButton, Priority.ALWAYS);
        VBox.setVgrow(sadButton, Priority.ALWAYS);
        VBox.setVgrow(angryButton, Priority.ALWAYS);
        VBox.setVgrow(anxiousButton, Priority.ALWAYS);
        VBox.setVgrow(calmButton, Priority.ALWAYS);

        Scene mainScene = new Scene(root, 300, 400);

        // Create the history scene
        tableView = new TableView<>();
        TableColumn<MoodEntry, String> moodColumn = new TableColumn<>("Mood");
        moodColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getMood()));
        TableColumn<MoodEntry, String> dateColumn = new TableColumn<>("Date");
        dateColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getDate()));

        // Add columns to the table using an explicit list
        tableView.getColumns().add(moodColumn);
        tableView.getColumns().add(dateColumn);

        tableView.setItems(FXCollections.observableArrayList(moodEntries.values()));

        Button backButton = new Button("Back to Main");
        backButton.setOnAction(e -> primaryStage.setScene(mainScene));

        VBox historyLayout = new VBox(10, tableView, backButton);
        historyLayout.setAlignment(Pos.CENTER);
        historyLayout.setStyle("-fx-padding: 20;");
        Scene historyScene = new Scene(historyLayout, 300, 400);

        // Event handler for the history button
        historyButton.setOnAction(e -> primaryStage.setScene(historyScene));

        primaryStage.setTitle("Mood Tracker");
        primaryStage.setScene(mainScene);
        primaryStage.show();
    }

    private Button createMoodButton(String mood, String color) {
        Button button = new Button(mood);
        button.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; -fx-font-size: 14px;");
        button.setMaxWidth(Double.MAX_VALUE); // Make button expand to fill available width
        button.setPrefHeight(50); // Set a preferred height for all buttons

        button.setOnAction(e -> {
            moodLabel.setText("Current Mood: " + mood);
            storeMoodEntry(mood);
        });

        return button;
    }

    private void storeMoodEntry(String mood) {
        String date = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        MoodEntry newEntry = new MoodEntry(mood, date);

        moodEntries.put(date, newEntry); // Store or replace the entry in the map

        saveMoodEntries(); // Save the updated entries to file
        tableView.setItems(FXCollections.observableArrayList(moodEntries.values()));
        tableView.refresh(); // Refresh the table view
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

        public void setMood(String mood) { // Setter for mood
            this.mood = mood;
        }

        public String getDate() {
            return date;
        }
    }
}
