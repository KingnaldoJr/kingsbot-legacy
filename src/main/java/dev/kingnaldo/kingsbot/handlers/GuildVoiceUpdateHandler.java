package dev.kingnaldo.kingsbot.handlers;

import dev.kingnaldo.kingsbot.KingsBot;
import dev.kingnaldo.kingsbot.music.LeaveReason;
import dev.kingnaldo.kingsbot.music.MusicPlayerHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceJoinEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceLeaveEvent;
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceMoveEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

public class GuildVoiceUpdateHandler extends ListenerAdapter {

    @Override
    public void onGuildVoiceJoin(@NotNull GuildVoiceJoinEvent event) {
        if(isSelfEvent(event.getEntity(), event.getChannelJoined(), event.getGuild())) return;
        if(event.getEntity().getUser().isBot()) return;

        if(MusicPlayerHandler.connectedToVoiceChannel(event.getChannelJoined())) {
            MusicPlayerHandler.getInstance(event.getGuild(), event.getGuild().getDefaultChannel())
                    .cancelAutoLeave(LeaveReason.EMPTY_CHANNEL);
        }
    }

    @Override
    public void onGuildVoiceMove(@NotNull GuildVoiceMoveEvent event) {
        if(isSelfEvent(event.getEntity(), event.getChannelJoined(), event.getGuild())) return;
        if(event.getEntity().getUser().isBot()) return;

        if(MusicPlayerHandler.connectedToVoiceChannel(event.getChannelLeft())) {
            if(event.getChannelLeft().getMembers().stream().allMatch(member -> member.getUser().isBot())) {
                MusicPlayerHandler.getInstance(event.getGuild(), event.getGuild().getDefaultChannel())
                        .startAutoLeave(LeaveReason.EMPTY_CHANNEL);
            }
        }

        if(MusicPlayerHandler.connectedToVoiceChannel(event.getChannelJoined())) {
            MusicPlayerHandler.getInstance(event.getGuild(), event.getGuild().getDefaultChannel())
                    .cancelAutoLeave(LeaveReason.EMPTY_CHANNEL);
        }
    }

    @Override
    public void onGuildVoiceLeave(@NotNull GuildVoiceLeaveEvent event) {
        if(event.getEntity().getId().equals(KingsBot.getBOT().getSelfUser().getId()))
            MusicPlayerHandler.removeGuild(event.getGuild());

        if(MusicPlayerHandler.connectedToVoiceChannel(event.getChannelLeft())) {
            if(event.getChannelLeft().getMembers().stream().allMatch(member -> member.getUser().isBot())) {
                MusicPlayerHandler.getInstance(event.getGuild(), event.getGuild().getDefaultChannel())
                        .startAutoLeave(LeaveReason.EMPTY_CHANNEL);
            }
        }
    }

    private boolean isSelfEvent(Member entity, VoiceChannel channel, Guild guild) {
        if(entity.getId().equals(KingsBot.getBOT().getSelfUser().getId())) {
            if(channel.getMembers().stream().allMatch(member -> member.getUser().isBot()))
                MusicPlayerHandler.getInstance(guild, guild.getDefaultChannel())
                        .startAutoLeave(LeaveReason.EMPTY_CHANNEL);
            else MusicPlayerHandler.getInstance(guild, guild.getDefaultChannel())
                    .cancelAutoLeave(LeaveReason.EMPTY_CHANNEL);
            return true;
        }
        return false;
    }
}
