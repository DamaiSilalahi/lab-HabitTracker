package com.example.project_lab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class HabitStorageService {
    private static final Path DATA_DIR = Paths.get("data");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE; // yyyy-MM-dd

    public HabitStorageService() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
        } catch (IOException e) {
            System.err.println("Error initializing habit storage service directory: " + e.getMessage());
        }
    }

    private Path getHabitFilePath(String username) {
        return DATA_DIR.resolve(username + "_habits.csv");
    }

    public List<Habit> loadHabits(String username) {
        List<Habit> loadedHabits = new ArrayList<>();
        Path habitFile = getHabitFilePath(username);

        if (!Files.exists(habitFile)) {
            return loadedHabits;
        }

        try (BufferedReader reader = Files.newBufferedReader(habitFile)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",", 3);
                if (parts.length == 3) {
                    String name = parts[0];
                    LocalDate date = LocalDate.parse(parts[1], DATE_FORMATTER);
                    boolean status = Boolean.parseBoolean(parts[2]);

                    Habit habit = new Habit(name, date);
                    if (status) {
                        habit.markCompletedInternal();
                    }
                    loadedHabits.add(habit);
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading habits for " + username + ": " + e.getMessage());
        }
        return loadedHabits;
    }

    public void saveHabits(String username, List<Habit> habits) {
        Path habitFile = getHabitFilePath(username);
        try (BufferedWriter writer = Files.newBufferedWriter(habitFile, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (Habit habit : habits) {
                writer.write(habit.getName() + "," +
                        habit.getDate().format(DATE_FORMATTER) + "," +
                        habit.isCompleted());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving habits for " + username + ": " + e.getMessage());
        }
    }
}