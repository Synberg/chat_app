package org.synberg.pet.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synberg.pet.chat.entity.Chat;
import org.synberg.pet.chat.entity.Message;
import org.synberg.pet.chat.entity.User;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
    List<Message> findByChatOrderByCreatedAt(Chat chat);
}
