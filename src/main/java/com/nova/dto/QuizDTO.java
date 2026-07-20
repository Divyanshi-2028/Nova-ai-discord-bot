package com.nova.dto;

public record QuizDTO(

        String question,

        String optionA,

        String optionB,

        String optionC,

        String optionD,

        String answer

) {}