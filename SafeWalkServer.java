import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Serializable {
    int port;
    final ServerSocket serverSocket;
    
    public SafeWalkServer(int port) throws SocketException, IOException {
        this.serverSocket = new ServerSocket(port);
        serverSocket.setReuseAddress(true);
        this.port = port;
    }

    public SafeWalkServer() throws SocketException, IOException {
        this.serverSocket = new ServerSocket(0);
        serverSocket.setReuseAddress(true);
        this.port = serverSocket.getLocalPort();
    }

    public int getLocalPort() {
        return port;
    }
 
    // Run Method
    public void run() {
        ArrayList<Socket> clientSockets = new ArrayList<Socket>();
        while (true) {
            try {
                Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String s = in.readLine();
                System.out.printf("%s\n", s);

                // Structure that excecutes correct steps for specific client inputs
                if (s.startsWith(":")) {
                    if (s.equals(":LIST_PENDING_REQUESTS")) { 
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("Printing Pending Requests...");
                        out.flush();
                        in.close();
                        client.close();
                    }
                    // Resets the server if client inputs command RESET
                    else if (s.equals(":RESET")) {
                        
                        for (Socket c: clientSockets) {
                            PrintWriter outc = new PrintWriter(new OutputStreamWriter(c.getOutputStream()));
                            outc.flush();
                            outc.println("ERROR: connection reset");
                            outc.flush();
                            outc.close();
                            c.close();
                        }
                        clientSockets.clear();
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("Response: Success");
                        out.flush();
                        out.close();
                        in.close();
                        client.close(); 
                    }
                    // Shuts down the server if client inputs command SHUTDOWN
                    else if (s.equals(":SHUTDOWN")) {
                        for (Socket c: clientSockets) {
                            PrintWriter outc = new PrintWriter(new OutputStreamWriter(c.getOutputStream()));
                            outc.flush();
                            outc.println("ERROR: connection shutdown");
                            outc.flush();
                            outc.close();
                            c.close(); 
                        } 
                        clientSockets.clear();
                        
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("Response: Success");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                        serverSocket.close();
                        return;
                    }
                    // Responsds to client if invalid command was given
                    else {
                        
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("ERROR: Invalid Command");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                    }
                }
                else {
                    // Adds client to pendinglist
                    clientSockets.add(client);   
                }   
            } catch (Exception e) {  
            }   
        }
    }
    
    // Main Method
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        if (args.length == 0) {
            SafeWalkServer safe = new SafeWalkServer();
            System.out.printf("Port not specified. Using free port %d\n", safe.getLocalPort());
            safe.run(); 
        }
        else if (args.length == 1) {
            if (Integer.parseInt(args[0]) < 1025 || Integer.parseInt(args[0]) > 65535) {
                System.out.println("Error: Port number invalid. Exiting Program . . .");
            }
            else {
                SafeWalkServer safe = new SafeWalkServer(Integer.parseInt(args[0]));
                safe.run();
            }
        }
    }
}
