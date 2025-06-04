package com.example.project_lab;

public abstract class UserActivity {
    protected String name;
    protected boolean status;

    public UserActivity(String name) {
        this.name = name;
        this.status = false;
    }

    public String getName() {
        return name;
    }

    public boolean isCompleted() {
        return status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public abstract void markCompleted();

    public void resetStatus() {
        status = false;
    }
}

