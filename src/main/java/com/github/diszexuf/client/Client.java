package com.github.diszexuf.client;

import com.github.diszexuf.xml.XmlProcessor;
import noNamespace.MessageDocument;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class Client {

    private static final String HOST = "localhost";
    private static final int PORT = 9090;
    private static final int SOCKET_TIMEOUT = 5000;

    public static void main(String[] args) {
        String username = System.getProperty("user.name");

        try (Socket socket = new Socket(HOST, PORT)) {
            socket.setSoTimeout(SOCKET_TIMEOUT);
            PrintWriter out = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));

            System.out.println("[Client] Подключен к " + HOST + ":" + PORT);
            System.out.println("Команды: -m <текст>  |  -h (выход)");

            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();

                if (input.equals("-h")) {
                    System.out.println("[Client] Выход");
                    break;
                }

                if (input.startsWith("-m ")) {
                    String text = input.substring(3).trim();
                    if (text.isEmpty()) {
                        System.out.println("[Client] Текст не может быть пустым");
                        continue;
                    }
                    System.out.println(XmlProcessor.buildRequest(username, text));
                    out.println(XmlProcessor.buildRequest(username, text));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line).append("\n");
                        if (line.contains("</message>")) break;
                    }
                    printStatus(sb.toString());

                } else {
                    System.out.println("[Client] Неизвестная команда");
                }
            }
        } catch (IOException e) {
            System.err.println("[Client] Не удалось подключиться: " + e.getMessage());
        }
    }

    private static void printStatus(String xml) {
        try {
            MessageDocument doc = XmlProcessor.parse(xml);
            var status = doc.getMessage().getResponse().getStatus();
            if (status.getCode().intValue() == 0) {
                System.out.println("[Client] Сообщение принято");
            } else {
                System.out.println("[Client] Сообщение отклонено: " + status.getReason());
            }
        } catch (Exception e) {
            System.out.println("[Client] Не удалось разобрать ответ");
        }
    }
}