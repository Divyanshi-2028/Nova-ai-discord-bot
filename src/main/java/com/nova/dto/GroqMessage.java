package com.nova.dto;

public record GroqMessage(
        String role,
        String content
) {}