package org.sushino;

import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

public class CoreClass {

    public static void main(String ... args){
        System.out.println("Starting the bot...");
        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new org.sushino.FtpBot(org.sushino.Parameters.BOT_TOKEN, org.sushino.Parameters.BOT_USERNAME));
        } catch (TelegramApiException e) {
            System.err.println("An error has occurred while starting the bot.");
        }
    }

}