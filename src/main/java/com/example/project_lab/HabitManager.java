package com.example.project_lab;

// HabitManager.java
import java.time.LocalDate;
import java.util.ArrayList;

public class HabitManager {
    private ArrayList<Habit> habitList = new ArrayList<>();

    public void addHabit(Habit habit) {
        habitList.add(habit);
    }

    public void resetAllHabitsIfNewDay() {
        LocalDate today = LocalDate.now();
        for (Habit h : habitList) {
            h.resetStatusIfNewDay(today);
        }
    }

    public ArrayList<Habit> getHabitList() {
        return habitList;
    }
}

