package uz.kpi.telegramchatgpt.service.ai;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import uz.kpi.telegramchatgpt.domain.dto.request.ChatRequest;
import uz.kpi.telegramchatgpt.domain.dto.request.MessageRequestDto;
import uz.kpi.telegramchatgpt.domain.dto.response.ChatCompletionDTO;
import uz.kpi.telegramchatgpt.domain.entity.message.GPTChat;
import uz.kpi.telegramchatgpt.domain.entity.message.MessageEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.exceptions.DataNotFoundException;
import uz.kpi.telegramchatgpt.repository.message.GPTChatRepository;
import uz.kpi.telegramchatgpt.repository.message.MessageRepository;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class GPTService {
    private final RestTemplate restTemplate;
    private final GPTChatRepository chatRepository;
    private final MessageRepository messageRepository;
    @Value("${bot.OPEN_AI_TOKEN}")
    private String gptToken;

    public ChatCompletionDTO answer(UserEntity user, String text) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString("https://api.openai.com/v1/chat/completions");
        GPTChat currentChat = chatRepository.findGPTChatByUser(user)
                .orElseThrow(() -> new DataNotFoundException("Chat not found!"));
        List<MessageEntity> messages = currentChat.getMessages();
        List<MessageRequestDto> messageRequestDto = new ArrayList<>();
        for (MessageEntity message : messages) {
            messageRequestDto.add(new MessageRequestDto(message.getRole(), message.getContent()));
        }
        boolean isTextPresent = false;
        for (MessageEntity message : messages) {
            if (message.getContent().equals(text)) {
                isTextPresent = true;
            }
        }
        if(!isTextPresent) {
            throw new RuntimeException("Message is not available!");
        }
        ChatRequest chatRequest = new ChatRequest();
        chatRequest.setModel("gpt-3.5-turbo");
        chatRequest.setMessages(messageRequestDto);
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentType(MediaType.APPLICATION_JSON);
        httpHeaders.setBearerAuth(gptToken);
        HttpEntity<ChatRequest> entity = new HttpEntity<>(chatRequest, httpHeaders);
        ResponseEntity<ChatCompletionDTO> exchange = restTemplate.exchange(builder.toUriString(), HttpMethod.POST, entity, ChatCompletionDTO.class);
        if (exchange.hasBody()) {
            ChatCompletionDTO response = exchange.getBody();
            assert response != null;
            List<MessageEntity> messages1 = currentChat.getMessages();
            MessageEntity message = new MessageEntity("assistant", response.getChoices().get(0).getMessage().getContent());
            messageRepository.save(message);
            messages1.add(message);
            chatRepository.save(currentChat);
            return response;
        }
        return null;
    }
}
