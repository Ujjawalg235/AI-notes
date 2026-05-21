package com.myanatomy.notesapp.service;

import com.myanatomy.notesapp.exception.NoteNotFoundException;
import com.myanatomy.notesapp.model.Note;
import com.myanatomy.notesapp.repository.NoteRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * NoteService – Business logic layer for all Note operations.
 *
 * Rule: Controller only handles HTTP. Service handles business logic.
 * This separation makes code testable, readable, and maintainable.
 */
@Service
public class NoteService {

    private static final Logger log = LoggerFactory.getLogger(NoteService.class);

    @Autowired
    private NoteRepository noteRepository;

    @Autowired
    private GeminiService geminiService;

    // ─── CRUD Operations ──────────────────────────────────────────────────────

    public List<Note> getAllNotes() {
        log.debug("Fetching all notes ordered by creation date");
        return noteRepository.findAllByOrderByCreatedAtDesc();
    }

    public Note getNoteById(String id) {
        return noteRepository.findById(id)
            .orElseThrow(() -> new NoteNotFoundException(id));
    }

    public Note createNote(Note note) {
        log.debug("Creating new note: {}", note.getTitle());
        // createdAt and updatedAt are auto-set by @CreatedDate / @LastModifiedDate
        return noteRepository.save(note);
    }

    public Note updateNote(String id, Note updatedNote) {
        Note existing = getNoteById(id);      // throws 404 if not found
        existing.setTitle(updatedNote.getTitle());
        existing.setContent(updatedNote.getContent());
        // Preserve AI summary and tags — don't reset on edit
        log.debug("Updating note id: {}", id);
        return noteRepository.save(existing); // @LastModifiedDate auto-updates
    }

    public void deleteNote(String id) {
        getNoteById(id); // verify exists first, throws 404 if not
        noteRepository.deleteById(id);
        log.debug("Deleted note id: {}", id);
    }

    // ─── Search ───────────────────────────────────────────────────────────────

    public List<Note> searchNotes(String keyword) {
        log.debug("Searching notes with keyword: {}", keyword);
        // Searches across title, content, AND ai-generated tags
        return noteRepository.searchAllFields(keyword);
    }

    public List<Note> getNotesByTag(String tag) {
        return noteRepository.findByTagsContainingIgnoreCase(tag);
    }

    // ─── AI Feature ───────────────────────────────────────────────────────────

    /**
     * Calls Gemini AI to analyze the note, then saves the AI summary and tags.
     *
     * @param id Note ID to summarize
     * @return Updated Note with aiSummary and tags filled in
     */
    @SuppressWarnings("unchecked")
    public Note summarizeNote(String id) {
        Note note = getNoteById(id);
        log.debug("Requesting AI summary for note: {}", note.getTitle());

        Map<String, Object> aiResult = geminiService.analyzeNote(
            note.getTitle(),
            note.getContent()
        );

        note.setAiSummary((String) aiResult.get("summary"));

        Object tagsObj = aiResult.get("tags");
        if (tagsObj instanceof List<?> rawList) {
            note.setTags((List<String>) rawList);
        }

        return noteRepository.save(note);
    }
}

