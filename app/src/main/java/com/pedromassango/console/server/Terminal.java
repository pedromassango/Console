package com.pedromassango.console.server;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;
import java.util.Scanner;
/**
 * Created by Pedro Massango on 03-04-2017.
 *
 * Terminal-Server
 * Classe servidor que responde a comandos do Terminal, deste aplicativo.
 *
 * OBS: este codigo deve estar em execucao, para o bom funcionamento  do app, e permitir
 * a recepcao de comandos enviados pelo app.
 */

public class Terminal {

    private static final int DEFAULT_CONNECTION_PORT = 5123;
    private static final String GET_PROPERTY_COMMAND = ".getProperty/";
    private static final String EXEC_COMMAND = ".exec/";
    private static final String CD_COMMAND = ".cd/";
    private static final String LS_COMMAND = ".ls";
    private static final String CD_DEFAULT_COMMAND = ".cd/root";

    public static void main(String[] args) {

        File root = new File(System.getProperty("user.home"));
        File cFile = root;

        ServerSocket server;
        Scanner leitor;
        PrintWriter escritor;

        try {
            server = new ServerSocket(DEFAULT_CONNECTION_PORT);

            while (true) {
                Socket s = server.accept();
                leitor = new Scanner(s.getInputStream());
                escritor = new PrintWriter(s.getOutputStream());

                if (!leitor.hasNextLine()) {
                    return;
                }

                // String command = leitor.nextLine();
                String command = leitor.next();

                if (command.startsWith(EXEC_COMMAND)) {
                    command = command.replace(EXEC_COMMAND, "");

                    // throw an error if the program does not exist
                    try {
                        Process p = Runtime.getRuntime().exec(command);
                        if (p.isAlive()) {
                            //if (p.exitValue() == 0) {
                            sendMessage(escritor, "Executando " + command + " com sucesso");
                        }
                    }catch (IOException e){
                        sendMessage(escritor, "Programa não encontrado");
                    }

                    return;
                }


                if (command.startsWith(GET_PROPERTY_COMMAND)) {
                    command = command.replace(GET_PROPERTY_COMMAND, "");
                    try {
                        sendMessage(escritor, System.getProperties().getProperty(command, "RED propiedade desconhecida"));
                    }catch (Exception e){
                        sendMessage(escritor, "RED not found");
                    }
                    return;
                }

                if (command.startsWith(CD_COMMAND)) {
                    command = command.replace(CD_COMMAND, "");

                    if (command.equals(CD_DEFAULT_COMMAND)) {
                        cFile = root;
                        sendFileInfo(escritor, cFile);
                        sendContainingFiles(escritor, cFile);
                        return;
                    }

                    for (File f : Objects.requireNonNull(cFile.listFiles())) {
                        if (f.isDirectory() && f.getName().toLowerCase().equals(command.toLowerCase())) {

                            cFile = new File(f.getPath());
                            sendFileInfo(escritor, cFile);
                            return;
                        }
                    }
                }

                if (command.equals(LS_COMMAND)) {
                    sendContainingFiles(escritor, cFile);
                    return;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
            server = null;
            escritor = null;
            leitor = null;
            main(null); // reconnect if error
        }
    }

    private static void sendContainingFiles(PrintWriter escritor, File cFile) {
        //sendMessage(escritor, "RED " + cFile.getName());
        for (int i = 0, size = Objects.requireNonNull(cFile.listFiles()).length; i < size; i++) {

            File f = Objects.requireNonNull(cFile.listFiles())[i];

            String fileName = f.isDirectory() ? "../" + f.getName() : f.getName();
            sendMessage(escritor, "GREEN " + fileName);
        }
    }

    private static void sendFileInfo(PrintWriter escritor, File file) {

        sendMessage(escritor, "Localização actual:");
        sendMessage(escritor, "RED " + file.getName());
        sendMessage(escritor, "");
    }

    private static void sendMessage(PrintWriter escritor, String message) {
        try {
            escritor.println(message);
            escritor.flush();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
