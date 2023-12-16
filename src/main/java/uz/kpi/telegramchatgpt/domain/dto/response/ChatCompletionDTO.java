package uz.kpi.telegramchatgpt.domain.dto.response;

import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class ChatCompletionDTO {
    private String warning;
    private String id;
    private String object;
    private long created;
    private String model;
    private String systemFingerprint;
    private List<Choice> choices;
    private Usage usage;
}

