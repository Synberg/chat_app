package org.synberg.pet.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.synberg.pet.chat.dto.ChatDto;
import org.synberg.pet.chat.dto.UserDto;
import org.synberg.pet.chat.dto.create.UserCreateDto;
import org.synberg.pet.chat.dto.update.UserUpdateDto;
import org.synberg.pet.chat.entity.Chat;
import org.synberg.pet.chat.entity.User;
import org.synberg.pet.chat.exception.AlreadyExistsException;
import org.synberg.pet.chat.exception.NotFoundException;
import org.synberg.pet.chat.repository.ChatRepository;
import org.synberg.pet.chat.repository.UserRepository;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final ChatRepository chatRepository;

    public UserDto find(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new NotFoundException("User not found"));
    }

    public List<UserDto> findAll() {
        return userRepository.findAll()
                .stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserDto findByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("User not found"));
        return mapToDto(user);
    }

    public List<ChatDto> findAllChats(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        List<Chat> chats = chatRepository.findByUser1OrUser2(user, user);
        return chats.stream().map(this::mapToChatDto).toList();
    }

    public UserDto create(UserCreateDto userDto) {
        if (userRepository.existsByUsername(userDto.username())) {
            throw new AlreadyExistsException("Username is already exists");
        }
        User user = new User();
        user.setUsername(userDto.username());
        user.setDisplayName(userDto.displayName());
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    public UserDto update(Long id, UserUpdateDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        if (userRepository.existsByUsername(userDto.username())) {
            throw new AlreadyExistsException("Username already exists");
        }
        user.setUsername(userDto.username());
        user.setDisplayName(userDto.displayName());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    public void delete(Long id) {
       User user = userRepository.findById(id)
               .orElseThrow(() -> new NotFoundException("User not found"));
       userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName());
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
}
