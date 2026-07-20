package com.nova.config;

import com.nova.discord.AutoModerationListener;
import com.nova.discord.BotReadyListener;
import com.nova.discord.ButtonListener;
import com.nova.discord.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DiscordConfig {

    @Value("${discord.token}")
    private String token;

    @Bean
    public JDA jda(BotReadyListener botReadyListener,
                   CommandListener commandListener,
                   ButtonListener buttonListener,
                   AutoModerationListener autoModerationListener) throws InterruptedException {

        JDA jda = JDABuilder.createDefault(token)
                .enableIntents(GatewayIntent.MESSAGE_CONTENT)
                .addEventListeners(
                        botReadyListener,
                        commandListener,
                        buttonListener,
                        autoModerationListener
                )
                .build();

        jda.awaitReady();

        System.out.println("Discord Bot Connected!");

        return jda;
    }
}