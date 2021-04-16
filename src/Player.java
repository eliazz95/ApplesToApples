import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Player implements Runnable{

    private Socket aSocket;
    private PrintWriter outToServer;
    private BufferedReader inFromServer;
    int port = 2049;

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("An ip address need to be specified...");
            System.exit(0);
        }
        new Player(args[0]);
    }

    Player(String ipAddress){
        try {
            aSocket = new Socket(ipAddress, port);
            inFromServer = new BufferedReader(new InputStreamReader(aSocket.getInputStream()));
            outToServer = new PrintWriter(aSocket.getOutputStream(), true);
            this.run();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    private String takeInput() {
        String input;
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
            input = br.readLine();
        } catch (IOException e) {
            System.out.println("Invalid input, try again...");
            input = takeInput();
            e.printStackTrace();
        }
        return input;
    }

    private void msgServer(String data) {
        outToServer.println(data);
    }

    @Override
    public void run() {
        String serverData;
        try {
            while ((serverData = inFromServer.readLine()) != null) {
                if (serverData.startsWith("end")) {
                    System.exit(0);
                }
                System.out.println(serverData);// Server response

                if(serverData.endsWith(":")){
                    String input = takeInput();
                    if(input.contains("exit")){
                        msgServer("end");
                        System.out.println("Goodbye!");
                        System.exit(0);
                    }
                    msgServer(input);
                }

            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
