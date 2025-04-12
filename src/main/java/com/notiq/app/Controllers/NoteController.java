package com.notiq.app.Controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.notiq.app.Dtos.NoteRequestDto;
import com.notiq.app.Dtos.NoteResponseDto;

import com.notiq.app.Service.NoteService;

@RestController
@RequestMapping("/notes")
public class NoteController {
    private final NoteService noteService;

    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    // post notes
    @PostMapping
    public ResponseEntity<String> createNote(@RequestBody NoteRequestDto noteDto) {
        return noteService.createNote(noteDto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<String> editNote(@PathVariable Integer id, @RequestBody NoteRequestDto updatedNote) {
        return noteService.editNote(id, updatedNote);
    }

    @PutMapping("/{id}/favorite")
    public ResponseEntity<String> toggleFavorite(@PathVariable Integer id) {
        return noteService.toggleFavorite(id);
    }

    @PutMapping("/{id}/archive")
    public ResponseEntity<String> toggleArchive(@PathVariable Integer id) {
        return noteService.toggleArchive(id);
    }

    @PutMapping("/{id}/trash")
    public ResponseEntity<String> toggleTrash(@PathVariable Integer id) {
        return noteService.toggleTrash(id);
    }

    // Get notes
    @GetMapping
    public ResponseEntity<List<NoteResponseDto>> getAllNotes() {
        return noteService.getAllActiveNotes();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<List<NoteResponseDto>> getDashboardNote() {
        return noteService.getRecentNotesForDashboard();
    }

    @GetMapping("/favorite")
    public ResponseEntity<List<NoteResponseDto>> getFavorite() {
        return noteService.getAllFavoriteNotes();
    }

    @GetMapping("/archived")
    public ResponseEntity<List<NoteResponseDto>> getArchived() {
        return noteService.getAllArchivedNotes();
    }

    @GetMapping("/trashed")
    public ResponseEntity<List<NoteResponseDto>> getTrashed() {
        return noteService.getAllTrashedNotes();
    }
}
