/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ftpstorageserver.server;

import ftpstorageserver.server.conflicts.RequestStore;
import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Date;

/**
 *
 * @author HP Zbook 15
 */
public class Server implements Runnable, SessionListener, ConflictListener {

    private ServerListener listener;
    private Thread thread;
    private Object lock = new Object();
    private boolean notified = true;

    private ServerSocket socket;
    private boolean isListening;

    private ArrayList<Session> clients;
    private ArrayList<RequestStore> requests;
    private ArrayList<File> filesProcessing;
    private int maxClient;

    public Server() {
        this.clients = new ArrayList<>();
        this.requests = new ArrayList<>();
        this.filesProcessing = new ArrayList<>();
    }

    public synchronized void startListening(int port, int maxClient) {
        this.maxClient = maxClient;
        if (thread == null) {
            try {
                this.socket = new ServerSocket(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.isListening = true;
            thread = new Thread(this);
            thread.start();
        }
        System.out.println("Start listening...");
    }

    public synchronized void stopListening() {
        System.out.println("Stop listening...");
        this.isListening = false;

        if (socket != null) {
            try {
                this.socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.socket = null;
        }

        if (thread != null) {
            thread.interrupt();
            thread = null;
        }
    }

    @Override
    public void run() {
        System.out.println("running");
        try {
            while (isListening) {
                if (this.clients.size() >= maxClient) {
                    pauseListening();
                }

                Socket s = this.socket.accept();

                synchronized (lock) {
                    if (!notified) {
                        lock.wait();
                    }
                }

                System.out.println(s.getInetAddress().getHostAddress()
                        + ":" + s.getPort() + " has connected at "
                        + new Date().toString());
                Session session = new Session(s);
                session.setListener(this);
                session.setConflictListener(this);
                this.clients.add(session);
                this.listener.notifyConnected(session);

                session.start();
                
            }
        } catch (SocketException e) {
            System.out.println("Closed Socket!");
        } catch (InterruptedException e) {
            System.err.println("look failure");
        } catch (IOException e) {
            System.err.println("session get socket failure");
        }
    }

    public void setListener(ServerListener l) {
        this.listener = l;
    }

    public synchronized boolean isListening() {
        return this.isListening;
    }

    public synchronized boolean isNotified() {
        return this.notified;
    }

    public synchronized void pauseListening() {
        synchronized (lock) {
            lock.notify();
        }
        this.notified = false;
        System.out.println("Pause listening.");
    }

    public synchronized void resumeListening() {
        synchronized (lock) {
            lock.notify();
        }
        this.notified = true;
        System.out.println("Resume listening.");
    }

    @Override
    public void notifyDisconnected(Session s) {
        int index = this.clients.indexOf(s);
        this.clients.remove(s);
        this.listener.notifyDisconnected(index);
    }

    public void disconnectClient(int index) {
        this.clients.get(index).stop();
    }

    @Override
    public void addRequest(Session s, String request) {
        RequestStore r = new RequestStore(s, request);
        this.requests.add(r);
    }

    @Override
    public RequestStore popRequest() {
        if (requests.size() > 0) {
            return requests.remove(0);
        }
        return null;
    }

    public int getSession(Session s) {
        return this.clients.indexOf(s);
    }

    @Override
    public void notifyLogin(Session s) {
        this.listener.notifyLogin(s);
    }
    
    @Override
    public void addFileProcessing(File file){
        this.filesProcessing.add(file);
    }
    
    @Override
    public boolean removeFileProccessing(File file){
        return this.filesProcessing.remove(file);
    }

    @Override
    public boolean isFileProcessing(File file){
        return this.filesProcessing.contains(file);
    }

}
