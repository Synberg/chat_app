package org.synberg.pet.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synberg.pet.chat.dto.ChatDto;
import org.synberg.pet.chat.dto.UserDto;
import org.synberg.pet.chat.dto.create.UserCreateDto;
import org.synberg.pet.chat.dto.update.UserUpdateDto;
import org.synberg.pet.chat.service.UserService;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public UserDto getUserById(@PathVariable Long id) {
        return userService.find(id);
    }

    @GetMapping("/by-username/{username}")
    public UserDto getUserByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping
    public List<UserDto> getUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}/chats")
    public List<ChatDto> getUserChats(@PathVariable Long id) {
        return userService.findAllChats(id);
    }

    @PostMapping
    public ResponseEntity<UserDto> createUser(@RequestBody UserCreateDto userDto) {
        UserDto createdUser = userService.create(userDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
    }

    @PutMapping("/{id}")
    public UserDto updateUser(@PathVariable Long id, @RequestBody UserUpdateDto userDto) {
        return userService.update(id, userDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable Long id) {
        userService.delete(id);
    }
}
