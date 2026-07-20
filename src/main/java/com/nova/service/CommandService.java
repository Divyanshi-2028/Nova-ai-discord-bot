package com.nova.service;

import com.nova.entity.Flashcard;
import com.nova.entity.User;
import com.nova.repository.UserRepository;
import net.dv8tion.jda.api.components.buttons.Button;
import net.dv8tion.jda.api.components.actionrow.ActionRow;
import lombok.RequiredArgsConstructor;
import com.nova.entity.Quiz;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import org.springframework.stereotype.Service;
import java.util.*;
import net.dv8tion.jda.api.entities.Guild;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final FlashcardService flashcardService;
    private final AIService aiService;
    private final UserRepository userRepository;
    private final QuizService quizService;
    private final UserService userService;

    private static final int DISCORD_MAX_LENGTH = 2000;

    public void handlePing(SlashCommandInteractionEvent event) {

        event.reply("🏓 Pong!").queue();
    }

    public void handleRegister(SlashCommandInteractionEvent event) {

        String discordId = event.getUser().getId();
        String username = event.getUser().getName();

        Optional<User> registeredUser =
                userService.registerUser(discordId, username);

        if (registeredUser.isPresent()) {

            event.reply("""
                    ✅ Registration Successful!

                    Welcome **%s** 🎉
                    """.formatted(registeredUser.get().getUsername()))
                    .queue();

        } else {

            event.reply("⚠️ You are already registered!")
                    .setEphemeral(true)
                    .queue();

        }
    }

    public void handleProfile(SlashCommandInteractionEvent event) {

        Optional<User> profile =
                userService.getUserByDiscordId(event.getUser().getId());

        if (profile.isEmpty()) {

            event.reply("""
                    ❌ You are not registered.

                    Use `/register` first.
                    """)
                    .setEphemeral(true)
                    .queue();

            return;
        }

        User user = profile.get();

        String message = """
                📚 **Nova Profile**

                👤 Username : %s
                ⭐ XP : %d
                🏅 Level : %d
                📅 Joined : %s
                """
                .formatted(
                        user.getUsername(),
                        user.getXp(),
                        user.getLevel(),
                        user.getCreatedAt().toLocalDate()
                );

        event.reply(message).queue();
    }

    public void handleAsk(SlashCommandInteractionEvent event) {

        String question =
                event.getOption("question").getAsString();

        event.deferReply().queue();

        try {
            String answer = aiService.askAI(question);
            sendAsEmbed(event, answer);
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("❌ Something went wrong generating a response.").queue();
        }
    }

    private void sendAsEmbed(SlashCommandInteractionEvent event, String content) {
        net.dv8tion.jda.api.EmbedBuilder embedBuilder = new net.dv8tion.jda.api.EmbedBuilder()
                .setTitle("📚 Nova AI")
                .setColor(java.awt.Color.CYAN);

        if (content.length() <= 4096) {
            embedBuilder.setDescription(content);
            event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
        } else {
            // still too long even for one embed — truncate safely
            embedBuilder.setDescription(content.substring(0, 4090) + "\n...");
            event.getHook().sendMessageEmbeds(embedBuilder.build()).queue();
        }
    }

    private void sendChunkedFollowup(SlashCommandInteractionEvent event, String content) {
        if (content.length() <= DISCORD_MAX_LENGTH) {
            event.getHook().sendMessage(content).queue();
            return;
        }

        List<String> chunks = splitMessage(content, DISCORD_MAX_LENGTH);

        // first chunk goes as the deferred reply's followup
        event.getHook().sendMessage(chunks.get(0)).queue();

        // remaining chunks sent as additional followups
        for (int i = 1; i < chunks.size(); i++) {
            event.getHook().sendMessage(chunks.get(i)).queue();
        }
    }

    private List<String> splitMessage(String content, int maxLength) {
        List<String> chunks = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder current = new StringBuilder();

        for (String line : lines) {
            if (current.length() + line.length() + 1 > maxLength) {
                chunks.add(current.toString());
                current = new StringBuilder();
            }
            current.append(line).append("\n");
        }

        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }

        return chunks;
    }
    public void handleFlashcards(SlashCommandInteractionEvent event) {

        String topic = event.getOption("topic").getAsString();

        Optional<User> optionalUser =
                userService.getUser(event.getUser().getId());

        if (optionalUser.isEmpty()) {

            event.reply("❌ Please register first using `/register`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply().queue();

        User user = optionalUser.get();

        List<Flashcard> flashcards =
                flashcardService.generateFlashcards(user, topic);

        StringBuilder message = new StringBuilder();

        message.append("📚 **Flashcards for ").append(topic).append("**\n\n");

        int count = 1;

        for (Flashcard card : flashcards) {

            message.append("**").append(count++).append(". ")
                    .append(card.getQuestion())
                    .append("**\n");

            message.append(card.getAnswer())
                    .append("\n\n");
        }

        event.getHook().sendMessage(message.toString()).queue();
    }
    public void handleMyFlashcards(SlashCommandInteractionEvent event) {

        Optional<User> optionalUser =
                userService.getUser(event.getUser().getId());

        if (optionalUser.isEmpty()) {

            event.reply("❌ Please register first.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        User user = optionalUser.get();

        List<Flashcard> flashcards =
                flashcardService.getUserFlashcards(user);

        if (flashcards.isEmpty()) {

            event.reply("📭 You don't have any flashcards yet.\nUse `/flashcards topic:<topic>` first.")
                    .queue();

            return;
        }

        Map<String, Long> topicCounts = flashcards.stream()
                .collect(Collectors.groupingBy(
                        Flashcard::getTopic,
                        Collectors.counting()
                ));

        StringBuilder message = new StringBuilder();

        message.append("📚 **Your Saved Flashcards**\n\n");

        topicCounts.forEach((topic, count) ->
                message.append("📖 ")
                        .append(topic)
                        .append(" (")
                        .append(count)
                        .append(")\n")
        );

        message.append("\nUse `/review topic:<topic>` to revise them.");

        event.reply(message.toString()).queue();
    }
    public void handleReview(SlashCommandInteractionEvent event) {

        String topic = event.getOption("topic").getAsString();

        Optional<User> optionalUser =
                userService.getUser(event.getUser().getId());

        if (optionalUser.isEmpty()) {

            event.reply("❌ Please register first.")
                    .setEphemeral(true)
                    .queue();

            return;
        }

        User user = optionalUser.get();

        List<Flashcard> flashcards =
                flashcardService.getFlashcardsByTopic(user, topic);

        if (flashcards.isEmpty()) {

            event.reply("📭 No flashcards found for **" + topic + "**.")
                    .queue();

            return;
        }

        StringBuilder message = new StringBuilder();

        message.append("📚 **Review: ").append(topic).append("**\n\n");

        int count = 1;

        for (Flashcard card : flashcards) {

            message.append("**")
                    .append(count++)
                    .append(". ")
                    .append(card.getQuestion())
                    .append("**\n");

            message.append(card.getAnswer())
                    .append("\n\n");
        }

        event.reply(message.toString()).queue();
    }
    public void handleQuiz(SlashCommandInteractionEvent event) {

        String topic = event.getOption("topic").getAsString();

        Optional<User> optionalUser =
                userService.getUser(event.getUser().getId());

        if (optionalUser.isEmpty()) {

            event.reply("❌ Please register first using `/register`.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        event.deferReply().queue();

        User user = optionalUser.get();

        List<Quiz> quizzes =
                quizService.generateQuiz(user, topic);

        Quiz quiz = quizzes.getFirst();

        event.getHook().sendMessage(
                        """
                        📝 **Quiz Started!**
    
                        **%s**
    
                        🇦 %s
                        🇧 %s
                        🇨 %s
                        🇩 %s
                        """
                                .formatted(
                                        quiz.getQuestion(),
                                        quiz.getOptionA(),
                                        quiz.getOptionB(),
                                        quiz.getOptionC(),
                                        quiz.getOptionD()
                                )
                )
                .setComponents(
                        ActionRow.of(
                                Button.primary("quiz_%d_0_A_0".formatted(quiz.getId()), "A"),
                                Button.primary("quiz_%d_0_B_0".formatted(quiz.getId()), "B"),
                                Button.primary("quiz_%d_0_C_0".formatted(quiz.getId()), "C"),
                                Button.primary("quiz_%d_0_D_0".formatted(quiz.getId()), "D")
                        )
                )
                .queue();
    }
    public void handleLeaderboard(SlashCommandInteractionEvent event) {

        List<User> topUsers = userRepository.findTop10ByOrderByXpDesc();

        if (topUsers.isEmpty()) {

            event.reply("📊 No users on the leaderboard yet. Be the first — use `/register`!")
                    .queue();

            return;
        }

        StringBuilder sb = new StringBuilder(" **Nova Leaderboard**\n\n");

        String[] medals = { "🥇", "🥈", "🥉" };

        for (int i = 0; i < topUsers.size(); i++) {

            User u = topUsers.get(i);

            String rankLabel = i < 3 ? medals[i] : "#" + (i + 1);

            sb.append("%s **%s** — Level %d — %d XP\n"
                    .formatted(rankLabel, u.getUsername(), u.getLevel(), u.getXp()));
        }

        event.reply(sb.toString()).queue();
    }
    public void handleExplainCode(SlashCommandInteractionEvent event) {

        String code = event.getOption("code").getAsString();

        event.deferReply().queue();

        try {
            String explanation = aiService.explainCode(code);
            sendAsEmbed(event, explanation);   // reuse the embed helper you already have from handleAsk
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("❌ Something went wrong explaining the code.").queue();
        }
    }
    public void handleDebug(SlashCommandInteractionEvent event) {

        String codeAndError = event.getOption("code").getAsString();

        event.deferReply().queue();

        try {
            String result = aiService.debugCode(codeAndError);
            sendAsEmbed(event, result);
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("❌ Something went wrong while debugging.").queue();
        }
    }
    public void handleRoadmap(SlashCommandInteractionEvent event) {

        String topic = event.getOption("topic").getAsString();

        event.deferReply().queue();

        try {
            String roadmap = aiService.generateRoadmap(topic);
            sendAsEmbed(event, roadmap);
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("❌ Something went wrong generating the roadmap.").queue();
        }
    }
    public void handleServerInfo(SlashCommandInteractionEvent event) {

        Guild guild = event.getGuild();

        if (guild == null) {
            event.reply("❌ This command only works inside a server.")
                    .setEphemeral(true)
                    .queue();
            return;
        }

        String message = """
            🖥️ **Server Info**

            📛 Name: %s
            👑 Owner: %s
            👥 Members: %d
            📅 Created: %s
            🆔 Server ID: %s
            """
                .formatted(
                        guild.getName(),
                        guild.getOwner() != null ? guild.getOwner().getEffectiveName() : "Unknown",
                        guild.getMemberCount(),
                        guild.getTimeCreated().toLocalDate(),
                        guild.getId()
                );

        event.reply(message).queue();
    }
    public void handleFlowchart(SlashCommandInteractionEvent event) {

        String topic = event.getOption("topic").getAsString();

        event.deferReply().queue();

        try {
            String flowchart = aiService.generateFlowchart(topic);
            sendAsEmbed(event, flowchart);
        } catch (Exception e) {
            e.printStackTrace();
            event.getHook().sendMessage("❌ Something went wrong generating the flowchart.").queue();
        }
    }
}