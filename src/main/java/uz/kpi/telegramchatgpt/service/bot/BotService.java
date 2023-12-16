package uz.kpi.telegramchatgpt.service.bot;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import uz.kpi.telegramchatgpt.domain.dto.response.ChatCompletionDTO;
import uz.kpi.telegramchatgpt.domain.entity.user.UserEntity;
import uz.kpi.telegramchatgpt.service.ai.GPTService;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BotService {
    private final GPTService gptService;
    public SendMessage welcome(Long chatId, Chat chat, boolean isMenu) {
        SendMessage sendMessage = new SendMessage(chatId.toString(), "Welcome " + chat.getFirstName());
        sendMessage.setReplyMarkup(menu());
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

    public SendMessage welcomeBackMessage(String chatId, Chat chat) {
        SendMessage sendMessage = new SendMessage(chatId, "Welcome back " + chat.getFirstName());
        sendMessage.setReplyMarkup(menu());
        return sendMessage;
    }

    private ReplyKeyboardMarkup menu() {
        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setOneTimeKeyboard(true);
        KeyboardRow row = new KeyboardRow();
        KeyboardButton button = new KeyboardButton();
        button.setText("Start new chat");
        row.add(button);
        button = new KeyboardButton();
        button.setText("Settings⚙️");
        row.add(button);
        replyKeyboardMarkup.setKeyboard(List.of(row));
        return replyKeyboardMarkup;
    }

    public SendMessage answerWithGPT(UserEntity user, String text) {
        SendMessage sendMessage = new SendMessage();
        ChatCompletionDTO answer = gptService.answer(user,text);
        sendMessage.setText(answer.getChoices().get(0).getMessage().getContent());
        sendMessage.setChatId(user.getChatId());
        sendMessage.setReplyMarkup(null);
        sendMessage.setParseMode("Markdown");
        return sendMessage;
    }

    public SendMessage settings(Long chatId) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.setText("Settings⚙️");
        sendMessage.setChatId(chatId);
        sendMessage.setReplyMarkup(settings());
        return sendMessage;
    }

    private InlineKeyboardMarkup settings() {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText("Language");
        button.setCallbackData("settingslanguage");
        row.add(button);
        rows.add(row);
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public SendMessage showLanguages(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),"Please choose language:");
        sendMessage.setReplyMarkup(displayLanguages());
        return sendMessage;
    }

    private InlineKeyboardMarkup displayLanguages() {
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
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public SendMessage gptGreeting(Long chatId) {
        SendMessage sendMessage = new SendMessage(chatId.toString(),"Hi there, I am GPT Assistant, how can i help you?");
        sendMessage.setReplyMarkup(menu());
        return sendMessage;
    }
}
