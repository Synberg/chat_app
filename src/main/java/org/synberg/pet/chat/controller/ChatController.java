package org.synberg.pet.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synberg.pet.chat.dto.ChatDto;
import org.synberg.pet.chat.dto.create.ChatCreateDto;
import org.synberg.pet.chat.service.ChatService;

import java.util.List;

@RestController
@RequestMapping("/api/chats")
@RequiredArgsConstructor
public class ChatController {
    private final ChatService chatService;

    @GetMapping("/{id}")
    public ChatDto getUserById(@PathVariable Long id) {
        return chatService.find(id);
    }

    @GetMapping
    public List<ChatDto> getChats() {
        return chatService.findAll();
    }

    @PostMapping
    public ResponseEntity<ChatDto> createChat(@RequestBody ChatCreateDto chatDto) {
        ChatDto createdChat = chatService.create(chatDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(createdChat);
    }

    @DeleteMapping("/{id}")
    public void deleteChat(@PathVariable Long id) {
        chatService.delete(id);
    }
}
