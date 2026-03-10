package PART2;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.List;




public class WorldPanel extends JPanel
{
    private GameLauncher game;
    private final int TILE_SIZE = 48;
   
    private boolean showPokedex = false;
    private boolean canSurf = false; // NEW: Surf Mechanic
   
    // Map Constants
    private final int MAP_PALLET = 0;
    private final int MAP_ROUTE1 = 1;
    private final int MAP_VIRIDIAN = 2;
    private final int MAP_ROUTE2 = 3;
    private final int MAP_FOREST = 4;
    private final int MAP_PEWTER = 5;
    private final int MAP_ROUTE3 = 6;
    private final int MAP_MT_MOON = 7;
    private final int MAP_ROUTE4 = 8;
    private final int MAP_CERULEAN = 9;
    private final int MAP_ROUTE5 = 10;
    private final int MAP_GYM_PEWTER = 11;
    private final int MAP_GYM_CERULEAN = 12;
    private final int MAP_CENTER = 13;
    private final int MAP_SAFFRON = 14;
    private final int MAP_ROUTE6 = 15;
    private final int MAP_VERMILION = 16;
    private final int MAP_ROUTE11 = 17;
    private final int MAP_GYM_VERMILION = 18;
    private final int MAP_ROUTE7 = 19;
    private final int MAP_CELADON = 20;
    private final int MAP_ROUTE8 = 21;
    private final int MAP_LAVENDER = 22;
    private final int MAP_GYM_CELADON = 23;
    private final int MAP_ROUTE12 = 24;
    private final int MAP_ROUTE16 = 25;
   
    // New Map Constants (Southern Loop)
    private final int MAP_ROUTE15 = 26;
    private final int MAP_FUCHSIA = 27;
    private final int MAP_ROUTE19 = 28;
    private final int MAP_ROUTE20 = 29;
    private final int MAP_CINNABAR = 30;
    private final int MAP_ROUTE21 = 31;
    private final int MAP_GYM_FUCHSIA = 32;
    private final int MAP_GYM_CINNABAR = 33;
    private final int MAP_WARDEN_HOUSE = 34; // NEW: Warden's House Map ID
    private final int MAP_MART = 35;
   
    private int currentMapId = MAP_PALLET;
    private int returnMapId = MAP_VIRIDIAN;
    private int[][] currentMap;
    private List<NPC> npcs = new ArrayList<>();
    private List<MapLabel> labels = new ArrayList<>();




    // --- PERSISTENCE STORAGE (Expanded to 40 maps) ---
    private int[][][] allMaps = new int[40][][];
    private List<List<NPC>> allNPCs = new ArrayList<>();
    private List<List<MapLabel>> allLabels = new ArrayList<>();
   
    private int playerX = 10, playerY = 10;
    private boolean moving = false;




    public WorldPanel(GameLauncher game)
    {
        this.game = game;
       
        for(int i = 0; i < 40; i++) {
            allNPCs.add(new ArrayList<>());
            allLabels.add(new ArrayList<>());
        }




        setPreferredSize(new Dimension(800, 600));
        setBackground(Color.BLACK);
        setFocusable(true);
        loadMap(MAP_PALLET, 10, 10);




       addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // 1. If the Pokedex is open, check if we should close it
                if (showPokedex) {
                    if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        showPokedex = false;
                        repaint();
                    }
                    return; // Stop reading keys so the player doesn't move behind the Pokedex!
                }
               
                // 2. If 'P' is pressed, open the Pokedex
                if (e.getKeyCode() == KeyEvent.VK_P) {
                    showPokedex = true;
                    repaint();
                    return;
                }


                // 3. ---> NEW: If 'B' is pressed, open the Bag! <---
                if (e.getKeyCode() == KeyEvent.VK_B) {
                    game.openInventory();
                    return; // Stop reading keys so the player doesn't move while opening the bag
                }


