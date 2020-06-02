package com.rpgbot.cs.discordbot.daos;

import com.rpgbot.cs.discordbot.entities.BasicCommand;
import com.rpgbot.cs.discordbot.entities.CommandType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BasicCommandDao extends JpaRepository<BasicCommand, Long> {
    List<BasicCommand> findByCommandCommandType(CommandType commandType);

    Optional<BasicCommand> findByCommandCommandText(String command);
}