import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RedApplesDeckTest {

    RedApplesDeck redDeck = new RedApplesDeck(false, false);

    @Test
    @DisplayName("Rule 2 | Read all read apples from file")
    void readAllRedApples(){

        // Check that the first card from the given text document has been read
        assertEquals("[A Bad Haircut] - The perfect start to a bad hair day. ", redDeck.drawCard());

        // Save the initial deck size
        int size = redDeck.deckSize();
        String lastCard = "";

        // Loop through the deck and remove all cards except the last
        for(int i=0; i<size; i++){
            lastCard = redDeck.drawCard();
        }

        // Check last card
        assertEquals("[Zucchini] - A squashed vegetable. ", lastCard);
    }

    @Test
    @DisplayName("Rule 3 | Shuffle red deck")
    void shuffleRedDeck(){
        // Shuffle
        redDeck.shuffle();

        // Check that the first card is not equal to the first card of an unshuffled deck
        assertNotEquals("[A Bad Haircut] - The perfect start to a bad hair day. ", redDeck.drawCard());
    }

}