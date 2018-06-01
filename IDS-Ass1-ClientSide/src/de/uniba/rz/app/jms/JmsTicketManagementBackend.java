package de.uniba.rz.app.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.rz.app.TicketManagementBackend;
import de.uniba.rz.entities.*;
import de.uniba.rz.entities.extras.JmsConstants;
import de.uniba.rz.entities.extras.Request;
import de.uniba.rz.entities.extras.Response;
import de.uniba.rz.entities.extras.TicketConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;


import static de.uniba.rz.entities.extras.JmsConstants.CONNECTION_FACTORY_NAME;
import static de.uniba.rz.entities.extras.JmsConstants.CONTEXT_FACTORY;
import static de.uniba.rz.entities.extras.JmsConstants.PROVIDER_URL;
import static de.uniba.rz.entities.extras.JmsConstants.QUEUE_NAME;
import static de.uniba.rz.entities.extras.TicketConstants.ACTION_CREATE_NEW_TICKET;
import static de.uniba.rz.entities.extras.TicketConstants.ACTION_GET_TICKETS;

/**
 * User: sini_ann
 * Date: 23/05/18 1:33 PM
 */
public class JmsTicketManagementBackend implements TicketManagementBackend {

    AtomicInteger nextId;
    JmsSender sender;


    public JmsTicketManagementBackend() throws NamingException {
        nextId = new AtomicInteger(1);
        Hashtable<String, String> contextParams = new Hashtable<>();
        contextParams.put(Context.INITIAL_CONTEXT_FACTORY, CONTEXT_FACTORY);
        contextParams.put(Context.PROVIDER_URL, PROVIDER_URL);
        Context ctx = new InitialContext(contextParams);

        System.out.println("Sending some message to Queue " );
        //sender.sendMessage("Hallo Server!");

        sender = new JmsSender(ctx, JmsConstants.CONNECTION_FACTORY_NAME, JmsConstants.QUEUE_NAME);




    }

    /**
     * Method to create a new Ticket based on the provided information
     *
     * @param reporter    the name of the reporter
     * @param topic       the topic of the ticket
     * @param description a textual description of the problem
     * @param type        the {@link Type} of the ticket to be created
     * @param priority    the {@link Priority} of the problem
     * @return a {@link Ticket} representation of the newly created ticket
     * @throws TicketException if the creation failed
     */
    @Override
    public Ticket createNewTicket(String reporter, String topic, String description, Type type, Priority priority) throws TicketException {

        System.out.println("Creating a new ticket..... ");
        Ticket ticket = new Ticket(nextId.getAndIncrement(), reporter, topic, description, type, priority, Status.NEW);

        Request request = new Request();
        request.setAction(ACTION_CREATE_NEW_TICKET);
        request.setTicket(ticket);
        request.setPacketId(UUID.randomUUID());

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);
        //System.out.println(requestString);

