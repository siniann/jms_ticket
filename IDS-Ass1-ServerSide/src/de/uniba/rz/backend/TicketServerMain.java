package de.uniba.rz.backend;

import de.uniba.rz.backend.jmsbackend.JmsRemoteAccess;
import de.uniba.rz.backend.localStorage.TicketStoreImpl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.naming.NamingException;

import static de.uniba.rz.entities.extras.PortConstants.SERVER_PORT;

public class TicketServerMain {

	public static void main(String[] args) throws IOException, NamingException {
		TicketStore ticketStore = new TicketStoreImpl();

		List<RemoteAccess> remoteAccessImplementations = getAvailableRemoteAccessImplementations(args);

		// Starting remote access implementations:
		for (RemoteAccess implementation : remoteAccessImplementations) {
			implementation.prepareStartup(ticketStore);
			new Thread(implementation).start();
		}

		BufferedReader shutdownReader = new BufferedReader(new InputStreamReader(System.in));
		System.out.println("Press enter to shutdown system.");
		shutdownReader.readLine();
		System.out.println("Shutting down...");

		// Shuttung down all remote access implementations
		for (RemoteAccess implementation : remoteAccessImplementations) {
			implementation.shutdown();
		}
		System.out.println("completed. Bye!");
	}

	private static List<RemoteAccess> getAvailableRemoteAccessImplementations(String[] args) {
		List<RemoteAccess> implementations = new ArrayList<>();

		// TODO Add your implementations of the RemoteAccess interface
		// e.g.:
		// implementations.add(new UdpRemoteAccess(args[0], args[1]));
		switch (args[0]) {
			case "UDP":
				System.out.println("running only UDP");
				break;
			case "JMS":
				JmsRemoteAccess jmsRemoteAccess = new JmsRemoteAccess();
				implementations.add(jmsRemoteAccess);
				System.out.println("running only JMS server");
				break;
			case "BOTH":

				System.out.println("running both implementations");
				break;
			default:
				System.out.println("Unknown backend type. Using local backend implementation.");

		}
		return implementations;

	}
}
