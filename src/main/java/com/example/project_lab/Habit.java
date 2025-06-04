package com.example.project_lab;

import java.time.LocalDate;

public class Habit extends UserActivity {
    private LocalDate date;

    public Habit(String name, LocalDate date) {
        super(name);
        this.date = date;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    @Override
    public void markCompleted() {
        this.status = true;
    }

    public void markCompletedInternal() {
        this.status = true;
    }

    public void resetStatusIfNewDay(LocalDate today) {
        if (!date.equals(today)) {
            resetStatus();
            date = today;
        }
    }
}