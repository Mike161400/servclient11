package com.rentalservice.antivirus.auth.dto;

public class TicketResponse {

    private Ticket ticket;
    private String signature;

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }
}