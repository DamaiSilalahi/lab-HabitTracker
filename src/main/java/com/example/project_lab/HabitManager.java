package com.example.project_lab;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class HabitManager {
    private List<Habit> habitList;
    private String username;
    private HabitStorageService habitStorageService;

    public HabitManager(String username, HabitStorageService habitStorageService) {
        this.username = username;
        this.habitStorageService = habitStorageService;
        this.habitList = habitStorageService.loadHabits(username);
        resetAllHabitsIfNewDay();
    }

    public void addHabit(Habit habit) {
        boolean exists = habitList.stream()
                .anyMatch(h -> h.getName().equalsIgnoreCase(habit.getName()) && h.getDate().equals(habit.getDate()));
        if (!exists) {
            habitList.add(habit);
            saveHabitsData();
        } else {
            System.out.println("Habit '" + habit.getName() + "' sudah ada untuk hari ini.");
        }
    }

    public void resetAllHabitsIfNewDay() {
        LocalDate today = LocalDate.now();
        boolean changed = false;
        for (Habit h : habitList) {
            if (!h.getDate().equals(today)) {
                h.resetStatusIfNewDay(today);
                changed = true;
            }
        }
        if (changed) {
            saveHabitsData();
        }
    }

    public void updateHabitStatus(Habit habit, boolean completed) {
        if (completed) {
            habit.markCompleted();
        } else {
            habit.resetStatus();
        }
        saveHabitsData();
    }

    public List<Habit> getHabitList() {
        resetAllHabitsIfNewDay();
        return new ArrayList<>(habitList);
    }

    public void saveHabitsData() {
        habitStorageService.saveHabits(username, habitList);
    }
}