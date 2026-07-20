package com.nova.discord;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import org.springframework.stereotype.Component;
import net.dv8tion.jda.api.interactions.commands.OptionType;
@Component
@RequiredArgsConstructor
public class SlashCommandManager {

    private final JDA jda;

    @PostConstruct
    public void registerCommands() {

        jda.updateCommands()
                .addCommands(
                        Commands.slash("ping", "Check if the bot is online"),
                        Commands.slash("register", "Register yourself with Nova"),
                        Commands.slash("profile", "View your Nova profile"),
                        Commands.slash("ask", "Ask the AI Study Assistant")
                                .addOption(
                                        OptionType.STRING,
                                        "question",
                                        "Ask any study related question",
                                        true
                                ),
                        Commands.slash("flashcards", "Generate AI flashcards")
                                .addOption(
                                        OptionType.STRING,
                                        "topic",
                                        "Enter the topic",
                                        true
                                ),
                        Commands.slash(
                                "myflashcards",
                                "View all your saved flashcards"
                        ),
                        Commands.slash("review", "Review your flashcards")
                                .addOption(
                                        OptionType.STRING,
                                        "topic",
                                        "Enter the topic",
                                        true
                                ),
                        Commands.slash("quiz", "Generate an AI quiz")
                                .addOption(
                                        OptionType.STRING,
                                        "topic",
                                        "Enter the topic",
                                        true
                                ),
                        Commands.slash("leaderboard", "View the top Nova users"),
                        Commands.slash("explaincode", "Get an AI explanation of a code snippet")
                                .addOption(
                                        OptionType.STRING,
                                        "code",
                                        "Paste the code you want explained",
                                        true
                                ),
                        Commands.slash("debug", "Get AI help debugging broken code")
                                .addOption(
                                        OptionType.STRING,
                                        "code",
                                        "Paste your code and error message/description",
                                        true
                                ),
                        Commands.slash("roadmap", "Generate a study roadmap for a topic")
                                .addOption(
                                        OptionType.STRING,
                                        "topic",
                                        "Topic and optional timeframe (e.g. 'DSA in 4 weeks')",
                                        true
                                ),
                        Commands.slash("serverinfo", "View information about this server"),
                        Commands.slash("flowchart", "Generate a text-based flowchart for a process or algorithm")
                                .addOption(
                                        OptionType.STRING,
                                        "topic",
                                        "The process or algorithm to visualize (e.g. 'binary search')",
                                        true
                                )
                )
                .queue(success ->
                        System.out.println("Slash Commands Registered!")
                );
    }
}