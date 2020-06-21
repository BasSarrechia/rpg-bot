package com.rpgbot.cs.discordbot.services;

import com.rpgbot.cs.discordbot.daos.BasicCommandDao;
import com.rpgbot.cs.discordbot.daos.CommandDao;
import com.rpgbot.cs.discordbot.entities.*;
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
         lookUp(command).ifPresent(basicCommand -> {throw new CommandExistsException(command);});
            BasicCommand basicCommand = basicCommandDao.save(BasicCommand.builder()
                    .response(respond)
                    .command(Command.builder()
                            .commandText(command)
                            .requiredAuthorization(Authorization.BASIC)
                            .commandType(CommandType.BASIC)
                            .build())
                    .build());
            basicCommandDao.save(basicCommand);
    }

    public Optional<BasicCommand> lookUp(String command) {
        return basicCommandDao.findByCommandCommandText(command);
    }

    public boolean removeCommand(String commandName) {
        Optional<Command> command = commandDao.findByCommandText(commandName);
        if (command.isPresent()) {
            commandDao.delete(command.get());
            return true;
        }
        return false;
    }

    public boolean modifyCommand(String command, String respond) {
        Optional<BasicCommand> basicCommandOptional = basicCommandDao.findByCommandCommandText(command);
        if (basicCommandOptional.isPresent()) {
            BasicCommand basicCommand = basicCommandOptional.get();
            basicCommand.setResponse(respond);
            basicCommandDao.save(basicCommand);
            return true;
        }
        return false;
    }
}
