package com.github.diszexuf.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {

    private static final int PORT = 9090;
    private static final String BANNED_WORDS_FILE = "banned_words.txt";

    public static void main(String[] args) {
        Set<String> bannedWords = loadBannedWords(Path.of(BANNED_WORDS_FILE));
        System.out.println("[Server] Launch on port " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Connection: " + clientSocket.getInetAddress());
                Thread handler = new Thread(new ClientHandler(clientSocket, bannedWords));
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Error: " + e.getMessage());
        }
    }

    static Set<String> loadBannedWords(Path path) {
        if (!Files.exists(path)) {
            System.out.println("[Server] banned_words.txt not found");
            return Collections.emptySet();
        }
        try (Stream<String> lines = Files.lines(path)) {
            return lines
                    .map(String::trim)
                    .filter(s -> !s.isEmpty()).collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            System.err.println("[Server] Error reading banned_words.txt");
            return Collections.emptySet();
        }
    }
}