package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

public class Server {

	private ServerSocket socket;
	private boolean isListening;

	public Server() {
		try {
			this.socket = new ServerSocket(21);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public synchronized void startListening() {
		System.out.println("Start listening...");
		if (!isListening) {
			this.isListening = true;
		}
		run();
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
		}
		this.socket = null;
	}

	private void run() {
		try {
			while (isListening) {
				Socket s = this.socket.accept();
				System.out.println(s.getInetAddress().getHostAddress() 
						+ ":" + s.getPort() + " has connected at "
						+ new Date().toString());
				Session session = new Session(s);
				session.start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
