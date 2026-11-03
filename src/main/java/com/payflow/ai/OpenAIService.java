
package com.payflow.ai;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class OpenAIService {

    private static final String API_URL =
            "https://api.openai.com/v1/responses";

    private static final String MODEL = "gpt-4.1-mini";

    private final HttpClient client;

    public OpenAIService() {
        client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(30))
                .build();
    }

    public String askAI(String question)
            throws IOException, InterruptedException {

        String apiKey = System.getenv("OPENAI_API_KEY");

        if (apiKey == null || apiKey.isBlank()) {
            throw new IllegalStateException(
                    "OpenAI API key was not found."
            );
        }

        if (question == null || question.isBlank()) {
            throw new IllegalArgumentException(
                    "Please enter a question."
            );
        }

        String requestBody = """
                {
                  "model": "%s",
                  "instructions": "You are PayFlow AI, an intelligent payroll assistant. Help users understand payroll, employee management, salary calculations, tax, SSNIT, payroll reports and business expenses. Be accurate, professional and easy to understand. Do not invent employee records or financial figures. Explain that actual employee records must be checked in PayFlow.",
                  "input": "%s"
                }
                """.formatted(MODEL, escapeJson(question));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .timeout(Duration.ofSeconds(90))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> response = client.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() < 200
                || response.statusCode() >= 300) {
            throw new IOException(
                    "OpenAI API error (HTTP "
                    + response.statusCode()
                    + "). Please check your API account and billing."
            );
        }

        return extractAIResponse(response.body());
    }

    private String extractAIResponse(String json)
            throws IOException {

        Pattern pattern = Pattern.compile(
                "\"text\"\\s*:\\s*\"((?:\\\\.|[^\"\\\\])*)\""
        );

        Matcher matcher = pattern.matcher(json);

        if (!matcher.find()) {
            throw new IOException(
                    "PayFlow AI could not read the response."
            );
        }

        return unescapeJson(matcher.group(1));
    }

    private String escapeJson(String text) {
        return text
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private String unescapeJson(String text) {
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);

            if (c == '\\' && i + 1 < text.length()) {
                char next = text.charAt(++i);

                switch (next) {
                    case '"': result.append('"'); break;
                    case '\\': result.append('\\'); break;
                    case '/': result.append('/'); break;
                    case 'n': result.append('\n'); break;
                    case 'r': result.append('\r'); break;
                    case 't': result.append('\t'); break;
                    case 'b': result.append('\b'); break;
                    case 'f': result.append('\f'); break;
                    case 'u':
                        if (i + 4 < text.length()) {
                            String hex = text.substring(i + 1, i + 5);
                            result.append((char) Integer.parseInt(hex, 16));
                            i += 4;
                        }
                        break;
                    default:
                        result.append(next);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }
}
