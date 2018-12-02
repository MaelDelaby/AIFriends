class Path implements Cloneable{ 
    private String finalCommande;
    private BoardGame boardGame;
    private HashSet<Card> cardsNeedAttack;
    private HashSet<Integer> phase4;

    //#region constructor
    public Path(String finalCommande, BoardGame boardGame, HashSet<Card> cardsNeedAttack){
        this (finalCommande, boardGame, cardsNeedAttack, new HashSet<Integer>());
    }

    private Path(String finalCommande, BoardGame boardGame, HashSet<Card> cardsNeedAttack, HashSet<Integer> phase4){
        this.finalCommande = finalCommande;
        this.boardGame = boardGame;
        this.cardsNeedAttack = cardsNeedAttack;
        this.phase4 = phase4;
    }
    //#endregion

    //#region accessor & mutator
    public void addPath(String path){
        this.finalCommande += path;
    }

    public String getFinalCommande(){
        return this.finalCommande;
    }

    public BoardGame getBoardGame(){
        return this.boardGame;
    }

    public HashSet<Card> getCardsNeedAttack(){
        return this.cardsNeedAttack;
    }

    public HashSet<Integer> getPhase4(){
        return this.phase4;
    }
    //#endregion

    @Override
    public Path clone(){
        return new Path(this.finalCommande, this.boardGame.clone(), (HashSet)this.cardsNeedAttack.clone(), (HashSet)this.phase4.clone());
    }
} 
