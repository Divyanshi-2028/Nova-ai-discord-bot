package com.nova.discord;

import com.nova.service.AIService;
import lombok.RequiredArgsConstructor;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.awt.Color;

@Component
@RequiredArgsConstructor
public class AutoModerationListener extends ListenerAdapter {

    private final AIService aiService;

    @Value("${moderation.log-channel-id}")
    private String logChannelId;

    private static final int MIN_MESSAGE_LENGTH = 5;

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {

        if (event.getAuthor().isBot()) {
            return;
        }

        if (!event.isFromGuild()) {
            return;
        }

        String content = event.getMessage().getContentRaw();

        if (content.length() < MIN_MESSAGE_LENGTH) {
            return;
        }

        try {
            String result = aiService.moderateContent(content);

            if (isFlagged(result)) {
                logToModChannel(event, content, result);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean isFlagged(String moderationResult) {
        String upper = moderationResult.toUpperCase();
        return upper.contains("VERDICT: TOXIC") || upper.contains("VERDICT: SPAM");
    }

    private void logToModChannel(MessageReceivedEvent event, String content, String result) {

        MessageChannel logChannel =
                event.getJDA().getTextChannelById(logChannelId);

        if (logChannel == null) {
            System.out.println("⚠️ Mod-log channel not found. Check moderation.log-channel-id.");
            return;
        }

        Message originalMessage = event.getMessage();

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🚩 Flagged Message")
                .setColor(Color.RED)
                .addField("User", event.getAuthor().getAsMention(), true)
                .addField("Channel", event.getChannel().getAsMention(), true)
                .addField("Message", content, false)
                .addField("AI Analysis", result, false)
                .addField("Jump to Message", originalMessage.getJumpUrl(), false);

        logChannel.sendMessageEmbeds(embed.build()).queue();
    }
}