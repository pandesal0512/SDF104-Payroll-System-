package models;

public class Department {
    // Fields - data that each Department object holds
    private int id;
    private String name;
    private String description;

    // Constructor 1: For creating NEW departments (no ID yet)
    public Department(String name, String description) {
        this.name = name;
        this.description = description;
    }

    // Constructor 2: For loading EXISTING departments from database (has ID)
    public Department(int id, String name, String description) {
        this.id = id;
        this.name = name;
        this.description = description;
    }

    // Getters - methods to READ the data
    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Setters - methods to CHANGE the data
    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
