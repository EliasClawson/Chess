package model;

public class UserData {
    private String username;
    private String password;
    private String email;

    public UserData(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }

    // Getters
    public String getUsername() {
        System.out.println("Getting username: " + username);
        return username;
    }

    public String getPassword() {
        System.out.println("Getting password");
        return password;
    }

    public String getEmail() {
        System.out.println("Getting email: " + email);
        return email;
    }
}