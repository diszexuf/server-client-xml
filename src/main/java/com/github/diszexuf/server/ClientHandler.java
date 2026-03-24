package com.github.diszexuf.server;

import com.github.diszexuf.server.db.DatabaseManager;
import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;
import noNamespace.MessageType;
import org.apache.xmlbeans.XmlException;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
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
                socket;
                InputStream is = socket.getInputStream();
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true)
        ) {
            MessageDocument doc = XmlProcessor.parse(is);
            MessageType message = doc.getMessage();

            if (message.isSetRequest()) {
                String user = message.getRequest().getUser();
                String text = message.getRequest().getText();
                LocalDateTime now = LocalDateTime.now();

                boolean isBanned = checkBannedWords(text);
                int code = isBanned ? 1 : 0;
                String reason = isBanned ? "used inappropriate language" : "success";

                DatabaseManager.getInstance().saveMessage(now, user, text, code);

                String xmlResponse = XmlProcessor.buildResponse(code, reason);
                out.println(xmlResponse);

                System.out.printf("[Server] Обработано сообщение от '%s'. Результат: %d (%s)%n", user, code, reason);
            }
        } catch (XmlException | IOException | SQLException e) {
            System.err.println("[Server] Ошибка при обработке клиента: " + e.getMessage());
        }
    }

    private boolean checkBannedWords(String text) {
        if (text == null || text.isEmpty()) return false;

        String lowerText = text.toLowerCase();
        return bannedWords.stream().anyMatch(lowerText::contains);
    }
}
