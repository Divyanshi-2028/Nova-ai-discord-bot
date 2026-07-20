package com.nova.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nova.dto.FlashcardDTO;
import com.nova.entity.Flashcard;
import com.nova.entity.User;
import com.nova.repository.FlashcardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FlashcardService {

    private final AIService aiService;
    private final FlashcardRepository flashcardRepository;
    private final ObjectMapper objectMapper;

    public List<Flashcard> generateFlashcards(User user, String topic) {

        String prompt = """
Generate exactly 5 flashcards on the topic: %s.

Return ONLY a valid JSON array.

Do NOT include:
- markdown
- ```json
- explanations
- emojis
- headings
- extra text

Output must start with '[' and end with ']'.

Example:

[
  {
    "question":"What is JVM?",
    "answer":"Java Virtual Machine"
  },
  {
    "question":"What is JDK?",
    "answer":"Java Development Kit"
  }
]
""".formatted(topic);
        String aiResponse = aiService.askAI(prompt);

        System.out.println("========== FLASHCARD RESPONSE ==========");
        System.out.println(aiResponse);
        System.out.println("========================================");

        aiResponse = aiResponse
                .replace("```json", "")
                .replace("```", "")
                .trim();

        int start = aiResponse.indexOf("[");
        int end = aiResponse.lastIndexOf("]");

        if (start != -1 && end != -1) {
            aiResponse = aiResponse.substring(start, end + 1);
        }
        try {

            List<FlashcardDTO> cards =
                    objectMapper.readValue(
                            aiResponse,
                            new TypeReference<>() {}
                    );

            List<Flashcard> flashcards = cards.stream()
                    .map(card -> Flashcard.builder()
                            .topic(topic)
                            .question(card.question())
                            .answer(card.answer())
                            .createdAt(LocalDateTime.now())
                            .user(user)
                            .build())
                    .toList();

            return flashcardRepository.saveAll(flashcards);

        } catch (Exception e) {
            throw new RuntimeException("Unable to generate flashcards.", e);
        }
    }
    public List<Flashcard> getUserFlashcards(User user) {
        return flashcardRepository.findByUser(user);
    }
    public List<Flashcard> getFlashcardsByTopic(User user, String topic) {
        return flashcardRepository.findByUserAndTopic(user, topic);
    }
}


