package org.synberg.pet.chat.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.synberg.pet.chat.dto.MessageDto;
import org.synberg.pet.chat.dto.create.MessageCreateDto;
import org.synberg.pet.chat.service.MessageService;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {
    private final SimpMessagingTemplate messagingTemplate;
    private final MessageService messageService;

    @MessageMapping("/chat.send")
    public void sendMessage(MessageCreateDto dto) {
        MessageDto saved = messageService.create(dto);
        messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), saved);
    }
}
