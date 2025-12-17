package org.synberg.pet.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.synberg.pet.chat.entity.User;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);
    boolean existsByUsername(String username);
}
