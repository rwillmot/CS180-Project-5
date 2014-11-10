import java.util.ArrayList;

public class RequestHandling {
    class Request {
        String name;
        String from;
        String to;
        String type;
    }
    ArrayList<Request> clientList;
    
    public static boolean isValid (String s) {
        String[] locations = {"CL50", "EE", "LWSN", "PMU", "PUSH", "*"};
        String[] tokens = s.split(",");
        int count = 0;
        
        for (int i = 1; i < 3; i++) {
            for (int j = 0; j < locations.length; j++) {
                if (tokens[j].equals(locations[i])) {
                    count++;
                }
            }
        }
        
        if (tokens.length != 4)
            return false;
        if (count != 2)
            return false;
        if (tokens[1].equals(tokens[2]))
            return false;
        if(tokens[1].equals("*"))
            return false;
        
        return true;
    }
    
    public void handleRequest (String s) {
        String[] tokens = s.split(",");
        Request lol = new Request();
        lol.name = tokens[0];
        lol.from = tokens[1];
        lol.to = tokens[2];
        lol.type = tokens[3];
        
        for (Request r : clientList) {
            if (r.from.equals(lol.from)) {
                if (r.to.equals(lol.to) || (r.to.equals("*") ^ lol.to.equals("*"))) {
                    // Match the clients
                }
            }
        }
    }
}