package com.myanatomy.notesapp.repository;

import com.myanatomy.notesapp.model.Note;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * NoteRepository – Data access layer for Note documents.
 *
 * By extending MongoRepository<Note, String> we get FREE:
 *   - findAll()        → db.notes.find({})
 *   - findById(id)     → db.notes.find({ _id: id })
 *   - save(note)       → insert or update
 *   - deleteById(id)   → db.notes.deleteOne({ _id: id })
 *   - count()          → db.notes.countDocuments()
 *
 * We add custom queries using Spring Data method naming convention.
 * Spring converts the method name → MongoDB query automatically!
 */
@Repository
public interface NoteRepository extends MongoRepository<Note, String> {

    /**
     * findByTitleContainingIgnoreCase("java")
     * → db.notes.find({ title: /java/i })
     */
    List<Note> findByTitleContainingIgnoreCase(String keyword);

    /**
     * Search in both title AND content simultaneously.
     * Spring generates: find where title OR content matches regex.
     */
    List<Note> findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(
        String titleKeyword,
        String contentKeyword
    );

    /**
     * Find notes that have a specific tag in their tags array.
     * → db.notes.find({ tags: { $in: [tag] } })
     */
    List<Note> findByTagsContainingIgnoreCase(String tag);

    /**
     * Get all notes ordered by creation time (newest first).
     * Equivalent to: ORDER BY created_at DESC in SQL
     */
    List<Note> findAllByOrderByCreatedAtDesc();

    /**
     * Custom MongoDB query using @Query annotation.
     * Useful when method naming isn't enough.
     *
     * ?0 = first parameter (keyword)
     * $regex = MongoDB regex operator
     * $options: "i" = case-insensitive
     */
    @Query("{ $or: [ { 'title': { $regex: ?0, $options: 'i' } }, { 'content': { $regex: ?0, $options: 'i' } }, { 'tags': { $regex: ?0, $options: 'i' } } ] }")
    List<Note> searchAllFields(String keyword);
}
