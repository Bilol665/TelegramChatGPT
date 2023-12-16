package uz.kpi.telegramchatgpt.service;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import uz.kpi.telegramchatgpt.domain.entity.message.MessageEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.LanguageEnum;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserState;
import uz.kpi.telegramchatgpt.exceptions.DataNotFoundException;
import uz.kpi.telegramchatgpt.service.bot.BotService;
import uz.kpi.telegramchatgpt.service.message.MessageService;
import uz.kpi.telegramchatgpt.service.user.UserService;

import java.util.Objects;

@Service
@Slf4j
public class GPTBot extends TelegramLongPollingBot {
    private final UserService userService;
    private final BotService botService;
    private final MessageService messageService;

    public GPTBot(@Value("${bot.token}") String botToken,
                  @Autowired UserService userService,
                  @Autowired BotService botService,
                  @Autowired MessageService messageService) {
        super(botToken);
        this.botService = botService;
        this.userService = userService;
        this.messageService = messageService;
    }

    @SneakyThrows
    @Override
    public void onUpdateReceived(Update update) {
        Message message = update.getMessage();
        Long chatId;
        String text;
        if (message != null) {
            chatId = message.getChatId();
            text = message.getText();
        } else if (update.getCallbackQuery() != null) {
            message = update.getCallbackQuery().getMessage();
            chatId = message.getChatId();
            text = message.getText();
        } else {
            return;
        }
        UserEntity currentUser;
        try {
            currentUser = userService.getByChatId(chatId);
            if (Objects.equals(text, "/start")) {
                currentUser.setIsFirstTime(true);
                userService.save(currentUser);
            }
            if (currentUser.getIsFirstTime()) {
                execute(botService.welcomeBackMessage(chatId.toString(), message.getChat()));
                currentUser.setState(UserState.REGISTERED);
                userService.updateState(chatId, currentUser.getState());
                currentUser.setIsFirstTime(false);
                userService.save(currentUser);
                currentUser.setState(UserState.CHAT_WITH_GPT);
                userService.updateState(chatId, currentUser.getState());
                execute(botService.gptGreeting(chatId));
                return;
            }
            if (update.hasCallbackQuery()) {
                if (update.getCallbackQuery().getData().startsWith("settings")) {
                    String settingsType = update.getCallbackQuery().getData().substring(8);
                    switch (settingsType) {
                        case "language" -> {
                            currentUser.setState(UserState.SETTINGS_LANGUAGE);
                            userService.updateState(chatId, UserState.SETTINGS_LANGUAGE);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            String inlineMessageId = update.getCallbackQuery().getInlineMessageId();
//                            execute(botService.showLanguages());
                        }
                    }
                } else if (update.getCallbackQuery().getData().startsWith("language-")) {
                    String language = update.getCallbackQuery().getData().substring(9);
                    currentUser.setLanguage(LanguageEnum.valueOf(language.toUpperCase()));
                    currentUser.setState(UserState.CHAT_WITH_GPT);
                    userService.updateState(chatId, currentUser.getState());
                    userService.updateLanguage(chatId, currentUser.getLanguage());
                    execute(botService.gptGreeting(chatId));
                }
            } else {
                switch (currentUser.getState()) {
                    case CHAT_WITH_GPT -> {
                        if (messageService.isChatPresent(currentUser)) {
                            MessageEntity messageEntity = MessageEntity.builder()
                                    .role("user")
                                    .content(text)
                                    .build();
                            messageService.addMessage(currentUser, messageEntity);
                            execute(botService.answerWithGPT(currentUser, text));
                        } else {
                            MessageEntity messageEntity = MessageEntity.builder()
                                    .role("user")
                                    .content(text)
                                    .build();
                            messageService.registerNewChat(currentUser);
                            messageService.addMessage(currentUser,messageEntity);
                            execute(botService.answerWithGPT(currentUser,text));
                        }
                    }
                    default -> {
                        switch (text) {
                            case "Start chat" -> {
                                currentUser.setState(UserState.CHAT_WITH_GPT);
                                userService.updateState(chatId, UserState.CHAT_WITH_GPT);
                                execute(new SendMessage(chatId.toString(), "Ask!"));
                            }
                            case "Settings⚙️" -> {
                                currentUser.setState(UserState.SETTINGS);
                                userService.updateState(chatId, UserState.SETTINGS);
                                execute(botService.settings(chatId));
                            }
                            default -> {
                                switch (currentUser.getLanguage()) {
                                    case UZBEK -> execute(new SendMessage(chatId.toString(), "Man sizni chunmadim!"));
                                    case RUSSIAN -> execute(new SendMessage(chatId.toString(), "Я вас не понял!"));
                                    case ENGLISH -> execute(new SendMessage(chatId.toString(), "i don't understand you!"));
                                }
                            }
                        }
                    }
                }
            }
        } catch (DataNotFoundException e) {
            if (message != null && message.hasContact()) {
                userService.addUser(message.getChat(), message.getContact());
                execute(botService.welcome(chatId, message.getChat(), false));
                execute(botService.showLanguages(chatId));
            } else if (e.getMessage().equals("User not found!")) {
                execute(botService.requestContact(chatId));
            }
        }
    }

    @Override
    public String getBotUsername() {
        return "SlovoSkazkabot";
    }
}
