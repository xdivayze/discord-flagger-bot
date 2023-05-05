package com.zazabeyligisf;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import org.apache.commons.text.similarity.LevenshteinDistance;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;

public class FlagShortener {
    static final double THRESHOLD = 1.5;

    static Map<Boolean, Integer> countDistance(String s1, String s2) {
        LevenshteinDistance levenshteinDistance = new LevenshteinDistance();
        int dist = levenshteinDistance.apply(s1, s2);
        return Map.of(dist <= THRESHOLD, dist);
    }
}

class MessageListener extends ListenerAdapter {

    static File file;

    static {
        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(jarFile.getParentFile(), "cache.txt");
            JDA jda = JDABuilder.createDefault("")
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT).build();
            jda.addEventListener(new MessageListener());
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }

    }

    MessageListener() throws URISyntaxException {
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        LinkedList<String> contentList = new LinkedList<>(List.of(content.split("\\s+")));
        for (String s : contentList) {
            try {
                Optional<Map<String, Map<String, Integer>>> stringStringMap = check(s.toLowerCase());
                if (stringStringMap.isPresent()) {
                    String repliable = stringStringMap.get() + " -> " + message.getAuthor().getName() + "\n";
                    FileWriter writer = new FileWriter(file, true);
                    writer.write(repliable);
                    writer.close();
                    message.delete().queue();

                    channel.sendMessage(repliable).queue();
                }
            } catch (IOException e) {
                System.out.println((e.getMessage()));
            }
        }
    }

    private Optional<Map<String, Map<String, Integer>>> check(String s) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(Objects.requireNonNull(getClass().getResourceAsStream("/flags.txt"))));
        String line;
        while (true) {
            line = reader.readLine();
            if(line == null) break;
            Map<Boolean, Integer> distance;
            if(line.length() > s.length()) {
                distance = FlagShortener.countDistance(line, s);
            } else
                distance = FlagShortener.countDistance(s, line);
            boolean test123 = distance.keySet().stream().findFirst().get();
            if (test123) {
                return Optional.of(Map.of(s, Map.of(line, distance.get(true))));
            }
        }
        return Optional.empty();
    }
}