package uz.kpi.telegramchatgpt.domain.dto.response;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
@Builder
public class MessageResponseDto {
    private String role;
    private String content;
}
