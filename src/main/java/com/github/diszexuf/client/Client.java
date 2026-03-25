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

            System.out.println("[Client] Connected to " + HOST + ":" + PORT);
            System.out.println("Commands: -m <text>  |  -h (exit)");

            Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
            while (scanner.hasNextLine()) {
                String input = scanner.nextLine().trim();

                if (input.equals("-h")) {
                    System.out.println("[Client] Exit");
                    break;
                }

                if (input.startsWith("-m ")) {
                    String text = input.substring(3).trim();
                    if (text.isEmpty()) {
                        System.out.println("[Client] Text cannot be empty");
                        continue;
                    }
                    out.println(XmlProcessor.buildRequest(username, text));

                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = in.readLine()) != null) {
                        sb.append(line).append("\n");
                        if (line.contains("</message>")) break;
                    }
                    printStatus(sb.toString());

                } else {
                    System.out.println("[Client] Unknown command");
                }
            }
        } catch (IOException e) {
            System.err.println("[Client] Failed to connect: " + e.getMessage());
        }
    }

    private static void printStatus(String xml) {
        try {
            MessageDocument doc = XmlProcessor.parse(xml);
            var status = doc.getMessage().getResponse().getStatus();
            if (status.getCode().intValue() == 0) {
                System.out.println("[Client] Message received");
            } else {
                System.out.println("[Client] Message rejected: " + status.getReason());
            }
        } catch (Exception e) {
            System.out.println("[Client] Unable to parse the response");
        }
    }
}