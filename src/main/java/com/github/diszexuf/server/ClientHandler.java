package com.github.diszexuf.server;

import com.github.diszexuf.server.db.DatabaseManager;
import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;
import noNamespace.RequestType;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Set;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final Set<String> bannedWords;

    public ClientHandler(Socket socket, Set<String> bannedWords) {
        this.socket = socket;
        this.bannedWords = bannedWords;
    }

    @Override
    public void run() {
        try (
                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
                PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true)
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
            System.err.println("[Server] Error: " + e.getMessage());
        } finally {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    private String processMessage(String xml) {
        try {
            MessageDocument doc = XmlProcessor.parse(xml);
            var message = doc.getMessage();

            RequestType request = message.getRequest();
            if (request == null) {
                System.err.println("[Server] Received message without <request>");
                return XmlProcessor.buildResponse(1, "invalid message format");
            }

            String user = request.getUser();
            String text = request.getText();
            String time = message.getHeader().getTime();

            LocalDateTime sentTime;
            try {
                sentTime = LocalDateTime.parse(time, XmlProcessor.FMT);
            } catch (Exception e) {
                sentTime = LocalDateTime.now();
            }

            boolean hasBanned = bannedWords.stream()
                    .anyMatch(w -> text.toLowerCase().contains(w.toLowerCase()));

            int code = hasBanned ? 1 : 0;
            String reason = hasBanned ? "used inappropriate language" : "success";

            DatabaseManager.getInstance().saveMessage(sentTime, user, text, code);

            System.out.printf("[Server] '%s': \"%s\" : %s%n",
                    user, text, code == 0 ? "accepted" : "rejected");

            return XmlProcessor.buildResponse(code, reason);

        } catch (Exception e) {
            System.err.println("[Server] Parsing error: " + e.getMessage());
            return XmlProcessor.buildResponse(1, "invalid message format");
        }
    }
}