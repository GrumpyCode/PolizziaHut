package me.nickolasgupton.polizziahut;

import me.nickolasgupton.polizziahut.commands.GeneralCommands;
import me.nickolasgupton.polizziahut.commands.MusicCommands;

import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;

import java.util.*;

public class CommandHandler {

    // static maps of commands mapping from command string to the functional impl
    private static Map<String, Command> generalCommands = GeneralCommands.getGeneralCommands();
    private static Map<String, Command> musicCommands = MusicCommands.getMusicCommands();


    @EventSubscriber
    public void onMessageReceived(MessageReceivedEvent event) {

        // Given a message "/test arg1 arg2", argArray will contain ["/test", "arg1", "arg"]
        String[] argArray = event.getMessage().getContent().split(" ");

        // First ensure at least the command and prefix is present, the arg length can be handled by your command func
        if (argArray.length == 0)
            return;

        // Check if the first arg (the command) starts with the prefix defined in the utils class
        if (argArray[0].startsWith(BotUtils.DEFAULT_PREFIX)){

            // Extract the "command" part of the first arg out by ditching the amount of characters present in the prefix
            String commandStr = argArray[0].substring(BotUtils.DEFAULT_PREFIX.length());

            // Load the rest of the args in the array into a List for safer access
            List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
            argsList.remove(0); // Remove the command

            // Instead of delegating the work to a switch, automatically do it via calling the mapping if it exists
            if (generalCommands.containsKey(commandStr)) {
                generalCommands.get(commandStr).runCommand(event, argsList);
            }
        }

        if (argArray[0].startsWith(BotUtils.MUSIC_PREFIX)){

            // Extract the "command" part of the first arg out by ditching the amount of characters present in the prefix
            String commandStr = argArray[0].substring(BotUtils.MUSIC_PREFIX.length());

            // Load the rest of the args in the array into a List for safer access
            List<String> argsList = new ArrayList<>(Arrays.asList(argArray));
            argsList.remove(0); // Remove the command

            // Instead of delegating the work to a switch, automatically do it via calling the mapping if it exists
            if (musicCommands.containsKey(commandStr)) {
                musicCommands.get(commandStr).runCommand(event, argsList);
            }
        }

    }

}