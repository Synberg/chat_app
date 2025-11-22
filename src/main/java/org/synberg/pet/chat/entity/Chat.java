package org.synberg.pet.chat.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Table(name="chats")
@Entity
@Getter
@Setter
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    private User user2;
}
