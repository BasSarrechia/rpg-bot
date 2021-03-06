package com.rpgbot.cs.discordbot.services;

import com.rpgbot.cs.discordbot.daos.BasicCommandDao;
import com.rpgbot.cs.discordbot.daos.CommandDao;
import com.rpgbot.cs.discordbot.entities.Authorization;
import com.rpgbot.cs.discordbot.entities.BasicCommand;
import com.rpgbot.cs.discordbot.entities.Command;
import com.rpgbot.cs.discordbot.entities.CommandType;
import com.rpgbot.cs.discordbot.exception.CommandExistsException;
import com.rpgbot.cs.discordbot.exception.CommandNotExistsException;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.springframework.stereotype.Service;

import java.awt.*;
import java.util.Optional;

@Service
public class CommandService {
    private final BotService botService;
    private final BasicCommandDao basicCommandDao;
    private final CommandDao commandDao;

    public CommandService(BotService botService, BasicCommandDao basicCommandDao, CommandDao commandDao) {
        this.botService = botService;
        this.basicCommandDao = basicCommandDao;
        this.commandDao = commandDao;
    }

    private void addCharacterSelectionMenu() {
        botService.getDiscordApi().addMessageCreateListener(messageCreateEvent -> {
            if (messageCreateEvent.getMessageContent().toLowerCase().startsWith("!characters")) {

                messageCreateEvent.getChannel().sendMessage(new EmbedBuilder()
                        .setColor(Color.PINK)
                        .setAuthor("Hi")
                );
            }
        });
    }

    public void register(String command, String respond) {
        basicCommandDao.findByCommandCommandText(command).ifPresentOrElse(basicCommand -> {
            throw new CommandExistsException(command);
        }, () -> {
            BasicCommand basicCommand = basicCommandDao.save(BasicCommand.builder()
                    .response(respond)
                    .command(Command.builder()
                            .commandText(command)
                            .requiredAuthorization(Authorization.BASIC)
                            .commandType(CommandType.BASIC)
                            .build())
                    .build());
            basicCommandDao.save(basicCommand);
        });

    }

    public BasicCommand lookUp(String command) {
        return basicCommandDao.findByCommandCommandText(command).orElseThrow(() -> new CommandNotExistsException(command));
    }

    public void removeCommand(String commandName) {
        commandDao.findByCommandText(commandName)
                .ifPresentOrElse(commandDao::delete, () -> {
                    throw new CommandNotExistsException(commandName);
                });
    }

    public void modifyCommand(String command, String respond) {
        Optional<BasicCommand> basicCommandOptional = basicCommandDao.findByCommandCommandText(command);
        if (basicCommandOptional.isPresent()) {
            BasicCommand basicCommand = basicCommandOptional.get();
            basicCommand.setResponse(respond);
            basicCommandDao.save(basicCommand);
        } else {
            throw new CommandNotExistsException(command);
        }
    }
}
