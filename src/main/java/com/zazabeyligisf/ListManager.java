package com.zazabeyligisf;

import java.io.*;

enum Type {BLACK, WHITE}

public class ListManager {
    static void manipulateWhite(String s, Type type) {
        File f;
        switch (type) {
            case WHITE -> f = MessageListener.whitelist;
            case BLACK -> f = MessageListener.blacklist;
            default -> {
                f = null;
                throw new NullPointerException("file not found");
            }
        }

        try (FileWriter fw = new FileWriter(f, true);
                BufferedWriter bufferedWriter = new BufferedWriter(fw)) {
            fw.write(s + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
