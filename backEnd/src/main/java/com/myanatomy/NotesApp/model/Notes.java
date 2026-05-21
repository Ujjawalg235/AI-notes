package com.myanatomy.notesapp.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.index.TextIndexed;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * MongoDB Document representing a Note.
 *
 * @Document → maps this class to the "notes" collection in MongoDB
 * @Id       → marks the primary key field (MongoDB auto-generates ObjectId)
 *
 * Why MongoDB for Notes?
 * - Flexible schema: we can add aiSummary/tags later without migration
 * - Native JSON storage: our Note IS a JSON document
 * - Fast text search on title/content
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "notes")
public class Note {

    @Id
    private String id;           // MongoDB ObjectId (auto-generated, String not Long)

    @NotBlank(message = "Title cannot be empty")
    @Size(max = 200, message = "Title must be under 200 characters")
    @TextIndexed                 // Enables full-text search on this field
    private String title;

    @TextIndexed                 // Enables full-text search on this field
    private String content;

    private String aiSummary;    // Filled by Gemini AI when user clicks Summarize

    private List<String> tags = new ArrayList<>();   // AI-generated topic tags

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    // Convenience constructor for creation
    public Note(String title, String content) {
        this.title = title;
        this.content = content;
    }
}
