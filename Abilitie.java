enum Abilitie{ 
    breakthrough("B"), charge("C"), drain("D"), guard("G"), lethal("L"), ward("W");

    private String cara;

    private Abilitie(String cara){
        this.cara = cara;
    }

    public String getCara(){
        return this.cara;
    }
} 
