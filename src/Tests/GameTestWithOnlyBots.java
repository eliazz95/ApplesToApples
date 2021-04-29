import org.junit.jupiter.api.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;



@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameTestWithOnlyBots {

    private static Apples2ApplesServer server;
    private static ArrayList<PlayerHandler> players;
    private static ArrayList<Apples2ApplesServer.PlayedApple> playedApples;

    @BeforeAll
    public static void init(){
        server = new Apples2ApplesServer(0, false);
        server.setupCards();
    }

    @Test
    @Order(1)
    @DisplayName("Rule 1 | Read all green apples from file")
    void greenCardsShouldBeReadFromFile(){
        int deckSize = server.greenDeck.deckSize();
        assertEquals(614, deckSize);
    }

    @Test
    @Order(2)
    @DisplayName("Rule 2 | Read all red apples from file")
    void redCardsShouldBeReadFromFile(){
        int deckSize = server.redDeck.deckSize();
        assertEquals(1826, deckSize);
    }

    @Test
    @Order(3)
    @DisplayName("Rule 3 | Shuffle both decks")
    void deckShouldBeShuffled(){
        GreenApplesDeck greenDeck = new GreenApplesDeck();
        RedApplesDeck redDeck = new RedApplesDeck();

        // Create new unshuffled decks
        ArrayList<String> greenApples = greenDeck.getDeck();
        ArrayList<String> redApples = redDeck.getDeck();

        // Compare new unshuffled decks with the shuffled decks created on the server
        boolean greenApplesShuffled = greenApples.equals(server.greenDeck.getDeck());
        boolean redApplesShuffled = redApples.equals(server.redDeck.getDeck());

        assertFalse(greenApplesShuffled);
        assertFalse(redApplesShuffled);
    }

    @Test
    @Order(4)
    @DisplayName("Rule 4 | Deal seven cards to each player")
    void playersShouldGetSevenCardsEach(){
        // Add only bots
        server.addPlayers(2050, server.getNumOfOnlinePlayers());
        players = server.getPlayers();
        // Check the size of every players hand
        for(PlayerHandler player: players){
            assertEquals(7, player.getHandSize());
        }
    }

    @Test
    @Order(5)
    @DisplayName("Rule 5 | Randomise which player starts being the judge")
    void choseRandomJudge(){
        // Not actually gonna test the randomness but instead just gonna
        // check if a judge is chosen among the number of players
        assertEquals(-1, server.getJudge());
        server.newJudge();
        int currJudge = server.getJudge();
        assertTrue(currJudge < players.size());
        assertTrue(currJudge >= 0);
    }


    @Test
    @Order(6)
    @DisplayName("Rule 7 | Randomise which player starts being the judge")
    void playersShouldPlayACardEach(){
        // Let every player play a red apple
        server.playCards();
        playedApples = server.getPlayedApples();

        // Check that the played apples equal the number of players minus the judge
        assertEquals(3, playedApples.size());
    }

    @Test
    @Order(7)
    @DisplayName("Rule 8 | Randomise played apples")
    void playedApplesShouldBeShuffled(){
        ArrayList<String> playerNames = new ArrayList<>();
        ArrayList<String> playedAppleNames = new ArrayList<>();

        // Add names in list of players in an arraylist
        for(int i=0; i<players.size(); i++){
            if(i != server.getJudge()){
                playerNames.add(players.get(i).getName());
            }
        }

        // Add names of the played apples in arraylist
        for(Apples2ApplesServer.PlayedApple element: playedApples){
            playedAppleNames.add(element.playerName);
        }

        // Compare the arraylists to check that they are not the same
        boolean arrListEqual = playerNames.equals(playedAppleNames);
        assertFalse(arrListEqual);
    }

    @Test
    @Order(8)
    @DisplayName("Rule 10 | Judge selects winner and winner gets a point")
    void pointShouldBeGivenToTheRoundWinner(){
        // Let the judge select a winning apple
        int winner = server.judge();
        String winnerName = playedApples.get(winner).playerName;

        // Add point to winner
        server.addPointTo(winnerName, server.getCurrGreenApple());

        // Check that the winner actually got a point
        for(PlayerHandler player: players){
            if(player.getName().equals(winnerName)){
                assertEquals(1, player.getPoints());
            }
        }
    }

    @Test
    @Order(9)
    @DisplayName("Rule 11 | All submitted red apples are discarded")
    void shouldDiscardPlayedCards(){
        // Check that there are played apples
        playedApples = server.getPlayedApples();
        assertEquals(3, playedApples.size());

        // Then discard all the played apples and check again
        server.clearPlayedApples();
        playedApples = server.getPlayedApples();
        assertEquals(0,playedApples.size());
    }

    @Test
    @Order(10)
    @DisplayName("Rule 12 | All players are given new red apples until they have 7 red apples")
    void shouldRefillPlayersHand(){
        // Refill hands and check that each player has seven cards
        server.refillPlayersHand();
        players = server.getPlayers();
        for(PlayerHandler player: players){
            assertEquals(7, player.getHandSize());
        }

        // Play some cards to remove from players hand and check refill again
        server.playCards();
        server.playCards();
        server.playCards();
        server.refillPlayersHand();
        players = server.getPlayers();
        for(PlayerHandler player: players){
            assertEquals(7, player.getHandSize());
        }
    }

    @Test
    @Order(11)
    @DisplayName("Rule 13 | The next player in the list becomes the judge")
    void nextPlayerInListShouldBeJudge(){
        int currJudge = server.getJudge();
        server.newJudge();
        int newJudge = server.getJudge();

        // Check if the current judge is the last player in list or not
        if(currJudge == players.size()-1){
            assertEquals(0, newJudge);
        }else{
            assertEquals(currJudge+1, newJudge);
        }
    }

    @Test
    @Order(12)
    @DisplayName("Rule 14 | Keep score by keeping the green apples you've won")
    void shouldGiveGreenApplePointsToPlayers(){
        for(PlayerHandler player: players){
            player.removeAllPoints();

            String greenApple = server.greenDeck.drawCard();
            player.addPoint(greenApple);

            ArrayList<String> greenApplePoints = player.getGreenApples();

            // Check that each player has got 1 point and check that the green apple matches the drawn card from deck
            assertEquals(1, player.getPoints());
            assertEquals(greenApple, greenApplePoints.get(0));
        }
    }

    @Test
    @Order(13)
    @DisplayName("Rule 15 | Check if there is a winner")
    void shouldReturnAWinner(){
        // 4 players - 8 apples to win
        // 5 players - 7 apples to win
        // 6 players - 6 apples to win
        // 7 players - 5 apples to win
        // 8+ players - 4 apples to win
        for(int i=0; i<4; i++){
            players.get(0).removeAllPoints();
            // Add points just below the win limit to the first player in list
            for(int j=0; j<(7-i); j++){
                players.get(0).addPoint(server.greenDeck.drawCard());
            }
            // Should return -1 as there isn't a winner yet
            assertEquals(-1, server.getWinner());

            // Add another point to the first player
            players.get(0).addPoint(server.greenDeck.drawCard());

            // Should return the first player in list as the winner
            assertEquals(0, server.getWinner());

            // Add another player to check the next win limit
            PlayerHandler bot = new PlayerHandler("Bot " + server.getRandomName(), server.redDeck.getHand(false), true);
            players.add(bot);
        }

    }

}

