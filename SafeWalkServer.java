import java.io.*;
import java.net.*;
import java.util.*;

public class SafeWalkServer implements Serializable, Runnable {
    int port;
    final ServerSocket serverSocket;
    private ArrayList<Request> clientList = new ArrayList<Request>();
    
    class Request {
        String name;
        String from;
        String to;
        String type;
        Socket client;
    }
    
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
                Socket client = serverSocket.accept();
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                String s = in.readLine();
                //System.out.printf("%s\n", s);

                // Structure that excecutes correct steps for specific client inputs
                if (s.startsWith(":")) {
                    if (s.equals(":LIST_PENDING_REQUESTS")) { 
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        int length = clientList.size();
                        if (length > 1) {
                            for (Request c: clientList) {
                                out.printf("[%s,%s,%s,%s]", c.name, c.from, c.to, c.type);
                                out.flush();
                                length--;
                                if (length > 0)
                                    out.printf(",");
                            }
                        } else {
                            out.printf("[%s,%s,%s,%s]", clientList.get(0).name, clientList.get(0).from,
                                       clientList.get(0).to, clientList.get(0).type);
                            out.flush();
                        }
                        out.close();
                        in.close();
                        client.close();
                    }
                    // Resets the server if client inputs command RESET
                    else if (s.equals(":RESET")) {
                        
                        for (Request c: clientList) {
                            PrintWriter outc = new PrintWriter(new OutputStreamWriter(c.client.getOutputStream()));
                            outc.flush();
                            outc.println("ERROR: connection reset");
                            outc.flush();
                            outc.close();
                            c.client.close();
                        }
                        clientList.clear();
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("RESPONSE: success");
                        out.flush();
                        out.close();
                        in.close();
                        client.close(); 
                    }
                    // Shuts down the server if client inputs command SHUTDOWN
                    else if (s.equals(":SHUTDOWN")) {
                        for (Request c: clientList) {
                            PrintWriter outc = new PrintWriter(new OutputStreamWriter(c.client.getOutputStream()));
                            outc.flush();
                            outc.println("ERROR: connection reset");
                            outc.flush();
                            outc.close();
                            c.client.close(); 
                        } 
                        clientList.clear();
                        
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("RESPONSE: success");
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
                        out.println("ERROR: invalid request");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                    }
                }
                else {                    
                    if (isValid(s)) {
                        String[] tokens = s.split(",");
                        Request lol = new Request();
                        lol.name = tokens[0];
                        lol.from = tokens[1];
                        lol.to = tokens[2];
                        lol.type = tokens[3];
                        lol.client = client;
                        
                        clientList.add(lol);
                        
                        for (Request r : clientList) {
                            if (!r.name.equals(lol.name)) {
                                if (r.from.equals(lol.from)) {
                                    if ((r.to.equals("*") && !lol.to.equals("*")) ||
                                         (!r.to.equals("*") && lol.to.equals("*")) || (r.to.equals(lol.to) && 
                                                                                       !r.to.equals("*"))) {
                                        //System.out.println("Matching " + r.name + " with " + lol.name);
                                        
                                        PrintWriter out = new PrintWriter(new 
                                                                     OutputStreamWriter(lol.client.getOutputStream()));
                                        out.flush();
                                        out.printf("RESPONSE: %s,%s,%s,%s\n", r.name, r.from, r.to, r.type);
                                        out.flush();
                                        
                                        PrintWriter outc = new PrintWriter(new 
                                                                       OutputStreamWriter(r.client.getOutputStream()));
                                        outc.flush();
                                        outc.printf("RESPONSE: %s,%s,%s,%s\n", lol.name, lol.from, lol.to, lol.type);
                                        outc.flush();
                                        out.close();
                                        outc.close();
                                        in.close();
                                        r.client.close();
                                        lol.client.close();
                                        
                                        clientList.remove(lol);
                                        clientList.remove(r);
                                        break;
                                    }
                                }
                            }
                        }
                    } 
                    else {
                        PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));
                        out.flush();
                        out.println("ERROR: invalid request");
                        out.flush();
                        out.close();
                        in.close();
                        client.close();
                    }
                } 
            } catch (Exception e) { 
                e.printStackTrace();
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
    
    public static boolean isValid (String s) {
        String[] locations = {"CL50", "EE", "LWSN", "PMU", "PUSH", "*"};
        String[] tokens = s.split(",");
        int count = 0;
        
        if (tokens.length != 4)
            return false;
        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < locations.length; j++) {
                if (tokens[i].equals(locations[j])) {
                    count++;
                }
            }
        }
        
        if (count != 2)
            return false;
        if (tokens[1].equals(tokens[2]))
            return false;
        if (tokens[1].equals("*"))
            return false;
        return true;
    }
}