package com.gypsyengineer.runtime.exec;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class Utils {

    public static String cowsay(String phrase) throws IOException {
        String saferPhrase = String.format("'%s'", phrase);
        String[] cmd = new String[] {
                "/bin/sh",
                "-c",
                "cowsay " + saferPhrase};
        Process p = Runtime.getRuntime().exec(cmd);

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(p.getInputStream()))) {

            String line;
            StringBuilder builder = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                builder.append(line).append("\n");
            }

            return builder.toString();
        }
    }
}
