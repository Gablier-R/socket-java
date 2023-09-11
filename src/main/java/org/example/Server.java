package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Server {
    static List<ClienteHandler> clientes = new ArrayList<>();

    public static void main(String[] args) {

        try (ServerSocket serverSocket = new ServerSocket(9999)) {
            System.out.println("Servidor esperando conexões na porta " + 9999);

            while (true) {
                Socket clienteSocket = serverSocket.accept();
                System.out.println("Cliente conectado: " + clienteSocket.getInetAddress());

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
        var  time = LocalDateTime.now();
        for (ClienteHandler cliente : clientes) {
            if (cliente != remetente) {
                cliente.enviarMensagem(time + " Cliente " + remetente.getId() + ": " + mensagem);
            }
        }
    }
}

class ClienteHandler implements Runnable { //Alterar para extends, para manter padrão SOLID
    private static int contador = 1;
    private Socket clienteSocket;
    private BufferedReader input;
    private PrintWriter output;
    private int id;

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
        try {
            String mensagem;
            while ((mensagem = input.readLine()) != null) {
                System.out.println("Cliente " + id + ": " + mensagem);
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
            System.out.println("Cliente " + id + " desconectado");
            Server.clientes.remove(this);
        }
    }
}
