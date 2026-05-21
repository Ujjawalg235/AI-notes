package com.myanatomy.notesapp.controller;

import com.myanatomy.notesapp.model.Note;
import com.myanatomy.notesapp.service.NoteService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * NoteController – REST API controller.
 *
 * Responsibilities (Controller's ONLY job):
 *   1. Map HTTP requests to the right method
 *   2. Extract data from request (body, path, query params)
 *   3. Call NoteService
 *   4. Return proper HTTP response
 *
 * Business logic belongs in NoteService — NEVER here.
 *
 * Base URL: http://localhost:8080/api/notes
 */
@RestController
@RequestMapping("/api/notes")
public class NoteController {

    @Autowired
    private NoteService noteService;

    /**
     * GET /api/notes
     * Returns all notes, newest first.
     */
    @GetMapping
    public ResponseEntity<List<Note>> getAllNotes() {
        return ResponseEntity.ok(noteService.getAllNotes());
    }

    /**
     * GET /api/notes/{id}
     * Returns one specific note by its MongoDB ObjectId.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Note> getNoteById(@PathVariable String id) {
        return ResponseEntity.ok(noteService.getNoteById(id));
    }

    /**
     * POST /api/notes
     * Creates a new note. @Valid triggers @NotBlank validation on the Note fields.
     * Returns 201 Created with the saved note (including generated id).
     */
    @PostMapping
    public ResponseEntity<Note> createNote(@Valid @RequestBody Note note) {
        Note created = noteService.createNote(note);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * PUT /api/notes/{id}
     * Replaces the note's content. Returns the updated note.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Note> updateNote(
            @PathVariable String id,
            @Valid @RequestBody Note note) {
        return ResponseEntity.ok(noteService.updateNote(id, note));
    }

    /**
     * DELETE /api/notes/{id}
     * Deletes the note. Returns 200 with a success message.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteNote(@PathVariable String id) {
        noteService.deleteNote(id);
        return ResponseEntity.ok(Map.of("message", "Note deleted successfully", "id", id));
    }

    /**
     * GET /api/notes/search?keyword=java
     * Full-text search across title, content, and AI tags.
     */
    @GetMapping("/search")
    public ResponseEntity<List<Note>> searchNotes(@RequestParam String keyword) {
        return ResponseEntity.ok(noteService.searchNotes(keyword));
    }

    /**
     * GET /api/notes/tag?name=spring
     * Filter notes by a specific AI-generated tag.
     */
    @GetMapping("/tag")
    public ResponseEntity<List<Note>> getNotesByTag(@RequestParam String name) {
        return ResponseEntity.ok(noteService.getNotesByTag(name));
    }

    /**
     * POST /api/notes/{id}/ai
     * Triggers Gemini AI to analyze the note.
     * Returns the updated note with aiSummary and tags populated.
     */
    @PostMapping("/{id}/ai")
    public ResponseEntity<Note> summarizeNote(@PathVariable String id) {
        return ResponseEntity.ok(noteService.summarizeNote(id));
    }
}

// Start coding here