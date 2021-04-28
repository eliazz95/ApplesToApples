import java.io.*;
import java.net.Socket;
import java.util.ArrayList;

public class Player implements Runnable{

    private Socket aSocket;
    public PrintWriter outToServer;
    public BufferedReader inFromServer;
    int port = 2049;

    public static void main(String[] args) {
        if (args == null || args.length < 1) {
            System.out.println("An ip address need to be specified...");
            System.exit(0);
        }
        new Player(args[0], true);
    }

    Player(String ipAddress, boolean run){
        try {
            aSocket = new Socket(ipAddress, port);
            inFromServer = new BufferedReader(new InputStreamReader(aSocket.getInputStream()));
            outToServer = new PrintWriter(aSocket.getOutputStream(), true);
            if(run){
                this.run();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String takeInput() {
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

    public String getServerData(){
        String serverData = null;
        try{
            serverData = inFromServer.readLine();
        }catch(IOException e){

        }
        return serverData;
    }

    public void msgServer(String data) {
        outToServer.println(data);
    }

    @Override
    public void run() {
        String fromServer;
        try {
            while ((fromServer = getServerData()) != null) {
                if (fromServer.startsWith("end")) {
                    System.exit(0);
                }
                System.out.println(fromServer);// Print server response

                // If server sends a message ending with ":" then it expects a reply from the player
                if(fromServer.endsWith(":")){
                    String input = takeInput();

                    if(input.contains("exit")){
                        // If the player types exit then message the server "end" to end the game
                        //msgServer("end");

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
