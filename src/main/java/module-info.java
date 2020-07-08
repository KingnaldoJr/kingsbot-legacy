module kingsbot {
    requires com.fasterxml.jackson.databind;
    requires com.google.api.client;
    requires com.google.api.client.json.jackson2;
    requires com.google.api.services.youtube;
    requires com.google.gson;
    requires com.sedmelluq.lavaplayer;
    requires java.net.http;
    requires net.dv8tion.jda;
    requires org.apache.logging.log4j;

    opens dev.kingnaldo.kingsbot.music.spotify.objects;
}