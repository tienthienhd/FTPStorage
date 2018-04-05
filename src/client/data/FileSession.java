package client.data;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class FileSession implements Runnable, PieceListener {

	private File file;
	private ArrayList<Piece> pieces;
	private boolean isSend;

	private Thread thread;

	public FileSession(File file) {
		this.file = file;
		this.pieces = new ArrayList<>();
	}

	public void receiveFile(int numPieces, long lengthOfFile) throws IOException {
		this.isSend = false;
		long lengthPiece = lengthOfFile / numPieces;
		for (int i = 0; i < numPieces; i++) {
			Socket s = new Socket("localhost", 20);
			System.out.println("Socket " + s.getInetAddress() + s.getPort() + "has connected");
			long offset = i * lengthPiece;
			long length = i != numPieces - 1 ? lengthPiece : lengthOfFile - lengthPiece * (numPieces - 1);
			Piece p = new Piece(s, file, offset, length, false, i);
			this.pieces.add(p);
		}
	}

	public void sendFile(int numPieces) throws IOException {
		this.isSend = true;
		long lengthPiece = file.length() / numPieces;
		for (int i = 0; i < numPieces; i++) {
			Socket s = new Socket("localhost", 20);
			System.out.println("Socket " + s.getInetAddress() + s.getPort() + "has connected");
			long offset = i * lengthPiece;
			long length = i != numPieces - 1 ? lengthPiece : file.length() - lengthPiece * (numPieces - 1);
			Piece p = new Piece(s, file, offset, length, true, i);
			this.pieces.add(p);
		}
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	@Override
	public void run() {
		for (Piece p : pieces) {
			p.start();
		}

		try {
			for (Piece p : pieces) {
				synchronized (p) {
					p.wait();
				}
			}
			
			//TODO : craft file with receive mode
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			
		}
	}
	
	
	@Override
	public void transferComplete(Piece p) {
		System.out.println("Piece " + p.getPieceIndex() + " : transfer complete!");
		synchronized (p) {
			p.notify();
		}
	}

}
