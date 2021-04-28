import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class Apples2ApplesServer{

    private ArrayList<PlayerHandler> players = new ArrayList<>();
    List<String> rndNames = new ArrayList<>(Arrays.asList("Isabelle", "Sharon", "Aminah", "Georgina", "Aidan", "Declan", "Oscar", "Raphael", "Robbie ", "Leroy"));

    public GreenApplesDeck greenDeck;
    public RedApplesDeck redDeck;
    private ArrayList<PlayedApple> playedApples = new ArrayList<>();

    private int judge = -1;
    private final int numOfOnlinePlayers;
    private String currGreenApple;
    private Random rnd;

    // GAME SETTINGS
    private boolean voting = false;
    private boolean wildRedApples = false;
    private boolean applesAndPears = false;     // Only works if voting is enabled

    public static void main(String[] args){
        if (args == null || args.length < 1 || args[0].equals("0")) {
            System.out.println("You need to specify the number of online players and it must be at least 1...");
            System.exit(0);
        }

        new Apples2ApplesServer(Integer.parseInt(args[0]), true);
    }

    Apples2ApplesServer(int numOfOnlinePlayers, boolean startGame){
        this.numOfOnlinePlayers = numOfOnlinePlayers;

        if(startGame){
            newGame();
        }
    }

    public void newGame(){
        // Setup game
        setupCards();
        addPlayers(2049, numOfOnlinePlayers);

        msgAllPlayers("Connected to server!\n");
        msgAllPlayers("Type 'exit' at any time to quit the game :)\n");

        msgAllPlayers("Setting up game...");
        sleep(1000);
        msgAllPlayers("Shuffling decks...");
        sleep(1000);
        msgAllPlayers("Done!\n");

        setNames();
        // Set option for voting
        if(!voting){
            newJudge();
        }

        // Setup is done, start the game
        try{
            while(true){

                printNewRound();

                /* ------------- PHASE x ------------- */

                if(!voting){
                    replaceJudgeCards();
                }


                /* ------------- PHASE A ------------- */

                // Draw a green card and show it to all the players
                msgAllPlayers("\nPicking a card from the green apple deck...\n\n");
                sleep(2000);
                msgAllPlayers("--------GREEN APPLE--------\n");
                currGreenApple = greenDeck.drawCard();
                msgAllPlayers(currGreenApple + "\n");
                sleep(2000);



                /* ------------- PHASE B ------------- */

                // Show every player except the judge their hand
                for(int i=0; i<players.size(); i++){
                    if(i != judge){
                        players.get(i).msgPlayer("\n--------YOUR HAND--------\n");
                        players.get(i).showHand();
                        players.get(i).msgPlayer("\n\nChoose a card from your hand [0-6]:");
                    }else{
                        players.get(i).msgPlayer("\nWaiting for every player to play their red apple card...\n\n");
                    }
                }

                // Let all the players (except the judge if voting is disabled) play their red apple and then shuffle the cards
                playCards();

                // Show every player the played red apple cards
                msgAllPlayers("\n--------PLAYED APPLE CARDS--------\n");
                for(int i=0; i<playedApples.size(); i++){
                    msgAllPlayers("\t["+i+"] " + playedApples.get(i).redApple);
                }



                /* ------------- PHASE C ------------- */

                // Judge or vote which apple should win
                int winChoice;
                if(voting){
                    winChoice = vote();
                }else {
                    winChoice = judge();
                }


                // Show every player who won and give the winner a point
                String winner = playedApples.get(winChoice).playerName;
                msgAllPlayers("\n-------- WINNER OF THIS ROUND --------\n");
                msgAllPlayers(winner + " won this round with --> " + playedApples.get(winChoice).redApple);
                addPointTo(winner, currGreenApple);
                sleep(2000);



                /* ------------- PHASE D ------------- */

                // Clear the played apples and refill every players hand with new red apples
                clearPlayedApples();
                refillPlayersHand();

                showScoreboard();
                sleep(3000);

                // Show all players the winner if there is one and then end the game
                if(getWinner() > -1){
                    msgAllPlayers("-------- GAME OVER --------\n");
                    msgAllPlayers("THE WINNER IS...");
                    msgAllPlayers(players.get(getWinner()).getName());
                    // Message all players "end" to exit their running task
                    msgAllPlayers("end");
                    break;
                }

                if(!voting){
                    newJudge();
                }

            }
        }catch(Exception e){
            endGame();
        }
    }

    /* ------------- GETTERS ------------- */

    public ArrayList<PlayerHandler> getPlayers(){
        return players;
    }

    public ArrayList<PlayedApple> getPlayedApples(){ return playedApples; }

    public int getJudge(){
        return judge;
    }

    public int getNumOfOnlinePlayers(){
        return numOfOnlinePlayers;
    }

    public boolean getVoting(){
        return voting;
    }

    public String getCurrGreenApple(){
        return currGreenApple;
    }

    private String getRandomName(){
        rnd = ThreadLocalRandom.current();
        return rndNames.remove(rnd.nextInt(rndNames.size()));
    }


    /* ------------- GAME SETUP ------------- */

    public void setupCards(){
        greenDeck = new GreenApplesDeck();
        if(!voting){
            applesAndPears = false;
        }
        redDeck = new RedApplesDeck(wildRedApples, applesAndPears);
        greenDeck.shuffle();
        redDeck.shuffle();
    }

    public void addPlayers(int port, int numOfOnlinePlayers){
        ServerSocket aSocket = null;
        try {
            // Open a new socket
            aSocket = new ServerSocket(port);
            System.out.println("[SERVER] Game started on port " + port);
            System.out.println("[SERVER] Expecting " + numOfOnlinePlayers + " online players to join..");

            for(int i=0; i<numOfOnlinePlayers; i++) {
                // Wait for a player to connect
                System.out.println("[SERVER] Waiting for a client connection...");
                Socket playerSocket = aSocket.accept();
                System.out.println("[SERVER] Connected to client " + i + "!");

                // Create a new player handler for the connected player
                PlayerHandler player = new PlayerHandler(playerSocket, "Player " + i, redDeck.getHand(true), false);
                // Add to player list
                players.add(player);
            }
        } catch (Exception e){
            System.out.println(e.getMessage());
        }

        // Add bots if minimum player is less than 4
        if(numOfOnlinePlayers < 3){
            for(int j=numOfOnlinePlayers; j<=3; j++){
                PlayerHandler bot = new PlayerHandler("Bot " + getRandomName(), redDeck.getHand(false), true);
                players.add(bot);
            }
        }
    }

    public void setNames() {
        msgAllPlayers("Type in your name or just press enter to get a random name:");

        // Create a threadpool so all players can chose a name simultaneously
        ExecutorService threadpool = Executors.newFixedThreadPool(numOfOnlinePlayers);

        for(int i=0; i<players.size(); i++) {
            PlayerHandler currentPlayer = players.get(i);

            Runnable task = new Runnable() {
                @Override
                public void run() {
                    if(!currentPlayer.isBot){
                        String name = currentPlayer.getPlayerInput();
                        if(name.equals("")){
                            name = getRandomName();
                        }
                        // Check if name is already taken
                        int condition = 0;
                        do {
                            condition = 0;
                            for (PlayerHandler player : players) {
                                if (player.getName().equals(name)) {
                                    currentPlayer.msgPlayer("Name is already taken, chose another one:");
                                    name = currentPlayer.getPlayerInput();
                                    condition = 1;
                                }
                            }
                        } while (condition != 0);

                        currentPlayer.setName(name);
                        currentPlayer.msgPlayer("Welcome " + name + "!");
                        currentPlayer.msgPlayer("\nWaiting for other players...\n\n");
                    }
                }
            };
            threadpool.execute(task);
        }
        threadpool.shutdown();

        // Wait for all the answers to come in
        while(!threadpool.isTerminated()) {
            sleep(100);
        }
    }


    /* ------------- OTHER ------------- */

    public void endGame(){
        msgAllPlayers("end");
        System.exit(0);
    }

    private void sleep(int milliSec){
        // Made this to avoid having to throw exceptions everywhere
        try{
            Thread.sleep(milliSec);
        }catch (InterruptedException e){}
    }

    public void msgAllPlayers(String msg){
        for(PlayerHandler player: players){
            if(!player.isBot){
                player.msgPlayer(msg);
            }
        }
    }


    /* ------------- GAMEPLAY ------------- */

    public void newJudge(){
        if(judge==-1){
            // Randomise which player starts as judge the first round
            rnd = ThreadLocalRandom.current();
            judge = rnd.nextInt(players.size());
        }else{
            // Next player in list becomes the judge in the other rounds
            judge=((judge==(players.size()-1))?0:(judge+1));
        }
    }

    private void printNewRound(){
        msgAllPlayers("*****************************************************");
        for(int i=0; i<players.size(); i++){
            if(i == judge) {
                players.get(i).msgPlayer("**           NEW ROUND - YOU ARE THE JUDGE         **");
            } else {
                players.get(i).msgPlayer("**                    NEW ROUND                    **");
            }
        }
        msgAllPlayers("*****************************************************\n");
    }

    private void replaceJudgeCards(){
        // First tell all players except the judge to wait
        for(int i=0; i<players.size(); i++){
            if(i != judge){
                players.get(i).msgPlayer("\nWaiting for the judge to replace some cards in their hand...\n");
            }
        }
        // If not a bot then ask if yhe judge would like to replace any cards
        if(!players.get(judge).isBot){
            players.get(judge).msgPlayer("\nYou are the judge this round and you can choose to replace cards in your hand!\n");
            players.get(judge).msgPlayer("Would you like to replace cards in your hand? [No=0][Yes=1]:");
            int yesOrNo = players.get(judge).getPlayerChoice(1);

            // If yes, then get a string of numbers as input
            if(yesOrNo == 1){
                players.get(judge).msgPlayer("");
                players.get(judge).showHand();
                players.get(judge).msgPlayer("\nType in every card you want to replace as a string of numbers:");
                String cardIndexes = players.get(judge).getPlayerInput();

                // Check if input is valid or not
                while(true){
                    boolean wrongInput = false;
                    for(int i = 0; i < cardIndexes.length(); i++) {
                        int digit = Character.getNumericValue(cardIndexes.charAt(i));
                        if(digit<0 || digit>6){
                            wrongInput = true;
                        }
                    }
                    if(wrongInput || cardIndexes.equals("") || cardIndexes.length() > 7){
                        players.get(judge).msgPlayer("\nNot a valid option, try again:");
                        cardIndexes = players.get(judge).getPlayerInput();
                    }else{
                        break;
                    }
                }

                // Loop through every digit and replace with a new card
                for (int i = 0; i < cardIndexes.length(); ++i) {
                    int digit = Character.getNumericValue(cardIndexes.charAt(i));
                    players.get(judge).replaceACard(digit, redDeck.drawCard());
                }

                players.get(judge).msgPlayer("\nDone! Your new cards are: ");
                players.get(judge).showHand();
            }
        }
    }

    public int judge(){
        // First tell all players except the judge to wait for a decision
        for(int i=0; i<players.size(); i++){
            if(i != judge){
                players.get(i).msgPlayer("\n\nWaiting for the judge to make a decision...");
            }
        }
        // Then let the judge make a choice
        players.get(judge).msgPlayer("\n\nChoose which red apple card win [0-"+(players.size()-2)+"]:");
        int winChoice = players.get(judge).getPlayerChoice(players.size()-2);

        return winChoice;
    }

    private int vote(){

        msgAllPlayers("\n\nVote for which card you think should win [0-"+(players.size()-1)+"]:");

        // Create a threadpool so all players can pick their red apple at once
        ExecutorService threadpool = Executors.newFixedThreadPool(players.size());

        int[] votes = new int[players.size()];

        for(int i=0; i<players.size(); i++) {
            PlayerHandler currentPlayer = players.get(i);
            int index = i;
            //Make sure every player can answer at the same time
            Runnable task = new Runnable() {
                @Override
                public void run() {
                    String currPlayerName = currentPlayer.getName();
                    int vote = currentPlayer.getPlayerChoice(players.size()-1);

                    while(currPlayerName.equals(playedApples.get(vote).playerName)){
                        currentPlayer.msgPlayer("You can't vote for your own card, try again:");
                        vote = currentPlayer.getPlayerChoice(players.size()-1);
                    }
                    votes[index] = vote;
                    currentPlayer.msgPlayer("\nWaiting for other players to vote...");
                }
            };
            threadpool.execute(task);
        }
        threadpool.shutdown();

        // Wait for all the answers to come in
        while(!threadpool.isTerminated()) {
            sleep(100);
        }

        // Checks and returns which card got the most votes
        return getMostVoted(votes);
    }

    private int getMostVoted(int[] votes){

        Map<Integer,Integer> voteMap = new HashMap<Integer, Integer>();

        // Put all the votes in Map with the card index
        // as the key and the number of votes of the card
        // as the value
        for(int i=0; i< votes.length; i++){

            // Add 1 to the value for every occurrence of each card
            voteMap.put(votes[i], voteMap.getOrDefault(votes[i], 0) + 1);
        }

        // Create a list for all the elements in the HashMap
        List<Map.Entry<Integer,Integer>> voteList = new ArrayList<Map.Entry<Integer,Integer>>(voteMap.entrySet());

        // Sort list with most votes first
        Collections.sort(voteList, new Comparator<Map.Entry<Integer, Integer>>() {
            @Override
            public int compare(Map.Entry<Integer, Integer> o1, Map.Entry<Integer, Integer> o2) {
                if (o1.getValue().equals(o2.getValue()))
                    return o2.getKey() - o1.getKey();
                else
                    return o2.getValue() - o1.getValue();
            }
        });

        // Check if there is two or more cards that has equal number of votes
        int equalVotes = 0;
        for (int j=0; j<voteList.size()-1; j++){
            if(voteList.get(0).getValue().equals(voteList.get(j+1).getValue())){
                equalVotes++;
            }
        }

        // If two or more card has equal number of votes then randomise a winner
        if(equalVotes>0){
            equalVotes = rnd.nextInt(equalVotes);
        }

        // Return the key, i.e. the card with most votes
        return voteList.get(equalVotes).getKey();
    }

    public void playCards(){

        int nThreads = (voting?players.size():players.size()-1);
        //System.out.println(nThreads);

        // Create a threadpool so all players can pick their red apple at once
        ExecutorService threadpool = Executors.newFixedThreadPool(nThreads);

        for(int i=0; i<players.size(); i++) {
            // Bots choices sometimes doesn't get stored because the process goes too fast
            // so sleep is needed to ensure that every choice gets stored
            sleep(50);

            if(i!=judge) {
                PlayerHandler currentPlayer = players.get(i);
                int answerBound = players.get(i).getHandSize();
                //Make sure every player can answer at the same time
                Runnable task = new Runnable() {
                    @Override
                    public void run() {
                        int choice = currentPlayer.getPlayerChoice(answerBound);
                        choice = checkCardVariation(currentPlayer, choice);
                        PlayedApple nameAndCard = new PlayedApple(currentPlayer.getName(), currentPlayer.getCardFromHand(choice));
                        playedApples.add(nameAndCard);
                        currentPlayer.removeFromHand(choice);
                        currentPlayer.msgPlayer("\nWaiting for other players to make their choice...");
                    }
                };
                threadpool.execute(task);
            }
        }
        threadpool.shutdown();

        // Wait for all the answers to come in
        while(!threadpool.isTerminated()) {
            sleep(100);
        }
        // Shuffle played cards
        rnd = ThreadLocalRandom.current();
        Collections.shuffle(playedApples, rnd);
    }

    public void addPointTo(String name, String greenApple){
        for(PlayerHandler player: players){
            if(player.getName().equals(name)){
                player.addPoint(greenApple);
            }
        }
    }

    public void clearPlayedApples(){
        playedApples.clear();
    }

    public void refillPlayersHand(){
        for(PlayerHandler player: players){
            for(int i=player.getHandSize(); i<7; i++){
                player.addRedApple(redDeck.drawCard());
            }
        }
    }

    public void showScoreboard(){
        msgAllPlayers("\n\n--------SCOREBOARD--------\n");
        for(PlayerHandler player: players){
            msgAllPlayers(player.getPoints() + (player.getPoints()>1?" points":" point\t") + " | \t" + player.getName());
        }
        msgAllPlayers("");
        msgAllPlayers("");
    }

    private int checkCardVariation(PlayerHandler player, int cardChoice){
        String card = player.getCardFromHand(cardChoice);
        int newChoice = cardChoice;

        switch(card){
            case "[Wildcard] - Create your own card!":
                // Checks if a wildcard has been played and asks the player of a new name and description
                player.msgPlayer("Enter a name for your card:");
                String newCard = "["+player.getPlayerInput()+"] - ";

                player.msgPlayer("Good choice! Now enter a description:");
                newCard += player.getPlayerInput();

                player.msgPlayer("\nYour new card --> " + newCard);
                player.replaceACard(cardChoice, newCard);
                break;

            case "[Apples and Pears] - Play this card together with a red card to have the green card replaced.":
                currGreenApple = greenDeck.drawCard();
                player.msgPlayer("\nChose another red apple that you want to play:");
                newChoice = player.getPlayerChoice(5);
                newChoice = checkCardVariation(player, newChoice);
                break;
        }

        return newChoice;
    }

    private int getWinner(){
        // As the name says, it checks if there is a winner by checking the points
        int playerSize = players.size();
        int pointsToWin = 0;

        // Check points to win depending on number of players
        if (playerSize < 5) {
            pointsToWin = 1;
        } else if (playerSize < 6) {
            pointsToWin = 7;
        } else if (playerSize < 7) {
            pointsToWin = 6;
        } else if (playerSize < 8) {
            pointsToWin = 5;
        } else {
            pointsToWin = 4;
        }

        int winner = -1;
        for(int i=0; i<players.size(); i++){
            if(players.get(i).getPoints() == pointsToWin){
                winner = i;
            }
        }
        return winner;
    }

    public class PlayedApple{
        public String playerName;
        public String redApple;

        public PlayedApple(String playerName, String redApple){
            this.playerName = playerName;
            this.redApple = redApple;
        }
    }
}
