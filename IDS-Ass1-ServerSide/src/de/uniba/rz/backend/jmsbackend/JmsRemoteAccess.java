package de.uniba.rz.backend.jmsbackend;

import de.uniba.rz.backend.RemoteAccess;
import de.uniba.rz.backend.TicketStore;
import de.uniba.rz.backend.localStorage.TicketStoreImpl;
import de.uniba.rz.entities.extras.JmsConstants;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Hashtable;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.uniba.rz.entities.extras.JmsConstants.*;

/**
 * User: sini_ann
 * Date: 23/05/18 6:16 PM
 */
public class JmsRemoteAccess implements RemoteAccess{

    JmsReceiver receiver;
    TicketStore ticketStore;


    /**
     * Generic startup method which might be used to prepare the actual execution
     *
     * @param ticketStore reference to the {@link TicketStore} which is used by the application
     */
    @Override
    public void prepareStartup(TicketStore ticketStore) {

        this.ticketStore = ticketStore;
        Hashtable<String, String> contextParams = new Hashtable<>();
        contextParams.put(Context.INITIAL_CONTEXT_FACTORY, JmsConstants.CONTEXT_FACTORY);
        contextParams.put(Context.PROVIDER_URL, JmsConstants.PROVIDER_URL);

        Context ctx = null;
        try {
            ctx = new InitialContext(contextParams);
        } catch (NamingException e) {
            Logger.getLogger(JmsRemoteAccess.class.getName()).log(Level.SEVERE, null, e);
        }

        try {
            receiver = new JmsReceiver(ctx, JmsConstants.CONNECTION_FACTORY_NAME, JmsConstants.QUEUE_NAME,(TicketStoreImpl) ticketStore);
        } catch (NamingException e) {
            Logger.getLogger(JmsRemoteAccess.class.getName()).log(Level.SEVERE, null, e);
        }

        receiver.start();

        // Wait for Input to shutdown the Server properly
        Scanner scanner = new Scanner(System.in);
        System.out.println("Hit Enter to stop the server.");
        scanner.nextLine();
        scanner.close();
        receiver.stopServer();

    }

    /**
     * Triggers the graceful shutdown of the system.
     */
    @Override
    public void shutdown() {

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

    }
}
