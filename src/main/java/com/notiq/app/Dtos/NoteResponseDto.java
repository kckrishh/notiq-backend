package com.notiq.app.Dtos;

import java.time.LocalDateTime;

public record NoteResponseDto(Integer id, String title, String content, boolean isFavorite, boolean isArchived,
                boolean isTrashed, LocalDateTime createdAt, LocalDateTime updatedAt, String email) {
}
