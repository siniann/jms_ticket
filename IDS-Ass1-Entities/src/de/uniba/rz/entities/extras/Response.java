package de.uniba.rz.entities.extras;

import de.uniba.rz.entities.Ticket;

import java.util.List;
import java.util.UUID;

/**
 * User: sini_ann
 * Date: 23/05/18 2:35 PM
 */
public class Response {
    private UUID serverId;
    private Ticket ticket;
    private List<Ticket> tickets;
    private int action;
    private int totalPckCount;
    private int packetNo;

    //getters and setters

    public UUID getServerId() {
        return serverId;
    }

    public void setServerId(UUID serverId) {
        this.serverId = serverId;
    }

    public Ticket getTicket() {
        return ticket;
    }

    public void setTicket(Ticket ticket) {
        this.ticket = ticket;
    }

    public List<Ticket> getTickets() {
        return tickets;
    }

    public void setTickets(List<Ticket> tickets) {
        this.tickets = tickets;
    }

    public int getAction() {
        return action;
    }

    public void setAction(int action) {
        this.action = action;
    }

    public int getTotalPckCount() {
        return totalPckCount;
    }

    public void setTotalPckCount(int totalPckCount) {
        this.totalPckCount = totalPckCount;
    }

    public int getPacketNo() {
        return packetNo;
    }

    public void setPacketNo(int packetNo) {
        this.packetNo = packetNo;
    }
}
