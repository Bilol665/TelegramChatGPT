package uz.kpi.telegramchatgpt.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Usage {
    private int promptTokens;
    private int completionTokens;
    private int totalTokens;
}