package uz.kpi.telegramchatgpt.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class Choice {
    private int index;
    private MessageResponseDto message;
    private String finishReason;
}