package com.zazabeyligisf;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.requests.restaction.CommandListUpdateAction;
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
    static File whitelist;
    static File blacklist;

    static {

        try {
            File jarFile = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            file = new File(jarFile.getParentFile(), "cache.txt");
            whitelist = new File(jarFile.getParentFile(), "whitelist.txt");
            blacklist = new File(jarFile.getParentFile(), "flags.txt");
            JDA jda = JDABuilder.createDefault(Main.getArgs()[0])
                    .enableIntents(GatewayIntent.MESSAGE_CONTENT).addEventListeners(new MessageListener()).build();
            CommandListUpdateAction c = jda.updateCommands();
            c.addCommands(Commands.slash("listeyekle", "Listeye bir kelime ekle!").addOption(OptionType.STRING, "ekle", "Listeye eklenecek kelime!", true)
                    .addOption(OptionType.STRING, "tip", "liste rengi(beyaz/siyah)", true));
            c.queue();
        } catch (URISyntaxException e) {
            System.out.println(e.getMessage());
        }

    }

    MessageListener() throws URISyntaxException {
    }

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        switch (event.getOption("tip").getAsString().toLowerCase().charAt(0)) {
            case 'b' ->
                    ListManager.manipulateWhite(Objects.requireNonNull(event.getOption("ekle")).getAsString(), Type.WHITE);
            case 's' ->
                    ListManager.manipulateWhite(Objects.requireNonNull(event.getOption("ekle")).getAsString(), Type.BLACK);
        }
        event.reply("T").queue();
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        TextChannel channel = event.getChannel().asTextChannel();
        Message message = event.getMessage();
        String content = message.getContentRaw();
        LinkedList<String> contentList = new LinkedList<>(List.of(content.split("\\s+")));
        for (String s : contentList) {
            try {
                if (!isWhiteList(s))
                    try {
                        Optional<Map<String, Map<String, Integer>>> stringStringMap = check(s.toLowerCase());
                        if (stringStringMap.isPresent()) {
                            String repliable = stringStringMap.get() + " -> " + message.getAuthor().getName() + "\n";
                            FileWriter writer = new FileWriter(file, true);
                            writer.write(repliable);
                            writer.close();
                            message.delete().queue();
                            channel.sendMessage(repliable).queue();
                            break;
                        }
                    } catch (IOException e) {
                        System.out.println((e.getMessage()));
                    }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    private boolean isWhiteList(String s) throws IOException {
        BufferedReader readerWhite = new BufferedReader(new FileReader(whitelist));
        String line;
        while (true) {
            line = readerWhite.readLine();
            if (line == null) break;
            if (line.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;


    }

    private Optional<Map<String, Map<String, Integer>>> check(String s) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(blacklist));
        String line;
        while (true) {
            line = reader.readLine();
            if (line == null) break;
            Map<Boolean, Integer> distance;
            if (line.length() > s.length()) {
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