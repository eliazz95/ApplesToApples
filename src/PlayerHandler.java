import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class PlayerHandler {

    private String name;
    public boolean isBot;
    private ArrayList<String> hand;
    private ArrayList<String> greenApples = new ArrayList<>();

    private Random rnd;
    private Socket socket;
    private BufferedReader inFromClient;
    private PrintWriter outToClient;

    public PlayerHandler(String name, ArrayList<String> hand, boolean isBot){
        this.name = name;
        this.hand = hand;
        this.isBot = isBot;
    }

    public PlayerHandler(Socket clientSocket, String name, ArrayList<String> hand, boolean isBot){
        try {
            this.name = name;
            this.hand = hand;
            this.isBot = isBot;

            this.socket = clientSocket;
            inFromClient = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outToClient = new PrintWriter(socket.getOutputStream());
            sendWelcome();
        } catch (Exception ex) {
        }
    }

    public String getName(){
        return name;
    }

    public int getPoints(){
        return greenApples.size();
    }

    public void removeAllPoints(){
        greenApples.clear();
    }

    public ArrayList<String> getGreenApples(){
        return greenApples;
    }

    public int getHandSize(){
        return hand.size();
    }

    public String getCardFromHand(int cardIndex){
        return hand.get(cardIndex);
    }

    public void setName(String newName){
        this.name = newName;
    }

    public void addPoint(String greenApple){
        greenApples.add(greenApple);
    }

    public void addRedApple(String redApple){
        hand.add(redApple);
    }

    public void removeFromHand(int cardIndex){
        hand.remove(cardIndex);
    }

    public void replaceACard(int cardIndex, String newCard){
        hand.set(cardIndex, newCard);
    }

    private void sendWelcome(){
        outToClient.println("*****************************************************");
        outToClient.println("**          LET'S PLAY APPLES TO APPLES!           **");
        outToClient.println("*****************************************************");
        outToClient.println("\nConnecting to server...");
        outToClient.flush();
    }

    public void showHand(){
        for(int i=0; i<7;i++){
            msgPlayer("["+i+"]   " + hand.get(i));
        }
    }

    public void msgPlayer(String msg){
        if(!isBot){
            outToClient.println(msg);
            outToClient.flush();
        }
    }

    public String getPlayerInput(){
        String input = null;
        try {
            input = inFromClient.readLine();
        } catch(Exception e){
            System.err.println("Exception in playerHandler | getPlayerInput()");
            System.err.println(Arrays.toString(e.getStackTrace()));
        }
        return input;
    }

    public int getPlayerChoice(int answerBound){
        int choice = -1;
        if(isBot){
            rnd = ThreadLocalRandom.current();
            choice = rnd.nextInt(answerBound);
            return choice;
        }

        String input = getPlayerInput();
        choice = Integer.parseInt(input);

        if(choice<0 || choice>answerBound){
            msgPlayer("That is not a valid option, try again:");
            choice = getPlayerChoice(answerBound);
        }
        return choice;
    }

}