package com.example.project_lab;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class UserService {
    private static final Path DATA_DIR = Paths.get("data");
    private static final Path USERS_FILE = DATA_DIR.resolve("users.csv");
    private List<User> users;

    public UserService() {
        try {
            if (!Files.exists(DATA_DIR)) {
                Files.createDirectories(DATA_DIR);
            }
            if (!Files.exists(USERS_FILE)) {
                Files.createFile(USERS_FILE);
            }
        } catch (IOException e) {
            System.err.println("Error initializing user service directory/file: " + e.getMessage());
        }
        this.users = loadUsers();
    }

    private List<User> loadUsers() {
        List<User> loadedUsers = new ArrayList<>();
        try (BufferedReader reader = Files.newBufferedReader(USERS_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 2) {
                    loadedUsers.add(new User(parts[0], parts[1]));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
        return loadedUsers;
    }

    private void saveUsers() {
        try (BufferedWriter writer = Files.newBufferedWriter(USERS_FILE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING)) {
            for (User user : users) {
                writer.write(user.getUsername() + "," + user.getHashedPassword());
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Error saving users: " + e.getMessage());
        }
    }

    public boolean signUp(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.isEmpty()) {
            System.err.println("Username or password cannot be empty.");
            return false;
        }
        if (users.stream().anyMatch(u -> u.getUsername().equalsIgnoreCase(username))) {
            System.err.println("Username already exists.");
            return false;
        }
        String hashedPassword = PasswordUtils.hashPassword(password);
        users.add(new User(username, hashedPassword));
        saveUsers();
        return true;
    }

    public Optional<User> signIn(String username, String password) {
        Optional<User> foundUser = users.stream()
                .filter(u -> u.getUsername().equalsIgnoreCase(username))
                .findFirst();

        if (foundUser.isPresent()) {
            if (PasswordUtils.verifyPassword(password, foundUser.get().getHashedPassword())) {
                return foundUser;
            }
        }
        return Optional.empty();
    }
}