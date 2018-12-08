class Card implements Cloneable{ 
    final static int nbTotalOfCardTypes = 161;

    private int number;
    private int instanceId;
    private int location;
    private int type;
    private int cost;
    private int attack;
    private int defense;
    private HashSet<Abilitie> abilities;
    private int myHealthChange;
    private int enemyHealthChange;
    private int cardDraw;

    //#region constructor
    public Card(int number,
            int instanceId,
            int location,
            int type,
            int cost,
            int attack,
            int defense,
            String abilities,
            int myHealthChange, 
            int enemyHealthChange,
            int cardDraw){
        this(number, instanceId, location, type, cost, attack, defense, new HashSet<Abilitie>(), myHealthChange, enemyHealthChange, cardDraw);

        for (Abilitie abilitie : Abilitie.values()){
            if (abilities.contains(abilitie.getCara()) == true){
                this.abilities.add(abilitie);
            }
        }
    }

    public Card(int number,
            int instanceId,
            int location,
            int type,
            int cost,
            int attack,
            int defense,
            HashSet<Abilitie> abilities,
            int myHealthChange, 
            int enemyHealthChange,
            int cardDraw){

        this.number = number;
        this.instanceId = instanceId;
        this.location = location;
        this.type = type;
        this.cost = cost;
        this.attack = attack;
        this.defense = defense;
        this.abilities = abilities;
        this.myHealthChange = myHealthChange;
        this.enemyHealthChange = enemyHealthChange;
        this.cardDraw = cardDraw;
    }
    //#endregion

    //#region accesor & mutator
    public int getCardNumber(){
        return this.number;
    }
    
    public int getInstanceId(){
        return this.instanceId;
    }

    public int getLocation(){
        return this.location;
    }

    public int getType(){
        return this.type;
    }

    public int getCost(){
        return this.cost;
    }

    public int getAttack(){
        return this.attack;
    }

    public int getDefense(){
        return this.defense;
    }
    
    public void setDefense(int defense){
        this.defense = defense;
    }

    public HashSet<Abilitie> getAbilities(){
        return this.abilities;
    }

    public boolean is(Abilitie abilitie){
        return this.abilities.contains(abilitie);
    }

    public int getMyHealthChange(){
        return this.myHealthChange;
    }
    
    public int getEnemyHealthChange(){
        return this.enemyHealthChange;
    }

    public int getCardDraw(){
        return this.cardDraw;
    }
    //#endregion

    //#region action
    public int dealDamage(Card otherCard){
        if (otherCard.is(Abilitie.ward) == false){
            if (this.is(Abilitie.lethal) && this.attack > 0 && otherCard.location != -2){
                otherCard.defense = 0;
            } else {
                otherCard.takeDamage(this.attack);
            }
            if (otherCard.getDefense() < 0){
                return this.attack + otherCard.defense;
            } else {
                return this.attack;
            }
        } else {
            otherCard.getAbilities().remove(Abilitie.ward);
            return 0;
        }
    }

    public void takeDamage(int damage){
        this.defense -= damage;
    }

    public void takeSpell(Card spell){
        this.attack += spell.attack;
        this.defense += spell.defense;
        for (Abilitie abilitie : spell.getAbilities()){
            if (spell.getType() == 1){
                this.abilities.add(abilitie);
            } else if (spell.getType() == 2){
                this.abilities.remove(abilitie);
            }
        }
    }
    //#endregion

    //#region value
    public double getValue(){
        double value = Settings.VALUEOFPOSEDCARD;

        value += attack + defense;
        value += Settings.ATTACKXDEFENSECOEF * (attack * defense);
        value += this.is(Abilitie.breakthrough) ? attack * Settings.BREAKTHROUGHATTACKCOEF : 0;
        value += this.is(Abilitie.drain) ? attack * Settings.DRAINATTACKCOEF : 0;
        value += this.is(Abilitie.guard) ? defense * Settings.GUARDDEFENSECOEF : 0;
        value += this.is(Abilitie.lethal) && this.attack > 0 ? Settings.LETHALVALUE : 0;
        value += this.is(Abilitie.ward) ? attack * Settings.WARDATTCKCOEF : 0;
        value -= Settings.CARDSLADDER[this.number] * 0.01;

        return value;
    }

    public double getDraftValue(){
        return Settings.CARDSLADDER[this.number] + 0.4 * getAverageValue();
    }

    private double getAverageValue(){
        return Math.abs(Settings.AVERAGE - 
                            (
                                (Player.getDeckAverage() + this.cost + this.cardDraw * 1.5)
                                / 2
                            )
                        );
    }
    //#endregion

    @Override
    public Card clone(){
        return new Card(this.number, this.instanceId, this.location, this.type, this.cost, this.attack, this.defense, (HashSet)this.abilities.clone(), this.myHealthChange, this.enemyHealthChange, this.cardDraw);
    }

    @Override
    public boolean equals(Object object){
        return this.instanceId == ((Card)object).instanceId;
    }

    @Override
    public int hashCode(){
        return this.instanceId;
    }
} 
