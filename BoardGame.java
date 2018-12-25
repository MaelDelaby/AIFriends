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

    public Player getPlayer(PlayerName playerName){
        return playerName == PlayerName.me ? this.me : this.enemy;
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
    private void allFace(HashSet<Path> pathSet){
        BoardGame boardGame = this.clone();
        String finalCommande = "";
        if (boardGame.enemy.getGuardBoard().isEmpty()){
            for (Card card : boardGame.me.getBoard()){
                boardGame.enemy.addHealth(-card.getAttack());
                finalCommande += "ATTACK " + card.getInstanceId() + " -1;";
            }
        }

        pathSet.add(new Path(finalCommande, boardGame, new HashSet<Card>()));
    }

    //Summon charge creature
    private void fillPathsPhase1(HashSet<Path> pathSet, Path path){
        for (Card card : path.getBoardGame().me.getHandCharge()){
            //Si on pas assez de mana
            if (path.getBoardGame().me.getMana() - card.getCost() < 0){
                continue;
            }

            Path nextPath = path.clone();

            Card myCardCloned = nextPath.getBoardGame().me.getHandCharge().stream()
                .filter(x -> x.equals(card))
                .findAny()
                .get();
            
            nextPath.addInstruction("SUMMON " + myCardCloned.getInstanceId() + ";");

            nextPath.getBoardGame().summon(myCardCloned);
            nextPath.getCardsNeedAttack().add(myCardCloned);
            
            fillPathsPhase1(pathSet, nextPath);

            if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
                return;
            }
        }
        
        fillPathsPhase2(pathSet, path);
    }

    //Put spell
    private void fillPathsPhase2(HashSet<Path> pathSet, Path path){
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

                Card spellCloned = nextPath.getBoardGame().me.getHand().stream()
                    .filter(x -> x.equals(spell))
                    .findAny()
                    .get();

                HashSet<Card> targetBoardCloned = nextPath.getBoardGame()
                    .getPlayer(spell.getType() == 1 ? PlayerName.me : PlayerName.enemy)
                    .getBoard();

                Card creatureCloned = targetBoardCloned.stream()
                    .filter(x -> x.equals(creature))
                    .findAny()
                    .get();

                nextPath.addInstruction("USE " + spellCloned.getInstanceId() + " " + creatureCloned.getInstanceId() + ";");

                nextPath.getBoardGame().spell(spellCloned, creatureCloned);

                fillPathsPhase2(pathSet, nextPath);

                if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
                    return;
                }
            }
        }

        fillPathsPhase3(pathSet, path, PlayerName.me, PlayerName.enemy);
        
    }

    //Fight
    private void fillPathsPhase3(HashSet<Path> pathSet, Path path, PlayerName attackingPlayer, PlayerName defendingPlayer){
        if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
            return;
        }

        //We create a new fight
        for (Card attackingCard : path.getCardsNeedAttack()){
            for (Card defendingCard : path.getBoardGame().getPlayer(defendingPlayer).getBoard()){
                //Si la carte adverse est bloquee par des provocations
                if (!path.getBoardGame().getPlayer(defendingPlayer).getGuardBoard().isEmpty() && defendingCard.is(Abilitie.guard) == false){
                    continue;
                }

                Path nextPath = path.clone();
                
                Card attackingCardCloned = nextPath.getBoardGame().getPlayer(attackingPlayer).getBoard().stream()
                    .filter(x -> x.equals(attackingCard))
                    .findAny()
                    .get();

                Card defendingCardCloned = nextPath.getBoardGame().getPlayer(defendingPlayer).getBoard().stream()
                    .filter(x -> x.equals(defendingCard))
                    .findAny()
                    .get();

                nextPath.addInstruction("ATTACK " + attackingCard.getInstanceId() + " " + defendingCard.getInstanceId() + ";");
                nextPath.getCardsNeedAttack().remove(attackingCard);

                nextPath.getBoardGame().fight(attackingCardCloned, nextPath.getBoardGame().getPlayer(attackingPlayer), defendingCardCloned, nextPath.getBoardGame().getPlayer(defendingPlayer));

                fillPathsPhase3(pathSet, nextPath, PlayerName.me, PlayerName.enemy);
                
                if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
                    return;
                }
            }
        }

        //Si il n y a plus de taunt on envoie tout le reste des creature dans la tete de l'adversaire
        if (path.getBoardGame().enemy.getGuardBoard().isEmpty()){
            for (Card card : path.getCardsNeedAttack()){
                path.getBoardGame().enemy.addHealth(-card.getAttack());
                path.addInstruction("ATTACK " + card.getInstanceId() + " -1;");
            }
        }

        if (attackingPlayer == PlayerName.me){
            fillPathsPhase4(pathSet, path);
        } else {
            pathSet.add(path);
        }
    }
    
    //Summon creature
    private void fillPathsPhase4(HashSet<Path> pathSet, Path path){
        //If the board is full
        if (path.getBoardGame().me.getBoard().size() == Player.BOARDMAXSIZE){
            return;
        }

        if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
            pathSet.add(path);
            return;
        }

        //We add a creature to summon
        for (Card card : path.getBoardGame().me.getHand()){
            if (path.getBoardGame().me.getMana() - card.getCost() < 0 || card.getType() != 0 || path.getPhase4().contains(card.getCardNumber())){
                continue;
            }
            
            Path nextPath = path.clone();

            path.getPhase4().add(card.getCardNumber());

            Card myCardCloned = nextPath.getBoardGame().me.getHand().stream()
                .filter(x -> x.equals(card))
                .findAny()
                .get();

            nextPath.addInstruction("SUMMON " + myCardCloned.getInstanceId() + ";");

            nextPath.getBoardGame().summon(myCardCloned);
            
            fillPathsPhase4(pathSet, nextPath);

            if (System.nanoTime() - Player.beginTime > Settings.MAXTIME){
                return;
            }
        }
        
        pathSet.add(path);
    }
    //#endregion


    public String getBestPlay(){
        HashSet<Path> pathSet = new HashSet<Path>();
        this.allFace(pathSet);
        fillPathsPhase1(pathSet, new Path("", this.clone(), (HashSet)this.me.getBoard().clone()));

        pathSet.add(new Path("", this, null));
        System.err.println("-- Board value before : " + this.getValue());

        String bestAttack = "";
        double bestBoardGameValue = -9999.0;


        //Tente de faire un min max si on a encore du temps
        if (System.nanoTime() - Player.beginTime < Settings.MAXTIME){
            for (Path path : pathSet){
                if (System.nanoTime() - Player.beginTime >= Settings.MAXTIME){
                    break;
                }

                System.err.print(path.getFinalCommande() + " : " + path.getBoardGame().getValue());
    
                path.getBoardGame().getEnemy().draw();

                fillPathsPhase3(path.getEnemyPaths(), new Path("", path.getBoardGame(), (HashSet)path.getBoardGame().enemy.getBoard().clone()), PlayerName.enemy, PlayerName.me);

                String enemyBestAttack = "";
                double enemyLessBoardGameValue = 9999.0;

                for (Path enemyPath : path.getEnemyPaths()){
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
            }
        }

        //Si c etait deja overtime ou que le min max fut trop long
        if (System.nanoTime() - Player.beginTime >= Settings.MAXTIME){
            pathSet.stream().forEach(System.err::println);
            return pathSet.stream()
                .max(Comparator.comparingDouble(Path::getBoardGameValue))//A modifier
                .get()
                .getFinalCommande();
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
