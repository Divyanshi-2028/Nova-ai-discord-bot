package com.nova.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String topic;

    @Column(length = 1000)
    private String question;

    private String optionA;

    private String optionB;

    private String optionC;

    private String optionD;

    private String correctAnswer;

    private LocalDateTime createdAt;

    private String sessionId;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;

    // Temporary explicit getter
    public String getCorrectAnswer() {
        return correctAnswer;
    }
}