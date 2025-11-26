package org.synberg.pet.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.synberg.pet.chat.dto.MessageDto;
import org.synberg.pet.chat.dto.create.MessageCreateDto;
import org.synberg.pet.chat.dto.update.MessageUpdateDto;
import org.synberg.pet.chat.entity.Chat;
import org.synberg.pet.chat.entity.Message;
import org.synberg.pet.chat.entity.User;
import org.synberg.pet.chat.exception.NotFoundException;
import org.synberg.pet.chat.repository.ChatRepository;
import org.synberg.pet.chat.repository.MessageRepository;
import org.synberg.pet.chat.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public MessageDto find(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        return toMessageDto(message);
    }

    public List<MessageDto> findAll() {
        return messageRepository.findAll().stream().map(this::toMessageDto).toList();
    }

    public MessageDto create(MessageCreateDto messageDto) {
        User user = userRepository.findById(messageDto.userId())
                .orElseThrow(() -> new NotFoundException("User not found"));
        Chat chat = chatRepository.findById(messageDto.chatId())
                .orElseThrow(() -> new NotFoundException("Chat not found"));
        Message message = new Message();
        message.setText(messageDto.text());
        message.setUser(user);
        message.setChat(chat);
        message.setEdited(false);
        message.setCreatedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);
        return toMessageDto(saved);
    }

    public MessageDto update(Long id, MessageUpdateDto messageDto) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        message.setText(messageDto.text());
        message.setEdited(true);
        message.setEditedAt(LocalDateTime.now());
        Message saved = messageRepository.save(message);
        return toMessageDto(saved);
    }

    public void delete(Long id) {
        Message message = messageRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Message not found"));
        messageRepository.delete(message);
    }

    private MessageDto toMessageDto(Message message) {
        return new MessageDto(message.getId(),
                              message.getText(),
                              message.getUser().getId(),
                              message.getChat().getId(),
                              message.isEdited(),
                              message.getCreatedAt(),
                              message.getEditedAt()
        );
    }
}
