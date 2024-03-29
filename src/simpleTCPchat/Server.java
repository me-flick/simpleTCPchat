package simpleTCPchat;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server implements Runnable {
    
    private ArrayList<ConnectionHandler> connections = new ArrayList<>();
    private ServerSocket server;
    private boolean done = false;
    private ExecutorService pool; // thread pool to reuse threads
    private int port = 9999; // port of 9999 for demonstration
    
    @Override
    public void run() {
        try {
            server = new ServerSocket(port); // corrected syntax
            pool = Executors.newCachedThreadPool(); // initializes the thread pool
            while (!done) {
                Socket client = server.accept();
                ConnectionHandler handler = new ConnectionHandler(client);
                connections.add(handler);
                pool.execute(handler);
            }
        } catch (IOException e) { 
            shutdown(); // if exceptions are thrown, server will initiate shutdown
        }
    }
    
    public void broadcast(String message) {
        for (ConnectionHandler ch : connections) {
            if (ch != null) {
                ch.sendMessage(message);
            }
        }
    }
    
    public void shutdown() {
        try {
            done = true;
            if (!server.isClosed()) {
                server.close();
            }
            for (ConnectionHandler ch : connections) {
                ch.shutdown();
            }
        } catch (IOException e) {
            // ignore
        }
    }
    
    class ConnectionHandler implements Runnable {

        private Socket client;
        private BufferedReader in;  
        private PrintWriter out;    
        private String nickname;    
        
        public ConnectionHandler(Socket client) {
            this.client = client;
        }
        
        @Override
        public void run() { // nickname was declared twice, removed
            try {
                out = new PrintWriter(client.getOutputStream(), true);
                in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                out.println("Please enter a nickname: ");
                nickname = in.readLine();
                System.out.println(nickname + " connected! ");
                broadcast(nickname + " joined the chat!");
                String message;
                while ((message = in.readLine()) != null) {
                    if (message.startsWith("/nick ")) {
                        String[] messageSplit = message.split(" ", 2);
                        if (messageSplit.length == 2) {
                            broadcast(nickname + " renamed themselves to " + messageSplit[1]);
                            System.out.println(nickname + " renamed themselves to " + messageSplit[1]);
                            nickname = messageSplit[1];
                            out.println("Successfully changed nickname to: " + nickname);
                        } else {
                            out.println("No nickname provided!");
                        }
                    } else if (message.startsWith("/quit")) {
                        out.println("Quitting...");
                        break; // Exits the loop, leading to closing the connection
                    } else {
                        broadcast(nickname + ": " + message);
                    }
                }
            } catch (IOException e) {
                broadcast(nickname + " has left the chat!");
            } finally {
                shutdown();
            }
        }
        
        public void sendMessage(String message) {
            out.println(message);
        }
    
        public void shutdown() {
            try {
                if (in != null) in.close();
                if (out != null) out.close();
                if (client != null && !client.isClosed()) {
                    client.close();
                }
            } catch (IOException e) {
                // ignore, server is closing, no need to handle
            } 
        }
    }
    
    public static void main(String[] args) {
        new Thread(new Server()).start();
    }
}
