package com.nova.discord;

import com.nova.entity.Quiz;
import com.nova.entity.User;
import com.nova.repository.QuizRepository;
import com.nova.repository.UserRepository;
import com.nova.service.UserService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ButtonListener extends ListenerAdapter {

    private static final int XP_PER_CORRECT_ANSWER = 10;

    private final UserRepository userRepository;
    private final QuizRepository quizRepository;
    private final UserService userService;

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {

        String id = event.getComponentId();

        if (!id.startsWith("quiz_")) {
            return;
        }

        String[] data = id.split("_");

        Long quizId = Long.parseLong(data[1]);
        int currentIndex = Integer.parseInt(data[2]);
        String selectedOption = data[3];
        int scoreSoFar = Integer.parseInt(data[4]);

        Optional<Quiz> optionalQuiz = quizRepository.findById(quizId);

        if (optionalQuiz.isEmpty()) {

            event.reply("❌ Quiz not found.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        Quiz quiz = optionalQuiz.get();

        boolean correct =
                quiz.getCorrectAnswer()
                        .equalsIgnoreCase(selectedOption);

        int newScore = correct ? scoreSoFar + 1 : scoreSoFar;

        User user = quiz.getUser();
        boolean leveledUp = false;

        if (correct) {
            leveledUp = userService.addXp(user, XP_PER_CORRECT_ANSWER);
        }

        StringBuilder feedback = new StringBuilder(
                correct
                        ? "✅ Correct! (+" + XP_PER_CORRECT_ANSWER + " XP)"
                        : "❌ Wrong! Correct Answer: " + quiz.getCorrectAnswer()
        );

        if (leveledUp) {
            feedback.append("\n🎉 Level Up! You're now **Level ")
                    .append(user.getLevel())
                    .append("**!");
        }

        List<Quiz> quizzes =
                quizRepository.findBySessionIdOrderById(quiz.getSessionId());

        int nextIndex = currentIndex + 1;

        if (nextIndex >= quizzes.size()) {

            event.reply(
                            """
                            %s

                            🎉 **Quiz Finished!**

                            🏆 Final Score: **%d / %d**
                            ⭐ Total XP: **%d**
                            🏅 Level: **%d**
                            """
                                    .formatted(
                                            feedback,
                                            newScore,
                                            quizzes.size(),
                                            user.getXp(),
                                            user.getLevel()
                                    )
                    )
                    .setEphemeral(true)
                    .queue();

            return;
        }

        Quiz nextQuiz = quizzes.get(nextIndex);

        event.reply(
                        """
                        %s

                        **%s**

                        🇦 %s
                        🇧 %s
                        🇨 %s
                        🇩 %s
                        """
                                .formatted(
                                        feedback,
                                        nextQuiz.getQuestion(),
                                        nextQuiz.getOptionA(),
                                        nextQuiz.getOptionB(),
                                        nextQuiz.getOptionC(),
                                        nextQuiz.getOptionD()
                                )
                )
                .setComponents(
                        ActionRow.of(
                                Button.primary("quiz_%d_%d_A_%d".formatted(nextQuiz.getId(), nextIndex, newScore), "A"),
                                Button.primary("quiz_%d_%d_B_%d".formatted(nextQuiz.getId(), nextIndex, newScore), "B"),
                                Button.primary("quiz_%d_%d_C_%d".formatted(nextQuiz.getId(), nextIndex, newScore), "C"),
                                Button.primary("quiz_%d_%d_D_%d".formatted(nextQuiz.getId(), nextIndex, newScore), "D")
                        )
                )
                .queue();
    }
}