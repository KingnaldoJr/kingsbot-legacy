package dev.kingnaldo.kingsbot.music;

import net.dv8tion.jda.api.entities.Guild;

import java.util.concurrent.TimeUnit;

public class AutoLeaveVoiceServer extends Thread {
    private final Guild guild;
    private volatile boolean interrupted;
    private volatile LeaveReason reason;

    public AutoLeaveVoiceServer(Guild guild, LeaveReason reason) {
        this.guild = guild;
        this.interrupted = false;
        this.reason = reason;
    }

    @Override
    public void run() {
        try{
            Thread.sleep(TimeUnit.MINUTES.toMillis(5));
        }catch(InterruptedException ignored) {}
        if(!this.interrupted) {
            MusicPlayerHandler.removeGuild(guild);
        }
    }

    public void cancel() {
        this.interrupted = true;
        this.interrupt();
    }

    public LeaveReason getReason() {
        return reason;
    }
}
