package xyz.gupton.nickolas.beepsky.owner.commands;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Collectors;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.EmbedBuilder;
import xyz.gupton.nickolas.beepsky.BotUtils;
import xyz.gupton.nickolas.beepsky.Command;
import xyz.gupton.nickolas.beepsky.owner.Owner;

public class UnbanCommand implements Command {

  /**
   * Checks things such as prefix and permissions to determine if a commands should be executed.
   *
   * @param message The message received.
   * @return True if the commands should be executed.
   */
  @Override
  public boolean shouldExecute(IMessage message) {
    if (message.getChannel().isPrivate() && message.getAuthor() == Owner.user) {
      if (message.toString().split(" ").length != 2) {
        return false;
      }
      return (message.toString().toLowerCase().startsWith("unban"));
    }

    return false;
  }

  /**
   * Unbans the user specified in the message.
   *
   * @param event Provided by D4J.
   */
  @Override
  public void execute(MessageReceivedEvent event) {
    EmbedBuilder builder = new EmbedBuilder();
    builder.withColor(100, 255, 100);

    Long userId = Long.parseUnsignedLong(event.getMessage().toString().split(" ", 2)[1]);

    if (!BotUtils.isBanned(userId.toString())) {
      builder.withColor(255, 0, 0);
      builder.withTitle("Error Unbanning");
      builder.withDescription(BotUtils.CLIENT.getUserByID(userId).getName()
          + " is not banned.");
      Owner.sendMessage(builder);
      return;
    }

    try {
      File file = new File("banned.txt");
      List<String> out = Files.lines(file.toPath())
          .filter(line -> !line.contains(userId.toString()))
          .collect(Collectors.toList());
      Files.write(file.toPath(), out, StandardOpenOption.WRITE,
          StandardOpenOption.TRUNCATE_EXISTING);

      builder.withTitle("Unban Successful");
      builder.withDescription(BotUtils.CLIENT.getUserByID(userId).getName()
          + " has been unbanned.");
    } catch (Exception e) {
      builder.withColor(255, 0, 0);
      e.printStackTrace();
      builder.withTitle("Error Unbanning");
      builder.withDescription("Error: " + e.getMessage());
    }

    Owner.sendMessage(builder);
  }

  /**
   * Returns the usage string for the commands.
   *
   * @return String of the correct usage for the commands.
   */
  @Override
  public String getCommand(IUser recipient) {
    if (recipient == Owner.user) {
      return "`unban <Discord ID>` - Unbans the user with that Discord ID from using this bot.";
    }

    return "";
  }
}