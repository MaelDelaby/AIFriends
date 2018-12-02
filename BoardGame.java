class BoardGame implements Cloneable{
    private Player me;
    private Player enemy;

    public BoardGame(){
        this.me = new Player();
        this.enemy = new Player();
    }

    //#region accessor & mutator
    public Player getMe(){
        return this.me;
    }

    public Player getEnemy(){
        return this.enemy;
    }
    //#endregion

    public void addCard(Card card){
        switch (card.getLocation()){
            case 0 :{
                this.me.getHand().add(card);
                if (card.getType() != 0){
                    this.me.getHandSpell().add(card);
                } else if (card.is(Abilitie.charge)){
                    this.me.getHandCharge().add(card);
                }
                break;
            }
            case 1 :{           
                this.me.getBoard().add(card);
                if (card.is(Abilitie.guard)){
                    this.me.getGuardBoard().add(card);
                }
                break;
            }
            case -1 :{
                this.enemy.getBoard().add(card);
                if (card.is(Abilitie.guard)){
                    this.enemy.getGuardBoard().add(card);
                }
                break;
            }
        }
    }
    
 
    //#region differents AI game phase
    private void allFace(){
        BoardGame boardGame = this.clone();
        String finalCommande = "";
        if (boardGame.enemy.getGuardBoard().isEmpty()){
            for (Card card : boardGame.me.getBoard()){
                boardGame.enemy.addHealth(-card.getAttack());
                finalCommande += "ATTACK " + card.getInstanceId() + " -1;";
            }
        }

        Player.paths.add(new Path(finalCommande, boardGame, new HashSet<Card>()));
    }

    //Summon charge creature
    private void fillPathsPhase1(Path path){
        for (Card card : path.getBoardGame().me.getHandCharge()){
            //Si on pas assez de mana
            if (path.getBoardGame().me.getMana() - card.getCost() < 0){
                continue;
            }

            Path nextPath = path.clone();

            Card myCardCloned = null;

            for (Card cardCloned : nextPath.getBoardGame().me.getHandCharge()){
                if (card.equals(cardCloned)){
                    myCardCloned = cardCloned;
                    break;
                }
            }
            
            nextPath.addPath("SUMMON " + myCardCloned.getInstanceId() + ";");

            nextPath.getBoardGame().summon(myCardCloned);
            nextPath.getCardsNeedAttack().add(myCardCloned);
            
            fillPathsPhase1(nextPath);

            if (Player.paths.size() + 1 >= Settings.NBOFPATHMAX){
                return;
            }
        }
        
        fillPathsPhase2(path);
    }

    //Put spell
    private void fillPathsPhase2(Path path){
        for (Card spell : path.getBoardGame().me.getHandSpell()){
            if (path.getBoardGame().me.getMana() - spell.getCost() < 0){
                continue;
            }
            
            HashSet<Card> targetBoard = null;
            if (spell.getType() == 1){
                targetBoard = path.getBoardGame().me.getBoard();
            } else {
                targetBoard = path.getBoardGame().enemy.getBoard();
            }
            
            for (Card creature : targetBoard){
                Path nextPath = path.clone();

                Card spellCloned = null;
                Card creatureCloned = null;

                for (Card cardCloned : nextPath.getBoardGame().me.getHandSpell()){
                    if (spell.equals(cardCloned)){
                        spellCloned = cardCloned;
                        break;
                    }
                }

                HashSet<Card> targetBoardCloned = null;
                if (spell.getType() == 1){
                    targetBoardCloned = nextPath.getBoardGame().me.getBoard();
                } else {
                    targetBoardCloned = nextPath.getBoardGame().enemy.getBoard();
                }
                for (Card cardCloned : targetBoardCloned){
                    if (creature.equals(cardCloned)){
                        creatureCloned = cardCloned;
                        break;
                    }
                }

                nextPath.addPath("USE " + spellCloned.getInstanceId() + " " + creatureCloned.getInstanceId() + ";");

                nextPath.getBoardGame().spell(spellCloned, creatureCloned);

                fillPathsPhase2(nextPath);

                if (Player.paths.size() + 1 >= Settings.NBOFPATHMAX){
                    return;
                }
            }
        }

        fillPathsPhase3(path);
        
    }

    //Fight
    private void fillPathsPhase3(Path path){
        //We create a new fight
        for (Card myCard : path.getCardsNeedAttack()){
            for (Card enemyCard : path.getBoardGame().enemy.getBoard()){
                //Si la carte adverse est bloquee par des provocations
                if (!path.getBoardGame().enemy.getGuardBoard().isEmpty() && enemyCard.is(Abilitie.guard) == false){
                    continue;
                }

                Path nextPath = path.clone();

                Card myCardCloned = null;
                Card enemyCardCloned = null;

                for (Card cardCloned : nextPath.getBoardGame().me.getBoard()){
                    if (myCard.equals(cardCloned)){
                        myCardCloned = cardCloned;
                        break;
                    }
                }

                for (Card cardCloned : nextPath.getBoardGame().enemy.getBoard()){
                    if (enemyCard.equals(cardCloned)){
                        enemyCardCloned = cardCloned;
                        break;
                    }
                }

                nextPath.addPath("ATTACK " + myCard.getInstanceId() + " " + enemyCard.getInstanceId() + ";");
                nextPath.getCardsNeedAttack().remove(myCardCloned);

                nextPath.getBoardGame().fight(myCardCloned, nextPath.getBoardGame().me, enemyCardCloned, nextPath.getBoardGame().enemy);

                fillPathsPhase3(nextPath);
                
                if (Player.paths.size() + 1 >= Settings.NBOFPATHMAX){
                    return;
                }
            }
        }

        //Si il n y a plus de taunt on envoie tout le reste des creature dans la tete de l'adversaire
        if (path.getBoardGame().enemy.getGuardBoard().isEmpty()){
            for (Card card : path.getCardsNeedAttack()){
                path.getBoardGame().enemy.addHealth(-card.getAttack());
                path.addPath("ATTACK " + card.getInstanceId() + " -1;");
            }
        }

        fillPathsPhase4(path);
    }
    
    //Summon creature
    private void fillPathsPhase4(Path path){
        //If the board is full
        if (path.getBoardGame().me.getBoard().size() == Player.BOARDMAXSIZE){
            return;
        }

        if (Player.paths.size() + 1 >= Settings.NBOFPATHMAX){
            Player.paths.add(path);
            return;
        }

        //We add a creature to summon
        for (Card card : path.getBoardGame().me.getHand()){
            if (path.getBoardGame().me.getMana() - card.getCost() < 0 || card.getType() != 0 || path.getPhase4().contains(card.getCardNumber())){
                continue;
            }
            
            Path nextPath = path.clone();

            path.getPhase4().add(card.getCardNumber());

            Card myCardCloned = null;

            for (Card cardCloned : nextPath.getBoardGame().me.getHand()){
                if (card.equals(cardCloned)){
                    myCardCloned = cardCloned;
                    break;
                }
            }

            nextPath.addPath("SUMMON " + myCardCloned.getInstanceId() + ";");

            nextPath.getBoardGame().summon(myCardCloned);
            
            fillPathsPhase4(nextPath);

            if (Player.paths.size() + 1 >= Settings.NBOFPATHMAX){
                break;
            }
        }
        
        Player.paths.add(path);
    }

    //Enemy Fight
    private void fillPathsPhase5(Path path){
        if (Player.enemyPaths.size() + 1 >= Settings.NBOFPATHMAXFORENEMY){
            if (path.getBoardGame().me.getGuardBoard().isEmpty()){
                for (Card card : path.getCardsNeedAttack()){
                    path.getBoardGame().me.addHealth(-card.getAttack());
                    path.addPath("ATTACK " + card.getInstanceId() + " -1;");
                }
            }

            Player.enemyPaths.add(path);
            return;
        }

        //We create a new fight
        for (Card enemyCard : path.getCardsNeedAttack()){
            for (Card myCard : path.getBoardGame().me.getBoard()){
                //Si la carte adverse est bloquee par des provocations
                if (!path.getBoardGame().me.getGuardBoard().isEmpty() && myCard.is(Abilitie.guard) == false){
                    continue;
                }

                Path nextPath = path.clone();

                Card enemyCardCloned = null;
                Card myCardCloned = null;


                for (Card cardCloned : nextPath.getBoardGame().enemy.getBoard()){
                    if (enemyCard.equals(cardCloned)){
                        enemyCardCloned = cardCloned;
                        break;
                    }
                }
                
                for (Card cardCloned : nextPath.getBoardGame().me.getBoard()){
                    if (myCard.equals(cardCloned)){
                        myCardCloned = cardCloned;
                        break;
                    }
                }

                nextPath.addPath("ATTACK " + enemyCard.getInstanceId() + " " + myCard.getInstanceId() + ";");
                nextPath.getCardsNeedAttack().remove(enemyCardCloned);

                nextPath.getBoardGame().fight(enemyCardCloned, nextPath.getBoardGame().enemy, myCardCloned, nextPath.getBoardGame().me);

                fillPathsPhase5(nextPath);

                if (Player.enemyPaths.size() + 1 >= Settings.NBOFPATHMAXFORENEMY){
                    break;
                }
            }
            if (Player.enemyPaths.size() + 1 >= Settings.NBOFPATHMAXFORENEMY){
                break;
            }
        }

        //Si il n y a plus de taunt on envoie tout le reste des creature dans la tete de l'adversaire
        if (path.getBoardGame().me.getGuardBoard().isEmpty()){
            for (Card card : path.getCardsNeedAttack()){
                path.getBoardGame().me.addHealth(-card.getAttack());
                path.addPath("ATTACK " + card.getInstanceId() + " -1;");
            }
        }

        Player.enemyPaths.add(path);
    }
    //#endregion


    public String getBestPlay(){
        Player.paths = new HashSet<Path>();
        this.allFace();
        fillPathsPhase1(new Path("", this.clone(), (HashSet)this.me.getBoard().clone()));

        System.err.println("Nombre paths : " + Player.paths.size());

        if (Player.paths.size() >= Settings.NBOFPATHMAX){
            System.err.println("Nombre de paths >= " + Settings.NBOFPATHMAX + " -> OVERTIME");
        }

        if (Player.paths.size() >= Settings.NBOFPATHMAXFORMINMAX){
            System.err.println("Nombre de paths >= " + Settings.NBOFPATHMAXFORMINMAX + " -> Pas de min max");
        }

        String bestAttack = "";

        double bestBoardGameValue = -9999.0;

        System.err.println("Board value before : " + this.getValue());

        for (Path path : Player.paths){
            System.err.print(path.getFinalCommande() + " : " + path.getBoardGame().getValue());

            if (Player.paths.size() < Settings.NBOFPATHMAXFORMINMAX){
                Player.enemyPaths = new HashSet<Path>();
                path.getBoardGame().getEnemy().draw();
                fillPathsPhase5(new Path("", path.getBoardGame(), (HashSet)path.getBoardGame().enemy.getBoard().clone()));

                System.err.print(" Enemy nombre paths : " + Player.enemyPaths.size() + " ");

                String enemyBestAttack = "";

                double enemyLessBoardGameValue = 9999.0;

                for (Path enemyPath : Player.enemyPaths){
                    double enemyBoardGameValue = enemyPath.getBoardGame().getValue();
                    if (enemyBoardGameValue < enemyLessBoardGameValue){
                        enemyLessBoardGameValue = enemyBoardGameValue;
                        enemyBestAttack = enemyPath.getFinalCommande();
                    }
                }

                if (enemyLessBoardGameValue > bestBoardGameValue){
                    bestBoardGameValue = enemyLessBoardGameValue;
                    bestAttack = path.getFinalCommande();
                }

                
                System.err.println(" && " + enemyBestAttack + " : " + enemyLessBoardGameValue);
            } else {
                double boardGameValue = path.getBoardGame().getValue();
                if (boardGameValue > bestBoardGameValue){
                    bestBoardGameValue = boardGameValue;
                    bestAttack = path.getFinalCommande();
                }
                System.err.println();
            }
        }

        return bestAttack;
    }

    //#region actions
    public void summon(Card card){
        this.me.getHand().remove(card);
        if (card.is(Abilitie.charge)){
            this.me.getHandCharge().remove(card);
        }
        this.me.getBoard().add(card);
        if (card.is(Abilitie.guard)){
            this.me.getGuardBoard().add(card);
        }
        this.me.addHealth(card.getMyHealthChange());
        this.enemy.addHealth(card.getEnemyHealthChange());
        this.me.removeMana(card.getCost());
    }

    public void spell(Card spell, Card creature){
        this.me.getHand().remove(spell);
        this.me.getHandSpell().remove(spell);
        creature.takeSpell(spell);
        if (creature.is(Abilitie.guard) == false){
            this.enemy.getGuardBoard().remove(creature);
        }
        if (creature.getDefense() <= 0){
            this.enemy.getBoard().remove(creature);
            this.enemy.getGuardBoard().remove(creature);
        }
        this.me.addHealth(spell.getMyHealthChange());
        this.enemy.addHealth(spell.getEnemyHealthChange());
        this.me.removeMana(spell.getCost());
    }

    public void fight(Card attackingCreature, Player attackingOwner, Card defendingCreature, Player defendingOwner){
        attackingCreature.dealDamage(defendingCreature);

        if (attackingCreature.is(Abilitie.drain) == true){
            attackingOwner.addHealth(defendingCreature.dealDamage(attackingCreature));
        } else {
            defendingCreature.dealDamage(attackingCreature);
        }


        if (attackingCreature.getDefense() <= 0){
            attackingOwner.getBoard().remove(attackingCreature);
            if (attackingCreature.is(Abilitie.guard) == true){
                attackingOwner.getGuardBoard().remove(attackingCreature);
            }
        }
        if (defendingCreature.getDefense() <= 0){
            defendingOwner.getBoard().remove(defendingCreature);
            if (defendingCreature.is(Abilitie.guard) == true){
                defendingOwner.getGuardBoard().remove(defendingCreature);
            }
            if (attackingCreature.is(Abilitie.breakthrough) == true){
                defendingOwner.addHealth(defendingCreature.getDefense());
                if (attackingCreature.is(Abilitie.drain) == true){
                    attackingOwner.addHealth(-defendingCreature.getDefense());
                }
            }
        }
    }
    //#endregion

    //#region value
    public double getValue(){
        return me.getValue() - enemy.getValue();
    }

    public String getValueDetail(){
        return "TOT = " + (me.getValue() - enemy.getValue()) + " ME : " + me.getValueDetail() + " ENEMY : " + enemy.getValueDetail();
    }
    //#endregion

    @Override
    public BoardGame clone(){
        BoardGame boardGameCloned = new BoardGame();

        boardGameCloned.me = this.me.clone();
        boardGameCloned.enemy = this.enemy.clone();

        return boardGameCloned;
    }
} 
