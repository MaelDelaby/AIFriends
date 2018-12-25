abstract class Settings{
    private static final int SETTINGSTIMERCHOSED = 1;
    private static final int SETTINGSDRAFTCHOSED = 1;
    private static final int SETTINGSFIGHTCHOSED = 1;

    public static void initSettings(){
        HANDVALUE = new double[Player.HANDMAXSIZE];

        switch (Settings.SETTINGSTIMERCHOSED){
            case 1:
                settingsTimer1();
                break;
        }

        switch (Settings.SETTINGSDRAFTCHOSED){
            case 1:
                settingsDraft1();
                break;
        }

        switch (Settings.SETTINGSFIGHTCHOSED){
            case 1:
                settingsFight1();
                break;
        }
    }

    //#region timer
    public static int MAXTIME;

    private static void settingsTimer1(){
        MAXTIME = 1000000000;
    }
    //#endregion

    //#region draft
    public static double AVERAGE;//we are aiming this average
    public static int[] CARDSLADDER;

    private static void settingsDraft1(){
        CARDSLADDER = new int[]{
            0,
            2,3,1,4,3,2,1,3,2,5,
            4,4,2,4,1,4,1,1,1,4,
            2,3,1,6,4,2,4,3,2,3,
            5,2,2,3,5,2,1,2,2,4,
            3,5,3,2,3,4,3,1,1,2,
    
            1,5,1,3,4,4,5,3,2,5,
            4,3,3,3,2,2,3,1,1,2,
            4,3,1,3,2,3,3,4,3,1,
            5,1,1,4,2,4,3,2,4,5,
            2,5,2,4,3,3,3,3,3,5,
    
            3,4,2,2,2,3,5,5,2,5,
            2,3,4,2,4,2,4,2,2,4,
            4,3,5,4,5,4,3,3,3,5,
            5,5,4,5,3,3,3,5,3,9,
            2,1,6,1,3,5,2,2,5,1,
    
            2,3,6,6,3,6,5,3,5,6,
        };

        AVERAGE = 4.5;
    }
    //#endregion

    //#region fight
    public static double VALUEOFPOSEDCARD;
    public static double[] HANDVALUE;
    public static double RUNEVALUE;

    public static double ATTACKXDEFENSECOEF;
    public static double BREAKTHROUGHATTACKCOEF;
    public static double DRAINATTACKCOEF;
    public static double GUARDDEFENSECOEF;
    public static double LETHALVALUE;
    public static double WARDATTCKCOEF;

    private static void settingsFight1(){
        VALUEOFPOSEDCARD = 4;

        HANDVALUE[0] = 7;
        HANDVALUE[1] = 7;
        HANDVALUE[2] = 7;
        HANDVALUE[3] = 6;
        HANDVALUE[4] = 5;
        HANDVALUE[5] = 4;
        HANDVALUE[6] = 4;
        HANDVALUE[7] = 4;

        RUNEVALUE = 3;

        ATTACKXDEFENSECOEF = 0.3;
        BREAKTHROUGHATTACKCOEF = 0.2;
        DRAINATTACKCOEF = 0.4;
        GUARDDEFENSECOEF = 0.3;
        LETHALVALUE = 4;
        WARDATTCKCOEF = 1;
    }
    //#endregion
} 
