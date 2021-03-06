package com.rpgbot.cs.discordbot.router;

import com.rpgbot.cs.discordbot.annotations.Command;
import com.rpgbot.cs.discordbot.configuration.DiscordBotConfiguration;
import com.rpgbot.cs.discordbot.events.CommandMessageEvent;
import com.rpgbot.cs.discordbot.exception.CommandExistsException;
import com.rpgbot.cs.discordbot.exception.CommandNotExistsException;
import com.rpgbot.cs.discordbot.messagehandlers.DiscordMessage;
import com.rpgbot.cs.discordbot.services.CommandService;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.util.Arrays;

@Component
public class DbCommandRouter {

    private final CommandService commandService;
    private final DiscordBotConfiguration discordBotConfiguration;

    @Autowired
    public DbCommandRouter(CommandService commandService, DiscordBotConfiguration discordBotConfiguration) {
        this.commandService = commandService;
        this.discordBotConfiguration = discordBotConfiguration;
    }

    @Order(-1)
    @Command
    public DiscordMessage dbCommand(final CommandMessageEvent commandMessageEvent) {
        try{
          return DiscordMessage.plain(commandService.lookUp(commandMessageEvent.getCommand()).getResponse());
        } catch (CommandNotExistsException existsException){
            return DiscordMessage.empty();
        }
    }

    @Command(alias = "addcommand")
    public DiscordMessage addCommand(final CommandMessageEvent commandMessageEvent) {
        if (validateCommandLength(commandMessageEvent)) {
            String command = commandMessageEvent.getArgs()[0];
            String response = String.join(" ", Arrays.copyOfRange(commandMessageEvent.getArgs(), 1, commandMessageEvent.getArgs().length));
            try {
                commandService.register(command, response);
                return DiscordMessage.success("Command Added", command + ": " + response);
            } catch (CommandExistsException commandExistsException) {
                return DiscordMessage.error(commandExistsException .getMessage());
            }
        }
        return DiscordMessage.error("usage: " + discordBotConfiguration.getPrefix() + "addcommand <command> <response>");
    }

    @Command(alias = "modifycommand")
    public DiscordMessage editCommand(final CommandMessageEvent commandMessageEvent) {
        if (validateCommandLength(commandMessageEvent)) {
            String command = commandMessageEvent.getArgs()[0];
            String response = String.join(" ", Arrays.copyOfRange(commandMessageEvent.getArgs(), 1, commandMessageEvent.getArgs().length));
            try {
                commandService.modifyCommand(command, response);
                return DiscordMessage.embedded(new EmbedBuilder().setTitle("Command Modified").setDescription(command + ": " + response).setColor(Color.pink));
            } catch (CommandNotExistsException commandNotExistsException) {
                return DiscordMessage.error(commandNotExistsException.getMessage());
            }
        }
        return DiscordMessage.error("usage: " + discordBotConfiguration.getPrefix() + "modifycommand <command> <response>");
    }

    @Command(alias = "removecommand")
    public DiscordMessage removeCommand(final CommandMessageEvent commandMessageEvent) {
        if (commandMessageEvent.getArgs().length == 1) {
            String command = commandMessageEvent.getArgs()[0];
            try {
                commandService.removeCommand(command);
                return DiscordMessage.success("Command removed", command);
            } catch (CommandNotExistsException commandNotExistsException) {
                // TODO replace this with a followup dialog "would you like to add it?"
                return DiscordMessage.error(commandNotExistsException.getMessage());
            }
        }
        return DiscordMessage.error("Please provide one argument");
    }

    private boolean validateCommandLength(final CommandMessageEvent commandMessageEvent) {
        return commandMessageEvent.getArgs().length >= 2;
    }
}