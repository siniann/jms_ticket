package de.uniba.rz.backend.localStorage;

import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.backend.UnknownTicketException;
import de.uniba.rz.entities.Priority;
import de.uniba.rz.entities.Status;
import de.uniba.rz.entities.Ticket;
import de.uniba.rz.entities.Type;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

//Thread safe implementation of "in memory database"

public class TicketStoreImpl implements TicketStore {

	private ConcurrentLinkedQueue<Ticket> alltickets = new ConcurrentLinkedQueue<>();
	private ConcurrentHashMap<Integer, Ticket> ticketHashMap= new ConcurrentHashMap<>();
	AtomicInteger nextId;

	public TicketStoreImpl() {
		nextId = new AtomicInteger();
	}

	@Override
	public synchronized Ticket storeNewTicket(String reporter, String topic, String description, Type type, Priority priority) {
		int id = nextId.getAndIncrement();
		Ticket ticket = new Ticket(id, reporter, topic, description, type, priority);
		alltickets.add(ticket);
		ticketHashMap.put(id, ticket);
		return ticket;
	}

	@Override
	public synchronized void updateTicketStatus(int ticketId, Status newStatus)
			throws UnknownTicketException, IllegalStateException {
		Ticket ticket = ticketHashMap.remove(ticketId);
		alltickets.remove(ticket);
                
		ticket.setStatus(newStatus);
		alltickets.add(ticket);
		ticketHashMap.put(ticketId, ticket);
	}

	@Override
	public synchronized List<Ticket> getAllTickets() {
		ArrayList<Ticket> allTickets = new ArrayList<>();
		for(Ticket ticket : alltickets){
			allTickets.add(ticket);
		}
		return allTickets;
	}
        
	public synchronized Ticket getTicketById(int id) {
		return ticketHashMap.get(id);
	}

}