                // 4. If no menus are opening, allow the player to move
                if(!moving) {
                    movePlayer(e.getKeyCode());
                }
            }
        });
    }


    public void refreshMap() { loadMapInternal(currentMapId, playerX, playerY, false); }
    public void respawn() { loadMap(MAP_PALLET, 10, 10); }




    private void loadMap(int mapId, int startX, int startY) { loadMapInternal(mapId, startX, startY, true); }




    private void loadMapInternal(int mapId, int startX, int startY, boolean movePlayer)
    {
        currentMapId = mapId;
        if (movePlayer) { playerX = startX; playerY = startY; }
       
        if (allMaps[mapId] == null)
        {
            npcs.clear(); labels.clear();
           
            if (mapId == MAP_PALLET) generatePalletTown();
            else if (mapId == MAP_ROUTE1) generateRoute1();
            else if (mapId == MAP_VIRIDIAN) generateViridian();
            else if (mapId == MAP_ROUTE2) generateRoute2();
            else if (mapId == MAP_FOREST) generateForest();
            else if (mapId == MAP_PEWTER) generatePewter();
            else if (mapId == MAP_ROUTE3) generateRoute3();
            else if (mapId == MAP_MT_MOON) generateMtMoon();
            else if (mapId == MAP_ROUTE4) generateRoute4();
            else if (mapId == MAP_CERULEAN) generateCeruleanCity();
            else if (mapId == MAP_ROUTE5) generateRoute5();
            else if (mapId == MAP_SAFFRON) generateSaffronCity();
            else if (mapId == MAP_ROUTE6) generateRoute6();
            else if (mapId == MAP_VERMILION) generateVermilionCity();
            else if (mapId == MAP_ROUTE11) generateRoute11();
            else if (mapId == MAP_ROUTE7) generateRoute7();
            else if (mapId == MAP_CELADON) generateCeladonCity();
            else if (mapId == MAP_ROUTE8) generateRoute8();
            else if (mapId == MAP_LAVENDER) generateLavenderTown();
            else if (mapId == MAP_ROUTE12) generateRoute12();
            else if (mapId == MAP_ROUTE16) generateRoute16();
            // New Southern Maps
            else if (mapId == MAP_ROUTE15) generateRoute15();
            else if (mapId == MAP_FUCHSIA) generateFuchsiaCity();
            else if (mapId == MAP_ROUTE19) generateRoute19();
            else if (mapId == MAP_ROUTE20) generateRoute20();
            else if (mapId == MAP_CINNABAR) generateCinnabarIsland();
            else if (mapId == MAP_ROUTE21) generateRoute21();
            else if (mapId == MAP_GYM_FUCHSIA) generateGymFuchsia();
            else if (mapId == MAP_GYM_CINNABAR) generateGymCinnabar();
            // Interiors
            // Interiors
            else if (mapId == MAP_GYM_PEWTER) generateGymPewter();
            else if (mapId == MAP_GYM_CERULEAN) generateGymCerulean();
            else if (mapId == MAP_GYM_VERMILION) generateGymVermilion();
            else if (mapId == MAP_GYM_CELADON) generateGymCeladon();
            else if (mapId == MAP_CENTER) generateCenter();
            else if (mapId == MAP_MART) generateMart();   // ✅ ADDED
            else if (mapId == MAP_WARDEN_HOUSE) generateWardenHouse();




            allMaps[mapId] = currentMap;
            allNPCs.set(mapId, new ArrayList<>(npcs));
            allLabels.set(mapId, new ArrayList<>(labels));
        } else {
            currentMap = allMaps[mapId];
            npcs = allNPCs.get(mapId);
            labels = allLabels.get(mapId);
        }
        repaint();
    }




    // --- MAP GENERATION METHODS ---
    private void generatePalletTown() {
        currentMap = new int[20][15]; fillMap(0); drawBorder(20, 15);
        currentMap[9][0] = 0; currentMap[10][0] = 0; // North exit
        currentMap[9][14] = 5; currentMap[10][14] = 5; // South water exit to Route 21
        buildHouse(12, 8, 5, 4, 200, "OAK'S LAB");
        npcs.add(new NPC("Oak", 14, 13, "OAK", "NONE"));
        labels.add(new MapLabel("PALLET TOWN", 2, 2));
    }




    private void generateRoute1() {
        currentMap = new int[14][30]; fillMap(0); drawBorder(14, 30);
        for(int y=0; y<30; y++) { currentMap[6][y]=3; currentMap[7][y]=3; }
        for(int y=5; y<25; y++) { currentMap[3][y]=4; currentMap[4][y]=4; currentMap[9][y]=4; currentMap[10][y]=4; }
        labels.add(new MapLabel("ROUTE 1", 2, 2));
    }




    private void generateViridian() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[9][19]=3; currentMap[10][19]=3; currentMap[9][0]=3; currentMap[10][0]=3;
        buildHouse(14, 4, 4, 4, 202, "MART"); buildHouse(14, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("VIRIDIAN CITY", 2, 2));
    }




    private void generateRoute2() {
        currentMap = new int[16][30]; fillMap(0); drawBorder(16, 30);
        for(int y=0; y<30; y++) { currentMap[7][y]=3; currentMap[8][y]=3; }
        buildHouse(5, 12, 6, 4, 205, "VIRIDIAN FOREST");
        labels.add(new MapLabel("ROUTE 2", 2, 2));
    }




    private void generateForest() {
        currentMap = new int[20][30]; fillMap(0); drawBorder(20, 30);
        for (int x = 1; x < 19; x++) {
            for (int y = 1; y < 28; y++) {
                if (x != 9 && x != 10) {
                    if ((x < 8 || x > 11) && y % 3 == 0) currentMap[x][y] = 1;
                    if (x % 4 == 0 && y > 5 && y < 25) currentMap[x][y] = 1;
                }
            }
        }
        for (int y = 0; y < 30; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 2; x < 6; x++) for (int y = 5; y < 15; y++) currentMap[x][y] = 4;
        currentMap[9][29] = 3; currentMap[10][29] = 3; currentMap[9][0] = 3;  currentMap[10][0] = 3;
        npcs.add(new NPC("Bug Catcher", 8, 20, "TRAINER", "BUG1"));
        npcs.get(npcs.size() - 1).party = new Pokemon("Caterpie", "BUG", 6);
        labels.add(new MapLabel("VIRIDIAN FOREST", 2, 2));
    }




    private void generatePewter() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[9][19]=3; currentMap[10][19]=3; currentMap[19][9]=3; currentMap[19][10]=3;
        buildHouse(4, 4, 6, 5, 201, "BROCK'S GYM"); buildHouse(14, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("PEWTER CITY", 2, 2));
    }




    private void generateRoute3() {
        currentMap = new int[40][15]; fillMap(0); drawBorder(40, 15);
        for(int x=0; x<40; x++) { currentMap[x][7]=3; currentMap[x][8]=3; }
        for(int x=5; x<12; x++) for(int y=3; y<6; y++) currentMap[x][y]=4;
        NPC youngster=new NPC("Youngster", 15, 6, "TRAINER", "R3_TR1"); youngster.party=new Pokemon("Rattata","NORMAL",11); npcs.add(youngster);
        labels.add(new MapLabel("ROUTE 3", 2, 5));
    }




    private void generateMtMoon() {
        currentMap = new int[30][30];
        fillMap(3);
        drawBorder(30, 30);
       
        // 1. Create a Random instance with a fixed seed (e.g., 12345)
        // You can change '12345' to any number you like. As long as the number
        // stays the same, the map will always generate the exact same way.
        java.util.Random seededRandom = new java.util.Random(12345);


        for (int i = 0; i < 40; i++) {
            // 2. Replace Math.random() with seededRandom.nextInt()
            int rx = seededRandom.nextInt(26) + 2;
            int ry = seededRandom.nextInt(26) + 2;
            currentMap[rx][ry] = 1;
        }
       
        NPC rocket = new NPC("Rocket Grunt", 15, 15, "TRAINER", "MTM_R1");
        rocket.party = new Pokemon("Zubat", "POISON", 12);
        npcs.add(rocket);
       
        currentMap[29][14] = 99; // Exit east to Route 4
        labels.add(new MapLabel("MT. MOON", 2, 2));
        labels.add(new MapLabel("EXIT >", 25, 15));
    }


    private void generateRoute4() {
        currentMap = new int[30][15]; fillMap(0); drawBorder(30, 15);
        for(int x=0; x<30; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for(int x=5; x<20; x++) currentMap[x][5] = 1; // Ledges
        labels.add(new MapLabel("ROUTE 4", 15, 2));
    }




    private void generateCeruleanCity() {
        currentMap = new int[22][20]; fillMap(3); drawBorder(22, 20);
        currentMap[0][9]=3; currentMap[0][10]=3; currentMap[10][19]=3; currentMap[11][19]=3;
        buildHouse(8, 5, 6, 5, 204, "MISTY'S GYM"); buildHouse(16, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CERULEAN CITY", 2, 2));
    }




    private void generateRoute5() {
        currentMap = new int[20][30]; fillMap(0); drawBorder(20, 30);
        for(int y=0; y<30; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for(int x=4; x<9; x++) for(int y=5; y<15; y++) currentMap[x][y] = 4;
        buildHouse(15, 12, 4, 4, 206, "DAYCARE");
        labels.add(new MapLabel("ROUTE 5", 2, 2));
    }




    private void generateSaffronCity() {
        currentMap = new int[25][25]; fillMap(3); drawBorder(25, 25);
        currentMap[10][0]=3; currentMap[11][0]=3;   // North to R5
        currentMap[10][24]=3; currentMap[11][24]=3; // South to R6
        currentMap[0][12]=3; currentMap[0][13]=3;   // West to R7
        currentMap[24][12]=3; currentMap[24][13]=3; // East to R8
        buildHouse(4, 4, 8, 6, 207, "SILPH CO.");
        buildHouse(16, 16, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("SAFFRON CITY", 2, 2));
    }




    private void generateRoute6() {
        currentMap = new int[20][20]; fillMap(0); drawBorder(20, 20);
        for(int y=0; y<20; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for(int x=4; x<8; x++) for(int y=5; y<15; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 6", 2, 2));
    }




    private void generateVermilionCity() {
        currentMap = new int[25][25]; fillMap(3); drawBorder(25, 25);
        currentMap[9][0]=3; currentMap[10][0]=3; currentMap[24][10]=3; currentMap[24][11]=3;
        for(int x=1; x<24; x++) for(int y=20; y<24; y++) currentMap[x][y]=5;
        for(int y=20; y<24; y++) { currentMap[11][y]=3; currentMap[12][y]=3; }
        buildHouse(4, 12, 6, 5, 208, "SURGE'S GYM"); buildHouse(16, 4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("VERMILION CITY", 2, 2));
    }




    private void generateRoute11() {
        currentMap = new int[40][15]; fillMap(0); drawBorder(40, 15);
        for(int x=0; x<40; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for(int x=10; x<30; x++) for(int y=3; y<6; y++) currentMap[x][y] = 4;
        NPC sailor = new NPC("Sailor", 20, 8, "TRAINER", "R11_TR1"); sailor.party = new Pokemon("Machop", "FIGHTING", 16); npcs.add(sailor);
        labels.add(new MapLabel("ROUTE 11", 5, 2));
    }




    private void generateRoute7() {
        currentMap = new int[20][15]; fillMap(0); drawBorder(20, 15);
        for(int x=0; x<20; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for(int x=5; x<15; x++) for(int y=3; y<6; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 7", 5, 2));
    }




    private void generateCeladonCity() {
        currentMap = new int[30][25]; fillMap(3); drawBorder(30, 25);
        currentMap[0][12]=3; currentMap[0][13]=3;   // West to Route 16
        currentMap[29][12]=3; currentMap[29][13]=3; // East to Route 7
        buildHouse(4, 4, 10, 8, 209, "DEPT. STORE");
        buildHouse(10, 16, 6, 5, 210, "ERIKA'S GYM");
        buildHouse(20, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CELADON CITY", 2, 2));
    }




    private void generateRoute8() {
        currentMap = new int[30][15]; fillMap(0); drawBorder(30, 15);
        for(int x=0; x<30; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for(int x=5; x<25; x++) for(int y=9; y<12; y++) currentMap[x][y] = 4;
        NPC biker = new NPC("Biker", 15, 8, "TRAINER", "R8_TR1"); biker.party = new Pokemon("Koffing", "POISON", 20); npcs.add(biker);
        labels.add(new MapLabel("ROUTE 8", 10, 2));
    }




    private void generateLavenderTown() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[0][10]=3; currentMap[0][11]=3; // West to Route 8
        currentMap[9][19]=3; currentMap[10][19]=3; // South to Route 12
        buildHouse(12, 4, 6, 8, 211, "POKÉMON TOWER");
        buildHouse(4, 10, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("LAVENDER TOWN", 2, 2));
    }




    private void generateRoute12() {
        currentMap = new int[20][40]; fillMap(0); drawBorder(20, 40);
        for(int y=0; y<40; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; } // Vertical Path
        for(int x=13; x<19; x++) for(int y=5; y<35; y++) currentMap[x][y] = 5; // Water Coast
        for(int x=4; x<8; x++) for(int y=10; y<30; y++) currentMap[x][y] = 4; // Tall Grass
       
        NPC fisher = new NPC("Fisherman", 8, 20, "TRAINER", "R12_TR1");
        fisher.party = new Pokemon("Magikarp", "WATER", 15);
        npcs.add(fisher);
        labels.add(new MapLabel("ROUTE 12", 2, 5));
    }




    private void generateRoute16() {
        currentMap = new int[40][20]; fillMap(0); drawBorder(40, 20);
        for(int x=0; x<40; x++) { currentMap[x][12] = 3; currentMap[x][13] = 3; } // Horizontal Path
        for(int x=10; x<30; x++) for(int y=4; y<10; y++) currentMap[x][y] = 4; // Tall Grass
       
        NPC biker = new NPC("Roughneck", 20, 14, "TRAINER", "R16_TR1");
        biker.party = new Pokemon("Grimer", "POISON", 22);
        npcs.add(biker);
        labels.add(new MapLabel("ROUTE 16", 15, 2));
    }




    // --- NEW SOUTHERN LOOP MAPS ---
    private void generateRoute15() {
        currentMap = new int[40][20]; fillMap(0); drawBorder(40, 20);
        for(int x=0; x<40; x++) { currentMap[x][9] = 3; currentMap[x][10] = 3; } // Horizontal Path
        for(int x=5; x<35; x++) for(int y=13; y<17; y++) currentMap[x][y] = 4; // Grass
        labels.add(new MapLabel("ROUTE 15", 15, 2));
    }




    private void generateFuchsiaCity() {
        currentMap = new int[30][25]; fillMap(3); drawBorder(30, 25);
        currentMap[29][9]=3; currentMap[29][10]=3; // East to Route 15
        currentMap[14][24]=3; currentMap[15][24]=3; // South to Route 19
       
        buildHouse(4, 14, 6, 5, 212, "KOGA'S GYM");
        buildHouse(20, 4, 6, 5, 214, "WARDEN'S HOUSE");
        buildHouse(14, 4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("FUCHSIA CITY", 2, 2));
    }




    private void generateRoute19() {
        currentMap = new int[20][40]; fillMap(5); drawBorder(20, 40); // ALL WATER
        currentMap[14][0]=5; currentMap[15][0]=5; // North to Fuchsia
        currentMap[14][39]=5; currentMap[15][39]=5; // South to Route 20
        labels.add(new MapLabel("ROUTE 19 (SEA)", 2, 5));
    }




    private void generateRoute20() {
        currentMap = new int[40][20]; fillMap(5); drawBorder(40, 20); // ALL WATER
        currentMap[0][9]=5; currentMap[0][10]=5; // West to Cinnabar
        currentMap[39][9]=5; currentMap[39][10]=5; // East to Route 19
        labels.add(new MapLabel("ROUTE 20 (SEA)", 15, 2));
    }




    private void generateCinnabarIsland() {
        currentMap = new int[20][20]; fillMap(0); drawBorder(20, 20);
        currentMap[9][0]=5; currentMap[10][0]=5; // North to Route 21
        currentMap[19][9]=5; currentMap[19][10]=5; // East to Route 20
       
        buildHouse(12, 4, 6, 5, 213, "BLAINE'S GYM");
        buildHouse(4, 12, 6, 5, 215, "POKÉMON LAB");
        buildHouse(4, 4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CINNABAR ISLAND", 2, 2));
    }




    private void generateRoute21() {
        currentMap = new int[20][40]; fillMap(5); drawBorder(20, 40); // ALL WATER
        currentMap[9][0]=5; currentMap[10][0]=5; // North to Pallet Town
        currentMap[9][39]=5; currentMap[10][39]=5; // South to Cinnabar
        labels.add(new MapLabel("ROUTE 21 (SEA)", 2, 5));
    }




    // --- INTERIORS ---
    private void generateGymPewter() {
        currentMap=new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14]=99;
        NPC brock=new NPC("Brock", 7, 2, "LEADER", "LEADER"); brock.party=new Pokemon("Onix","ROCK",14); npcs.add(brock);
    }
    private void generateGymCerulean() {
        currentMap=new int[15][18]; fillMap(3); drawBorder(15, 18);
        for(int x=2; x<=12; x++) for(int y=4; y<=12; y++) currentMap[x][y]=5;
        for(int y=4; y<=12; y++) currentMap[7][y]=3; currentMap[7][17]=99;
        NPC misty=new NPC("Misty", 7, 3, "LEADER", "LEADER_MISTY"); misty.party=new Pokemon("Starmie","WATER",21); npcs.add(misty);
    }
    private void generateGymVermilion() {
        currentMap=new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14]=99;
        NPC surge=new NPC("Lt. Surge", 7, 2, "LEADER", "LEADER_SURGE"); surge.party=new Pokemon("Raichu","ELECTRIC",24); npcs.add(surge);
    }
    private void generateGymCeladon() {
        currentMap=new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14]=99;
        for(int x=2; x<=12; x++) for(int y=4; y<=12; y++) if(x!=7) currentMap[x][y]=1;
        NPC erika=new NPC("Erika", 7, 2, "LEADER", "LEADER_ERIKA"); erika.party=new Pokemon("Vileplume","GRASS",29); npcs.add(erika);
    }
    private void generateGymFuchsia() {
        currentMap=new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14]=99;
        for(int x=2; x<=12; x+=2) for(int y=4; y<=10; y++) currentMap[x][y]=1; // Invisible walls (kinda)
        NPC koga=new NPC("Koga", 7, 2, "LEADER", "LEADER_KOGA"); koga.party=new Pokemon("Weezing","POISON",37); npcs.add(koga);
    }
    private void generateGymCinnabar() {
        currentMap=new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14]=99;
        NPC blaine=new NPC("Blaine", 7, 2, "LEADER", "LEADER_BLAINE"); blaine.party=new Pokemon("Arcanine","FIRE",47); npcs.add(blaine);
    }
    private void generateCenter() {
        currentMap=new int[12][10]; fillMap(3); drawBorder(12, 10);
        for(int x=4; x<=7; x++) currentMap[x][3]=2; currentMap[5][9]=98; currentMap[6][9]=98;
        npcs.add(new NPC("Nurse Joy", 5, 2, "HEALER", "JOY"));
    }




    private void fillMap(int val) { for(int x=0; x<currentMap.length; x++) for(int y=0; y<currentMap[0].length; y++) currentMap[x][y]=val; }
    private void drawBorder(int w, int h) { for(int x=0; x<w; x++) { currentMap[x][0]=1; currentMap[x][h-1]=1; } for(int y=0; y<h; y++) { currentMap[0][y]=1; currentMap[w-1][y]=1; } }
    private void buildHouse(int x, int y, int w, int h, int doorID, String labelText) {
        for(int i=x; i<x+w; i++) for(int j=y; j<y+h; j++) currentMap[i][j] = 2;
        currentMap[x + (w/2)][y + h - 1] = doorID;
        labels.add(new MapLabel(labelText, x + (w/2), y - 1));
    }




    private void generateWardenHouse() {
    currentMap = new int[12][12];
    fillMap(3); // Fill with floor tiles
    drawBorder(12, 12);
 
    // Add the exit at the bottom
    currentMap[5][11] = 99;
    currentMap[6][11] = 99;
 
    // Place the Warden inside
    npcs.add(new NPC("Warden", 5, 4, "WARDEN", "NONE"));
    }
    private void generateMart() {
    currentMap = new int[12][10];
    fillMap(3);
    drawBorder(12, 10);


    // Exit tiles
    currentMap[5][9] = 98;
    currentMap[6][9] = 98;


    // Clerk NPC
    npcs.add(new NPC("Clerk", 5, 3, "MART", "SHOP"));
    }




    private void movePlayer(int key) {
        int dx=0, dy=0;
        if(key==KeyEvent.VK_W) dy=-1; if(key==KeyEvent.VK_S) dy=1;
        if(key==KeyEvent.VK_A) dx=-1; if(key==KeyEvent.VK_D) dx=1;
        if(key==KeyEvent.VK_SPACE) { interact(); return; }




        int nx = playerX+dx, ny = playerY+dy;
       
        // GEOGRAPHICAL TRANSITIONS
        // Pallet Town
        if(currentMapId == MAP_PALLET && ny < 0) { loadMap(MAP_ROUTE1, 6, 28); return; }
        if(currentMapId == MAP_PALLET && ny >= 15) { loadMap(MAP_ROUTE21, 9, 1); return; } // South to Sea
       
        if(currentMapId == MAP_ROUTE1 && ny >= 30) { loadMap(MAP_PALLET, 9, 1); return; }
        if(currentMapId == MAP_ROUTE1 && ny < 0) { loadMap(MAP_VIRIDIAN, 9, 18); return; }
       
        if(currentMapId == MAP_VIRIDIAN && ny >= 20) { loadMap(MAP_ROUTE1, 6, 1); return; }
        if(currentMapId == MAP_VIRIDIAN && ny < 0) { loadMap(MAP_ROUTE2, 7, 28); return; }
       
        if(currentMapId == MAP_ROUTE2 && ny >= 30) { loadMap(MAP_VIRIDIAN, 9, 1); return; }
        if(currentMapId == MAP_ROUTE2 && ny < 0) { loadMap(MAP_PEWTER, 9, 18); return; }
       
        if(currentMapId == MAP_PEWTER && ny >= 20) { loadMap(MAP_ROUTE2, 7, 1); return; }
        if(currentMapId == MAP_PEWTER && nx >= 20) { loadMap(MAP_ROUTE3, 1, 8); return; }
       
        if(currentMapId == MAP_ROUTE3 && nx < 0) { loadMap(MAP_PEWTER, 18, 10); return; }
        if(currentMapId == MAP_ROUTE3 && nx >= 40) { loadMap(MAP_MT_MOON, 2, 15); return; }




        if(currentMapId == MAP_MT_MOON && nx >= 30) { loadMap(MAP_ROUTE4, 2, 8); return; }
       
        if(currentMapId == MAP_ROUTE4 && nx < 0) { loadMap(MAP_MT_MOON, 28, 14); return; }
        if(currentMapId == MAP_ROUTE4 && nx >= 30) { loadMap(MAP_CERULEAN, 1, 10); return; }
       
        if(currentMapId == MAP_CERULEAN && nx < 0) { loadMap(MAP_ROUTE4, 28, 8); return; }
        if(currentMapId == MAP_CERULEAN && ny >= 20) { loadMap(MAP_ROUTE5, 9, 1); return; }




        if(currentMapId == MAP_ROUTE5 && ny < 0) { loadMap(MAP_CERULEAN, 10, 18); return; }
        if(currentMapId == MAP_ROUTE5 && ny >= 30) { loadMap(MAP_SAFFRON, 10, 1); return; }
       
        if(currentMapId == MAP_SAFFRON && ny < 0) { loadMap(MAP_ROUTE5, 9, 28); return; }
        if(currentMapId == MAP_SAFFRON && ny >= 25) { loadMap(MAP_ROUTE6, 9, 1); return; }
       
        if(currentMapId == MAP_ROUTE6 && ny < 0) { loadMap(MAP_SAFFRON, 10, 23); return; }
        if(currentMapId == MAP_ROUTE6 && ny >= 20) { loadMap(MAP_VERMILION, 9, 1); return; }
       
        if(currentMapId == MAP_VERMILION && ny < 0) { loadMap(MAP_ROUTE6, 9, 18); return; }
        if(currentMapId == MAP_VERMILION && nx >= 25) { loadMap(MAP_ROUTE11, 1, 7); return; }
       
        if(currentMapId == MAP_ROUTE11 && nx < 0) { loadMap(MAP_VERMILION, 23, 10); return; }
       
        if(currentMapId == MAP_SAFFRON && nx < 0) { loadMap(MAP_ROUTE7, 18, 7); return; }
        if(currentMapId == MAP_ROUTE7 && nx >= 20) { loadMap(MAP_SAFFRON, 1, 12); return; }
        if(currentMapId == MAP_ROUTE7 && nx < 0) { loadMap(MAP_CELADON, 28, 12); return; }
       
        if(currentMapId == MAP_CELADON && nx >= 30) { loadMap(MAP_ROUTE7, 1, 7); return; }
        if(currentMapId == MAP_SAFFRON && nx >= 25) { loadMap(MAP_ROUTE8, 1, 7); return; }
       
        if(currentMapId == MAP_ROUTE8 && nx < 0) { loadMap(MAP_SAFFRON, 23, 12); return; }
        if(currentMapId == MAP_ROUTE8 && nx >= 30) { loadMap(MAP_LAVENDER, 1, 10); return; }
        if(currentMapId == MAP_LAVENDER && nx < 0) { loadMap(MAP_ROUTE8, 28, 7); return; }
       
        if(currentMapId == MAP_FOREST && ny < 0) { loadMap(MAP_ROUTE2, 8, 2); return; }
        if(currentMapId == MAP_FOREST && ny >= 30) { loadMap(MAP_ROUTE2, 8, 15); return; }
       
        if(currentMapId == MAP_CELADON && nx < 0) { loadMap(MAP_ROUTE16, 38, 12); return; }
        if(currentMapId == MAP_ROUTE16 && nx >= 40) { loadMap(MAP_CELADON, 1, 12); return; }




        if(currentMapId == MAP_LAVENDER && ny >= 20) { loadMap(MAP_ROUTE12, 9, 1); return; }
        if(currentMapId == MAP_ROUTE12 && ny < 0) { loadMap(MAP_LAVENDER, 9, 18); return; }




        // --- NEW SOUTHERN LOOP TRANSITIONS ---
        // Route 12 -> Route 15
        if(currentMapId == MAP_ROUTE12 && ny >= 40) { loadMap(MAP_ROUTE15, 38, 9); return; }
        if(currentMapId == MAP_ROUTE15 && nx >= 40) { loadMap(MAP_ROUTE12, 9, 38); return; }




        // Route 15 -> Fuchsia City
        if(currentMapId == MAP_ROUTE15 && nx < 0) { loadMap(MAP_FUCHSIA, 28, 9); return; }
        if(currentMapId == MAP_FUCHSIA && nx >= 30) { loadMap(MAP_ROUTE15, 1, 9); return; }




        // Fuchsia City -> Route 19 (South)
        if(currentMapId == MAP_FUCHSIA && ny >= 25) { loadMap(MAP_ROUTE19, 14, 1); return; }
        if(currentMapId == MAP_ROUTE19 && ny < 0) { loadMap(MAP_FUCHSIA, 14, 23); return; }




        // Route 19 -> Route 20
        if(currentMapId == MAP_ROUTE19 && ny >= 40) { loadMap(MAP_ROUTE20, 38, 9); return; }
        if(currentMapId == MAP_ROUTE20 && nx >= 40) { loadMap(MAP_ROUTE19, 14, 38); return; }




        // Route 20 -> Cinnabar Island
        if(currentMapId == MAP_ROUTE20 && nx < 0) { loadMap(MAP_CINNABAR, 18, 9); return; }
        if(currentMapId == MAP_CINNABAR && nx >= 20) { loadMap(MAP_ROUTE20, 1, 9); return; }




        // Cinnabar Island -> Route 21 (North)
        if(currentMapId == MAP_CINNABAR && ny < 0) { loadMap(MAP_ROUTE21, 9, 38); return; }
        if(currentMapId == MAP_ROUTE21 && ny >= 40) { loadMap(MAP_CINNABAR, 9, 1); return; }




        // Route 21 -> Pallet Town
        if(currentMapId == MAP_ROUTE21 && ny < 0) { loadMap(MAP_PALLET, 9, 13); return; }








        if(nx<0 || ny<0 || nx>=currentMap.length || ny>=currentMap[0].length) return;
       
        // NEW SURF LOGIC
        if(currentMap[nx][ny] == 5 && !canSurf) return; // Blocked if you can't surf!
       
        if(currentMap[nx][ny] == 1 || currentMap[nx][ny] == 2 || currentMap[nx][ny] == 6) return;




        for(NPC n : npcs) {
            if(n.x == nx && n.y == ny) {
                if(n.type.equals("TRAINER") || n.type.equals("LEADER")) {
                    game.startBattle(n.party, true, n.name, n.id);
                } else if(n.type.equals("HEALER")) {
                    for(Pokemon p : GameLauncher.party) p.healFull();
                    JOptionPane.showMessageDialog(this, "Nurse Joy: Your Pokémon are fully healed!");
                }
                return;
            }
        }




        int tile = currentMap[nx][ny];
        if(tile == 201) { loadMap(MAP_GYM_PEWTER, 7, 13); return; }
        if(tile == 204) { loadMap(MAP_GYM_CERULEAN, 7, 16); return; }
        if(tile == 205) { loadMap(MAP_FOREST, 10, 28); return; }
        if(tile == 208) { loadMap(MAP_GYM_VERMILION, 7, 13); return; }
        if(tile == 210) { loadMap(MAP_GYM_CELADON, 7, 13); return; }
        if(tile == 212) { loadMap(MAP_GYM_FUCHSIA, 7, 13); return; }
        if(tile == 213) { loadMap(MAP_GYM_CINNABAR, 7, 13); return; }
        if(tile == 203) { returnMapId = currentMapId; loadMap(MAP_CENTER, 5, 8); return; }
        if(tile == 214) {
        loadMap(MAP_WARDEN_HOUSE, 5, 10);
        return;
        }


        if(tile == 202) {
            returnMapId = currentMapId;
            loadMap(MAP_MART, 5, 8);
            return;
        }
        if(tile == 99 || tile == 98) {
            if(tile == 98) loadMap(returnMapId, 14, 14);
            else if(currentMapId == MAP_GYM_PEWTER) loadMap(MAP_PEWTER, 9, 10);
            else if(currentMapId == MAP_GYM_CERULEAN) loadMap(MAP_CERULEAN, 11, 9);
            else if(currentMapId == MAP_GYM_VERMILION) loadMap(MAP_VERMILION, 7, 17);
            else if(currentMapId == MAP_GYM_CELADON) loadMap(MAP_CELADON, 13, 21);
            else if(currentMapId == MAP_GYM_FUCHSIA) loadMap(MAP_FUCHSIA, 7, 19);
            else if(currentMapId == MAP_GYM_CINNABAR) loadMap(MAP_CINNABAR, 15, 9);
            else if(currentMapId == MAP_MT_MOON) loadMap(MAP_ROUTE4, 2, 7);
            else if(currentMapId == MAP_WARDEN_HOUSE) loadMap(MAP_FUCHSIA, 23, 9);
            return;
             
            }




        playerX = nx; playerY = ny;
        repaint();
       
        if(currentMap[nx][ny] == 4 && Math.random() < 0.15) {
             game.startBattle(Pokemon.generateWild(4, 12), false, "Wild Pokemon", "WILD");
        }
        else if (currentMap[nx][ny] == 5 && Math.random() < 0.10) {
             game.startBattle(new Pokemon("Tentacool", "WATER", 20), false, "Wild Pokemon", "WILD");
        }
    }




    private void interact() {
        for(NPC n : npcs) {
            if(Math.abs(n.x - playerX) <= 1 && Math.abs(n.y - playerY) <= 1) {
                if(n.type.equals("OAK")) {
                    if(!GameLauncher.hasStarter) {
                        String[] starters = {"Charmander", "Squirtle", "Bulbasaur"};
                        int c = JOptionPane.showOptionDialog(this, "Choose!", "Oak", 0,3,null,starters,0);
                        if(c==0) GameLauncher.party.add(new Pokemon("Charmander","FIRE",5));
                        if(c==1) GameLauncher.party.add(new Pokemon("Squirtle","WATER",5));
                        if(c==2) GameLauncher.party.add(new Pokemon("Bulbasaur","GRASS",5));
                        if(c!=-1) GameLauncher.hasStarter=true;
                    }
                }
                else if (n.type.equals("WARDEN")) {
                    if (!canSurf) {
                        JOptionPane.showMessageDialog(this, "Safari Warden: Thanks for finding my Gold Teeth! Take this HM!\n\n(You can now SURF on water tiles!)");
                        canSurf = true;
                    } else {
                        JOptionPane.showMessageDialog(this, "Safari Warden: Enjoy surfing around Kanto!");
                    }
                }
                else if (n.type.equals("MART")) {
                    String[] items = {"Potion - $300", "Poké Ball - $200", "Cancel"};
                    JOptionPane.showOptionDialog(
                            this,
                            "Welcome to the Poké Mart! What would you like?",
                            "Poké Mart",
                            0,
                            3,
                            null,
                            items,
                            items[0]
                        );
                    }
            }
        }
    }




    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        int camX = (800/2) - (playerX * TILE_SIZE);
        int camY = (600/2) - (playerY * TILE_SIZE);




        for(int x=0; x<currentMap.length; x++) {
            for(int y=0; y<currentMap[0].length; y++) {
                int id = currentMap[x][y];
                Color c = Color.BLACK;
                if(id==0) c = new Color(50,205,50); // Grass
                else if(id==4) c = new Color(0,100,0); // Tall Grass
                else if(id==3) c = Color.LIGHT_GRAY; // Path
                else if(id==1) c = Color.GRAY; // Wall/Tree
                else if(id==2) c = new Color(178,34,34); // Building
                else if(id==5) c = new Color(30,144,255); // Water
               
                g.setColor(c);
                g.fillRect(x*TILE_SIZE+camX, y*TILE_SIZE+camY, TILE_SIZE, TILE_SIZE);
            }
        }
        for(NPC n : npcs) {
            if(n.type.equals("HEALER")) g.setColor(Color.PINK);
            else if(n.type.equals("TRAINER")) g.setColor(Color.CYAN);
            else if(n.type.equals("LEADER")) g.setColor(Color.MAGENTA);
            else if(n.type.equals("WARDEN")) g.setColor(Color.YELLOW);
            else g.setColor(Color.ORANGE);
            g.fillOval(n.x*TILE_SIZE+camX+5, n.y*TILE_SIZE+camY+5, TILE_SIZE-10, TILE_SIZE-10);
        }
       
        // Draw Player (Blue if surfing, Red if walking)
        if (currentMap[playerX][playerY] == 5) g.setColor(new Color(135, 206, 250)); // Light Blue
        else g.setColor(Color.RED);
       
        g.fillOval(playerX*TILE_SIZE+camX+5, playerY*TILE_SIZE+camY+5, TILE_SIZE-10, TILE_SIZE-10);
       
        g.setColor(Color.WHITE); g.setFont(new Font("Arial",Font.BOLD,14));
        for(MapLabel l : labels) g.drawString(l.text, l.x*TILE_SIZE+camX, l.y*TILE_SIZE+camY);




        if (showPokedex) {
            g.setColor(new Color(0, 0, 0, 180)); g.fillRect(0, 0, 800, 600);
            g.setColor(new Color(220, 20, 60)); g.fillRoundRect(100, 50, 600, 500, 20, 20);
            g.setColor(Color.WHITE); g.drawRoundRect(100, 50, 600, 500, 20, 20);
            g.setColor(new Color(30, 30, 30)); g.fillRect(150, 150, 500, 300);
            g.setColor(Color.WHITE); g.setFont(new Font("Monospaced", Font.BOLD, 36));
            g.drawString("POKÉDEX", 320, 110);
            g.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g.setColor(Color.GREEN); g.drawString("001. Bulbasaur  [SEEN]", 170, 190); g.drawString("004. Charmander [OWNED]", 170, 220);
            g.setColor(Color.GRAY); g.drawString("007. Squirtle   [???]", 170, 250); g.drawString("025. Pikachu    [???]", 170, 280);
            g.setColor(Color.WHITE); g.setFont(new Font("Arial", Font.ITALIC, 16)); g.drawString("Press 'P' to Close", 330, 520);
        }
    }
    class NPC { String name, type, id; int x, y; Pokemon party; NPC(String n, int x, int y, String t, String i){name=n;this.x=x;this.y=y;type=t;id=i;} }
    class MapLabel { String text; int x, y; MapLabel(String t, int x, int y){text=t;this.x=x;this.y=y;} }
}






