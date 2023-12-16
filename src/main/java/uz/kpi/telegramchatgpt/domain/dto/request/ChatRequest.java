package uz.kpi.telegramchatgpt.domain.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Getter
public class ChatRequest {
    private String model;
    private List<MessageRequestDto> messages;
}
