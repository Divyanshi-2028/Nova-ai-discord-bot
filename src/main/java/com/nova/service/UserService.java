package com.nova.service;

import com.nova.entity.User;
import com.nova.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private static final int XP_PER_LEVEL = 100;

    public Optional<User> registerUser(String discordId, String username) {

        Optional<User> existingUser = userRepository.findByDiscordId(discordId);

        if (existingUser.isPresent()) {
            return Optional.empty();
        }

        User user = User.builder()
                .discordId(discordId)
                .username(username)
                .xp(0)
                .level(1)
                .createdAt(LocalDateTime.now())
                .build();

        return Optional.of(userRepository.save(user));
    }

    public Optional<User> getUserByDiscordId(String discordId) {
        return userRepository.findByDiscordId(discordId);
    }

    public Optional<User> getUser(String discordId) {
        return userRepository.findByDiscordId(discordId);
    }

    /**
     * Adds XP to a user and recalculates their level.
     */
    public boolean addXp(User user, int amount) {

        int oldLevel = user.getLevel();

        int newXp = user.getXp() + amount;
        int newLevel = (newXp / XP_PER_LEVEL) + 1;

        user.setXp(newXp);
        user.setLevel(newLevel);

        userRepository.save(user);

        return newLevel > oldLevel;
    }
}