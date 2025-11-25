package org.synberg.pet.chat.dto;

import java.time.LocalDateTime;

public record MessageDto(Long id, String text, Long userId, Long chatId,
                         boolean edited, LocalDateTime createdAt, LocalDateTime editedAt) {
}
