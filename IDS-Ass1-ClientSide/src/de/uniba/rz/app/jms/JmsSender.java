package de.uniba.rz.app.jms;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import de.uniba.rz.entities.extras.Response;

import javax.jms.*;
import javax.naming.Context;
import javax.naming.NamingException;


/**
 * User: sini_ann
 * Date: 23/05/18 2:50 PM
 */
public class JmsSender {
    ConnectionFactory connectionFactory;
    Destination destination;

    public JmsSender(Context ctx, String connectionFactoryName, String queueName) throws NamingException {
        connectionFactory = (ConnectionFactory) ctx.lookup(connectionFactoryName);
        destination = (Destination) ctx.lookup(queueName);

    }

    public Response sendMessage(String message) {
        // Create JMSContext
        try (JMSContext jmsContext = connectionFactory.createContext()) {

            TemporaryQueue tempQueue = jmsContext.createTemporaryQueue();
            System.out.println("\t [SENDER]: Sending Message '" + message + "'");

            // Create a Producer and use it to send a TextMessage
            jmsContext.createProducer().setJMSReplyTo((Destination) tempQueue).send(destination, message);

            // Create a JMSConsumer to receive message
            JMSConsumer consumer = jmsContext.createConsumer(tempQueue);

            // blocks for 10000ms
            message = consumer.receiveBody(String.class, 10000);
            if (message != null) {
                System.out.println("\t [RECEIVER]: >Received from server: " + message);
                System.out.println("-------------");
                Gson json = new GsonBuilder().serializeNulls().create();
                return json.fromJson(message, Response.class);
            }
            return null;
        }
    }
}

