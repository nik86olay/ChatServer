package org.example;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    static ExecutorService executeIt = Executors.newFixedThreadPool(50);
    static final int PORT = 8080;
    public static LinkedList<Socket> serverList = new LinkedList<>();

    public static void main(String[] args) {

        // стартуем сервер
        try (ServerSocket server = new ServerSocket(PORT)) {
            System.out.println("Server socket created, command console reader for listen to server commands");

            // стартуем цикл при условии что серверный сокет не закрыт
            while (!server.isClosed()) {

                Socket client = server.accept();
                // добавляем socked в List
                serverList.add(client);

                // после получения запроса на подключение сервер создаёт сокет
                // для общения с клиентом и отправляет его в отдельный поток из пула
                executeIt.execute(() -> ClientHandler(client));
                System.out.print("Connection accepted.");
            }

            // закрытие пула нитей после завершения работы всех нитей
            executeIt.shutdown();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void ClientHandler(Socket client) {

        try (BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()))) {


            // диалог с клиентом в цикле, пока сокет не закрыт клиентом
            while (!client.isClosed()) {

                // считывание сообщения клиента
                String entry = in.readLine();

                // вывод в консоль сервера
                System.out.println("message on Server - " + entry);

                // проверяем условие работы с клиентом, при выполнении прерываем цикл
                if (entry.contains("/exit")) {
                    break;
                }

                for (Socket i : serverList) {
                    if (!i.equals(client)) {
                        PrintWriter outAll = new PrintWriter(i.getOutputStream(), true);
                        outAll.println(entry);
                        System.out.println("отправили всем");
                    }
                }
            }

            System.out.println("Client disconnected");

            // закрытие сокета клиента
            client.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}