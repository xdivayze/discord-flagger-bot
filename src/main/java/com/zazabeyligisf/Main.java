package com.zazabeyligisf;

import java.net.URISyntaxException;

public class Main {
    private static String[] savedArgs;
    public static String[] getArgs() {
        return savedArgs;
    }

    public static void main(String[] args) throws URISyntaxException {
        savedArgs = args;
        new MessageListener();
    }

}