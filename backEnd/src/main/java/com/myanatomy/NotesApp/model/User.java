package com.myanatomy.notesapp.model;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB Document representing a registered User.
 *
 * @Document  -> maps to the "users" collection in MongoDB
 * @Indexed(unique=true) -> ensures no two users share the same email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "users")
public class User {

    @Id
    private String id;

    @NotBlank(message = "Name cannot be empty")
    private String name;

    @Email
    @NotBlank(message = "Email cannot be empty")
    @Indexed(unique = true)
    private String email;

    // BCrypt-hashed password — NEVER store plain text
    @NotBlank
    private String password;

    @CreatedDate
    private LocalDateTime createdAt;
}