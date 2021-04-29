import org.junit.jupiter.api.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class GameTest {

    public static Apples2ApplesServer server;
    public static ArrayList<PlayerHandler> players;

    @BeforeEach
    void startServerTest(){
        Thread serverThread = new Thread(() -> {
            server = new Apples2ApplesServer(3,false);
            server.greenDeck = new GreenApplesDeck();
            server.redDeck = new RedApplesDeck();
            server.addPlayers(2049, server.getNumOfOnlinePlayers());
            players = server.getPlayers();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
        serverThread.start();
    }

    @Test
    @Order(1)
    void shouldGivePlayersSevenCards() throws InterruptedException {

        Player testPlayer = new Player("192.168.1.223", false);
        Player testPlayer1 = new Player("192.168.1.223", false);
        Player testPlayer2 = new Player("192.168.1.223", false);

        Thread.sleep(500);
        for(PlayerHandler currPlayer: players){
            assertEquals(7, currPlayer.getHandSize());
        }
    }

}
