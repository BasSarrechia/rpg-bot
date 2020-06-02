package com.rpgbot.cs.discordbot.services;

import com.rpgbot.cs.discordbot.daos.BasicCommandDao;
import com.rpgbot.cs.discordbot.daos.CommandDao;
import com.rpgbot.cs.discordbot.entities.*;
import lombok.RequiredArgsConstructor;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.user.User;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.persistence.Basic;
import java.awt.*;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CommandService {
    private final BotService botService;
    private final BasicCommandDao basicCommandDao;
    private final CommandDao commandDao;

    public void registerBasicCommand(String command, String respond) {
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

    public BasicCommand lookupCommand(String command) {
        return basicCommandDao.findByCommandCommandText(command).orElse(null);
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
