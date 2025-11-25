package org.synberg.pet.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synberg.pet.chat.entity.Message;

public interface MessageRepository extends JpaRepository<Message, Long> {

}
