package com.nova.dto;

import java.util.List;

public record GroqRequest(
        String model,
        List<GroqMessage> messages,
        double temperature,
        int max_completion_tokens
) {}