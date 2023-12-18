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
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
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
                execute(botService.welcomeBackMessage(currentUser, chatId.toString()));
                currentUser.setState(UserState.REGISTERED);
                userService.updateState(chatId, currentUser.getState());
                currentUser.setIsFirstTime(false);
                userService.save(currentUser);
                currentUser.setState(UserState.CHAT_WITH_GPT);
                userService.updateState(chatId, currentUser.getState());
                execute(botService.gptGreeting(currentUser, chatId));
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
                            execute(botService.editShowLanguagesText(currentUser, chatId, messageId));
                        }
                        case "models" -> {
                            currentUser.setState(UserState.SETTINGS_MODEL);
                            userService.updateState(chatId, UserState.SETTINGS_MODEL);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.editShowModelsText(currentUser, chatId, messageId));
                        }
                    }
                } else if (update.getCallbackQuery().getData().startsWith("language-")) {
                    String language = update.getCallbackQuery().getData().substring(9);
                    currentUser.setLanguage(LanguageEnum.valueOf(language.toUpperCase()));
                    currentUser.setState(UserState.CHAT_WITH_GPT);
                    userService.updateState(chatId, currentUser.getState());
                    userService.updateLanguage(chatId, currentUser.getLanguage());
                    if (language.equalsIgnoreCase("UZBEK")) {
                        execute(new SendMessage(chatId.toString(), "Iltimos faqat adabiy o'zbek tilida yozing,\n" +
                                "bo'lmasa GPT boshqa tilda javob qaytarishi mumkin."));
                    }
                    execute(botService.gptGreeting(currentUser, chatId));
                } else if (update.getCallbackQuery().getData().startsWith("back")) {
                    String type = update.getCallbackQuery().getData().substring(4);
                    switch (type) {
                        case "CHAT_WITH_GPT" -> {
                            currentUser.setState(UserState.CHAT_WITH_GPT);
                            userService.updateState(chatId, UserState.CHAT_WITH_GPT);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.delete(messageId, chatId));
                            execute(botService.gptGreeting(currentUser, chatId));
                        }
                        case "SETTINGS" -> {
                            currentUser.setState(UserState.SETTINGS);
                            userService.updateState(chatId, UserState.SETTINGS);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.editToSettingsText(currentUser, chatId, messageId));
//                            execute(botService.editToSettings(currentUser, chatId, messageId));
                        }
                        case "ADMIN_SIDE" -> {
                            currentUser.setState(UserState.ADMIN_SIDE);
                            userService.updateState(chatId, UserState.ADMIN_SIDE);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.editToAdminSiteText(currentUser, chatId, messageId));
                        }
                        case "ADMIN_SIDE_USER_SEARCH" -> {
                            currentUser.setState(UserState.ADMIN_SIDE_USER_SEARCH);
                            userService.updateState(chatId, UserState.ADMIN_SIDE_USER_SEARCH);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.showUserSearch(currentUser, chatId, messageId));
                        }
                    }
                } else if (update.getCallbackQuery().getData().startsWith("admin")) {
                    String type = update.getCallbackQuery().getData().substring(5);
                    switch (type) {
                        case "userscount" -> {
                            currentUser.setState(UserState.ADMIN_SIDE_USER_COUNT);
                            userService.updateState(chatId, UserState.ADMIN_SIDE_USER_COUNT);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.showUsersCount(currentUser, chatId, messageId));
                        }
                        case "usersearch" -> {
                            currentUser.setState(UserState.ADMIN_SIDE_USER_SEARCH);
                            userService.updateState(chatId, UserState.ADMIN_SIDE_USER_SEARCH);
                            Integer messageId = update.getCallbackQuery().getMessage().getMessageId();
                            execute(botService.showUserSearch(currentUser, chatId, messageId));
                        }
                    }
                }
            } else {
                switch (currentUser.getState()) {
                    case CHAT_WITH_GPT -> {
                        if (checkMenuOptions(text, currentUser, chatId)) {
                            return;
                        }
                        if (messageService.isChatPresent(currentUser)) {
                            Integer messageId = update.getMessage().getMessageId();
                            Message executedMessage = execute(botService.typing(chatId, messageId));
                            MessageEntity messageEntity = MessageEntity.builder()
                                    .role("user")
                                    .content(text)
                                    .build();
                            messageService.addMessage(currentUser, messageEntity);
                            execute(botService.answerWithGPT(currentUser, text, executedMessage.getMessageId()));
                        } else {
                            Integer messageId = update.getMessage().getMessageId();
                            Message executedMessage = execute(botService.typing(chatId, messageId));
                            MessageEntity messageEntity = MessageEntity.builder()
                                    .role("user")
                                    .content(text)
                                    .build();
                            messageService.registerNewChat(currentUser);
                            messageService.addMessage(currentUser, messageEntity);
                            execute(botService.answerWithGPT(currentUser, text, executedMessage.getMessageId()));
                        }
                    }
                    case SETTINGS -> {
                        if (checkMenuOptions(text, currentUser, chatId)) {
                            return;
                        }
                        Integer messageId = update.getMessage().getMessageId();
                        execute(botService.editShowLanguagesText(currentUser, chatId, messageId));
                    }
                    case ADMIN_SIDE_USER_SEARCH -> {
                        if (checkMenuOptions(text, currentUser, chatId)) {
                            return;
                        }
                        Integer messageId = update.getMessage().getMessageId();
                        execute(botService.showUserByUsername(currentUser, chatId, messageId, text));
                    }
                    default -> {
                        if (!checkMenuOptions(text, currentUser, chatId)) {
                            invalidText(currentUser, chatId);
                        }
                    }
                }
            }
        } catch (DataNotFoundException e) {
            if (message != null && message.hasContact()) {
                userService.addUser(message.getChat(), message.getContact());
                execute(botService.welcome(chatId, message.getChat()));
                execute(botService.showLanguages(null, chatId, false));
            } else if (e.getMessage().equals("User not found!")) {
                execute(botService.requestContact(chatId));
            }
        }
    }

    private boolean checkMenuOptions(String text, UserEntity currentUser, Long chatId) throws TelegramApiException {
        switch (text) {
            case "Start new chat\uD83D\uDCAC", "Yengi chat boshlash\uD83D\uDCAC", "Начать новый чат\uD83D\uDCAC" -> {
                currentUser.setState(UserState.CHAT_WITH_GPT);
                userService.updateState(chatId, UserState.CHAT_WITH_GPT);
                execute(botService.startNewChat(currentUser, chatId));
                return true;
            }
            case "Settings⚙️", "Sozlamalar⚙️", "Настройки⚙️" -> {
                currentUser.setState(UserState.SETTINGS);
                userService.updateState(chatId, UserState.SETTINGS);
                execute(botService.settings(currentUser, chatId));
                return true;
            }
            case "Mani chatlarim\uD83D\uDCAD", "My chats\uD83D\uDCAD", "Мои чаты\uD83D\uDCAD" -> {
                currentUser.setState(UserState.CHATS);
                userService.updateState(chatId, UserState.CHATS);
                execute(botService.showChats(currentUser, chatId));
                return true;
            }
            case "Админ панель\uD83C\uDF9B", "Admin taraf\uD83C\uDF9B", "Admin side\uD83C\uDF9B" -> {
                currentUser.setState(UserState.ADMIN_SIDE);
                userService.updateState(chatId, UserState.ADMIN_SIDE);
                execute(botService.showAdminSide(currentUser, chatId));
                return true;
            }
            default -> {
                return false;
            }
        }
    }

    private void invalidText(UserEntity currentUser, Long chatId) throws TelegramApiException {
        switch (currentUser.getLanguage()) {
            case UZBEK -> execute(new SendMessage(chatId.toString(), "Man sizni chunmadim!"));
            case RUSSIAN -> execute(new SendMessage(chatId.toString(), "Я вас не понял!"));
            case ENGLISH -> execute(new SendMessage(chatId.toString(), "I don't understand you!"));
        }
    }

    @Override
    public String getBotUsername() {
        return "SlovoSkazkabot";
    }
}
