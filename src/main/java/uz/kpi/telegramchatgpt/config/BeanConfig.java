package uz.kpi.telegramchatgpt.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;
import uz.kpi.telegramchatgpt.service.GPTBot;

@Configuration
public class BeanConfig {
    @Bean
    public TelegramBotsApi telegramBotsApi(GPTBot gptBot) throws TelegramApiException {
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi(DefaultBotSession.class);
        telegramBotsApi.registerBot(gptBot);
        return telegramBotsApi;
    }
    @Bean
    public RestTemplate restTemplate() {return new RestTemplate();}
}
