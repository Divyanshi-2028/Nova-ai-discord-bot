package com.nova.service;

import com.nova.dto.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
public class AIService {

    @Value("${groq.api.key}")
    private String apiKey;

    private final RestClient restClient;

    public AIService(RestClient restClient) {
        this.restClient = restClient;
    }

    public String askAI(String question) {
        try {
            String systemPrompt = """
                You are Nova AI.

                Format your answer using Discord-compatible Markdown only.
                Do NOT use HTML tags like <br>.
                Do NOT use Markdown tables — instead use plain bullet points or numbered lists for any tabular/schedule-style data.

                Answer in this format:

                📘 Explanation

                📝 Example

                """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", question)
                    ),
                    1.0,
                    2048
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return "❌ No response received.";
            }

            String content = response.choices().getFirst().message().content();

            return cleanForDiscord(content);

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof org.springframework.web.client.HttpStatusCodeException ex) {
                System.out.println("========== GROQ RESPONSE ==========");
                System.out.println(ex.getResponseBodyAsString());
                System.out.println("====================================");
            }

            return "❌ AI service is currently unavailable.";
        }
    }

    private String cleanForDiscord(String text) {
        return text
                .replace("<br>", "\n")
                .replace("<br/>", "\n")
                .replace("<br />", "\n");
    }
    public String moderateContent(String message) {
        try {
            String systemPrompt = """
            You are a content moderation assistant.

            Analyze the given message for toxicity, harassment, spam, or inappropriate content.

            Respond ONLY in this exact format:

            Verdict: [SAFE / TOXIC / SPAM]
            Confidence: [LOW / MEDIUM / HIGH]
            Reason: [one short sentence]
            """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", message)
                    ),
                    0.3,
                    200
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return "❌ No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();
            return "❌ Moderation service is currently unavailable.";
        }
    }
    public String explainCode(String code) {
        try {
            String systemPrompt = """
            You are Nova AI, a code explanation assistant.

            Format your answer using Discord-compatible Markdown only.
            Do NOT use HTML tags like <br>.
            Do NOT use Markdown tables.

            When given a piece of code, respond in this format:

            🔍 What This Code Does
            (1-2 sentence summary)

            📋 Line-by-Line Breakdown
            (walk through the key parts, not necessarily every single line)

            ⚠️ Potential Issues
            (bugs, edge cases, or bad practices — say "None found" if genuinely clean)

            💡 Improvement Suggestion
            (one concrete suggestion to make it better)
            """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", code)
                    ),
                    0.5,
                    1200
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return " No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof org.springframework.web.client.HttpStatusCodeException ex) {
                System.out.println("========== GROQ RESPONSE ==========");
                System.out.println(ex.getResponseBodyAsString());
                System.out.println("====================================");
            }

            return " AI service is currently unavailable.";
        }
    }
    public String debugCode(String codeAndError) {
        try {
            String systemPrompt = """
            You are Nova AI, a debugging assistant.

            Format your answer using Discord-compatible Markdown only.
            Do NOT use HTML tags like <br>.
            Do NOT use Markdown tables.

            The user will give you code and possibly an error message or description of unexpected behavior.

            Respond in this format:

            🐞 Root Cause
            (what's actually causing the bug, in plain terms)

            🔧 Fix
            (the corrected code snippet, or the specific change needed)

            💡 Why This Happens
            (brief explanation so the user learns the underlying concept, not just the patch)
            """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", codeAndError)
                    ),
                    0.4,
                    1200
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return " No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof org.springframework.web.client.HttpStatusCodeException ex) {
                System.out.println("========== GROQ RESPONSE ==========");
                System.out.println(ex.getResponseBodyAsString());
                System.out.println("====================================");
            }

            return " AI service is currently unavailable.";
        }
    }
    public String generateRoadmap(String topicAndDuration) {
        try {
            String systemPrompt = """
            You are Nova AI, a study planning assistant.

            Format your answer using Discord-compatible Markdown only.
            Do NOT use HTML tags like <br>.
            Do NOT use Markdown tables — use numbered lists or bullet points instead.

            The user will give you a topic and optionally a timeframe (e.g. "DSA in 4 weeks", "React basics in 10 days").

            Respond in this format:

            🗺️ Roadmap Overview
            (1-2 sentence summary of the plan's approach)

            📅 Day/Week-by-Day Breakdown
            (structured plan using numbered list or bullet points, NOT a table)

            💡 Tips
            (2-3 short practical tips for sticking to the plan)
            """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", topicAndDuration)
                    ),
                    0.7,
                    1500
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return " No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof org.springframework.web.client.HttpStatusCodeException ex) {
                System.out.println("========== GROQ RESPONSE ==========");
                System.out.println(ex.getResponseBodyAsString());
                System.out.println("====================================");
            }

            return " AI service is currently unavailable.";
        }
    }
    public String generateFlowchart(String topic) {
        try {
            String systemPrompt = """
    You are Nova AI, a flowchart generation assistant.

    Format your answer using Discord-compatible Markdown only.
    Do NOT use HTML tags like <br>.
    Do NOT use Markdown tables.
    Do NOT use Mermaid syntax or diagram code blocks — Discord cannot render them.

    Represent the flowchart using ONLY plain text arrows (→ and ↓) and indentation.
    Follow this exact structural pattern:

    - Use ↓ for sequential steps (one step leads directly to the next)
    - Use → for decision branches (Yes/No, True/False) written on the same line
    - Indent branches consistently so it's clear which path follows which decision
    - Keep each step short (under 8 words) — this is a flowchart, not a paragraph
    - Always start with "Start" and end with "End" or a clear terminal outcome

    Example of the expected structure (for "check if number is even"):

                           ( Start )                 <-- Oval Shape (Terminator)
                               |
                               v
                       [ Input Number N ]            <-- Parallelogram (Data Input)
                               |
                               v
                        /  Is N % 2 == 0? \\          <-- Diamond Shape (Decision Point)
                        \\                 /
                           /           \\
                      Yes /             \\ No
                         v               v
                     [Print "Even"]   [Print "Odd"]  <-- Parallelograms (Data Output)
                         \\               /
                          \\             /
                           v           v
                            (  End  )                <-- Oval Shape (Terminator)
                    

    Now generate a flowchart in this exact style for the user's topic.

    Respond in this format:

    🔀 Flowchart: [Topic Name]

    (the text-based flowchart itself, following the structure above)

    📝 Explanation
    (2-3 sentence summary of the logic flow)
    """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", topic)
                    ),
                    0.6,
                    1000
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return " No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();

            if (e instanceof org.springframework.web.client.HttpStatusCodeException ex) {
                System.out.println("========== GROQ RESPONSE ==========");
                System.out.println(ex.getResponseBodyAsString());
                System.out.println("====================================");
            }

            return " AI service is currently unavailable.";
        }
    }
    public String summarizeText(String text) {
        try {
            String systemPrompt = """
            You are Nova AI, a document summarization assistant.

            Format your answer using Discord-compatible Markdown only.
            Do NOT use HTML tags like <br>.
            Do NOT use Markdown tables.

            Respond in this format:

            📄 Summary
            (3-5 sentence overview of the document)

            🔑 Key Points
            (bullet list of the most important points, max 8 bullets)
            """;

            GroqRequest request = new GroqRequest(
                    "openai/gpt-oss-120b",
                    List.of(
                            new GroqMessage("system", systemPrompt),
                            new GroqMessage("user", text)
                    ),
                    0.5,
                    1200
            );

            GroqResponse response = restClient.post()
                    .uri("https://api.groq.com/openai/v1/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(GroqResponse.class);

            if (response == null || response.choices() == null || response.choices().isEmpty()) {
                return " No response received.";
            }

            return cleanForDiscord(response.choices().getFirst().message().content());

        } catch (Exception e) {
            e.printStackTrace();
            return " AI service is currently unavailable.";
        }
    }

}