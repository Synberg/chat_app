package org.synberg.pet.chat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.synberg.pet.chat.dto.UserDto;
import org.synberg.pet.chat.dto.create.UserCreateDto;
import org.synberg.pet.chat.dto.update.UserUpdateDto;
import org.synberg.pet.chat.entity.User;
import org.synberg.pet.chat.repository.UserRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public UserDto find(Long id) {
        return userRepository.findById(id)
                .map(this::mapToDto)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public List<UserDto> findAll() {
        return userRepository.findAll().stream()
                .map(this::mapToDto)
                .toList();
    }

    public UserDto create(UserCreateDto userDto) {
        if (userRepository.existsByUsername(userDto.username())) {
            throw new RuntimeException("Username is already in use");
        }
        User user = new User();
        user.setUsername(userDto.username());
        user.setDisplayName(userDto.displayName());
        User savedUser = userRepository.save(user);
        return mapToDto(savedUser);
    }

    public UserDto update(Long id, UserUpdateDto userDto) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));
        user.setUsername(userDto.username());
        user.setDisplayName(userDto.displayName());
        User updatedUser = userRepository.save(user);
        return mapToDto(updatedUser);
    }

    public void delete(Long id) {
       User user = userRepository.findById(id)
               .orElseThrow(() -> new RuntimeException("User not found"));
       userRepository.delete(user);
    }

    private UserDto mapToDto(User user) {
        return new UserDto(user.getId(), user.getUsername(), user.getDisplayName());
    }
}
