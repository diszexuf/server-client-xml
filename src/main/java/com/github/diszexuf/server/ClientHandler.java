package com.github.diszexuf.server;

import com.github.diszexuf.server.db.DatabaseManager;
import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;

import java.io.*;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public class ClientHandler implements Runnable {

    private static final DateTimeFormatter FMT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final Socket socket;
    private final Set<String> bannedWords;

    public ClientHandler(Socket socket, Set<String> bannedWords) {
        this.socket = socket;
        this.bannedWords = bannedWords;
    }

    @Override
    public void run() {
        try (
                BufferedReader in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                PrintWriter    out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true)
        ) {
            StringBuilder xmlBuffer = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                xmlBuffer.append(line).append("\n");
                if (line.contains("</message>")) {
                    String response = processMessage(xmlBuffer.toString());
                    out.println(response);
                    xmlBuffer.setLength(0);
                }
            }
        } catch (Exception e) {
            System.err.println("[Server] Ошибка: " + e.getMessage());
        } finally {
            try { socket.close(); } catch (IOException ignored) {}
        }
    }

    private String processMessage(String xml) {
        try {
            MessageDocument doc = XmlProcessor.parse(xml);
            var message = doc.getMessage();
            var request = message.getRequest();

            String user = request.getUser();
            String text = request.getText();
            String time = message.getHeader().getTime();

            LocalDateTime sentTime;
            try {
                sentTime = LocalDateTime.parse(time, FMT);
            } catch (Exception e) {
                sentTime = LocalDateTime.now();
            }

            boolean hasBanned = bannedWords.stream()
                    .anyMatch(w -> text.toLowerCase().contains(w.toLowerCase()));

            int code      = hasBanned ? 1 : 0;
            String reason = hasBanned ? "used inappropriate language" : "success";

            DatabaseManager.getInstance().saveMessage(sentTime, user, text, code);

            System.out.printf("[Server] '%s': \"%s\" → %s%n",
                    user, text, code == 0 ? "принято" : "отклонено");

            return XmlProcessor.buildResponse(code, reason);

        } catch (Exception e) {
            System.err.println("[Server] Ошибка парсинга: " + e.getMessage());
            return XmlProcessor.buildResponse(1, "invalid message format");
        }
    }
}