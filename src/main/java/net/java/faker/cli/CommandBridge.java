package net.java.faker.cli;

import net.java.faker.util.logging.Logger;

import javax.swing.*;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Lightweight IPC channel used to forward command line requests to an existing
 * application instance.
 */
public final class CommandBridge {

    private static final int PORT = 54173;
    private static final AtomicBoolean RUNNING = new AtomicBoolean();
    private static ServerSocket serverSocket;
    private static ExecutorService executorService;

    private CommandBridge() {
    }

    public static boolean sendOpenRequest() {
        return sendCommand("open");
    }

    public static boolean sendCommand(String command) {
        try (Socket socket = new Socket(InetAddress.getLoopbackAddress(), PORT);
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8), true);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
            writer.println(command);
            writer.flush();
            String response = reader.readLine();
            return response != null && response.toLowerCase(Locale.ROOT).contains("ok");
        } catch (IOException e) {
            return false;
        }
    }

    public static void start(Runnable openAction) {
        if (!RUNNING.compareAndSet(false, true)) {
            return;
        }
        executorService = Executors.newSingleThreadExecutor(r -> {
            Thread thread = new Thread(r, "phantom-command-bridge");
            thread.setDaemon(true);
            return thread;
        });
        executorService.submit(() -> {
            try (ServerSocket server = new ServerSocket(PORT, 1, InetAddress.getLoopbackAddress())) {
                serverSocket = server;
                while (!server.isClosed()) {
                    try {
                        Socket socket = server.accept();
                        handleClient(socket, openAction);
                    } catch (IOException e) {
                        if (!server.isClosed()) {
                            Logger.warn("Command bridge error: " + e.getMessage());
                        }
                    }
                }
            } catch (IOException e) {
                Logger.warn("Unable to start command bridge: " + e.getMessage());
            } finally {
                RUNNING.set(false);
            }
        });
    }

    private static void handleClient(Socket socket, Runnable openAction) {
        try (Socket client = socket;
             BufferedReader reader = new BufferedReader(new InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8));
             PrintWriter writer = new PrintWriter(new OutputStreamWriter(client.getOutputStream(), StandardCharsets.UTF_8), true)) {
            String command = reader.readLine();
            if (command != null) {
                switch (command.trim().toLowerCase(Locale.ROOT)) {
                    case "open" -> {
                        SwingUtilities.invokeLater(openAction);
                        writer.println("ok");
                    }
                    default -> writer.println("unknown");
                }
            }
        } catch (IOException e) {
            Logger.warn("Command bridge client error: " + e.getMessage());
        }
    }

    public static void stop() {
        RUNNING.set(false);
        if (executorService != null) {
            executorService.shutdownNow();
        }
        if (serverSocket != null && !serverSocket.isClosed()) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                Logger.warn("Failed to close command bridge: " + e.getMessage());
            }
        }
    }
}
