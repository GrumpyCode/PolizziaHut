package com.grumpycode.polizziahut;

import com.grumpycode.polizziahut.lavaplayer.GuildMusicManager;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.EmbedBuilder;
import sx.blah.discord.util.RequestBuffer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MusicHelper {

    private static final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private static final Map<Long, GuildMusicManager> playerInstances = new HashMap<>();

    public static synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
        long guildId = guild.getLongID();
        GuildMusicManager musicManager = playerInstances.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            playerInstances.put(guildId, musicManager);
        }

        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    public static void loadAndPlay(final IChannel channel, final String trackUrl, final String authorName) {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        EmbedBuilder builder = new EmbedBuilder();
        builder.withColor(255, 0, 0);
        builder.withFooterText(authorName);

        playerManager.loadItemOrdered(musicManager, trackUrl, new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                builder.withTitle("Adding to queue:");
                builder.withDescription("[" + track.getInfo().title + "](" + trackUrl + ")" + " by " + track.getInfo().author);

                RequestBuffer.request(() -> channel.sendMessage(builder.build()));
                play(musicManager, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();

                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                play(musicManager, firstTrack);

                // the queue for the playlist will start at the linked video
                boolean start = false;
                for(int i = 0; i < playlist.getTracks().size(); i++){
                    while(!start){
                        if(playlist.getTracks().get(i) == firstTrack){
                            start = true;
                            i++;
                        }else{
                            i++;
                        }
                    }

                    play(musicManager, playlist.getTracks().get(i));
                }

                String str = getQueue(getGuildAudioPlayer(channel.getGuild()).getScheduler().getQueue());

                builder.withTitle("Adding playlist to queue:");
                builder.withDescription(playlist.getName() + "\n\n" +
                        "**First track:** " + "[" + firstTrack.getInfo().title + "](" + firstTrack.getInfo().uri + ")\n\n" +
                        "**Up Next:**\n" + str);

                RequestBuffer.request(() -> channel.sendMessage(builder.build()));

            }

            @Override
            public void noMatches() {
                builder.withTitle("Error queueing track:");
                builder.withDescription("Nothing found at URL: " + trackUrl);

                RequestBuffer.request(() -> channel.sendMessage(builder.build()));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                builder.withTitle("Error queueing track:");
                builder.withDescription("Could not play track: " + exception.getMessage());

                RequestBuffer.request(() -> channel.sendMessage(builder.build()));
            }
        });
    }

    private static void play(GuildMusicManager musicManager, AudioTrack track) {
        musicManager.getScheduler().queue(track);
    }

    public static String getQueue(List<AudioTrack> queue){
        String str = "";

        for(int i = 0; i < queue.size(); i++){
            str += (i+1) + ". " + "[" + queue.get(i).getInfo().title + "](" + queue.get(i).getInfo().uri + ")" + " by " + queue.get(i).getInfo().author + "\n";
        }

        if(str.equals("")){
            str = "Nothing currently queued.";
        }

        return str;
    }
}