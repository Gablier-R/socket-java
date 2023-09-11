package org.example;

import java.io.*;
import java.net.Socket;

public class Client {
    public static void main(String[] args) {
        try (Socket socket = new Socket("127.0.0.1",9999);

             BufferedReader input = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter output = new PrintWriter(socket.getOutputStream(), true);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in))) {

            System.out.println("Bem-vindo ao chat do servidor!");

            Thread receberMensagens = new Thread(() -> {
                try {
                    String mensagem;
                    while ((mensagem = input.readLine()) != null) {
                        System.out.println(mensagem);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            receberMensagens.start();

            while (true) {
                String mensagem = userInput.readLine();

                if (mensagem.equalsIgnoreCase("exit")) {
                    break;
                }

                output.println(mensagem);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
