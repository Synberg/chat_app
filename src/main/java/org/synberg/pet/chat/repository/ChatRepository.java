package org.synberg.pet.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.synberg.pet.chat.entity.Chat;
import org.synberg.pet.chat.entity.User;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    @Query("""
            SELECT COUNT(c) > 0
            FROM Chat c
            WHERE (c.user1 = :u1 AND c.user2 = :u2)
            OR (c.user1 = :u2 AND c.user2 = :u1)
            """)
    boolean chatExists(@Param("u1") User u1, @Param("u2") User u2);
}
