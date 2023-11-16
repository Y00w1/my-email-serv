package model;

import java.io.Serializable;

public class Email implements Serializable {
    private final String sender;
    private String recipient;
    private String subject;
    private String body;

    public void sendEmail(User sender, User receiver, Email email){

    }

    public void receiveEmail(User receiver, Email email){

    }

    public void accessEmail(User user, Email email){

    }

    public void printEmails(User user){

    }

    //Constructor
    public Email(String sender, String recipient, String subject, String body) {
        this.sender = sender;
        this.recipient = recipient;
        this.subject = subject;
        this.body = body;
    }

    //Getters
    public String getSender() {
        return sender;
    }

    public String getRecipient() {
        return recipient;
    }

    public String getSubject() {
        return subject;
    }

    public String getBody() {
        return body;
    }

}
