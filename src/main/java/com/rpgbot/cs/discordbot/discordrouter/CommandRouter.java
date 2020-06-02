package com.rpgbot.cs.discordbot.discordrouter;

import com.rpgbot.cs.discordbot.configuration.DiscordBotConfiguration;
import com.rpgbot.cs.discordbot.entities.BasicCommand;
import com.rpgbot.cs.discordbot.entities.DiscordUser;
import com.rpgbot.cs.discordbot.services.BotService;
import com.rpgbot.cs.discordbot.services.CommandService;
import lombok.RequiredArgsConstructor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.awt.*;
import java.util.Arrays;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CommandRouter {
    private final BotService botService;
    private final CommandService commandService;
    private final DiscordBotConfiguration discordBotConfiguration;

    @PostConstruct
    private void register(){
        this.botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().stripLeading().toLowerCase();
            if (validateInput(message)) {
                if (getCommand(message).equals("register")) {
                    Optional<User> discordUser = messageCreateEvent.getMessageAuthor().asUser();
                    if(discordUser.isPresent()){
                        User user = discordUser.get();
                        DiscordUser.builder()
                                .id(user.getId())
                                .preferredColor(Color.pink)
                                .nickname("")
                                .build();
                    }
                }
            }

        });
    }

    @PostConstruct
    private void addCharacterSelectionMenu(){
        botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().toLowerCase().stripLeading();
            if (validateInput(message)) {
                if (getCommand(message).equals("characters")) {
                    messageCreateEvent.getChannel().sendMessage(new EmbedBuilder()
                    .setColor(Color.PINK)
                    .setAuthor("Hi")
                );

                }
            }
        });
    }

    @PostConstruct
    public void createStaticCommand() {
        this.botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().stripLeading().toLowerCase();
            if (validateInput(message)) {
                if (getCommand(message).equals("addcommand")) {
                    if (message.split(" ").length > 2) {
                        String command = message.split(" ")[1];
                        String response = String.join(" ", Arrays.copyOfRange(message.split(" "), 2, message.split(" ").length));
                        commandService.registerBasicCommand(command, response);
                        messageCreateEvent.getChannel().sendMessage("Command added - " + command +": " + response);
                    } else {
                        messageCreateEvent.getChannel().sendMessage(new EmbedBuilder()
                                .setColor(Color.RED)
                                .addField("How to use this command", discordBotConfiguration.getPrefix() + "addcommand <command> <respond>")
                                .setFooter("adds a static command to the bot")
                        );
                    }

                }

            }
        });
    }

    @PostConstruct
    public void updateStaticCommand() {
        this.botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().stripLeading().toLowerCase();
            if (validateInput(message)) {
                if (getCommand(message).equals("modifycommand")) {
                    if (message.split(" ").length > 2) {
                        String command = message.split(" ")[1];
                        String response = String.join(" ", Arrays.copyOfRange(message.split(" "), 2, message.split(" ").length));
                        if (commandService.modifyCommand(command, response)) {
                            messageCreateEvent.getChannel().sendMessage("Command modified - " + command +": " + response);
                        } else {
                            commandService.registerBasicCommand(command, response);
                            messageCreateEvent.getChannel().sendMessage("Command not found, added to database - " + command + ": " + response);
                        }
                    } else {
                        messageCreateEvent.getChannel().sendMessage(new EmbedBuilder()
                                .setColor(Color.RED)
                                .addField("How to use this command", discordBotConfiguration.getPrefix() + "modifycommand <command> <respond>")
                                .setFooter("modifies a static command in the bot")
                        );
                    }
                }
            }
        });
    }

    @PostConstruct
    public void deleteStaticCommand() {
        this.botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().stripLeading().toLowerCase();
            if (validateInput(message)) {
                if (getCommand(message).equals("removecommand")) {
                    if (message.indexOf(" ") > -1) {
                        if (commandService.removeCommand(message.split(" ")[1])) {
                            messageCreateEvent.getChannel().sendMessage("Command removed: " + message.split(" ")[1]);
                        } else {
                            messageCreateEvent.getChannel().sendMessage("Command not found, operation failed.");
                        }
                    }
                }
            }
        });
    }

    @PostConstruct
    public void getStaticCommand() {
        this.botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            String message = messageCreateEvent.getMessageContent().stripLeading().toLowerCase();
            if (validateInput(message)) {
                BasicCommand basicCommand = commandService.lookupCommand(getCommand(message));
                if (basicCommand != null) {
                    messageCreateEvent.getChannel().sendMessage(basicCommand.getResponse());
                }
            }
        });
    }

    private boolean validateInput(String input) {
        return input.startsWith(discordBotConfiguration.getPrefix());
    }

    private String getCommand(String input) {
        String message = input.split(" ")[0];
        if (message.length() > discordBotConfiguration.getPrefix().length()) {
            return message.substring(discordBotConfiguration.getPrefix().length());
        }
        return discordBotConfiguration.getPrefix();
    }

}
