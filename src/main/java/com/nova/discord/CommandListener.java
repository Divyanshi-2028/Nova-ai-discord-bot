package com.nova.discord;

import com.nova.service.CommandService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CommandListener extends ListenerAdapter {

    private final CommandService commandService;

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {

        switch (event.getName()) {

            case "ping":
                commandService.handlePing(event);
                break;

            case "register":
                commandService.handleRegister(event);
                break;

            case "profile":
                commandService.handleProfile(event);
                break;

            case "ask":
                commandService.handleAsk(event);
                break;

            case "flashcards":
                commandService.handleFlashcards(event);
                break;

            case "myflashcards":
                commandService.handleMyFlashcards(event);
                break;

            case "review":
                commandService.handleReview(event);
                break;

            case "quiz":
                commandService.handleQuiz(event);
                break;

            case "leaderboard":
                commandService.handleLeaderboard(event);
                break;

            case "explaincode":
                commandService.handleExplainCode(event);
                break;

            case "debug":
                commandService.handleDebug(event);
                break;

            case "roadmap":
                commandService.handleRoadmap(event);
                break;

            case "serverinfo":
                commandService.handleServerInfo(event);
                break;

            case "flowchart":
                commandService.handleFlowchart(event);
                break;

            default:
                event.reply(" Unknown command!")
                        .setEphemeral(true)
                        .queue();
        }
    }
}