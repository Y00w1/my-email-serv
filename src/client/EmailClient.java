package client;

import model.Email;
import model.User;

import javax.swing.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class EmailClient {
    public static void main(String[] args) {
        String id = JOptionPane.showInputDialog("Ingrese su ID:");
        String password = JOptionPane.showInputDialog("Ingrese su contraseña:");
        String name = JOptionPane.showInputDialog("Ingrese su nombre:");
        String email = JOptionPane.showInputDialog("Ingrese su correo electrónico:");

        User user = new User(id, password, name, email);

        EmailClient emailClient = new EmailClient(user);
        emailClient.startClient();
    }

    private User user;

    public EmailClient(User user) {
        this.user = user;
    }

    public void startClient() {
        try (Socket socket = new Socket("localhost", 8080);
             ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream ois = new ObjectInputStream(socket.getInputStream())) {

            //Save the user in the server
            oos.writeObject(user);
            String serverResponse = (String) ois.readObject();
            JOptionPane.showMessageDialog(null, serverResponse);

            int option;
            do {
                String[] options = {"Enviar Correo", "Mostrar Correos", "Salir"};
                option = JOptionPane.showOptionDialog(null, "Seleccione una opción", "Menú",
                        JOptionPane.DEFAULT_OPTION, JOptionPane.PLAIN_MESSAGE, null, options, options[0]);

                switch (option) {
                    case 0:
                        sendEmail(socket, oos);
                        String sentConfirmation = (String) ois.readObject();
                        JOptionPane.showMessageDialog(null, sentConfirmation);
                        break;
                    case 1:
                        // Solicita al servidor los correos asociados al cliente actual
                        List<Email> userEmails = requestUserEmails(ois, oos);
                        // Muestra los correos al usuario
                        showUserEmails(userEmails);
                        break;
                }
            } while (option != 2);

        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void sendEmail(Socket socket, ObjectOutputStream oos) throws IOException {

        String recipient = JOptionPane.showInputDialog("Ingrese el correo del destinatario:");
        String subject = JOptionPane.showInputDialog("Ingrese el asunto:");
        String body = JOptionPane.showInputDialog("Ingrese el cuerpo del correo:");

        Email email = new Email(user.getEmail(), recipient, subject, body);
        oos.writeObject(email);
    }

    private List<Email> requestUserEmails(ObjectInputStream ois, ObjectOutputStream oos) throws IOException, ClassNotFoundException {
        // Envía una solicitud al servidor para obtener los correos asociados al cliente actual
        oos.writeObject("REQUEST_EMAILS");
        // Recibe la lista de correos del servidor
        return (List<Email>) ois.readObject();
    }

    private void showUserEmails(List<Email> userEmails) {
        if (userEmails.isEmpty()) {
            JOptionPane.showMessageDialog(null, "No hay correos para mostrar.");
        } else {
            StringBuilder emailsText = new StringBuilder("Correos:\n");
            for (Email email : userEmails) {
                emailsText.append("De: ").append(email.getSender()).append("\n");
                emailsText.append("Para: ").append(email.getRecipient()).append("\n");
                emailsText.append("Asunto: ").append(email.getSubject()).append("\n");
                emailsText.append("Cuerpo: ").append(email.getBody()).append("\n\n");
            }
            JOptionPane.showMessageDialog(null, emailsText.toString());
        }
    }




}
