package uz.kpi.telegramchatgpt.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.Contact;
import uz.kpi.telegramchatgpt.domain.entity.user.LanguageEnum;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserRole;
import uz.kpi.telegramchatgpt.domain.entity.user.UserState;
import uz.kpi.telegramchatgpt.exceptions.DataNotFoundException;
import uz.kpi.telegramchatgpt.repository.user.UserRepository;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    public UserEntity getByChatId(Long chatId) {
        return userRepository.findUserEntityByChatId(chatId).orElseThrow(() -> new DataNotFoundException("User not found!"));
    }
    public UserEntity addUser(Chat chat, Contact contact) {
        if (checkIfNumberExists(contact.getPhoneNumber())) return null;
        UserEntity user = UserEntity.builder()
                .isFirstTime(false)
                .chatId(contact.getUserId())
                .bio(chat.getBio())
                .username(chat.getUserName())
                .role(UserRole.USER)
                .firstName(contact.getFirstName())
                .language(LanguageEnum.ENGLISH)
                .lastName(contact.getLastName() == null ? " " : contact.getLastName())
                .phoneNumber(contact.getPhoneNumber())
                .state(UserState.REGISTERED)
                .build();
        return userRepository.save(user);
    }

    public boolean checkIfNumberExists(String phoneNumber) {
        Optional<UserEntity> users = userRepository.findUserEntitiesByPhoneNumber(phoneNumber);
        return users.isPresent();
    }

    public void save(UserEntity currentUser) {
        userRepository.save(currentUser);
    }

    public void updateState(Long chatId, UserState state) {
        userRepository.updateState(state, chatId);
    }

    public void updateLanguage(Long chatId, LanguageEnum language) {
        userRepository.updateLanguage(language, chatId);
    }
}