        Response response = null;
        try {
            response = sender.sendMessage(requestString);
            return response.getTicket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a list of {@link Ticket}s currently available in the system.
     *
     * @return the list of {@link Ticket}s
     * @throws TicketException if technical problems occur
     */
    @Override
    public List<Ticket> getAllTickets() throws TicketException {

        System.out.println("Fetching all tickets..... ");


        Request request = new Request();
        request.setPacketId(UUID.randomUUID());
        request.setAction(ACTION_GET_TICKETS);

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);

        Response response = null;
        try {
            response = sender.sendMessage(requestString);
            List<Ticket> ticketList = response.getTickets();
            Collections.sort(ticketList, new Comparator<Ticket>() {
                public int compare(Ticket ticket1, Ticket ticket2) {
                    return Float.compare(ticket1.getId(), ticket2.getId());
                }
            });
            return ticketList;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Returns a single {@link Ticket} with the given {@code id}
     *
     * @param id the Id of the ticket to be accepted
     * @return a {@link Ticket} representation of the ticket
     * @throws TicketException thrown if the ticket with the {@code id} is unknown
     */
    @Override
    public Ticket getTicketById(int id) throws TicketException {
        return (Ticket) getTicketByIdInteral(id).clone();
    }

    private Ticket getTicketByIdInteral(int id) throws TicketException {
        System.out.println("Fetching ticket by ID..... ");

        Request request = new Request();
        request.setAction(TicketConstants.ACTION_GET_TICKET_BY_ID);
        request.setPacketId(UUID.randomUUID());
        request.setTicketId(id);

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);

        Response response = null;
        try {
            response = sender.sendMessage(requestString);
            return response.getTicket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Method to accept a Ticket, i.e., changing the {@link Status} to
     * {@code Status.ACCEPTED}
     * <p>
     * Throws an exception if this status change is not possible (i.e., the
     * current status is not {@code Status.NEW}) or if the {@code id} refers to
     * a {@link Ticket} that does not exist.
     *
     * @param id the Id of the ticket to be accepted
     * @return a {@link Ticket} representation of the modified ticket
     * @throws TicketException thrown if the status change is not allowed or the ticket with
     *                         the {@code id} is unknown
     */
    @Override
    public Ticket acceptTicket(int id) throws TicketException {
        System.out.println("Accepting ticket..... ");

        Request request = new Request();
        request.setAction(TicketConstants.ACTION_ACCEPT_TICKET);
        request.setPacketId(UUID.randomUUID());
        request.setTicketId(id);
        request.setStatus(Status.ACCEPTED);

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);

        Response response = null;
        try {
            response = sender.sendMessage(requestString);
            return response.getTicket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;    }

    /**
     * Method to reject a Ticket, i.e., changing the {@link Status} to
     * {@code Status.REJECTED}
     * <p>
     * Throws an exception if this status change is not possible (i.e., the
     * current status is not {@code Status.NEW}) or if the {@code id} refers to
     * a {@link Ticket} that does not exist.
     *
     * @param id the Id of the ticket to be rejected
     * @return a {@link Ticket} representation of the modified ticket
     * @throws TicketException thrown if the status change is not allowed or the ticket with
     *                         the {@code id} is unknown
     */
    @Override
    public Ticket rejectTicket(int id) throws TicketException {
        System.out.println("Rejecting ticket..... ");

        Request request = new Request();
        request.setAction(TicketConstants.ACTION_REJECT_TICKET);
        request.setPacketId(UUID.randomUUID());
        request.setTicketId(id);
        request.setStatus(Status.REJECTED);

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);

        Response responseDTO = null;
        try {
            responseDTO = sender.sendMessage(requestString);
            return responseDTO.getTicket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Method to close a Ticket, i.e., changing the {@link Status} to
     * {@code Status.CLOSED}
     * <p>
     * Throws an exception if this status change is not possible (i.e., the
     * current status is not {@code Status.ACCEPTED}) or if the {@code id}
     * refers to a {@link Ticket} that does not exist.
     *
     * @param id the Id of the ticket to be accepted
     * @return a {@link Ticket} representation of the modified ticket
     * @throws TicketException thrown if the status change is not allowed or the ticket with
     *                         the {@code id} is unknown
     */
    @Override
    public Ticket closeTicket(int id) throws TicketException {

        System.out.println("Closing ticket..... ");

        Request request = new Request();
        request.setAction(TicketConstants.ACTION_CLOSE_TICKET);
        request.setPacketId(UUID.randomUUID());
        request.setTicketId(id);
        request.setStatus(Status.CLOSED);

        Gson json = new GsonBuilder().serializeNulls().create();
        String requestString = json.toJson(request, Request.class);

        Response responseDTO = null;
        try {
            responseDTO = sender.sendMessage(requestString);
            return responseDTO.getTicket();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Method to be called to trigger graceful shutdown of a system
     */
    @Override
    public void triggerShutdown() {
        this.sender = null;

    }
}
