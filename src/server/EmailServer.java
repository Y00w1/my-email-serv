package server;

import model.Email;
import model.User;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EmailServer {
    public static void main(String[] args) {
        EmailServer emailServer = new EmailServer();
        emailServer.startServer();
    }

    private Set<ClientHandler> clients = new HashSet<>();
    private List<Email> emails = new ArrayList<>();

    public void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(8080)) {
            System.out.println("Servidor iniciado en el puerto 8080 esperando clientes...");

            while (true) {
                Socket clientSocket = serverSocket.accept();
                handleClientConnection(clientSocket);
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void handleClientConnection(Socket clientSocket) {
        try (ObjectInputStream ois = new ObjectInputStream(clientSocket.getInputStream());
             ObjectOutputStream oos = new ObjectOutputStream(clientSocket.getOutputStream())) {

            // Confirm the connection to the client
            oos.writeObject("Conexión exitosa con el servidor.");

            User user = (User) ois.readObject();

            ClientHandler clientHandler = new ClientHandler(clientSocket, ois, oos, user);
            clients.add(clientHandler);

            // Muestra la lista de clientes conectados
            System.out.println("Clientes conectados: ");
            for (ClientHandler handler : clients) {
                System.out.println("- " + handler.getUser().getEmail());
            }

            // Lógica para manejar solicitudes continuamente
            while (true) {
                try {
                    handleEmail(clientHandler);
                } catch (EOFException e) {
                    // El cliente se desconectó
                    System.out.println("Cliente desconectado: " + clientHandler.getUser().getEmail());
                    break;
                }

            }

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void handleEmail(ClientHandler clientHandler) throws IOException, ClassNotFoundException {
        Object receivedObject = clientHandler.getOis().readObject();

        if (receivedObject instanceof String) {
            String request = (String) receivedObject;

            if ("REQUEST_EMAILS".equals(request)) {
                // El cliente ha solicitado sus correos, envía la lista al cliente
                List<Email> userSpecificEmails = getUserSpecificEmails(clientHandler.getUser());
                clientHandler.getOos().writeObject(userSpecificEmails);
            }
        } else if (receivedObject instanceof Email) {
            Email email = (Email) receivedObject;
            String recipient = email.getRecipient();

            // Validación del destinatario
            boolean recipientExists = clients.stream()
                    .map(ClientHandler::getUser)
                    .anyMatch(user -> user.getEmail().equals(recipient));

            if (recipientExists) {
                // Destinatario válido, enviar respuesta al cliente
                clientHandler.getOos().writeObject("Correo enviado con éxito.");
                // Además, puedes almacenar el correo en la lista de correos del servidor
                emails.add(email);
            } else {
                // Destinatario no válido, enviar respuesta al cliente
                clientHandler.getOos().writeObject("El destinatario no existe. Correo no enviado.");
            }
        }
    }

    private List<Email> getUserSpecificEmails(User user) {
        // Filtra la lista de correos para incluir solo los que tienen al usuario como destinatario o emisor
        return emails.stream()
                .filter(email -> email.getRecipient().equals(user.getEmail()) || email.getSender().equals(user.getEmail()))
                .collect(Collectors.toList());
    }

    private static class ClientHandler {
        private Socket socket;
        private ObjectInputStream ois;
        private ObjectOutputStream oos;
        private User user;

        public ClientHandler(Socket socket, ObjectInputStream ois, ObjectOutputStream oos, User user) {
            this.socket = socket;
            this.ois = ois;
            this.oos = oos;
            this.user = user;
        }

        public User getUser() {
            return user;
        }

        public ObjectInputStream getOis() {
            return ois;
        }

        public ObjectOutputStream getOos() {
            return oos;
        }
    }
}
