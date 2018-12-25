class Path implements Cloneable{ 
    private String finalCommande;
    private BoardGame boardGame;
    private HashSet<Card> cardsNeedAttack;
    private HashSet<Integer> phase4;
    private HashSet<Path> enemyPaths;

    //#region constructor
    public Path(String finalCommande, BoardGame boardGame, HashSet<Card> cardsNeedAttack){
        this (finalCommande, boardGame, cardsNeedAttack, new HashSet<Integer>());
    }

    private Path(String finalCommande, BoardGame boardGame, HashSet<Card> cardsNeedAttack, HashSet<Integer> phase4){
        this.finalCommande = finalCommande;
        this.boardGame = boardGame;
        this.cardsNeedAttack = cardsNeedAttack;
        this.phase4 = phase4;
        this.enemyPaths = new HashSet<Path>();
    }
    //#endregion

    //#region accessor & mutator
    public void addInstruction(String path){
        this.finalCommande += path;
    }

    public void addEnemyPath(Path enemyPath){
        this.enemyPaths.add(enemyPath);
    }

    public String getFinalCommande(){
        return this.finalCommande;
    }

    public BoardGame getBoardGame(){
        return this.boardGame;
    }

    public Double getBoardGameValue(){
        return this.boardGame.getValue();
    }

    public HashSet<Card> getCardsNeedAttack(){
        return this.cardsNeedAttack;
    }

    public HashSet<Integer> getPhase4(){
        return this.phase4;
    }

    public HashSet<Path> getEnemyPaths(){
        return this.enemyPaths;
    }
    //#endregion

    @Override
    public String toString(){
        return this.finalCommande + " : " + this.boardGame.getValue();
    }

    @Override
    public Path clone(){
        return new Path(this.finalCommande, this.boardGame.clone(), (HashSet)this.cardsNeedAttack.clone(), (HashSet)this.phase4.clone());
    }
} 
