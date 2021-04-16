import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class GreenApplesDeckTest {

    GreenApplesDeck greenDeck = new GreenApplesDeck();

    @Test
    @DisplayName("Rule 1 | Read all green apples from file")
    void readAllGreenApples(){

        // Check that the first card from the given text document has been read
        assertEquals("[Absurd] - (ridiculous, senseless, foolish) ", greenDeck.drawCard());

        // Save the initial deck size
        int size = greenDeck.deckSize();
        String lastCard = "";

        // Loop through the deck and remove all cards except the last
        for(int i=0; i<size; i++){
            lastCard = greenDeck.drawCard();
        }

        // Check last card
        assertEquals("[Zany] - (crazy, funny, wacky) ", lastCard);
    }

    @Test
    @DisplayName("Rule 3 | Shuffle green deck")
    void shuffleRedDeck(){
        // Shuffle
        greenDeck.shuffle();

        // Check that the first card is not equal to the first card of an unshuffled deck
        assertNotEquals("[A Bad Haircut] - The perfect start to a bad hair day. ", greenDeck.drawCard());
    }

}