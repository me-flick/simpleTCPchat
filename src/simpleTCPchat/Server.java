import java.io.IOException; // always import when working with sockets
import java.net.Socket;
import java.net.ServerSocket;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;

public class Server implements Runnable {
	
	private ArrayList<ConnectionHandler> connections;
	
	@Override
	public void run() {
		try {
			ServerSocket server = new ServerSocket(port: 9999); // only port needs to be passed for socket
			Socket client = server.accept();
			ConnectionHandler handler = new ConnectionHandler(client);
			connections.add(handler);
		} catch (IOException e) {
			// TODO: handle (sockets always need IO exceptions to be dealt with, maybe shut down?) 
		}

	}
	
	public void broadcast(String message) {
		for (ConnectionHandler ch : connections) {
			if (ch != null) {
				ch.sendMessage(message);
			}
		}
	}
	
	class ConnectionHandler implements Runnable { // What handles the individual clients, themselves

		private Socket client;
		private BufferedReader in;  //for input from the client
		private PrintWriter out;    //for output to the client. 
		private String nickname;    //defines the nickname of the client
		
		public ConnectionHandler(Socket client);
			this.client = client;
		}
	
		@Override
		public void run() {
			try {
				out = new PrintWriter(client.getOutputStream(), autoFlush: true); // out.println("Hello, client!" 
				in = new BufferedReader(new InputStreamReader(client.getInputStream())); //in.readline(), print with system.println
				out.println("Please enter a nickname: ");
				nickname = in.readLine(); //needs input sanitization, if.nickname(isBlank)....
				System.out.println(nickname + " connected! ");
				broadcast(nickname + " joined the chat!");
				String message;
				while ((message = in.readLine())!= null) {
					if (message.startsWith("/nick ")) {
						// TODO: handle nickname
					} else if (message.startsWith("/quit")) {
						// TODO : handle quit
					} else {
						broadcast(nickname + ": " + message);
					}
				}
			} catch (IOException.e) {
				// TODO: Handle
			}
		}
		
		public void sendMessage(String message) {
			out.println(message);
		}
		
	}
}
