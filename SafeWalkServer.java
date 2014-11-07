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
        while (true) {
            try {
                ArrayList<Socket> clientSockets = new ArrayList<Socket>();
                
                Socket client = serverSocket.accept();
                
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String s = in.readLine();
                s.toUpperCase();
                
                BufferedWriter out = new BufferedWriter(new OutputStreamWriter(client.getOutputStream()));
                out.flush();
                
                // Structure that excecutes correct steps for specific client inputs
                
                if (s.startsWith(":")) {
                    
                    
                    if (s.equals(":LIST_PENDING_REQUESTS")) { 
                        out.write("Printing Pending Requests...\n");
                        in.close();
                        client.close();
                    }
                    // Resets the server if client inputs command RESET
                    else if (s.equals(":RESET")) {
                        
                        for (Socket c: clientSockets) {
                            BufferedWriter outc = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                            outc.flush();
                            outc.write("ERROR: connection reset\n");
                            outc.flush();
                            outc.close();
                            c.close();
                            clientSockets.remove(c);
                            
                        }
                        clientSockets.clear();
                        
                        out.write("Response: Success\n");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                        
                    }
                    // Shuts down the server if client inputs command SHUTDOWN
                    else if (s.equals(":SHUTDOWN")) {
                        for (Socket c: clientSockets) {
                            BufferedWriter outc = new BufferedWriter(new OutputStreamWriter(c.getOutputStream()));
                            outc.flush();
                            outc.write("ERROR: connection shutdown\n");
                            outc.flush();
                            outc.close();
                            c.close();
                            clientSockets.remove(c); 
                        } 
                        clientSockets.clear();
                        out.write("Response: Success\n");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                        serverSocket.close();
                        
                        //System.out.println("ServerSocket closed");
                        return;
                    }
                    // Responsds to client if invalid command was given
                    else {
                        out.write("ERROR: Invalid Command\n");
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
                out.close();
                client.close();
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
            //System.out.println("returned from run() method");
            return;
            
        }
        else if (args.length == 1) {
            if (Integer.parseInt(args[0]) < 1025 || Integer.parseInt(args[0]) > 65535) {
                System.out.println("Error: Port number invalid. Exiting Program . . .");
                return;
            }
            else {
                SafeWalkServer safe = new SafeWalkServer(Integer.parseInt(args[0]));
                safe.run();
               // System.out.println("returned from run() method");
                return;
              
            }

            
        }
        
    }

}
