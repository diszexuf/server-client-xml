package com.github.diszexuf.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

public class MessageServer {
    private static final int PORT = 8080;
    private final Set<String> bannedWords = Collections.synchronizedSet(new HashSet<>());
    private final ExecutorService threadPool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        new MessageServer().start();
    }

    public void start() {
        loadBannedWords("banned_words.txt");
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {

        } catch (IOException e) {

        } finally {
            threadPool.shutdown();
        }
    }

    private void loadBannedWords(String fileName) {
        try (Stream<String> lines = Files.lines(Paths.get(fileName))) {
            lines
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .forEach(word -> bannedWords.add(word.toLowerCase()));
            System.out.println();
        } catch (IOException e) {
            System.err.println("[Server] Ошибка загрузки " + fileName);
        }
    }

}
