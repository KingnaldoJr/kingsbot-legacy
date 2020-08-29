package dev.kingnaldo.kingsbot.music;

import net.dv8tion.jda.api.entities.Guild;

public class AutoLeaveVoiceServer extends Thread {

    private final Guild guild;
    private volatile boolean interrupted;

    public AutoLeaveVoiceServer(Guild guild) {
        this.guild = guild;
        this.interrupted = false;
    }

    @Override
    public void run() {
        try{
            Thread.sleep(300000);
        }catch(InterruptedException ignored) {}
        if(!this.interrupted) {
            MusicPlayerHandler.removeGuild(guild);
        }
    }

    public void cancel() {
        this.interrupted = true;
        this.interrupt();
    }
}
