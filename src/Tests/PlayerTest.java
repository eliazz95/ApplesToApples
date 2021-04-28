import org.junit.jupiter.api.*;
import java.util.ArrayList;
import static org.junit.jupiter.api.Assertions.*;


@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PlayerTest {

    public static Apples2ApplesServer server;
    public static ArrayList<PlayerHandler> players;

    @BeforeEach
    void startServerTest(){
        Thread serverThread1 = new Thread(() -> {
            server = new Apples2ApplesServer(1,false);
            server.greenDeck = new GreenApplesDeck();
            server.redDeck = new RedApplesDeck();
            server.addPlayers(2049, server.getNumOfOnlinePlayers());
            players = server.getPlayers();
            players.get(1).msgPlayer(server.greenDeck.drawCard());
        });
        serverThread1.start();


        /*Thread serverThread2 = new Thread(() -> {
            testPlayer = new Player("192.168.1.223", true);
        });
        serverThread2.start();*/
    }

    @Test
    @Order(1)
    void shouldGivePlayersSevenCards() throws InterruptedException{
        //Thread.sleep(2000);
        Player testPlayer = new Player("192.168.1.223", false);


        while(true){
            String serverData = testPlayer.getServerData();
            System.out.println(serverData);
            if(serverData.endsWith(".")) {
                //testPlayer.outToServer.println("Lame");
                //testPlayer.msgServer("TestPlayer");
                break;
            }
        }

        for(PlayerHandler currPlayer: players){
            assertEquals(7, currPlayer.getHandSize());
        }
    }

   @Test
   @Order(2)
   void shouldShowEveryoneAGreenApple() throws InterruptedException{
       Thread.sleep(5000);
        Player player = new Player("192.168.1.223", false);

        //String serverData = player.getServerData();
        //System.out.println(serverData);
       String serverData = null;
        for(int i=0; i<5; i++){
            serverData = player.getServerData();
            System.out.println(serverData);
        }
       assertEquals("[Absurd] - (ridiculous, senseless, foolish) ",serverData);
        /*while(true){
            String serverData = player.getServerData();
            //assertEquals("[Absurd] - (ridiculous, senseless, foolish) ",serverData);
            System.out.println(serverData);
            /*if(serverData.endsWith(":")) {
                player.msgServer("TestPlayer");
                break;
            }
        }*/

    }

}

class Server{

    public Apples2ApplesServer server;
    public static ArrayList<PlayerHandler> players;

    Server(){
        server = new Apples2ApplesServer(1,false);
        server.setupCards();
        server.addPlayers(2049, server.getNumOfOnlinePlayers());
        players = server.getPlayers();
        server.setNames();
    }
}