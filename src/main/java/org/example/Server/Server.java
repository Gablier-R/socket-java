package org.example.Server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server {
    static List<ClienteHandler> clientes = new ArrayList<>();
    private static final Scanner serverInput = new Scanner(System.in);

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Servidor esperando conexões na porta " + 9999);

            Thread serverInputThread = new Thread(() -> {
                while (true) {
                    String mensagemDoServidor = serverInput.nextLine();
                    if (!mensagemDoServidor.isEmpty()) {
                        enviarMensagemDoServidor(mensagemDoServidor);
                    }
                }
            });
            serverInputThread.start();

            while (true) {
                Socket clienteSocket = serverSocket.accept();

                var time = LocalDateTime.now();
                System.out.println( time + " Cliente conectado: " + clienteSocket.getInetAddress());

                ClienteHandler clienteHandler = new ClienteHandler(clienteSocket);
                clientes.add(clienteHandler);
                Thread clienteThread = new Thread(clienteHandler);
                clienteThread.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(String mensagem, ClienteHandler remetente) {

        for (ClienteHandler cliente : clientes) {
            String remetenteInfo = (remetente != null) ? " Cliente " + remetente.getId() : "";
            cliente.enviarMensagem( remetenteInfo + ":" + mensagem);
        }
    }


    public static void enviarMensagemDoServidor(String mensagem) {
        broadcast("Server: " + mensagem, null);
    }
}

class ClienteHandler implements Runnable {
    private static int contador = 1;
    private final Socket clienteSocket;
    private BufferedReader input;
    private PrintWriter output;
    private final int id;

    public ClienteHandler(Socket socket) {
        this.clienteSocket = socket;
        this.id = contador++;
        try {
            input = new BufferedReader(new InputStreamReader(clienteSocket.getInputStream()));
            output = new PrintWriter(clienteSocket.getOutputStream(), true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return id;
    }

    public void enviarMensagem(String mensagem) {
        output.println(mensagem);
    }

    @Override
    public void run() {
        var time = LocalDateTime.now();
        try {
            String mensagem;
            while ((mensagem = input.readLine()) != null) {
                System.out.println( time + " Cliente " + id + ": " + mensagem);
                Server.broadcast(mensagem, this);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                clienteSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            System.out.println(time + " Cliente " + id + " desconectado");
            Server.clientes.remove(this);
        }
    }
}
