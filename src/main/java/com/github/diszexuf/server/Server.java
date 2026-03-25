package com.github.diszexuf.server;

import java.io.*;
import java.net.*;
import java.nio.file.*;
import java.util.Set;
import java.util.stream.Collectors;

public class Server {

    private static final int PORT = 9090;
    private static final String BANNED_WORDS_FILE = "banned_words.txt";

    public static void main(String[] args) {
        Set<String> bannedWords = loadBannedWords();
        System.out.println("[Server] Запуск на порту " + PORT);

        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("[Server] Подключение: " + clientSocket.getInetAddress());
                Thread handler = new Thread(new ClientHandler(clientSocket, bannedWords));
                handler.setDaemon(true);
                handler.start();
            }
        } catch (IOException e) {
            System.err.println("[Server] Ошибка: " + e.getMessage());
        }
    }

    private static Set<String> loadBannedWords() {
        Path path = Path.of(BANNED_WORDS_FILE);
        if (!Files.exists(path)) {
            System.out.println("[Server] banned_words.txt не найден.");
            return Set.of();
        }
        try {
            return Files.lines(path)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toSet());
        } catch (IOException e) {
            System.err.println("[Server] Ошибка чтения banned_words.txt");
            return Set.of();
        }
    }
}