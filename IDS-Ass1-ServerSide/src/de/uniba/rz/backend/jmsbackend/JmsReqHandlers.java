package de.uniba.rz.backend.jmsbackend;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.backend.UnknownTicketException;
import de.uniba.rz.backend.localStorage.TicketStoreImpl;

import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.extras.Request;
import de.uniba.rz.entities.extras.Response;
import de.uniba.rz.entities.extras.TicketConstants;

import javax.jms.Destination;
import javax.jms.JMSContext;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: sini_ann
 * Date: 28/05/18 12:45 PM
 */
public class JmsReqHandlers implements Runnable {

    JMSContext jmsContext;
    Destination destination;

    private final String requestString;
    private int action;
    private int ticketId;
    private Ticket ticket;
    private UUID packetId;
    private Status newStatus;
    private final TicketStore ticketStore;
    private Response response = new Response();
    private Gson json;
    private String responseString;
    Gson responseObj;

    public JmsReqHandlers(String requestString, JMSContext jmsContext, Destination destination, TicketStore ticketStore) {
        this.requestString = requestString;
        this.jmsContext = jmsContext;
        this.destination = destination;
        this.ticketStore = ticketStore;
        requestGeneration();
    }

    private void requestGeneration() {
        json = new GsonBuilder().serializeNulls().create();
        Request request = json.fromJson(requestString.trim(), Request.class);

        this.action = request.getAction();
        if (action != TicketConstants.ACTION_GET_TICKETS) {
            this.ticket = request.getTicket();
        }
        this.packetId = request.getPacketId();
        this.ticketId = request.getTicketId();
        this.newStatus = request.getStatus();
    }

    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {
        switch (action) {
            case TicketConstants.ACTION_CREATE_NEW_TICKET:
                ticketStore.storeNewTicket(ticket.getReporter(), ticket.getTopic(), ticket.getDescription(), ticket.getType(), ticket.getPriority());
                response.setTicket(ticket);
                response.setServerId(UUID.randomUUID());
                response.setAction(TicketConstants.ACTION_CREATE_NEW_TICKET);

                responseObj = new GsonBuilder().serializeNulls().create();
                responseString = responseObj.toJson(response, Response.class);
                //System.out.println("response created: " + responseString);

                jmsContext.createProducer().send(destination, responseString);
                break;
            case TicketConstants.ACTION_GET_TICKETS:
                List<Ticket> tickets = ticketStore.getAllTickets();

                response.setTickets(tickets);
                response.setServerId(UUID.randomUUID());
                response.setAction(TicketConstants.ACTION_CREATE_NEW_TICKET);

                Gson responseObj = new GsonBuilder().serializeNulls().create();
                responseString = responseObj.toJson(response, Response.class);
                //System.out.println("response created! ");

                //System.out.println("response created: " + responseString);

                jmsContext.createProducer().send(destination, responseString);
                break;
            case TicketConstants.ACTION_GET_TICKET_BY_ID:
                ticket = ((TicketStoreImpl) ticketStore).getTicketById(ticketId);
                response = new Response();
                response.setTicket(ticket);
                response.setAction(TicketConstants.ACTION_GET_TICKET_BY_ID);
                response.setServerId(UUID.randomUUID());

                json = new GsonBuilder().serializeNulls().create();
                responseString = json.toJson(response, Response.class);
                //System.out.println("response created: " + responseString);
                jmsContext.createProducer().send(destination, responseString);

                break;
            case TicketConstants.ACTION_ACCEPT_TICKET:
            case TicketConstants.ACTION_REJECT_TICKET:
            case TicketConstants.ACTION_CLOSE_TICKET: {
                try {
                    ticketStore.updateTicketStatus(ticketId, newStatus);
                } catch (UnknownTicketException ex) {
                    Logger.getLogger(JmsReqHandlers.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            ticket = ((TicketStoreImpl) ticketStore).getTicketById(ticketId);
            response = new Response();
            response.setTicket(ticket);
            response.setAction(TicketConstants.ACTION_GET_TICKET_BY_ID);
            response.setServerId(UUID.randomUUID());

            json = new GsonBuilder().serializeNulls().create();
            responseString = json.toJson(response, Response.class);

            jmsContext.createProducer().send(destination, responseString);

            break;
            default:
                System.out.println("Invalid request");
        }

    }
}
