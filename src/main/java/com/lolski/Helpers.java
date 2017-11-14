package com.lolski;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Properties;

class Helpers {
    public static void p(String print) {
        System.out.println(print);
    }

    public static void printClasspath() {
        ClassLoader cl = ClassLoader.getSystemClassLoader();

        URL[] urls = ((URLClassLoader)cl).getURLs();

        for(URL url: urls){
            System.out.println(url.getFile());
        }
    }

    public static Properties loadConfigProperties(String configPath) {
        try {
            FileInputStream inputStream = new FileInputStream(configPath);
            Properties config = new Properties();
            config.load(inputStream);
            return config;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
