package com.nickolasgupton.beepsky;

import com.nickolasgupton.beepsky.music.VoiceEvents;
import com.nickolasgupton.beepsky.owner.Owner;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.ActivityType;
import sx.blah.discord.handle.obj.StatusType;
import sx.blah.discord.util.EmbedBuilder;

class Main {

  /**
   * The main method for the bot, handles login, registering listeners, and alerting the owner.
   *
   * @param args Must be in the form [Discord token, Owner ID]
   */
  public static void main(String[] args) {

    if (args.length != 2) {
      System.out.println("Usage: java -jar Officer-Beepsky-x.x.x.jar <Discord token> <Owner ID>");
      System.exit(1);
    }

    // setup client and owner ID
    BotUtils.CLIENT = new ClientBuilder().withToken(args[0]).withRecommendedShardCount().build();

    // Register listeners via the EventSubscriber annotation which allows for organisation and
    // delegation of events
    BotUtils.CLIENT.getDispatcher().registerListener(new CommandHandler());
    BotUtils.CLIENT.getDispatcher().registerListener(new VoiceEvents());

    // Only login after all events are registered otherwise some may be missed.
    BotUtils.CLIENT.login();

    try {
      // Will not continue until the bot is fully ready to use.
      BotUtils.CLIENT.getDispatcher().waitFor(ReadyEvent.class);


      // the "Playing:" text
      BotUtils.CLIENT.changePresence(StatusType.ONLINE, ActivityType.PLAYING, ".help for commands");

      Owner.user = BotUtils.CLIENT.getUserByID(Long.parseLong(args[1]));

      // send message of successful startup to bot owner
      EmbedBuilder startupMessage = new EmbedBuilder();
      startupMessage.withColor(100, 255, 100);
      startupMessage.withTitle("Startup complete!");
      startupMessage.withDescription("Current version: " + BotUtils.VERSION);
      Owner.sendMessage(startupMessage);
    } catch (InterruptedException e) {
      e.printStackTrace();
      BotUtils.CLIENT.logout();
      System.exit(1);
    }
  }
}
