package uz.kpi.telegramchatgpt.service.message;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import uz.kpi.telegramchatgpt.domain.entity.message.ChatStatus;
import uz.kpi.telegramchatgpt.domain.entity.message.GPTChat;
import uz.kpi.telegramchatgpt.domain.entity.message.MessageEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.exceptions.DataNotFoundException;
import uz.kpi.telegramchatgpt.repository.message.GPTChatRepository;
import uz.kpi.telegramchatgpt.repository.message.MessageRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MessageService {
    private final MessageRepository messageRepository;
    private final GPTChatRepository chatRepository;

    public MessageEntity addMessage(UserEntity currentUser, MessageEntity message) {
        MessageEntity saved = messageRepository.save(message);
        GPTChat chat = chatRepository.findGPTChatByUser(currentUser)
                .orElseThrow(() -> new DataNotFoundException("Chat not found!"));
        List<MessageEntity> messages = chat.getMessages();
        messages.add(saved);
        chat.setMessages(messages);
        chatRepository.save(chat);
        return saved;
    }

    public boolean isChatPresent(UserEntity currentUser) {
        Optional<GPTChat> or = chatRepository.findGPTChatByUser(currentUser);
        return or.isPresent();

    }

    public void registerNewChat(UserEntity currentUser) {
        GPTChat chat = new GPTChat(currentUser,null, ChatStatus.ACTIVE);
        chatRepository.save(chat);
    }
}
