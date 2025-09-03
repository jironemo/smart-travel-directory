package com.smarttravel.myanmar;

import com.google.firebase.Timestamp;

public class User {
    private String id;
    private String email;
    private String password;
    private String username;
    private String profile_picture;
    private Timestamp created_at;
    private String user_type; // Added userType property

    // Static field for current user
    private static User currentUser;

    public User() {}

    public static User getCurrentUser() {
        return currentUser;
    }
    public static void setCurrentUser(User user) {
        currentUser = user;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getProfile_picture() { return profile_picture; }
    public void setProfile_picture(String profile_picture) { this.profile_picture = profile_picture; }

    public Timestamp getCreated_at() { return created_at; }
    public void setCreated_at(Timestamp created_at) { this.created_at = created_at; }

    public String getUser_type() { return user_type; } // Getter for userType
    public void setUser_type(String user_type) { this.user_type = user_type; } // Setter for userType

    public String getCreatedAtString() {
        return created_at != null ? created_at.toDate().toString() : "";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
