package client.data;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Transfer a piece of file
 * 
 * @author HP Zbook 15
 *
 */
public class Piece implements Runnable {

	private Thread thread;
	private List<PieceListener> listeners = Collections.synchronizedList(new ArrayList<PieceListener>());

	private File file;
	private RandomAccessFile raf;
	private long offset;
	private long length;
	private int pieceIndex;

	private Socket socket;
	private DataInputStream input;
	private DataOutputStream output;

	private boolean isSend; // true: is send piece, false is receive piece
	private Object lock;
	private boolean notified; // if true : this thread is running else wait

	/**
	 * Initialize with parameters of file session
	 * 
	 * @param socket
	 * @param offset
	 * @param length
	 */
	public Piece(Socket socket, File file, long offset, long length, boolean isSend, int pieceIndex) {
		this.socket = socket;
		this.file = file;
		this.offset = offset;
		this.length = length;
		this.isSend = isSend;
		this.pieceIndex = pieceIndex;
		try {
			init();
		} catch (IOException e) {
			// TODO: call function to notify super class that this socket is not connection.
			// use Listener
			e.printStackTrace();
		}
	}

	/**
	 * Initialize data input/output and lock
	 * 
	 * @throws IOException
	 */
	public void init() throws IOException {
		this.isSend = true;
		this.lock = new Object();
		if (isSend) {
			this.raf = new RandomAccessFile(file, "r");
			this.raf.seek(offset);
		} else {
			String part = new String(file.getName() + ".part" + pieceIndex);
			File f = new File(file.getParentFile(), part);
			this.raf = new RandomAccessFile(f, "w");
		}
		this.input = new DataInputStream(socket.getInputStream());
		this.output = new DataOutputStream(socket.getOutputStream());
	}

	public void setOffset(long offset) {
		this.offset = offset;
	}
	
	public int getPieceIndex() {
		return this.pieceIndex;
	}

	public void start() {
		if (thread == null) {
			thread = new Thread(this);
			thread.start();
		}
	}

	public void stop() {
		if (socket != null) {
			try {
				socket.close();
			} catch (IOException e) {
				e.printStackTrace();
				socket = null;
			}
			socket = null;
		}

		if (raf != null) {
			try {
				raf.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			raf = null;
		}

		if (thread != null) {
			thread.interrupt();
			thread = null;
		}
	}

	@Override
	public void run() {
		try {
			synchronized (lock) {
				if (!notified) {
					System.out.println("DEBUG: lock.wait()");
					lock.wait(8 * 1000);
				}
			}

			if (isSend) {
				send();
			} else {
				receive();
			}

		} catch (InterruptedException e) {
			// TODO: notify to super class;
		} catch (IOException e) {

		} finally {
			stop();
		}
	}

	public synchronized void pause() {
		synchronized (lock) {
			lock.notify();
		}
		this.notified = false;
	}

	public synchronized void resume() {
		synchronized (lock) {
			lock.notify();
		}
		this.notified = true;
	}

	private void receive() throws InterruptedException, IOException {
		byte[] buffer = new byte[16384];
		int byteReads = 0;
		while (byteReads <= length) {
			synchronized (lock) {
				if (!notified) {
					lock.wait();
				}
			}

			int bytes = input.read(buffer, 0, (int) (length - byteReads));
			byteReads += bytes;
			raf.write(buffer, 0, bytes);
		}
	}

	private void send() throws InterruptedException, IOException {
		byte[] buffer = new byte[16384];
		int byteReads = 0;
		while (byteReads <= length) {
			synchronized (lock) {
				if (!notified) {
					lock.wait();
				}
			}

			int bytes = raf.read(buffer, 0, (int) (length - byteReads));
			byteReads += bytes;
			output.write(buffer, 0, bytes);
		}
	}

	public void addPieceListener(PieceListener l) {
		if (!listeners.contains(l))
			listeners.add(l);
	}

	public void removePieceListener(PieceListener l) {
		listeners.remove(l);
	}

	

}
