package com.nova.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.dto.QuizDTO;
import com.nova.entity.Quiz;
import com.nova.entity.User;
import com.nova.repository.QuizRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuizService {

    private final AIService aiService;
    private final QuizRepository quizRepository;
    private final ObjectMapper objectMapper;

    public List<Quiz> generateQuiz(User user, String topic) {

        String sessionId = UUID.randomUUID().toString();

        String prompt = """
Generate exactly 5 multiple choice questions on the topic: %s.

Return ONLY valid JSON.

Example:

[
 {
   "question":"What is JVM?",
   "optionA":"Java Virtual Machine",
   "optionB":"Java Variable Method",
   "optionC":"Java Visual Machine",
   "optionD":"None of these",
   "answer":"A"
 }
]
""".formatted(topic);

        String aiResponse = aiService.askAI(prompt);

        aiResponse = aiResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        try {

            List<QuizDTO> quizList =
                    objectMapper.readValue(
                            aiResponse,
                            new TypeReference<>() {}
                    );

            List<Quiz> quizzes = quizList.stream()
                    .map(q -> Quiz.builder()
                            .topic(topic)
                            .question(q.question())
                            .optionA(q.optionA())
                            .optionB(q.optionB())
                            .optionC(q.optionC())
                            .optionD(q.optionD())
                            .correctAnswer(q.answer())
                            .createdAt(LocalDateTime.now())
                            .user(user)
                            .sessionId(sessionId)
                            .build())
                    .toList();

            return quizRepository.saveAll(quizzes);

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate quiz.", e);
        }
    }
}