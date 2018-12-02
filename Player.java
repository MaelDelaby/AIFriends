class Player {
    //#region real class

    private int health;
    private int mana;
    private int nextRune;
    private int nextCardDraw;

    private int handSizeHidden;
    private HashSet<Card> hand;
    private HashSet<Card> handCharge;
    private HashSet<Card> handSpell;
    private HashSet<Card> board;
    private HashSet<Card> guardBoard;

    //#region constructor
    public Player(){
        this(30, 1, 0, 25);
    }

    public Player(int health, int mana, int handSizeHidden, int nextRune){
        this.health = health;
        this.mana = mana;
        this.handSizeHidden = handSizeHidden;
        this.nextRune = nextRune;

        this.handSizeHidden = 0;
        this.hand = new HashSet<Card>();
        this.handCharge = new HashSet<Card>();
        this.handSpell = new HashSet<Card>();
        this.board = new HashSet<Card>();
        this.guardBoard = new HashSet<Card>();
    }
    //#endregion

    //#region accessor & mutator
    public int getHealth(){
        return this.health;
    }

    private double getHealthValue(){
        return (health*health*health)/1000.0-(9.0*health*health)/100.0+(14.0*health)/5.0;
    }

    public void addHealth(int health){
        this.health += health;
        this.majRune();
    }

    public int getMana(){
        return this.mana;
    }

    public void removeMana(int mana){
        this.mana -= mana;
    }

    private void majRune(){
        if (this.health <= this.nextRune){
            ++this.nextCardDraw;
            this.nextRune -= 5;
            this.majRune();
        }
    }

    public int getHandSize(){
        int handSize = this.hand.size() + handSizeHidden;

        if (handSize > 8){
            return 8;
        } else {
            return handSize;
        }
    }

    public HashSet<Card> getHand(){
        return this.hand;
    }

    public HashSet<Card> getHandCharge(){
        return this.handCharge;
    }

    public HashSet<Card> getHandSpell(){
        return this.handSpell;
    }

    public HashSet<Card> getBoard(){
        return this.board;
    }

    public HashSet<Card> getGuardBoard(){
        return this.guardBoard;
    }
    //#endregion

    //#region action
    public void draw(){
        this.handSizeHidden += nextCardDraw;
        this.nextCardDraw = 1;
    }
    //#endregion

    //#region value
    public double getValue(){
        //Si il est mort
        if (health <= 0){
            return -999;
        }

        //Calcul enemyValue
        double value = this.getHealthValue();
        
        for (Card card : this.board){
            value += card.getValue();
        }

        for (int i = this.getHandSize() - 1; i > -1; --i){
            value += Settings.HANDVALUE[i] + Settings.VALUEOFPOSEDCARD;
        }

        return value;
    }

    //For debug
    public String getValueDetail(){
        String str = "";

        //Si il est mort
        if (health <= 0){   
            str += "ENEMY DEAD";
            return str;
        }

        //Calcul enemyValue
        double value = this.getHealthValue();

        str += " HEALTH VALUE : " + this.getHealthValue();

        str += " CARD VALUE : ";
        for (Card card : this.board){
            str += "ID : " + card.getInstanceId() + " VALUE : " + card.getDraftValue() + "; ";
        }

        double handValue = 0;

        for (int i = this.getHandSize() - 1; i > -1; --i){
            handValue += Settings.HANDVALUE[i] + Settings.VALUEOFPOSEDCARD;
        }

        str += "HAND VALUE : " + handValue;

        return str;
    }
    //#endregion

    @Override
    public Player clone(){
        Player playerCloned = new Player(this.health, this.mana, this.handSizeHidden, this.nextRune);
        for (Card card : this.board){
            playerCloned.board.add(card.clone());
        }
        for (Card card : this.hand){
            playerCloned.hand.add(card.clone());
        }
        for (Card card : playerCloned.hand){
            if (card.getType() != 0){
                playerCloned.handSpell.add(card);
            } else if (card.is(Abilitie.charge) == true){
                playerCloned.handCharge.add(card);
            }
        }
        for (Card card : playerCloned.board){
            if (card.is(Abilitie.guard)){
                playerCloned.guardBoard.add(card);
            }
        }

        return playerCloned;
    }

    //#endregion

    //#region static

    //Path for my turn
    public static HashSet<Path> paths;
    //Path for min max enemy turn (just the trade phase)
    public static HashSet<Path> enemyPaths;

    public static HashSet<Card> myDeck;
    public static HashSet<Card> enemyDeck;

    //#region constante
    public static final int BOARDMAXSIZE = 6;
    public static final int HANDMAXSIZE = 8;
    public static final int NBOFPICK = 30;
    //#endregion

    public static void main(String args[]) {
        Settings.initSettings();
        Scanner in = new Scanner(System.in);

        pickPhase(in);

        fightPhase(in);
        
        // Write an action using System.out.println()
        // To debug: System.err.println("Debug messages...");
    }

    //#region pick
    private static void pickPhase(Scanner in){
        Player.myDeck = new HashSet<Card>();
        Player.enemyDeck = new HashSet<Card>();

        for (int k = 0; k < Player.NBOFPICK; ++k) {
            for (int i = 0; i < 11; ++i) in.nextInt();

            int opponentActions = in.nextInt();
            if (in.hasNextLine()) {
                in.nextLine();
            }
            for (int i = 0; i < opponentActions; i++) {
                String cardNumberAndAction = in.nextLine();
            }

            int size = in.nextInt();
            ArrayList<Card> pickCards = new ArrayList<Card>();
            for (int i = 0; i < size; i++) {
                pickCards.add(new Card(in.nextInt(), in.nextInt() * 0 + k, in.nextInt() * 0 + i, in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), ((String)in.next()), in.nextInt(), in.nextInt(), in.nextInt()));
            }
            
            
            System.err.println("ACTUAL AVERAGE : " + Player.getDeckAverage());
            for (int i = 0; i < pickCards.size(); ++i){
                System.err.println("Card "+ (i + 1) + " : id = " + pickCards.get(i).getCardNumber() + " rank = " + Settings.CARDSLADDER[pickCards.get(i).getCardNumber()] + " real value = " + pickCards.get(i).getDraftValue());
            }

            sortCardPickValue(pickCards);

            Player.myDeck.add(pickCards.get(0));
            System.out.println("PICK " + pickCards.get(0).getLocation());
        }
    }

    public static double getDeckAverage(){
        double tot = 0;
        for (Card card : Player.myDeck){
            tot += card.getCost();
            tot += card.getCardDraw() * 1.5;
        }

        double average;
        if (Player.myDeck.isEmpty()){
            average = Settings.AVERAGE;
        } else {
            average = tot / Player.myDeck.size();
        }

        return average;
    }

    private static void sortCardPickValue(ArrayList<Card> cards){
        Collections.sort(cards, new Comparator<Card>() {
            @Override
            public int compare(Card card1, Card card2)
            {
                return card1.getDraftValue() < card2.getDraftValue() ? -1 : 1;
            }
        });
    }
    //#endregion

    //#region fight
    private static void fightPhase(Scanner in){

        while (true) {
            BoardGame boardGame = Player.getNewBoardGameInitialized(in);
            
            System.out.println(boardGame.getBestPlay() + "PASS");
        }
    }

    private static BoardGame getNewBoardGameInitialized(Scanner in){
        BoardGame boardGame = new BoardGame();

        //#region my informations
        boardGame.getMe().health = in.nextInt();
        boardGame.getMe().mana = in.nextInt();
        in.nextInt();//Deck size
        boardGame.getMe().nextRune = in.nextInt();
        in.nextInt();//Player draw
        //#endregion

        //#region enemy information
        boardGame.getEnemy().health = in.nextInt();
        boardGame.getEnemy().mana = in.nextInt();
        in.nextInt();//Deck size
        boardGame.getEnemy().nextRune = in.nextInt();
        boardGame.getEnemy().nextCardDraw = in.nextInt();
        boardGame.getEnemy().handSizeHidden = in.nextInt();
        //#endregion
    
        //#region opponent play (useless)
        int opponentActions = in.nextInt();
        if (in.hasNextLine()) {
            in.nextLine();
        }
        for (int i = 0; i < opponentActions; i++) {
            String cardNumberAndAction = in.nextLine();
        }
        //#endregion
    
        //#region board & hand filling
        int nbOfCard = in.nextInt();
        for (int i = 0; i < nbOfCard; i++) {
            boardGame.addCard(new Card(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), ((String)in.next()), in.nextInt(), in.nextInt(), in.nextInt()));
        }
        //#endregion

        return boardGame;
    }
    //#endregion

    //#endregion
}
