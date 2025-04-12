package com.notiq.app.Service;

import java.util.List;

import org.owasp.html.PolicyFactory;
import org.owasp.html.Sanitizers;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.notiq.app.Dtos.NoteRequestDto;
import com.notiq.app.Dtos.NoteResponseDto;
import com.notiq.app.Model.Note;
import com.notiq.app.Model.User;
import com.notiq.app.Repo.NoteRepo;
import com.notiq.app.Repo.UserRepo;

@Service
public class NoteService {
    private final NoteRepo noteRepository;
    private final UserRepo userRepository;

    private final PolicyFactory policy = Sanitizers.FORMATTING.and(Sanitizers.LINKS).and(Sanitizers.BLOCKS);

    public NoteService(NoteRepo noteRepository, UserRepo userRepository) {
        this.userRepository = userRepository;
        this.noteRepository = noteRepository;
    }

    public NoteResponseDto mapResponseDto(Note note) {
        return new NoteResponseDto(note.getId(), note.getTitle(), note.getContent(), note.isFavorite(),
                note.isArchived(), note.isTrashed(), note.getCreatedAt(), note.getUpdatedAt(),
                note.getUser().getEmail());
    }

    public ResponseEntity<String> createNote(NoteRequestDto noteDto) {
        try {
            String safeHtml = policy.sanitize(noteDto.content());
            String safeTitle = policy.sanitize(noteDto.title());

            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            Note note = new Note();
            note.setTitle(safeTitle);
            note.setContent(safeHtml);
            note.setUser(user);
            noteRepository.save(note);
            return ResponseEntity.ok("Note created successfully");
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("User not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error creating note: " + e.getMessage());
        }
    }

    public ResponseEntity<String> editNote(Integer id, NoteRequestDto updatedNote) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));

            Note existingNote = noteRepository.findById(id).orElseThrow(() -> new RuntimeException("Note not found"));

            if (!existingNote.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("You are not authorized to edit the note.");
            }

            String safeHtml = policy.sanitize(updatedNote.content());
            String safeTitle = policy.sanitize(updatedNote.title());

            existingNote.setTitle(safeTitle);
            existingNote.setContent(safeHtml);
            existingNote.setUpdatedAt(java.time.LocalDateTime.now());

            noteRepository.save(existingNote);
            return ResponseEntity.ok("Note edited successfully");

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body("User or note not found: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error editing note: " + e.getMessage());
        }
    }

    public ResponseEntity<String> toggleFavorite(Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized to modify this note");
            }

            note.setFavorite(!note.isFavorite());
            noteRepository.save(note);

            return ResponseEntity.ok("Note favourite status toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error toggling favourite: " + e.getMessage());
        }
    }

    public ResponseEntity<String> toggleArchive(Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized to archive this note");
            }

            note.setFavorite(false);
            note.setArchived(!note.isArchived());
            noteRepository.save(note);

            return ResponseEntity.ok("Note archive status toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error toggling archive: " + e.getMessage());
        }
    }

    public ResponseEntity<String> toggleTrash(Integer id) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new RuntimeException("User not found"));

            Note note = noteRepository.findById(id)
                    .orElseThrow(() -> new RuntimeException("Note not found"));

            if (!note.getUser().getId().equals(user.getId())) {
                return ResponseEntity.status(403).body("Unauthorized to trash this note");
            }

            note.setFavorite(false);
            note.setArchived(false);
            note.setTrashed(!note.isTrashed());
            noteRepository.save(note);

            return ResponseEntity.ok("Note trash status toggled successfully");
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error toggling trash: " + e.getMessage());
        }
    }

    public ResponseEntity<List<NoteResponseDto>> getAllActiveNotes() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            List<Note> notes = noteRepository.findByUserIdAndIsArchivedFalseAndIsTrashedFalse(user.getId());
            List<NoteResponseDto> noteResponseDto = notes.stream().map(this::mapResponseDto).toList();
            return ResponseEntity.ok(noteResponseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<NoteResponseDto>> getRecentNotesForDashboard() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();
            System.out.println("Current user email: " + email);

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            System.out.println("Current user ID: " + user.getId());

            List<Note> notes = noteRepository
                    .findTop6ByUserIdAndIsArchivedFalseAndIsTrashedFalseOrderByCreatedAtDesc(user.getId());

            List<NoteResponseDto> response = notes.stream().map(this::mapResponseDto).toList();

            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.status(404).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<NoteResponseDto>> getAllFavoriteNotes() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            List<Note> notes = noteRepository.findByUserIdAndIsFavoriteTrue(user.getId());
            List<NoteResponseDto> noteResponseDto = notes.stream().map(this::mapResponseDto).toList();
            return ResponseEntity.ok(noteResponseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<NoteResponseDto>> getAllArchivedNotes() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            List<Note> notes = noteRepository.findByUserIdAndIsArchivedTrue(user.getId());
            List<NoteResponseDto> noteResponseDto = notes.stream().map(this::mapResponseDto).toList();
            return ResponseEntity.ok(noteResponseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }

    public ResponseEntity<List<NoteResponseDto>> getAllTrashedNotes() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String email = auth.getName();

            User user = userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException("User not found"));
            List<Note> notes = noteRepository.findByUserIdAndIsTrashedTrue(user.getId());
            List<NoteResponseDto> noteResponseDto = notes.stream().map(this::mapResponseDto).toList();
            return ResponseEntity.ok(noteResponseDto);

        } catch (RuntimeException e) {
            return ResponseEntity.status(404).build();
        } catch (Exception e) {
            return ResponseEntity.status(500).build();
        }
    }
}
