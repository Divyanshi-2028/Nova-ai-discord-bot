package com.nova.repository;

import com.nova.entity.Flashcard;
import com.nova.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlashcardRepository
        extends JpaRepository<Flashcard, Long> {

    List<Flashcard> findByUser(User user);

    List<Flashcard> findByUserAndTopic(User user, String topic);
}