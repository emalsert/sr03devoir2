//package com.example.devoir2.models;
//
//import jakarta.persistence.*;
//import java.util.Arrays;
//
/// **
// * Represents a user entity in the system.
// * Maps to the 'users' table in the database.
// */
//@Entity
//@Table(name = "users")
//public class User {
//    /**
//     * Unique identifier for the user
//     * Primary key, auto-incremented
//     */
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    @Column(name = "user_id")
//    private Integer userId;
//
//    /**
//     * User's last name
//     * Required field, max length 100 characters
//     */
//    @Column(name = "last_name", nullable = false, length = 100)
//    private String lastName;
//
//    /**
//     * User's first name
//     * Required field, max length 100 characters
//     */
//    @Column(name = "first_name", nullable = false, length = 100)
//    private String firstName;
//
//    /**
//     * User's email address
//     * Required field, must be unique, max length 255 characters
//     */
//    @Column(name = "email", nullable = false, unique = true, length = 255)
//    private String email;
//
//    /**
//     * User's hashed password
//     * Required field, max length 255 characters
//     */
//    @Column(name = "password", nullable = false, length = 255)
//    private String password;
//
//    /**
//     * User's profile picture
//     * Stored as binary data (BLOB)
//     */
//    @Lob
//    @Column(name = "avatar", columnDefinition = "LONGBLOB")
//    private byte[] avatar;
//
//    /**
//     * Flag indicating admin privileges
//     * Defaults to false
//     */
//    @Column(name = "is_admin", nullable = false)
//    private Boolean isAdmin = false;
//
//    /**
//     * Flag indicating if user is currently connected
//     * Defaults to false
//     */
//    @Column(name = "is_connected", nullable = false)
//    private Boolean isConnected = false;
//
//    /**
//     * Default constructor required by JPA
//     */
//    public User() {
//    }
//
//    /**
//     * Creates a new user with basic information
//     * @param lastName User's last name
//     * @param firstName User's first name
//     * @param email User's email address
//     * @param password User's password (will be hashed)
//     */
//    public User(String lastName, String firstName, String email, String password) {
//        this.lastName = lastName;
//        this.firstName = firstName;
//        this.email = email;
//        this.password = password;
//    }
//
//    // Getters and setters with brief JavaDoc comments
//
//    /** @return the user's unique ID */
//    public Integer getUserId() {
//        return userId;
//    }
//
//    /** @param userId the ID to set */
//    public void setUserId(Integer userId) {
//        this.userId = userId;
//    }
//
//    /** @return the user's last name */
//    public String getLastName() {
//        return lastName;
//    }
//
//    /** @param lastName the last name to set */
//    public void setLastName(String lastName) {
//        this.lastName = lastName;
//    }
//
//    /** @return the user's first name */
//    public String getFirstName() {
//        return firstName;
//    }
//
//    /** @param firstName the first name to set */
//    public void setFirstName(String firstName) {
//        this.firstName = firstName;
//    }
//
//    /** @return the user's email */
//    public String getEmail() {
//        return email;
//    }
//
//    /** @param email the email to set */
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    /** @return the hashed password */
//    public String getPassword() {
//        return password;
//    }
//
//    /** @param password the password to set (will be hashed) */
//    public void setPassword(String password) {
//        this.password = password;
//    }
//
//    /** @return the user's avatar image data */
//    public byte[] getAvatar() {
//        return avatar;
//    }
//
//    /** @param avatar the avatar image data to set */
//    public void setAvatar(byte[] avatar) {
//        this.avatar = avatar;
//    }
//
//    /** @return true if user has admin privileges */
//    public Boolean getIsAdmin() {
//        return isAdmin;
//    }
//
//    /** @param isAdmin set admin privileges status */
//    public void setIsAdmin(Boolean isAdmin) {
//        this.isAdmin = isAdmin;
//    }
//
//    /** @return true if user is currently connected */
//    public Boolean getIsConnected() {
//        return isConnected;
//    }
//
//    /** @param isConnected set user connection status */
//    public void setIsConnected(Boolean isConnected) {
//        this.isConnected = isConnected;
//    }
//
//    /**
//     * Returns a string representation of the user (excluding sensitive data)
//     * @return string representation
//     */
//    @Override
//    public String toString() {
//        return "User{" +
//                "userId=" + userId +
//                ", lastName='" + lastName + '\'' +
//                ", firstName='" + firstName + '\'' +
//                ", email='" + email + '\'' +
//                ", password='[PROTECTED]'" +
//                ", avatar=" + (avatar != null ? "[" + avatar.length + " bytes]" : "null") +
//                ", isAdmin=" + isAdmin +
//                ", isConnected=" + isConnected +
//                '}';
//    }
//}