package de.uniba.rz.entities.extras;

import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;

import java.util.UUID;

/**
 * User: sini_ann
 * Date: 23/05/18 2:35 PM
 */
public class Request {
    private Ticket ticket;
    private UUID packetId;
    private int action;
    private int ticketId;
    private Status status;

    //getters and setters

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public UUID getPacketId() {
        return packetId;
    }

    public void setPacketId(UUID packetId) {
        this.packetId = packetId;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getTicketId() {
        return ticketId;
    }

    public void setTicketId(int ticketId) {
        this.ticketId = ticketId;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
