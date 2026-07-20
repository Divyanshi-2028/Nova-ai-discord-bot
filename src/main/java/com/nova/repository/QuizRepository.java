package com.nova.repository;

import com.nova.entity.Quiz;
import com.nova.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    List<Quiz> findBySessionIdOrderById(String sessionId);
    List<Quiz> findByUser(User user);
    List<Quiz> findByUserOrderById(User user);
    List<Quiz> findByUserAndTopic(User user, String topic);
}