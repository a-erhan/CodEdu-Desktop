package com.codedu.models;

/**
 * Model representing user data according to the UML diagram.
 */
public class User {

    private Long id;
    private String username;
    private String email;
    private String password;
    private Role role;
    private int tokenBalance;
    private Competitor competitor;
    private UserInventory inventory;

    public User() {
    }

    // --- ID ---
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    // --- Username ---
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    // --- Email ---
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // --- Password ---
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    // --- Role ---
    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    // --- Token Balance ---
    public int getTokenBalance() {
        return tokenBalance;
    }

    public void setTokenBalance(int tokenBalance) {
        this.tokenBalance = tokenBalance;
    }

    // --- Competitor ---
    public Competitor getCompetitor() {
        return competitor;
    }

    public void setCompetitor(Competitor competitor) {
        this.competitor = competitor;
    }

    // --- Inventory ---
    public UserInventory getInventory() {
        return inventory;
    }

    public void setInventory(UserInventory inventory) {
        this.inventory = inventory;
    }

    // --- Methods from UML ---
    public void login() {
        // TODO: implement login logic
    }

    public void updateProgress() {
        // TODO: implement progress update logic
    }
}