package uz.kpi.telegramchatgpt.service.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.kpi.telegramchatgpt.domain.dto.response.ChatCompletionDTO;
import uz.kpi.telegramchatgpt.domain.entity.user.LanguageEnum;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.domain.entity.user.UserRole;
import uz.kpi.telegramchatgpt.exceptions.DataNotFoundException;
import uz.kpi.telegramchatgpt.repository.user.UserRepository;
import uz.kpi.telegramchatgpt.service.ai.GPTService;
import uz.kpi.telegramchatgpt.service.message.MessageService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {
    private final GPTService gptService;
    private final MessageService messageService;
    private final UserRepository userRepository;

    public SendMessage welcome(Long chatId, Chat chat) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Welcome " + chat.getFirstName());
//        sendMessage.setReplyMarkup(menu(null));
        return sendMessage;
    }

    public SendMessage requestContact(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Please share your number📲");
        sendMessage.setReplyMarkup(requestContactButton());
        return sendMessage;
    }

    private ReplyKeyboardMarkup requestContactButton() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);

        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton("Share phone number");
        button.setRequestContact(true);
        row.add(button);

        replyKeyboardMarkup.setKeyboard(List.of(row));
        return replyKeyboardMarkup;
    }

    public SendMessage welcomeBackMessage(UserEntity currentUser, String chatId) {
        SendMessage sendMessage = new SendMessage();
        switch (currentUser.getLanguage()) {
            case UZBEK -> sendMessage.setText("Salom " + currentUser.getFirstName());
            case RUSSIAN -> sendMessage.setText("Привет " + currentUser.getFirstName());
            case ENGLISH -> sendMessage.setText("Welcome " + currentUser.getFirstName());
        }
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(menu(currentUser));
        return sendMessage;
    }

    private ReplyKeyboardMarkup menu(UserEntity currentUser) {
        if (currentUser == null) {
            currentUser = new UserEntity();
            currentUser.setLanguage(LanguageEnum.ENGLISH);
        }
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        List<KeyboardRow> rows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        switch (currentUser.getLanguage()) {
            case UZBEK -> button.setText("Yengi chat boshlash\uD83D\uDCAC");
            case ENGLISH -> button.setText("Start new chat\uD83D\uDCAC");
            case RUSSIAN -> button.setText("Начать новый чат\uD83D\uDCAC");
        }
        row.add(button);
        button = new KeyboardButton();
        switch (currentUser.getLanguage()) {
            case UZBEK -> button.setText("Mani chatlarim\uD83D\uDCAD");
            case ENGLISH -> button.setText("My chats\uD83D\uDCAD");
            case RUSSIAN -> button.setText("Мои чаты\uD83D\uDCAD");
        }
        row.add(button);
        rows.add(row);
        row = new KeyboardRow();
        if (currentUser.getRole().equals(UserRole.ADMIN)) {
            button = new KeyboardButton();
            switch (currentUser.getLanguage()) {
                case ENGLISH -> button.setText("Admin side\uD83C\uDF9B");
                case UZBEK -> button.setText("Admin taraf\uD83C\uDF9B");
                case RUSSIAN -> button.setText("Админ панель\uD83C\uDF9B");
            }
            row.add(button);
        }
        button = new KeyboardButton();
        switch (currentUser.getLanguage()) {
            case UZBEK -> button.setText("Sozlamalar⚙️");
            case RUSSIAN -> button.setText("Настройки⚙️");
            case ENGLISH -> button.setText("Settings⚙️");
        }
        row.add(button);
        rows.add(row);
        replyKeyboardMarkup.setKeyboard(rows);
        return replyKeyboardMarkup;
    }

    public EditMessageText answerWithGPT(UserEntity user, String text, Integer messageId) {
        EditMessageText editMessageText = new EditMessageText();
        ChatCompletionDTO answer = gptService.answer(user, text);
        editMessageText.setText(answer.getChoices().get(0).getMessage().getContent());
        editMessageText.setChatId(user.getChatId());
        editMessageText.setReplyMarkup(null);
        editMessageText.setMessageId(messageId);
        editMessageText.setParseMode("Markdown");
        return editMessageText;
    }

    public SendMessage settings(UserEntity currentUser, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        switch (currentUser.getLanguage()) {
            case UZBEK -> sendMessage.setText("Sozlamalar⚙️");
            case RUSSIAN -> sendMessage.setText("Настройки⚙️");
            case ENGLISH -> sendMessage.setText("Settings⚙️");
        }
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(settings(currentUser.getLanguage()));
        return sendMessage;
    }

    private InlineKeyboardMarkup settings(LanguageEnum language) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        switch (language) {
            case ENGLISH -> button.setText("Language\uD83C\uDDFA\uD83C\uDDF8");
            case UZBEK -> button.setText("Til\uD83C\uDDFA\uD83C\uDDFF");
            case RUSSIAN -> button.setText("Язык\uD83C\uDDF7\uD83C\uDDFA");
        }
        button.setCallbackData("settingslanguage");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (language) {
            case ENGLISH -> button.setText("Model\uD83E\uDD16");
            case UZBEK -> button.setText("Modellar\uD83E\uDD16");
            case RUSSIAN -> button.setText("Модели\uD83E\uDD16");
        }
        button.setCallbackData("settingsmodels");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (language) {
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
        }
        button.setCallbackData("backCHAT_WITH_GPT");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public SendMessage showLanguages(UserEntity currentUser, Long chatId, boolean hasBackButton) {
        if (currentUser == null) {
            currentUser = new UserEntity();
            currentUser.setLanguage(LanguageEnum.ENGLISH);
        }
        SendMessage sendMessage = new SendMessage();
        switch (currentUser.getLanguage()) {
            case UZBEK -> sendMessage.setText("Iltimos tilni tanlang:");
            case RUSSIAN -> sendMessage.setText("Пожалуйста выберите язык:");
            case ENGLISH -> sendMessage.setText("Please choose language:");
        }
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(displayLanguages(hasBackButton, currentUser.getLanguage()));
        return sendMessage;
    }

    private InlineKeyboardMarkup displayLanguages(boolean hasBackButton, LanguageEnum languageEnum) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Russian\uD83C\uDDF7\uD83C\uDDFA");
        button.setCallbackData("language-russian");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("Uzbek\uD83C\uDDFA\uD83C\uDDFF");
        button.setCallbackData("language-uzbek");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("English\uD83C\uDDFA\uD83C\uDDF8");
        button.setCallbackData("language-english");
        row.add(button);
        rows.add(row);
        if (hasBackButton) {
            row = new ArrayList<>();
            button = new InlineKeyboardButton();
            switch (languageEnum) {
                case ENGLISH -> button.setText("Back\uD83D\uDD19");
                case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
                case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
            }
            button.setCallbackData("backSETTINGS");
            row.add(button);
            rows.add(row);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public SendMessage gptGreeting(UserEntity currentUser, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        switch (currentUser.getLanguage()) {
            case UZBEK -> sendMessage.setText("Salom, men chatGPT yordamchiman, sizga qanday yordam berishim mumkin?");
            case RUSSIAN ->
                    sendMessage.setText("Здравствуйте, я професиональный помощьник ChatGPT, чем я могу вам помочь?");
            case ENGLISH -> sendMessage.setText("Hi there, I am professional assistant chatGPT, how can I help you?");
        }
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(menu(currentUser));
        return sendMessage;
    }

    public SendMessage startNewChat(UserEntity currentUser, Long chatId) {
        messageService.registerNewChat(currentUser);
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        switch (currentUser.getLanguage()) {
            case UZBEK ->
                    sendMessage.setText("Yengi chat boshlandi, har kuni chatni yangilab turishingizni tavsiya qilamiz!");
            case RUSSIAN -> sendMessage.setText("Новый чат, советуем обновлять чат ежедневно!");
            case ENGLISH -> sendMessage.setText("New chat started, we recommend to renew chat daily!");
        }
        return sendMessage;
    }

    public SendMessage showChats(UserEntity currentUser, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        switch (currentUser.getLanguage()) {
            case UZBEK ->
                    sendMessage.setText("Mana sizning chatlaringiz.\nQaysidur chatni davom etish uchin ! + raqamini" +
                            "yozing.");
            case ENGLISH -> sendMessage.setText("Here are your chats.\nTo continue chat type ! + chat's number.");
            case RUSSIAN ->
                    sendMessage.setText("Вот ваши чаты.\nЧтобы продолжить какой-то чат введите ! + номер чата.");
        }
        sendMessage.setReplyMarkup(cancelChoice(currentUser.getLanguage()));
        return sendMessage;
    }

    private InlineKeyboardMarkup cancelChoice(LanguageEnum language) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        switch (language) {
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
        }
        button.setCallbackData("backCHAT_WITH_GPT");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public EditMessageText editShowLanguagesText(UserEntity currentUser, Long chatId, Integer messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(messageId);
        editMessageText.setChatId(chatId);
        switch (currentUser.getLanguage()) {
            case ENGLISH -> editMessageText.setText("Settings⚙️\n-> Language\uD83C\uDDFA\uD83C\uDDF8");
            case RUSSIAN -> editMessageText.setText("Настройки⚙️\n-> Язык\uD83C\uDDF7\uD83C\uDDFA");
            case UZBEK -> editMessageText.setText("Sozlamalar⚙️\n-> Til\uD83C\uDDFA\uD83C\uDDFF");
        }
        editMessageText.setReplyMarkup(displayLanguages(true, currentUser.getLanguage()));
        return editMessageText;
    }

    public EditMessageText editToSettingsText(UserEntity currentUser, Long chatId, Integer messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        switch (currentUser.getLanguage()) {
            case ENGLISH -> editMessageText.setText("Settings⚙️");
            case RUSSIAN -> editMessageText.setText("Настройки⚙️");
            case UZBEK -> editMessageText.setText("Sozlamalar⚙️");
        }
        editMessageText.setReplyMarkup(settings(currentUser.getLanguage()));
        return editMessageText;
    }

    public EditMessageText editShowModelsText(UserEntity currentUser, Long chatId, Integer messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        switch (currentUser.getLanguage()) {
            case ENGLISH -> editMessageText.setText("Settings⚙️\n-> Models\uD83E\uDD16");
            case RUSSIAN -> editMessageText.setText("Настройки⚙️\n-> Модели\uD83E\uDD16");
            case UZBEK -> editMessageText.setText("Sozlamalar⚙️\n-> Modellar\uD83E\uDD16");
        }
        editMessageText.setReplyMarkup(models(currentUser.getLanguage()));
        return editMessageText;
    }

    private InlineKeyboardMarkup models(LanguageEnum language) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("GPT 3.5 turbo");
        button.setCallbackData("modelGPT_3.5_TURBO");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("GPT 4");
        button.setCallbackData("modelGPT_4");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        button.setText("GPT 4 turbo");
        button.setCallbackData("modelGPT_4_TURBO");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (language) {
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
        }
        button.setCallbackData("backSETTINGS");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public SendMessage typing(Long chatId, Integer messageId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyToMessageId(messageId);
        sendMessage.setText("\uD83D\uDCBB");
        return sendMessage;
    }

    public DeleteMessage delete(Integer messageId, Long chatId) {
        DeleteMessage deleteMessage = new DeleteMessage();
        deleteMessage.setChatId(chatId);
        deleteMessage.setMessageId(messageId);
        return deleteMessage;
    }

    public SendMessage showAdminSide(UserEntity currentUser, Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        switch (currentUser.getLanguage()) {
            case UZBEK -> sendMessage.setText("Admin taraf\uD83C\uDF9B");
            case ENGLISH -> sendMessage.setText("Admin side\uD83C\uDF9B");
            case RUSSIAN -> sendMessage.setText("Админ панель\uD83C\uDF9B");
        }
        sendMessage.setReplyMarkup(adminSide(currentUser));
        return sendMessage;
    }

    private InlineKeyboardMarkup adminSide(UserEntity currentUser) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Users count\uD83D\uDC64");
            case UZBEK -> button.setText("Foydalanuvchilar soni\uD83D\uDC64");
            case RUSSIAN -> button.setText("Количество пользователей\uD83D\uDC64");
        }
        button.setCallbackData("adminuserscount");
        row.add(button);
        button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Search user\uD83D\uDD0D");
            case UZBEK -> button.setText("Foydalanuvchi qidirish\uD83D\uDD0D");
            case RUSSIAN -> button.setText("Поиск пользователя\uD83D\uDD0D");
        }
        button.setCallbackData("adminusersearch");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Users by language\uD83C\uDDFA\uD83C\uDDF8");
            case RUSSIAN -> button.setText("Пользователи по языку\uD83C\uDDF7\uD83C\uDDFA");
            case UZBEK -> button.setText("Til boyicha qiridish\uD83C\uDDFA\uD83C\uDDFF");
        }
        button.setCallbackData("adminuserbylanguage");
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
        }
        button.setCallbackData("backCHAT_WITH_GPT");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public EditMessageText showUsersCount(UserEntity currentUser, Long chatId, Integer messageId) {
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            return null;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setMessageId(messageId);
        editMessageText.setChatId(chatId);
        editMessageText.setReplyMarkup(backToAdminSide(currentUser));
        switch (currentUser.getLanguage()) {
            case ENGLISH -> editMessageText.setText("Admin side\uD83C\uDF9B\n-> Users count\uD83D\uDC64" +
                    "\nCount: " + userRepository.countAll());
            case RUSSIAN ->
                    editMessageText.setText("Админ панель\uD83C\uDF9B\n-> Количество пользователей\uD83D\uDC64" +
                            "\nКоличество: " + userRepository.countAll());
            case UZBEK -> editMessageText.setText("Admin taraf\uD83C\uDF9B\n-> Foydalanuvchilar soni\uD83D\uDC64" +
                    "\nSoni: " + userRepository.countAll());
        }
        return editMessageText;
    }

    private InlineKeyboardMarkup backToAdminSide(UserEntity currentUser) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        InlineKeyboardButton button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
        }
        button.setCallbackData("backADMIN_SIDE");
        inlineKeyboardMarkup.setKeyboard(List.of(List.of(button)));
        return inlineKeyboardMarkup;
    }

    public EditMessageText editToAdminSiteText(UserEntity currentUser, Long chatId, Integer messageId) {
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        switch (currentUser.getLanguage()) {
            case UZBEK -> editMessageText.setText("Admin taraf\uD83C\uDF9B");
            case ENGLISH -> editMessageText.setText("Admin side\uD83C\uDF9B");
            case RUSSIAN -> editMessageText.setText("Админ панель\uD83C\uDF9B");
        }
        editMessageText.setReplyMarkup(adminSide(currentUser));
        return editMessageText;
    }

    public EditMessageText showUserSearch(UserEntity currentUser, Long chatId, Integer messageId) {
        if (!currentUser.getRole().equals(UserRole.ADMIN)) {
            return null;
        }
        EditMessageText editMessageText = new EditMessageText();
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        switch (currentUser.getLanguage()) {
            case ENGLISH -> editMessageText.setText("Enter phone number of the user: ");
            case RUSSIAN -> editMessageText.setText("Введите телефон номер пользователя: ");
            case UZBEK -> editMessageText.setText("Foydalanuvchining telefon raqamini kiriting: ");
        }
        editMessageText.setReplyMarkup(backToAdminSide(currentUser));
        return editMessageText;
    }

    public SendMessage showUserByUsername(UserEntity currentUser, Long chatId, Integer messageId, String phoneNumber) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(backToAdminSide(currentUser));
        UserEntity userEntity = userRepository.findUserEntitiesByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new DataNotFoundException("User not found!"));
        switch (currentUser.getLanguage()) {
            case ENGLISH -> sendMessage.setText("Admin side\uD83C\uDF9B\n" +
                    "-> Search user\uD83D\uDD0D\n" +
                    "Result: " + "\n" +
                    "Firstname: " + userEntity.getFirstName() + "\n" +
                    "Lastname: " + userEntity.getLastName() + "\n" +
                    "Role: " + userEntity.getRole() + "\n" +
                    "Language: " + userEntity.getLanguage());
            case RUSSIAN -> sendMessage.setText("Админ панель\uD83C\uDF9B\n" +
                    "-> Поиск пользователя\uD83D\uDD0D\n" +
                    "Результат: " + "\n" +
                    "Имя: " + userEntity.getFirstName() + "\n" +
                    "Фамилия: " + userEntity.getLastName() + "\n" +
                    "Роль: " + userEntity.getRole() + "\n" +
                    "Язык: " + userEntity.getLanguage());
            case UZBEK -> sendMessage.setText("Admin taraf\uD83C\uDF9B\n" +
                    "-> Foydalanuvchi qidirish\uD83D\uDD0D\n" +
                    "Natija: " + "\n" +
                    "Ismi: " + userEntity.getFirstName() + "\n" +
                    "Familiyasi: " + userEntity.getLastName() + "\n" +
                    "Roli: " + userEntity.getRole() + "\n" +
                    "Tili: " + userEntity.getLanguage());
        }
        sendMessage.setReplyMarkup(userActions(currentUser,userEntity));
        return sendMessage;
    }

    private InlineKeyboardMarkup userActions(UserEntity currentUser,UserEntity user) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Give unlimited access\uD83D\uDCB8");
            case RUSSIAN -> button.setText("Дать неограниченный доступ\uD83D\uDCB8");
            case UZBEK -> button.setText("Cheksiz ruxsat berish\uD83D\uDCB8");
        }
        button.setCallbackData("promoteunlimit" + user.getPhoneNumber());
        row.add(button);
        rows.add(row);
        row = new ArrayList<>();
        button = new InlineKeyboardButton();
        switch (currentUser.getLanguage()) {
            case ENGLISH -> button.setText("Back\uD83D\uDD19");
            case UZBEK -> button.setText("Orqaga\uD83D\uDD19");
            case RUSSIAN -> button.setText("Назад\uD83D\uDD19");
        }
        button.setCallbackData("backADMIN_SIDE_USER_SEARCH");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
