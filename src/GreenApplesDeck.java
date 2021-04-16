import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class GreenApplesDeck {
    private ArrayList<String> greenApples;
    private Random rnd;

    public GreenApplesDeck() {
        initDeck();
    }

    private void initDeck() {
        try{
            greenApples =  new ArrayList<String>(Files.readAllLines(Paths.get("", "C:\\Users\\Jesus\\IdeaProjects\\Apples2Apples\\src\\greenApples.txt"), StandardCharsets.ISO_8859_1));
        }
        catch (IOException err){
            System.out.println(err.getMessage());
        }
    }

    private void addCardVariation(String cardName, int numOfCards){
        for(int i=0; i<numOfCards; i++){
            int rndInt = ThreadLocalRandom.current().nextInt(0, greenApples.size());
            greenApples.add(rndInt, cardName);
        }
    }

    public void shuffle(){
        rnd = ThreadLocalRandom.current();
        Collections.shuffle(greenApples, rnd);
    }

    public String drawCard() {
        if(greenApples.isEmpty()){
            initDeck();
            shuffle();
        }
        return greenApples.remove(0);
    }

    public int deckSize(){
        return greenApples.size();
    }

}