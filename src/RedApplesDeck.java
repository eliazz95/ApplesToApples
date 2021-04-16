import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

public class RedApplesDeck {

    private ArrayList<String> redApples;
    private Random rnd;

    public RedApplesDeck(boolean wildCards, boolean applesAndPears){
        initDeck();
        if(wildCards){ addCardVariation("[Wildcard] - Create your own card!", 200);}
        if(applesAndPears){ addCardVariation("[Apples and Pears] - Play this card together with a red card to have the green card replaced.", 200);}
    }

    private void initDeck(){
        try{
            redApples =  new ArrayList<String>(Files.readAllLines(Paths.get("", "C:\\Users\\Jesus\\IdeaProjects\\Apples2Apples\\src\\redApples.txt"), StandardCharsets.ISO_8859_1));
        }
        catch (IOException err){
            System.out.println(err.getMessage());
        }
    }

    private void addCardVariation(String cardName, int numOfCards){
        for(int i=0; i<numOfCards; i++){
            int rndInt = ThreadLocalRandom.current().nextInt(0, redApples.size());
            redApples.add(rndInt, cardName);
        }
    }

    public void shuffle(){
        rnd = ThreadLocalRandom.current();
        Collections.shuffle(redApples, rnd);
    }

    public String drawCard(){
        checkIfEmpty();
        return redApples.remove(0);
    }

    public ArrayList<String> getHand(boolean wildcards){
        // Returns an arraylist of 7 cards or with other words a hand
        checkIfEmpty();
        ArrayList<String> handArray = new ArrayList<String>();
        for (int i = 0; i < 7; i++) {
            String card = drawCard();
            while(!wildcards && card.equals("[Wildcard] - Create your own card!")){
                card = drawCard();
            }
            handArray.add(card);
        }
        return handArray;
    }

    private void checkIfEmpty(){
        if(redApples.isEmpty()){
            initDeck();
            shuffle();
        }
    }

    public int deckSize(){
        return redApples.size();
    }
}
