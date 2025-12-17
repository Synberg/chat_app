package org.synberg.pet.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.synberg.pet.chat.dto.ChatDto;
import org.synberg.pet.chat.dto.MessageDto;
import org.synberg.pet.chat.dto.create.ChatCreateDto;
import org.synberg.pet.chat.entity.Chat;
import org.synberg.pet.chat.entity.Message;
import org.synberg.pet.chat.entity.User;
import org.synberg.pet.chat.exception.AlreadyExistsException;
import org.synberg.pet.chat.exception.NotFoundException;
import org.synberg.pet.chat.repository.ChatRepository;
import org.synberg.pet.chat.repository.MessageRepository;
import org.synberg.pet.chat.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;

    private final SimpMessagingTemplate messagingTemplate;

    public ChatDto find(Long id) {
        return chatRepository.findById(id)
                .map(this::mapToChatDto)
                .orElseThrow(() -> new NotFoundException("Chat not found"));
    }

    public List<ChatDto> findAll() {
        return chatRepository.findAll()
                .stream()
                .map(this::mapToChatDto)
                .toList();
    }

    public List<MessageDto> findAllMessages(Long id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chat not found"));
        List<Message> messages = messageRepository.findByChat(chat);
        return messages.stream().map(this::toMessageDto).toList();
    }

    public ChatDto create(ChatCreateDto chatCreateDto) {
        User user1 = userRepository.findByUsername(chatCreateDto.username1())
                .orElseThrow(() -> new NotFoundException("User not found"));
        User user2  = userRepository.findByUsername(chatCreateDto.username2())
                .orElseThrow(() -> new NotFoundException("User not found"));
        if (chatRepository.chatExists(user1, user2)) {
            throw new AlreadyExistsException("Chat already exists");
        }
        Chat chat = new Chat();
        chat.setUser1(user1);
        chat.setUser2(user2);
        Chat created = chatRepository.save(chat);
        ChatDto chatDto = mapToChatDto(created);

        messagingTemplate.convertAndSend("/topic/newChat." + user1.getId(), "update");
        messagingTemplate.convertAndSend("/topic/newChat." + user2.getId(), "update");

        return chatDto;
    }

    public void delete(Long id) {
        Chat chat = chatRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Chat not found"));
        chatRepository.delete(chat);

        Long user1Id = chat.getUser1().getId();
        Long user2Id = chat.getUser2().getId();

        messagingTemplate.convertAndSend("/topic/newChat." + user1Id, "update");
        messagingTemplate.convertAndSend("/topic/newChat." + user2Id, "update");
    }

    public ChatDto mapToChatDto(Chat chat) {
        return new ChatDto(
                chat.getId(),
                chat.getUser1().getId(),
                chat.getUser1().getUsername(),
                chat.getUser2().getId(),
                chat.getUser2().getUsername()
        );
    }

    private MessageDto toMessageDto(Message message) {
        return new MessageDto(
                message.getId(),
                message.getText(),
                message.getUser().getId(),
                message.getUser().getDisplayName(),
                message.getChat().getId(),
                message.isEdited(),
                message.getCreatedAt(),
                message.getEditedAt()
        );
    }
}
