package com.nova.dto;

import java.util.List;

public record GroqResponse(
        String id,
        List<Choice> choices
) {
    public record Choice(
            int index,
            GroqMessage message,
            String finish_reason
    ) {}
}