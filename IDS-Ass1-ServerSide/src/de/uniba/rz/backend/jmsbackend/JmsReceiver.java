package de.uniba.rz.backend.jmsbackend;

import com.sun.messaging.jms.MQMessageFormatRuntimeException;
import de.uniba.rz.backend.localStorage.TicketStoreImpl;

import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSConsumer;
import javax.jms.JMSContext;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.naming.Context;
import javax.naming.NamingException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: sini_ann
 * Date: 23/05/18 4:01 PM
 */
public class JmsReceiver extends Thread {
   private ConnectionFactory connectionFactory;
   private Destination destination;
   TicketStoreImpl ticketStore;

   private boolean active;

   public JmsReceiver(Context ctx, String connFactoryName, String queueName, TicketStoreImpl ticketStore)
           throws NamingException {
      connectionFactory = (ConnectionFactory) ctx.lookup(connFactoryName);
      destination = (Destination) ctx.lookup(queueName);
      this.ticketStore = ticketStore;
   }


   private void startServer() throws JMSException {

      System.out.println("\t [RECEIVER]: Start waiting for messages");
      active =true;
      Message msg;
      // Create JMS Context
      try(JMSContext jmsContext = connectionFactory.createContext()) {
          // Create a JMSConsumer to receive message
          JMSConsumer consumer = jmsContext.createConsumer(destination);
          while (active) {
              // Receive a TextMessage (-> String.class)
              // blocks for 5000ms
              try {
                  msg = consumer.receive();

              } catch (MQMessageFormatRuntimeException me) {
                  //System.out.println("bad format");
                  continue;
              }
                  // if no message is received with 5 secs messsage == null
              if (msg != null) {
                  System.out.println("--------------");
                  System.out.println("\t [RECEIVER]: >Received: " + msg);
                  String requestString = msg.getBody(String.class);

                  if (msg.getJMSReplyTo() != null) {
                      //System.out.println("not empty, processing request");
                      new JmsReqHandlers(requestString, jmsContext, msg.getJMSReplyTo(), ticketStore).run();

                  }
              }
          }

          System.out.println("\t [RECEIVER]: Stopped.");
      }

}

      public void stopServer() {
      active = false;
      System.out.println("\t [RECEIVER]: Stopping to listen for messages.");
   }

   @Override
   public void run() {
       try{
           startServer();

       } catch (JMSException ex) {
           Logger.getLogger(JmsReceiver.class.getName()).log(Level.SEVERE, null, ex);
       }

   }
}