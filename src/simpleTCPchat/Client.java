package simpleTCPchat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class Client implements Runnable {

	private Socket client;
	private BufferedReader in;
	private PrintWriter out;
	private boolean done;
	
	@Override
	public void run() {
		try {
			client = new Socket("127.0.0.1", 9999);
			out = new PrintWriter(client.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			
			InputHandler inHandler = new InputHandler();
			Thread t = new Thread(inHandler);
			t.start();
			
			String inMessage;
			while ((inMessage = in.readLine()) != null) {
				System.out.println(inMessage);
			}
		} catch (IOException e) {
			shutdown();
		}
	}
	public void shutdown() {
		done = true;
		try {
			if (in != null) in.close();
			if (out != null) out.close();
			if (client != null && !client.isClosed()) {
				client.close();
			}
		} catch (IOException e) {
			//ignore, no solution
		}
	}	
		
	
	class InputHandler implements Runnable {

		@Override
		public void run() {
			try (BufferedReader inReader = new BufferedReader(new InputStreamReader(System.in))) {
				String message; 
				while (!done && (message =inReader.readLine()) != null) {
					if (message.equals("/quit")) {
						shutdown();
						break; // exits the loops after shutdown, handles exception
					} else {
						out.println(message);
					}
				}
			} catch (IOException e) {
				shutdown();
			}
			
		}
		
	}
	
	public static void main(String[] args) {
		new Thread(new Client()).start(); // correctly starts the client in a new thread
	}
}

