package org.sushino;

import org.sushino.manager.ConnectionsManager;
import org.sushino.wrapper.FtpWrapper;
import org.sushino.wrapper.TelegramResponse;
import org.telegram.telegrambots.meta.api.objects.Document;

import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.InputFile;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;


public class FtpBot extends AbilityBot {

    protected ConnectionsManager manager;

    protected FtpBot(String botToken, String botUsername) {
        super(botToken, botUsername);
        manager = ConnectionsManager.getManager();
        System.out.println("Bot created correctly. Instantiating the connections manager.");
    }

    @Override
    public int creatorId() {
        return Parameters.CREATOR_ID;
    }

    public Ability sayHello() {

        return Ability.builder()
                .name("start")
                .info("Welcome message")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    silent.send("Hello! To connect use /connect ipAddress port username password. Have fun!", messageContext.chatId());
                    System.out.println("New connection from user " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId());
                })
                .build();
    }

    public Ability openConnection() {
        return Ability.builder()
                .name("connect")
                .info("start connection to ftp server")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {

                    if (messageContext.arguments().length <= 3) {
                        silent.send("Can't initiate connection. Missing parameters.", messageContext.chatId());
                        return;
                    }

                    if (manager.getConnection(messageContext.user().getId()) != null) {
                        silent.send("Before opening a new connection, /close the old one", messageContext.chatId());
                        return;
                    }

                    silent.send("Trying to connect ...", messageContext.chatId());

                    String[] params = messageContext.arguments();
                    Integer userId = messageContext.user().getId();

                    /*
                    String ipAddress = params [0];
                    String lPort = params [1];
                    String usr = params [2];
                    String pwd = params [3];
                    boolean tls = params [4];
                    */

                    try {

                        int port = Integer.parseInt(params[1]);
                        if (port < 1 || port > 65535) throw new Exception();

                        FtpWrapper tFtp = new FtpWrapper(params[0], port, params[2], params[3]);
                        if(params.length>4){
                            tFtp.connect(params[4].equals("ssl") || params[4].equals("tls"));
                        }else tFtp.connect(false);

                        manager.registerConnection(userId, tFtp);
                        silent.send("Connected, whenever you want to upload file, just send them.", messageContext.chatId());
                        System.out.println("User" + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId() + " now connected to " + params[0] + ":" + params[1]);

                        silent.send(tFtp.getDirs(), messageContext.chatId());

                    } catch (Exception ignored) {
                        silent.send("Error while connecting. Check your parameters!", messageContext.chatId());
                    }

                })
                .build();
    }

    public Ability currentConnection() {

        return Ability.builder()
                .name("current")
                .info("get current connection info")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    silent.send((tFtp == null) ? "You haven't established a connection yet." : tFtp.toString(), messageContext.chatId());
                })
                .build();
    }

    public Ability closeConnection() {

        return Ability.builder()
                .name("close")
                .info("close current connection")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    if (tFtp != null) {
                        silent.send("Closing connection ...", messageContext.chatId());

                        try {
                            tFtp.close();
                        } catch (IOException ignored) {
                        }

                        manager.unregisterConnection(userId);
                        silent.send("Connection closed.", messageContext.chatId());
                        System.out.println("Connection to ftp server closed by " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId());
                    } else {
                        silent.send("You haven't established a connection yet.", messageContext.chatId());
                    }

                })
                .build();
    }

    public Ability listDirs() {

        return Ability.builder()
                .name("list")
                .info("list dir")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    if (tFtp != null) {
                        try {
                            silent.send(tFtp.getDirs(), messageContext.chatId());
                            System.out.println("User " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId() + " has run the command /list");
                        } catch (IOException ignored) {
                        }
                    } else silent.send("You must establish a connection in order to do that!", messageContext.chatId());

                })
                .build();
    }

    public Ability changeDir() {

        return Ability.builder()
                .name("changedir")
                .info("change working directory")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    if (tFtp != null) {
                        if (messageContext.arguments() != null) {
                            try {
                                if (tFtp.changeDir(messageContext.firstArg())) {
                                    silent.send("Directory changed.", messageContext.chatId());
                                    silent.send(tFtp.getDirs(), messageContext.chatId());
                                    System.out.println("User " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId() + " has run the command /cd");
                                } else
                                    silent.send("Unable to change directory. Check your parameter!", messageContext.chatId());

                            } catch (IOException ignored) {
                            }
                        } else {
                            silent.send("Parameter is missing", messageContext.chatId());
                        }
                    } else silent.send("You must establish a connection in order to do that!", messageContext.chatId());

                })
                .build();
    }

    public Ability catFile() {

        return Ability.builder()
                .name("cat")
                .info("print a file")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    if (tFtp != null) {
                        if (messageContext.arguments() != null) {
                            try {
                                silent.send(tFtp.catFile(messageContext.firstArg()), messageContext.chatId());
                                System.out.println("User " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId() + " has run the command /cat");
                            } catch (IOException ignored) {
                                silent.send("Cannot output file longer than 4096 char, you may try downloading it.", messageContext.chatId());
                            }
                        } else {
                            silent.send("Parameter is missing", messageContext.chatId());
                        }
                    } else silent.send("You must establish a connection in order to do that!", messageContext.chatId());

                })
                .build();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Ability getFile() {
        return Ability.builder()
                .name("get")
                .info("print a file")
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(messageContext -> {
                    Integer userId = messageContext.user().getId();
                    FtpWrapper tFtp = manager.getConnection(userId);

                    if (tFtp != null) {
                        if (messageContext.arguments() != null) {
                            try {
                                File f = tFtp.getFile(messageContext.chatId(), messageContext.firstArg());
                                if (f == null) {
                                    silent.send("You can't download a directory!", messageContext.chatId());
                                    return;
                                }

                                SendDocument document = SendDocument.builder()
                                        .chatId(String.valueOf(messageContext.chatId()))
                                        .document(new InputFile(f))
                                        .build();

                                silent.send("Downloading ...", messageContext.chatId());
                                sender.sendDocument(document);
                                System.out.println("User " + messageContext.user().getFirstName() + " whose id=" + messageContext.user().getId() + " has run the command /get");
                                f.delete();
                            } catch (IOException | TelegramApiException ignored) {
                            }
                        } else {
                            silent.send("Parameter is missing", messageContext.chatId());
                        }
                    } else silent.send("You must establish a connection in order to do that!", messageContext.chatId());

                })
                .build();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (!update.hasMessage()) return;
        new Thread(() -> {

            if (update.getMessage().hasDocument()) {
                FtpWrapper tFtp = manager.getConnection(update.getMessage().getFrom().getId());
                if (tFtp != null) {

                    Document tDocument = update.getMessage().getDocument();
                    TelegramResponse response = TelegramResponse.getResponse(tDocument.getFileId());

                    if (response != null && response.ok) {
                        try {
                            tFtp.uploadFile(downloadFile(response.result.filePath), tDocument.getFileName());
                            System.out.println("User " + update.getMessage().getFrom().getFirstName() + " whose id=" + update.getMessage().getFrom().getId() + " has uploaded a file");
                        } catch (IOException | TelegramApiException ignored) {
                            silent.send("Couldn't upload fil. Retry!", update.getMessage().getChatId());
                        }
                    } else
                        silent.send("An error has occurred while downloading the file from the telegram server!", update.getMessage().getChatId());
                } else
                    silent.send("You must establish a connetion before sending files!", update.getMessage().getChatId());
            }

            FtpBot.super.onUpdateReceived(update);
        }).start();
    }
}
