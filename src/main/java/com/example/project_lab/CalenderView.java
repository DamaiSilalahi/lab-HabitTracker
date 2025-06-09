package com.example.project_lab;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.control.ScrollPane; // Import ScrollPane

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.*;

public class CalenderView extends VBox {
    private HabitManager habitManager;
    private DatePicker datePicker;
    private Text summaryText;
    private ComboBox<String> summaryTypeComboBox;
    private VBox habitListBox;

    public CalenderView(HabitManager habitManager) {
        this.habitManager = habitManager;
        this.datePicker = new DatePicker(LocalDate.now());
        this.summaryText = new Text();
        this.summaryTypeComboBox = new ComboBox<>();
        summaryTypeComboBox.getItems().addAll("Ringkasan Mingguan", "Ringkasan Bulanan");
        summaryTypeComboBox.setValue("Ringkasan Mingguan");
        this.habitListBox = new VBox(8);

        this.setPadding(new Insets(20));
        this.setSpacing(15); // Spacing between major elements

        HBox topControls = new HBox(15);
        topControls.setAlignment(Pos.CENTER_LEFT);
        topControls.getChildren().addAll(datePicker, summaryTypeComboBox);

        VBox summaryContainer = new VBox(5);
        summaryContainer.setAlignment(Pos.TOP_LEFT);
        summaryContainer.getChildren().add(summaryText);
        summaryContainer.setPadding(new Insets(10, 0, 10, 0));

        ScrollPane habitScrollPane = new ScrollPane(habitListBox);
        habitScrollPane.setFitToWidth(true);
        habitScrollPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        VBox.setVgrow(habitScrollPane, Priority.ALWAYS);

        getChildren().addAll(topControls, summaryContainer, new Label("Kebiasaan pada Tanggal Terpilih:"), habitScrollPane);
    }

    public DatePicker getDatePicker() {
        return datePicker;
    }

    public Text getSummaryText() {
        return summaryText;
    }

    public ComboBox<String> getSummaryTypeComboBox() {
        return summaryTypeComboBox;
    }

    public VBox getHabitListBox() {
        return habitListBox;
    }

    public void updateCalendar() {
        LocalDate selectedDate = datePicker.getValue();
        String selectedSummaryType = summaryTypeComboBox.getValue();
        if ("Ringkasan Mingguan".equals(selectedSummaryType)) {
            updateWeeklySummary(selectedDate);
        } else {
            updateMonthlySummary(selectedDate);
        }
    }

    public void updateHabitsForSelectedDate(LocalDate date) {
        habitListBox.getChildren().clear();

        List<Habit> habits = habitManager.getHabitList();
        List<Habit> habitsForDate = new ArrayList<>();

        for (Habit habit : habits) {
            if (habit.getDate().equals(date)) {
                habitsForDate.add(habit);
            }
        }

        if (habitsForDate.isEmpty()) {
            Label noHabitLabel = new Label("Tidak ada kegiatan pada tanggal ini.");
            noHabitLabel.setFont(Font.font("System", 14));
            noHabitLabel.setTextFill(Color.web("#6b7280"));
            habitListBox.getChildren().add(noHabitLabel);
            habitListBox.setAlignment(Pos.CENTER);
            habitListBox.setPadding(new Insets(20));
        } else {
            habitListBox.setAlignment(Pos.TOP_LEFT);
            habitListBox.setPadding(Insets.EMPTY);
            HabitTrackerApp.createHabitRows(habitsForDate, habitListBox);
        }
    }

    private void updateWeeklySummary(LocalDate startDate) {
        LocalDate endDate = startDate.plusDays(6);
        List<Habit> habits = habitManager.getHabitList();

        Map<LocalDate, List<String>> dailyHabits = new HashMap<>();
        for (Habit habit : habits) {
            if (!habit.getDate().isBefore(startDate) && !habit.getDate().isAfter(endDate)) {
                if (habit.isCompleted()) {
                    dailyHabits.computeIfAbsent(habit.getDate(), k -> new ArrayList<>()).add(habit.getName());
                }
            }
        }

        StringBuilder weeklySummary = new StringBuilder("Ringkasan Mingguan:\n");
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            List<String> completedHabits = dailyHabits.getOrDefault(date, Collections.emptyList());
            weeklySummary.append(date.getDayOfWeek().toString()).append(": ")
                    .append(completedHabits.isEmpty() ? "-" : String.join(", ", completedHabits))
                    .append("\n");
        }

        Set<String> uniqueHabitsInWeek = new HashSet<>();
        for (Habit habit : habits) {
            if (!habit.getDate().isBefore(startDate) && !habit.getDate().isAfter(endDate)) {
                uniqueHabitsInWeek.add(habit.getName());
            }
        }

        int totalPossibleHabitCompletions = uniqueHabitsInWeek.size() * 7;
        int actualCompletedHabits = 0;
        for (List<String> completedList : dailyHabits.values()) {
            actualCompletedHabits += completedList.size();
        }

        double consistency = (totalPossibleHabitCompletions > 0) ? ((double) actualCompletedHabits / totalPossibleHabitCompletions) * 100 : 0;


        weeklySummary.append(String.format("Total kebiasaan unik dalam minggu ini: %d\nTotal selesai: %d\nKonsistensi (dari kebiasaan unik): %.2f%%\n",
                uniqueHabitsInWeek.size(), actualCompletedHabits, consistency));

        summaryText.setText(weeklySummary.toString());
    }

    private void updateMonthlySummary(LocalDate date) {
        YearMonth yearMonth = YearMonth.from(date);
        LocalDate monthStart = yearMonth.atDay(1);
        LocalDate monthEnd = yearMonth.atEndOfMonth();

        List<Habit> habits = habitManager.getHabitList();

        int totalActiveDays = 0;
        int totalSuccessfulHabits = 0;
        int totalDaysWithAllHabits = 0;

        // Get all unique habit names that exist for the current user, regardless of date
        Set<String> allUserHabitNames = new HashSet<>();
        for (Habit habit : habits) {
            allUserHabitNames.add(habit.getName());
        }
        int totalUniqueHabits = allUserHabitNames.size();

        LocalDate day = monthStart;
        while (!day.isAfter(monthEnd)) {
            final LocalDate currentDay = day;
            long completedCountForDay = habits.stream()
                    .filter(h -> h.getDate().equals(currentDay) && h.isCompleted())
                    .count();

            if (completedCountForDay > 0) {
                totalActiveDays++;
                totalSuccessfulHabits += completedCountForDay;
            }

            if (totalUniqueHabits > 0) {
                long habitsDefinedForDay = habits.stream()
                        .filter(h -> h.getDate().equals(currentDay))
                        .count();
                if (habitsDefinedForDay > 0 && completedCountForDay == habitsDefinedForDay) {
                    totalDaysWithAllHabits++;
                }
            }
            day = day.plusDays(1);
        }

        StringBuilder monthlySummary = new StringBuilder("Ringkasan Bulanan:\n");
        monthlySummary.append(String.format("Total hari aktif (setidaknya 1 kebiasaan selesai): %d\nTotal kebiasaan berhasil diselesaikan: %d\nHari dengan semua kebiasaan selesai: %d\n",
                totalActiveDays, totalSuccessfulHabits, totalDaysWithAllHabits));

        summaryText.setText(monthlySummary.toString());
    }
}