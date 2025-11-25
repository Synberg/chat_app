package org.synberg.pet.chat.dto.create;

public record MessageCreateDto(String text, Long userId, Long chatId) {
}
