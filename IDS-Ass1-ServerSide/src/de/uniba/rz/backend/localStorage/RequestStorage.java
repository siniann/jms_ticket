package de.uniba.rz.backend.localStorage;

import java.net.DatagramPacket;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;


public class RequestStorage {

    private ConcurrentHashMap<UUID, DatagramPacket> requestMap = new ConcurrentHashMap<>();
    private ConcurrentLinkedQueue<DatagramPacket> requestQueue = new ConcurrentLinkedQueue<>();
    private static RequestStorage instance = null;

    public RequestStorage() {
    }

    public static RequestStorage getInstance() {
        if (instance == null) {
            createInstance();
        }
        return instance;
    }

    private static synchronized void createInstance() {
        if (instance == null) {
            instance = new RequestStorage();
        }
    }

    public ConcurrentHashMap<UUID, DatagramPacket> getRequestMap() {
        return requestMap;
    }

    public ConcurrentLinkedQueue<DatagramPacket> getRequestQueue() {
        return requestQueue;
    }

    public synchronized void storeRequest(DatagramPacket datagramPacket) {
        requestQueue.add(datagramPacket);
    }
}
