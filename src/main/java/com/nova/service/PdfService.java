package com.nova.service;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class PdfService {

    private static final int MAX_CHARS = 15000; // keeps prompt within safe token range

    public String extractText(InputStream inputStream) throws Exception {

        try (PDDocument document = Loader.loadPDF(inputStream.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            String text = stripper.getText(document);

            if (text.length() > MAX_CHARS) {
                text = text.substring(0, MAX_CHARS) + "\n\n[Content truncated due to length]";
            }

            return text;
        }
    }
}