package org.synberg.pet.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.synberg.pet.chat.dto.MessageDto;
import org.synberg.pet.chat.dto.create.MessageCreateDto;
import org.synberg.pet.chat.dto.update.MessageUpdateDto;
import org.synberg.pet.chat.service.MessageService;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
public class MessageController {
    private final MessageService messageService;

    @GetMapping("/{id}")
    public MessageDto getMessageById(@PathVariable Long id) {
        return messageService.find(id);
    }

    @GetMapping
    public List<MessageDto> getMessages() {
        return messageService.findAll();
    }

    @PostMapping
    public ResponseEntity<MessageDto> createMessage(@RequestBody MessageCreateDto messageDto) {
        MessageDto message = messageService.create(messageDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(message);
    }

    @PutMapping("/{id}")
    public MessageDto updateMessage(@PathVariable Long id, @RequestBody MessageUpdateDto messageDto) {
        return messageService.update(id, messageDto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteMessage(@PathVariable Long id) {
        messageService.delete(id);
    }
}
