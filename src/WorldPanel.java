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
    private boolean canSurf = false;

    // Map Constants — ordered as in Pokémon Red
    private final int MAP_PALLET        = 0;
    private final int MAP_ROUTE1        = 1;   // Pallet ↔ Viridian
    private final int MAP_VIRIDIAN      = 2;
    private final int MAP_ROUTE2_S      = 3;   // Viridian ↔ Viridian Forest entrance
    private final int MAP_FOREST        = 4;   // Viridian Forest
    private final int MAP_ROUTE2_N      = 5;   // Forest exit ↔ Pewter
    private final int MAP_PEWTER        = 6;
    private final int MAP_ROUTE3        = 7;   // Pewter → Mt. Moon
    private final int MAP_MT_MOON       = 8;
    private final int MAP_ROUTE4        = 9;   // Mt. Moon → Cerulean
    private final int MAP_CERULEAN      = 10;
    private final int MAP_ROUTE24       = 11;  // Cerulean north — Nugget Bridge
    private final int MAP_ROUTE25       = 12;  // Route 24 east — Bill's House
    private final int MAP_ROUTE5        = 13;  // Cerulean south ↔ Saffron
    private final int MAP_SAFFRON       = 14;
    private final int MAP_ROUTE6        = 15;  // Saffron ↔ Vermilion
    private final int MAP_VERMILION     = 16;
    private final int MAP_ROUTE11       = 17;  // Vermilion east
    private final int MAP_ROUTE9        = 18;  // Cerulean east → Rock Tunnel
    private final int MAP_ROUTE10       = 19;  // Rock Tunnel → Lavender
    private final int MAP_ROUTE7        = 20;  // Celadon ↔ Saffron
    private final int MAP_CELADON       = 21;
    private final int MAP_ROUTE8        = 22;  // Saffron ↔ Lavender
    private final int MAP_LAVENDER      = 23;
    private final int MAP_ROUTE12       = 24;  // Lavender south
    private final int MAP_ROUTE16       = 25;  // Celadon west → Cycling Road
    private final int MAP_ROUTE13       = 26;  // Route 12 tip → Route 14
    private final int MAP_ROUTE14       = 27;  // Route 13 → Route 15
    private final int MAP_ROUTE15       = 28;  // Route 14 ↔ Fuchsia west
    private final int MAP_FUCHSIA       = 29;
    private final int MAP_ROUTE17       = 30;  // Cycling Road (R16 → R18)
    private final int MAP_ROUTE18       = 31;  // Cycling Road east gate → Fuchsia
    private final int MAP_ROUTE19       = 32;  // Fuchsia south sea
    private final int MAP_ROUTE20       = 33;  // Seafoam → Cinnabar sea
    private final int MAP_CINNABAR      = 34;
    private final int MAP_ROUTE21       = 35;  // Cinnabar → Pallet sea
    private final int MAP_GYM_PEWTER    = 36;
    private final int MAP_GYM_CERULEAN  = 37;
    private final int MAP_GYM_VERMILION = 38;
    private final int MAP_GYM_CELADON   = 39;
    private final int MAP_GYM_FUCHSIA   = 40;
    private final int MAP_GYM_CINNABAR  = 41;
    private final int MAP_CENTER        = 42;
    private final int MAP_MART          = 43;
    private final int MAP_WARDEN_HOUSE  = 44;

    private static final int TOTAL_MAPS = 45;

    private int currentMapId = MAP_PALLET;
    private int returnMapId  = MAP_VIRIDIAN;
    private int[][] currentMap;
    private List<NPC>      npcs   = new ArrayList<>();
    private List<MapLabel> labels = new ArrayList<>();

    private int[][][] allMaps = new int[TOTAL_MAPS][][];
    private List<List<NPC>>      allNPCs   = new ArrayList<>();
    private List<List<MapLabel>> allLabels = new ArrayList<>();

    private int playerX = 10, playerY = 10;
    private boolean moving = false;

    public WorldPanel(GameLauncher game)
    {
        this.game = game;

        for (int i = 0; i < TOTAL_MAPS; i++) {
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
                if (showPokedex) {
                    if (e.getKeyCode() == KeyEvent.VK_P || e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                        showPokedex = false; repaint();
                    }
                    return;
                }
                if (e.getKeyCode() == KeyEvent.VK_P) { showPokedex = true; repaint(); return; }
                if (e.getKeyCode() == KeyEvent.VK_B) { game.openInventory(); return; }
                if (!moving) movePlayer(e.getKeyCode());
            }
        });
    }

    public void refreshMap() { loadMapInternal(currentMapId, playerX, playerY, false); }
    public void respawn()    { loadMap(MAP_PALLET, 10, 10); }
    public int  getMapId()   { return currentMapId; }
    public int  getPlayerX() { return playerX; }
    public int  getPlayerY() { return playerY; }
    public int  getMapWidth()  { return currentMap != null ? currentMap.length    : 20; }
    public int  getMapHeight() { return currentMap != null ? currentMap[0].length : 20; }

    private void loadMap(int mapId, int startX, int startY) { loadMapInternal(mapId, startX, startY, true); }

    private void loadMapInternal(int mapId, int startX, int startY, boolean doMove)
    {
        currentMapId = mapId;
        if (doMove) { playerX = startX; playerY = startY; }

        if (allMaps[mapId] == null) {
            npcs.clear(); labels.clear();
            switch (mapId) {
                case MAP_PALLET:        generatePalletTown();    break;
                case MAP_ROUTE1:        generateRoute1();        break;
                case MAP_VIRIDIAN:      generateViridian();      break;
                case MAP_ROUTE2_S:      generateRoute2S();       break;
                case MAP_FOREST:        generateForest();        break;
                case MAP_ROUTE2_N:      generateRoute2N();       break;
                case MAP_PEWTER:        generatePewter();        break;
                case MAP_ROUTE3:        generateRoute3();        break;
                case MAP_MT_MOON:       generateMtMoon();        break;
                case MAP_ROUTE4:        generateRoute4();        break;
                case MAP_CERULEAN:      generateCeruleanCity();  break;
                case MAP_ROUTE24:       generateRoute24();       break;
                case MAP_ROUTE25:       generateRoute25();       break;
                case MAP_ROUTE5:        generateRoute5();        break;
                case MAP_SAFFRON:       generateSaffronCity();   break;
                case MAP_ROUTE6:        generateRoute6();        break;
                case MAP_VERMILION:     generateVermilionCity(); break;
                case MAP_ROUTE11:       generateRoute11();       break;
                case MAP_ROUTE9:        generateRoute9();        break;
                case MAP_ROUTE10:       generateRoute10();       break;
                case MAP_ROUTE7:        generateRoute7();        break;
                case MAP_CELADON:       generateCeladonCity();   break;
                case MAP_ROUTE8:        generateRoute8();        break;
                case MAP_LAVENDER:      generateLavenderTown();  break;
                case MAP_ROUTE12:       generateRoute12();       break;
                case MAP_ROUTE16:       generateRoute16();       break;
                case MAP_ROUTE13:       generateRoute13();       break;
                case MAP_ROUTE14:       generateRoute14();       break;
                case MAP_ROUTE15:       generateRoute15();       break;
                case MAP_FUCHSIA:       generateFuchsiaCity();   break;
                case MAP_ROUTE17:       generateRoute17();       break;
                case MAP_ROUTE18:       generateRoute18();       break;
                case MAP_ROUTE19:       generateRoute19();       break;
                case MAP_ROUTE20:       generateRoute20();       break;
                case MAP_CINNABAR:      generateCinnabarIsland();break;
                case MAP_ROUTE21:       generateRoute21();       break;
                case MAP_GYM_PEWTER:    generateGymPewter();     break;
                case MAP_GYM_CERULEAN:  generateGymCerulean();   break;
                case MAP_GYM_VERMILION: generateGymVermilion();  break;
                case MAP_GYM_CELADON:   generateGymCeladon();    break;
                case MAP_GYM_FUCHSIA:   generateGymFuchsia();    break;
                case MAP_GYM_CINNABAR:  generateGymCinnabar();   break;
                case MAP_CENTER:        generateCenter();        break;
                case MAP_MART:          generateMart();          break;
                case MAP_WARDEN_HOUSE:  generateWardenHouse();   break;
            }
            allMaps[mapId]   = currentMap;
            allNPCs.set(mapId,   new ArrayList<>(npcs));
            allLabels.set(mapId, new ArrayList<>(labels));
        } else {
            currentMap = allMaps[mapId];
            npcs       = allNPCs.get(mapId);
            labels     = allLabels.get(mapId);
        }
        repaint();
    }

    // -----------------------------------------------------------------------
    // MAP GENERATION
    // -----------------------------------------------------------------------
    private void generatePalletTown() {
        currentMap = new int[20][15]; fillMap(0); drawBorder(20, 15);
        currentMap[9][0] = 0; currentMap[10][0] = 0;
        currentMap[9][14] = 5; currentMap[10][14] = 5;
        buildHouse(12, 8, 5, 4, 200, "OAK'S LAB");
        npcs.add(new NPC("Oak", 14, 13, "OAK", "NONE"));
        labels.add(new MapLabel("PALLET TOWN", 2, 2));
    }

    private void generateRoute1() {
        currentMap = new int[14][30]; fillMap(0); drawBorder(14, 30);
        for (int y = 0; y < 30; y++) { currentMap[6][y] = 3; currentMap[7][y] = 3; }
        for (int y = 5; y < 25; y++) { currentMap[3][y] = 4; currentMap[4][y] = 4; currentMap[9][y] = 4; currentMap[10][y] = 4; }
        labels.add(new MapLabel("ROUTE 1", 2, 2));
    }

    private void generateViridian() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[9][19] = 3; currentMap[10][19] = 3; currentMap[9][0] = 3; currentMap[10][0] = 3;
        buildHouse(14,  4, 4, 4, 202, "MART");
        buildHouse(14, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("VIRIDIAN CITY", 2, 2));
    }

    // Route 2 south — Viridian City to Viridian Forest south entrance
    private void generateRoute2S() {
        currentMap = new int[16][20]; fillMap(0); drawBorder(16, 20);
        for (int y = 0; y < 20; y++) { currentMap[7][y] = 3; currentMap[8][y] = 3; }
        for (int x = 3; x < 6; x++) for (int y = 5; y < 15; y++) currentMap[x][y] = 4;
        buildHouse(9, 12, 5, 4, 205, "FOREST GATE");
        labels.add(new MapLabel("ROUTE 2 (S)", 2, 2));
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
        currentMap[9][29] = 3; currentMap[10][29] = 3; currentMap[9][0] = 3; currentMap[10][0] = 3;
        NPC bug = new NPC("Bug Catcher", 8, 20, "TRAINER", "BUG1");
        bug.party = new Pokemon("Caterpie", "BUG", 6);
        npcs.add(bug);
        labels.add(new MapLabel("VIRIDIAN FOREST", 2, 2));
    }

    // Route 2 north — Forest north exit to Pewter City
    private void generateRoute2N() {
        currentMap = new int[16][20]; fillMap(0); drawBorder(16, 20);
        for (int y = 0; y < 20; y++) { currentMap[7][y] = 3; currentMap[8][y] = 3; }
        for (int x = 10; x < 14; x++) for (int y = 5; y < 15; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 2 (N)", 2, 2));
    }

    private void generatePewter() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[9][19] = 3; currentMap[10][19] = 3; currentMap[19][9] = 3; currentMap[19][10] = 3;
        buildHouse(4,  4, 6, 5, 201, "BROCK'S GYM");
        buildHouse(14, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("PEWTER CITY", 2, 2));
    }

    private void generateRoute3() {
        currentMap = new int[40][15]; fillMap(0); drawBorder(40, 15);
        for (int x = 0; x < 40; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 12; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        NPC youngster = new NPC("Youngster", 15, 6, "TRAINER", "R3_TR1");
        youngster.party = new Pokemon("Rattata", "NORMAL", 11);
        npcs.add(youngster);
        labels.add(new MapLabel("ROUTE 3", 2, 5));
    }

    private void generateMtMoon() {
        currentMap = new int[30][30]; fillMap(3); drawBorder(30, 30);
        java.util.Random sr = new java.util.Random(12345);
        for (int i = 0; i < 40; i++) currentMap[sr.nextInt(26) + 2][sr.nextInt(26) + 2] = 1;
        NPC rocket = new NPC("Rocket Grunt", 15, 15, "TRAINER", "MTM_R1");
        rocket.party = new Pokemon("Zubat", "POISON", 12);
        npcs.add(rocket);
        currentMap[29][14] = 99;
        labels.add(new MapLabel("MT. MOON", 2, 2));
        labels.add(new MapLabel("EXIT >", 25, 15));
    }

    private void generateRoute4() {
        currentMap = new int[30][15]; fillMap(0); drawBorder(30, 15);
        for (int x = 0; x < 30; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 20; x++) currentMap[x][5] = 1;
        labels.add(new MapLabel("ROUTE 4", 15, 2));
    }

    private void generateCeruleanCity() {
        currentMap = new int[22][20]; fillMap(3); drawBorder(22, 20);
        currentMap[0][9]   = 3; currentMap[0][10]  = 3;  // West  ← Route 4
        currentMap[10][0]  = 3; currentMap[11][0]  = 3;  // North → Route 24
        currentMap[21][9]  = 3; currentMap[21][10] = 3;  // East  → Route 9
        currentMap[10][19] = 3; currentMap[11][19] = 3;  // South → Route 5
        buildHouse(8,  5, 6, 5, 204, "MISTY'S GYM");
        buildHouse(16, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CERULEAN CITY", 2, 2));
    }

    // Route 24 — Nugget Bridge, north of Cerulean
    private void generateRoute24() {
        currentMap = new int[16][25]; fillMap(0); drawBorder(16, 25);
        for (int y = 0; y < 25; y++) { currentMap[7][y] = 3; currentMap[8][y] = 3; }
        for (int x = 3; x < 7; x++) for (int y = 8; y < 18; y++) currentMap[x][y] = 4;
        NPC camper = new NPC("Camper", 8, 12, "TRAINER", "R24_TR1");
        camper.party = new Pokemon("Oddish", "GRASS", 14);
        npcs.add(camper);
        labels.add(new MapLabel("ROUTE 24", 2, 2));
    }

    // Route 25 — east along cliff to Bill's House
    private void generateRoute25() {
        currentMap = new int[30][15]; fillMap(0); drawBorder(30, 15);
        for (int x = 0; x < 30; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 25; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        NPC lass = new NPC("Lass", 20, 8, "TRAINER", "R25_TR1");
        lass.party = new Pokemon("Clefairy", "NORMAL", 16);
        npcs.add(lass);
        labels.add(new MapLabel("ROUTE 25 - BILL'S HOUSE", 5, 2));
    }

    private void generateRoute5() {
        currentMap = new int[20][30]; fillMap(0); drawBorder(20, 30);
        for (int y = 0; y < 30; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 4; x < 9; x++) for (int y = 5; y < 15; y++) currentMap[x][y] = 4;
        buildHouse(15, 12, 4, 4, 206, "DAYCARE");
        labels.add(new MapLabel("ROUTE 5", 2, 2));
    }

    private void generateSaffronCity() {
        currentMap = new int[25][25]; fillMap(3); drawBorder(25, 25);
        currentMap[10][0]  = 3; currentMap[11][0]  = 3;
        currentMap[10][24] = 3; currentMap[11][24] = 3;
        currentMap[0][12]  = 3; currentMap[0][13]  = 3;
        currentMap[24][12] = 3; currentMap[24][13] = 3;
        buildHouse(4,  4, 8, 6, 207, "SILPH CO.");
        buildHouse(16, 16, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("SAFFRON CITY", 2, 2));
    }

    private void generateRoute6() {
        currentMap = new int[20][20]; fillMap(0); drawBorder(20, 20);
        for (int y = 0; y < 20; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 4; x < 8; x++) for (int y = 5; y < 15; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 6", 2, 2));
    }

    private void generateVermilionCity() {
        currentMap = new int[25][25]; fillMap(3); drawBorder(25, 25);
        currentMap[9][0]   = 3; currentMap[10][0]  = 3;
        currentMap[24][10] = 3; currentMap[24][11] = 3;
        for (int x = 1; x < 24; x++) for (int y = 20; y < 24; y++) currentMap[x][y] = 5;
        for (int y = 20; y < 24; y++) { currentMap[11][y] = 3; currentMap[12][y] = 3; }
        buildHouse(4,  12, 6, 5, 208, "SURGE'S GYM");
        buildHouse(16,  4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("VERMILION CITY", 2, 2));
    }

    private void generateRoute11() {
        currentMap = new int[40][15]; fillMap(0); drawBorder(40, 15);
        for (int x = 0; x < 40; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 10; x < 30; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        NPC sailor = new NPC("Sailor", 20, 8, "TRAINER", "R11_TR1");
        sailor.party = new Pokemon("Machop", "FIGHTING", 16);
        npcs.add(sailor);
        labels.add(new MapLabel("ROUTE 11", 5, 2));
    }

    // Route 9 — east of Cerulean, leads into Rock Tunnel area
    private void generateRoute9() {
        currentMap = new int[40][15]; fillMap(0); drawBorder(40, 15);
        for (int x = 0; x < 40; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 8; x < 32; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        NPC hiker = new NPC("Hiker", 20, 8, "TRAINER", "R9_TR1");
        hiker.party = new Pokemon("Geodude", "ROCK", 16);
        npcs.add(hiker);
        labels.add(new MapLabel("ROUTE 9", 5, 2));
    }

    // Route 10 — Rock Tunnel, connects Route 9 south to Lavender Town
    private void generateRoute10() {
        currentMap = new int[20][40]; fillMap(3); drawBorder(20, 40);
        for (int y = 1; y < 39; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        java.util.Random sr = new java.util.Random(99999);
        for (int i = 0; i < 25; i++) currentMap[sr.nextInt(16) + 2][sr.nextInt(36) + 2] = 1;
        NPC rockHiker = new NPC("Hiker", 10, 20, "TRAINER", "R10_TR1");
        rockHiker.party = new Pokemon("Onix", "ROCK", 20);
        npcs.add(rockHiker);
        labels.add(new MapLabel("ROCK TUNNEL / ROUTE 10", 2, 2));
    }

    private void generateRoute7() {
        currentMap = new int[20][15]; fillMap(0); drawBorder(20, 15);
        for (int x = 0; x < 20; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 15; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 7", 5, 2));
    }

    private void generateCeladonCity() {
        currentMap = new int[30][25]; fillMap(3); drawBorder(30, 25);
        currentMap[0][12]  = 3; currentMap[0][13]  = 3;  // West  → R16
        currentMap[29][12] = 3; currentMap[29][13] = 3;  // East  → R7
        buildHouse(4,  4, 10, 8, 209, "DEPT. STORE");
        buildHouse(10, 16, 6, 5, 210, "ERIKA'S GYM");
        buildHouse(20, 12, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CELADON CITY", 2, 2));
    }

    private void generateRoute8() {
        currentMap = new int[30][15]; fillMap(0); drawBorder(30, 15);
        for (int x = 0; x < 30; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 25; x++) for (int y = 9; y < 12; y++) currentMap[x][y] = 4;
        NPC biker = new NPC("Biker", 15, 8, "TRAINER", "R8_TR1");
        biker.party = new Pokemon("Koffing", "POISON", 20);
        npcs.add(biker);
        labels.add(new MapLabel("ROUTE 8", 10, 2));
    }

    private void generateLavenderTown() {
        currentMap = new int[20][20]; fillMap(3); drawBorder(20, 20);
        currentMap[0][10]  = 3; currentMap[0][11]  = 3;  // West  → R8
        currentMap[9][19]  = 3; currentMap[10][19] = 3;  // South → R12
        buildHouse(12,  4, 6, 8, 211, "POKÉMON TOWER");
        buildHouse(4,  10, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("LAVENDER TOWN", 2, 2));
    }

    private void generateRoute12() {
        currentMap = new int[20][40]; fillMap(0); drawBorder(20, 40);
        for (int y = 0; y < 40; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 13; x < 19; x++) for (int y = 5;  y < 35; y++) currentMap[x][y] = 5;
        for (int x = 4;  x < 8;  x++) for (int y = 10; y < 30; y++) currentMap[x][y] = 4;
        NPC fisher = new NPC("Fisherman", 8, 20, "TRAINER", "R12_TR1");
        fisher.party = new Pokemon("Magikarp", "WATER", 15);
        npcs.add(fisher);
        labels.add(new MapLabel("ROUTE 12", 2, 5));
    }

    // Route 16 — Celadon west gate to Cycling Road
    private void generateRoute16() {
        currentMap = new int[40][20]; fillMap(0); drawBorder(40, 20);
        for (int x = 0; x < 40; x++) { currentMap[x][9] = 3; currentMap[x][10] = 3; }
        for (int x = 10; x < 30; x++) for (int y = 4; y < 8; y++) currentMap[x][y] = 4;
        NPC roughneck = new NPC("Roughneck", 20, 10, "TRAINER", "R16_TR1");
        roughneck.party = new Pokemon("Grimer", "POISON", 22);
        npcs.add(roughneck);
        labels.add(new MapLabel("ROUTE 16", 15, 2));
    }

    // Route 13 — connects bottom of R12 eastward to R14
    private void generateRoute13() {
        currentMap = new int[30][20]; fillMap(0); drawBorder(30, 20);
        for (int x = 0; x < 30; x++) { currentMap[x][9] = 3; currentMap[x][10] = 3; }
        for (int x = 5; x < 25; x++) for (int y = 13; y < 17; y++) currentMap[x][y] = 4;
        NPC birdKeeper = new NPC("Bird Keeper", 15, 10, "TRAINER", "R13_TR1");
        birdKeeper.party = new Pokemon("Fearow", "NORMAL", 22);
        npcs.add(birdKeeper);
        labels.add(new MapLabel("ROUTE 13", 10, 2));
    }

    // Route 14 — vertical connector from R13 south down to R15
    private void generateRoute14() {
        currentMap = new int[20][30]; fillMap(0); drawBorder(20, 30);
        for (int y = 0; y < 30; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 3; x < 8; x++) for (int y = 8; y < 22; y++) currentMap[x][y] = 4;
        NPC lass = new NPC("Lass", 10, 15, "TRAINER", "R14_TR1");
        lass.party = new Pokemon("Gloom", "GRASS", 24);
        npcs.add(lass);
        labels.add(new MapLabel("ROUTE 14", 2, 2));
    }

    // Route 15 — west to Fuchsia
    private void generateRoute15() {
        currentMap = new int[40][20]; fillMap(0); drawBorder(40, 20);
        for (int x = 0; x < 40; x++) { currentMap[x][9] = 3; currentMap[x][10] = 3; }
        for (int x = 5; x < 35; x++) for (int y = 13; y < 17; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 15", 15, 2));
    }

    private void generateFuchsiaCity() {
        currentMap = new int[30][25]; fillMap(3); drawBorder(30, 25);
        currentMap[29][9]  = 3; currentMap[29][10] = 3;  // East  → R15
        currentMap[0][9]   = 3; currentMap[0][10]  = 3;  // West  → R18 (Cycling Road east gate)
        currentMap[14][24] = 3; currentMap[15][24] = 3;  // South → R19
        buildHouse(4,  14, 6, 5, 212, "KOGA'S GYM");
        buildHouse(20,  4, 6, 5, 214, "WARDEN'S HOUSE");
        buildHouse(14,  4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("FUCHSIA CITY", 2, 2));
    }

    // Route 17 — Cycling Road, one-way south from R16 to R18
    private void generateRoute17() {
        currentMap = new int[20][40]; fillMap(0); drawBorder(20, 40);
        for (int y = 0; y < 40; y++) { currentMap[9][y] = 3; currentMap[10][y] = 3; }
        for (int x = 3; x < 8; x++) for (int y = 5; y < 35; y++) currentMap[x][y] = 4;
        NPC cyclist = new NPC("Biker", 10, 20, "TRAINER", "R17_TR1");
        cyclist.party = new Pokemon("Ponyta", "FIRE", 25);
        npcs.add(cyclist);
        labels.add(new MapLabel("ROUTE 17 - CYCLING RD", 2, 2));
    }

    // Route 18 — east gate connecting Cycling Road to Fuchsia
    private void generateRoute18() {
        currentMap = new int[20][15]; fillMap(0); drawBorder(20, 15);
        for (int x = 0; x < 20; x++) { currentMap[x][7] = 3; currentMap[x][8] = 3; }
        for (int x = 5; x < 15; x++) for (int y = 3; y < 6; y++) currentMap[x][y] = 4;
        labels.add(new MapLabel("ROUTE 18", 5, 2));
    }

    private void generateRoute19() {
        currentMap = new int[20][40]; fillMap(5); drawBorder(20, 40);
        labels.add(new MapLabel("ROUTE 19 (SEA)", 2, 5));
    }

    private void generateRoute20() {
        currentMap = new int[40][20]; fillMap(5); drawBorder(40, 20);
        labels.add(new MapLabel("ROUTE 20 (SEA)", 15, 2));
    }

    private void generateCinnabarIsland() {
        currentMap = new int[20][20]; fillMap(0); drawBorder(20, 20);
        currentMap[9][0]   = 5; currentMap[10][0]  = 5;
        currentMap[19][9]  = 5; currentMap[19][10] = 5;
        buildHouse(12,  4, 6, 5, 213, "BLAINE'S GYM");
        buildHouse(4,  12, 6, 5, 215, "POKÉMON LAB");
        buildHouse(4,   4, 4, 4, 203, "POKECENTER");
        labels.add(new MapLabel("CINNABAR ISLAND", 2, 2));
    }

    private void generateRoute21() {
        currentMap = new int[20][40]; fillMap(5); drawBorder(20, 40);
        labels.add(new MapLabel("ROUTE 21 (SEA)", 2, 5));
    }

    // -----------------------------------------------------------------------
    // INTERIORS
    // -----------------------------------------------------------------------
    private void generateGymPewter() {
        currentMap = new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14] = 99;
        NPC brock = new NPC("Brock", 7, 2, "LEADER", "LEADER"); brock.party = new Pokemon("Onix", "ROCK", 14); npcs.add(brock);
    }
    private void generateGymCerulean() {
        currentMap = new int[15][18]; fillMap(3); drawBorder(15, 18);
        for (int x = 2; x <= 12; x++) for (int y = 4; y <= 12; y++) currentMap[x][y] = 5;
        for (int y = 4; y <= 12; y++) currentMap[7][y] = 3; currentMap[7][17] = 99;
        NPC misty = new NPC("Misty", 7, 3, "LEADER", "LEADER_MISTY"); misty.party = new Pokemon("Starmie", "WATER", 21); npcs.add(misty);
    }
    private void generateGymVermilion() {
        currentMap = new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14] = 99;
        NPC surge = new NPC("Lt. Surge", 7, 2, "LEADER", "LEADER_SURGE"); surge.party = new Pokemon("Raichu", "ELECTRIC", 24); npcs.add(surge);
    }
    private void generateGymCeladon() {
        currentMap = new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14] = 99;
        for (int x = 2; x <= 12; x++) for (int y = 4; y <= 12; y++) if (x != 7) currentMap[x][y] = 1;
        NPC erika = new NPC("Erika", 7, 2, "LEADER", "LEADER_ERIKA"); erika.party = new Pokemon("Vileplume", "GRASS", 29); npcs.add(erika);
    }
    private void generateGymFuchsia() {
        currentMap = new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14] = 99;
        for (int x = 2; x <= 12; x += 2) for (int y = 4; y <= 10; y++) currentMap[x][y] = 1;
        NPC koga = new NPC("Koga", 7, 2, "LEADER", "LEADER_KOGA"); koga.party = new Pokemon("Weezing", "POISON", 37); npcs.add(koga);
    }
    private void generateGymCinnabar() {
        currentMap = new int[15][15]; fillMap(3); drawBorder(15, 15); currentMap[7][14] = 99;
        NPC blaine = new NPC("Blaine", 7, 2, "LEADER", "LEADER_BLAINE"); blaine.party = new Pokemon("Arcanine", "FIRE", 47); npcs.add(blaine);
    }
    private void generateCenter() {
        currentMap = new int[12][10]; fillMap(3); drawBorder(12, 10);
        for (int x = 4; x <= 7; x++) currentMap[x][3] = 2;
        currentMap[5][9] = 98; currentMap[6][9] = 98;
        npcs.add(new NPC("Nurse Joy", 5, 2, "HEALER", "JOY"));
    }
    private void generateWardenHouse() {
        currentMap = new int[12][12]; fillMap(3); drawBorder(12, 12);
        currentMap[5][11] = 99; currentMap[6][11] = 99;
        npcs.add(new NPC("Warden", 5, 4, "WARDEN", "NONE"));
    }
    private void generateMart() {
        currentMap = new int[12][10]; fillMap(3); drawBorder(12, 10);
        currentMap[5][9] = 98; currentMap[6][9] = 98;
        npcs.add(new NPC("Clerk", 5, 3, "MART", "SHOP"));
    }

    // -----------------------------------------------------------------------
    // MAP UTILITIES
    // -----------------------------------------------------------------------
    private void fillMap(int val) {
        for (int x = 0; x < currentMap.length; x++)
            for (int y = 0; y < currentMap[0].length; y++)
                currentMap[x][y] = val;
    }
    private void drawBorder(int w, int h) {
        for (int x = 0; x < w; x++) { currentMap[x][0] = 1; currentMap[x][h - 1] = 1; }
        for (int y = 0; y < h; y++) { currentMap[0][y] = 1; currentMap[w - 1][y] = 1; }
    }
    private void buildHouse(int x, int y, int w, int h, int doorID, String labelText) {
        for (int i = x; i < x + w; i++) for (int j = y; j < y + h; j++) currentMap[i][j] = 2;
        currentMap[x + (w / 2)][y + h - 1] = doorID;
        labels.add(new MapLabel(labelText, x + (w / 2), y - 1));
    }

    // -----------------------------------------------------------------------
    // MOVEMENT & TRANSITIONS
    // -----------------------------------------------------------------------
    private void movePlayer(int key)
    {
        int dx = 0, dy = 0;
        if (key == KeyEvent.VK_W) dy = -1;
        if (key == KeyEvent.VK_S) dy =  1;
        if (key == KeyEvent.VK_A) dx = -1;
        if (key == KeyEvent.VK_D) dx =  1;
        if (key == KeyEvent.VK_SPACE) { interact(); return; }

        int nx = playerX + dx, ny = playerY + dy;

        // ---- GEOGRAPHICAL TRANSITIONS (Pokémon Red order) ----
        // Pallet Town
        if (currentMapId == MAP_PALLET    && ny <  0)   { loadMap(MAP_ROUTE1,    6, 28); return; }
        if (currentMapId == MAP_PALLET    && ny >= 15)   { loadMap(MAP_ROUTE21,   9,  1); return; }
        // Route 1
        if (currentMapId == MAP_ROUTE1    && ny >= 30)   { loadMap(MAP_PALLET,    9,  1); return; }
        if (currentMapId == MAP_ROUTE1    && ny <  0)    { loadMap(MAP_VIRIDIAN,  9, 18); return; }
        // Viridian City
        if (currentMapId == MAP_VIRIDIAN  && ny >= 20)   { loadMap(MAP_ROUTE1,    6,  1); return; }
        if (currentMapId == MAP_VIRIDIAN  && ny <  0)    { loadMap(MAP_ROUTE2_S,  7, 18); return; }
        // Route 2 south
        if (currentMapId == MAP_ROUTE2_S  && ny >= 20)   { loadMap(MAP_VIRIDIAN,  9,  1); return; }
        if (currentMapId == MAP_ROUTE2_S  && ny <  0)    { loadMap(MAP_FOREST,   10, 28); return; }
        // Viridian Forest
        if (currentMapId == MAP_FOREST    && ny >= 30)   { loadMap(MAP_ROUTE2_S,  8,  1); return; }
        if (currentMapId == MAP_FOREST    && ny <  0)    { loadMap(MAP_ROUTE2_N,  8, 18); return; }
        // Route 2 north
        if (currentMapId == MAP_ROUTE2_N  && ny >= 20)   { loadMap(MAP_FOREST,   10,  1); return; }
        if (currentMapId == MAP_ROUTE2_N  && ny <  0)    { loadMap(MAP_PEWTER,    9, 18); return; }
        // Pewter City
        if (currentMapId == MAP_PEWTER    && ny >= 20)   { loadMap(MAP_ROUTE2_N,  7,  1); return; }
        if (currentMapId == MAP_PEWTER    && nx >= 20)   { loadMap(MAP_ROUTE3,    1,  8); return; }
        // Route 3
        if (currentMapId == MAP_ROUTE3    && nx <  0)    { loadMap(MAP_PEWTER,   18, 10); return; }
        if (currentMapId == MAP_ROUTE3    && nx >= 40)   { loadMap(MAP_MT_MOON,   2, 15); return; }
        // Mt. Moon
        if (currentMapId == MAP_MT_MOON   && nx >= 30)   { loadMap(MAP_ROUTE4,    2,  8); return; }
        // Route 4
        if (currentMapId == MAP_ROUTE4    && nx <  0)    { loadMap(MAP_MT_MOON,  28, 14); return; }
        if (currentMapId == MAP_ROUTE4    && nx >= 30)   { loadMap(MAP_CERULEAN,  1, 10); return; }
        // Cerulean City
        if (currentMapId == MAP_CERULEAN  && nx <  0)    { loadMap(MAP_ROUTE4,   28,  8); return; }
        if (currentMapId == MAP_CERULEAN  && ny <  0)    { loadMap(MAP_ROUTE24,   7, 23); return; }
        if (currentMapId == MAP_CERULEAN  && nx >= 22)   { loadMap(MAP_ROUTE9,    1,  7); return; }
        if (currentMapId == MAP_CERULEAN  && ny >= 20)   { loadMap(MAP_ROUTE5,    9,  1); return; }
        // Route 24 (Nugget Bridge)
        if (currentMapId == MAP_ROUTE24   && ny >= 25)   { loadMap(MAP_CERULEAN, 10,  1); return; }
        if (currentMapId == MAP_ROUTE24   && nx >= 16)   { loadMap(MAP_ROUTE25,   1,  7); return; }
        // Route 25 (to Bill's)
        if (currentMapId == MAP_ROUTE25   && nx <  0)    { loadMap(MAP_ROUTE24,  14,  7); return; }
        // Route 9 (east of Cerulean → Rock Tunnel)
        if (currentMapId == MAP_ROUTE9    && nx <  0)    { loadMap(MAP_CERULEAN, 20,  9); return; }
        if (currentMapId == MAP_ROUTE9    && nx >= 40)   { loadMap(MAP_ROUTE10,   9,  1); return; }
        // Route 10 / Rock Tunnel (→ Lavender)
        if (currentMapId == MAP_ROUTE10   && ny <  0)    { loadMap(MAP_ROUTE9,   38,  7); return; }
        if (currentMapId == MAP_ROUTE10   && ny >= 40)   { loadMap(MAP_LAVENDER,  1, 10); return; }
        // Route 5 (Cerulean ↔ Saffron)
        if (currentMapId == MAP_ROUTE5    && ny <  0)    { loadMap(MAP_CERULEAN, 10, 18); return; }
        if (currentMapId == MAP_ROUTE5    && ny >= 30)   { loadMap(MAP_SAFFRON,  10,  1); return; }
        // Saffron City
        if (currentMapId == MAP_SAFFRON   && ny <  0)    { loadMap(MAP_ROUTE5,    9, 28); return; }
        if (currentMapId == MAP_SAFFRON   && ny >= 25)   { loadMap(MAP_ROUTE6,    9,  1); return; }
        if (currentMapId == MAP_SAFFRON   && nx <  0)    { loadMap(MAP_ROUTE7,   18,  7); return; }
        if (currentMapId == MAP_SAFFRON   && nx >= 25)   { loadMap(MAP_ROUTE8,    1,  7); return; }
        // Route 6 (Saffron ↔ Vermilion)
        if (currentMapId == MAP_ROUTE6    && ny <  0)    { loadMap(MAP_SAFFRON,  10, 23); return; }
        if (currentMapId == MAP_ROUTE6    && ny >= 20)   { loadMap(MAP_VERMILION, 9,  1); return; }
        // Vermilion City
        if (currentMapId == MAP_VERMILION && ny <  0)    { loadMap(MAP_ROUTE6,    9, 18); return; }
        if (currentMapId == MAP_VERMILION && nx >= 25)   { loadMap(MAP_ROUTE11,   1,  7); return; }
        // Route 11 (east of Vermilion)
        if (currentMapId == MAP_ROUTE11   && nx <  0)    { loadMap(MAP_VERMILION,23, 10); return; }
        // Route 7 (Celadon ↔ Saffron)
        if (currentMapId == MAP_ROUTE7    && nx >= 20)   { loadMap(MAP_SAFFRON,   1, 12); return; }
        if (currentMapId == MAP_ROUTE7    && nx <  0)    { loadMap(MAP_CELADON,  28, 12); return; }
        // Celadon City
        if (currentMapId == MAP_CELADON   && nx >= 30)   { loadMap(MAP_ROUTE7,    1,  7); return; }
        if (currentMapId == MAP_CELADON   && nx <  0)    { loadMap(MAP_ROUTE16,  38,  9); return; }
        // Route 8 (Saffron ↔ Lavender)
        if (currentMapId == MAP_ROUTE8    && nx <  0)    { loadMap(MAP_SAFFRON,  23, 12); return; }
        if (currentMapId == MAP_ROUTE8    && nx >= 30)   { loadMap(MAP_LAVENDER,  1, 10); return; }
        // Lavender Town
        if (currentMapId == MAP_LAVENDER  && nx <  0)    { loadMap(MAP_ROUTE8,   28,  7); return; }
        if (currentMapId == MAP_LAVENDER  && ny >= 20)   { loadMap(MAP_ROUTE12,   9,  1); return; }
        // Route 12 (Lavender → R13)
        if (currentMapId == MAP_ROUTE12   && ny <  0)    { loadMap(MAP_LAVENDER,  9, 18); return; }
        if (currentMapId == MAP_ROUTE12   && ny >= 40)   { loadMap(MAP_ROUTE13,   1,  9); return; }
        // Route 16 (Celadon west → Cycling Road)
        if (currentMapId == MAP_ROUTE16   && nx >= 40)   { loadMap(MAP_CELADON,   1, 12); return; }
        if (currentMapId == MAP_ROUTE16   && nx <  0)    { loadMap(MAP_ROUTE17,   9,  1); return; }
        // Route 17 — Cycling Road
        if (currentMapId == MAP_ROUTE17   && ny <  0)    { loadMap(MAP_ROUTE16,   1,  9); return; }
        if (currentMapId == MAP_ROUTE17   && ny >= 40)   { loadMap(MAP_ROUTE18,   1,  7); return; }
        // Route 18 — east gate → Fuchsia
        if (currentMapId == MAP_ROUTE18   && nx <  0)    { loadMap(MAP_ROUTE17,   9, 38); return; }
        if (currentMapId == MAP_ROUTE18   && nx >= 20)   { loadMap(MAP_FUCHSIA,   1,  9); return; }
        // Route 13 (R12 tip → R14)
        if (currentMapId == MAP_ROUTE13   && nx <  0)    { loadMap(MAP_ROUTE12,   9, 38); return; }
        if (currentMapId == MAP_ROUTE13   && nx >= 30)   { loadMap(MAP_ROUTE14,   9,  1); return; }
        // Route 14 (R13 → R15)
        if (currentMapId == MAP_ROUTE14   && ny <  0)    { loadMap(MAP_ROUTE13,  28,  9); return; }
        if (currentMapId == MAP_ROUTE14   && ny >= 30)   { loadMap(MAP_ROUTE15,   1,  9); return; }
        // Route 15 (R14 ↔ Fuchsia east)
        if (currentMapId == MAP_ROUTE15   && nx <  0)    { loadMap(MAP_ROUTE14,   9, 28); return; }
        if (currentMapId == MAP_ROUTE15   && nx >= 40)   { loadMap(MAP_FUCHSIA,  28,  9); return; }
        // Fuchsia City
        if (currentMapId == MAP_FUCHSIA   && nx >= 30)   { loadMap(MAP_ROUTE15,   1,  9); return; }
        if (currentMapId == MAP_FUCHSIA   && nx <  0)    { loadMap(MAP_ROUTE18,  18,  7); return; }
        if (currentMapId == MAP_FUCHSIA   && ny >= 25)   { loadMap(MAP_ROUTE19,  14,  1); return; }
        // Route 19 (sea south of Fuchsia)
        if (currentMapId == MAP_ROUTE19   && ny <  0)    { loadMap(MAP_FUCHSIA,  14, 23); return; }
        if (currentMapId == MAP_ROUTE19   && ny >= 40)   { loadMap(MAP_ROUTE20,  38,  9); return; }
        // Route 20 (Seafoam → Cinnabar)
        if (currentMapId == MAP_ROUTE20   && nx >= 40)   { loadMap(MAP_ROUTE19,  14, 38); return; }
        if (currentMapId == MAP_ROUTE20   && nx <  0)    { loadMap(MAP_CINNABAR, 18,  9); return; }
        // Cinnabar Island
        if (currentMapId == MAP_CINNABAR  && nx >= 20)   { loadMap(MAP_ROUTE20,   1,  9); return; }
        if (currentMapId == MAP_CINNABAR  && ny <  0)    { loadMap(MAP_ROUTE21,   9, 38); return; }
        // Route 21 (Cinnabar → Pallet)
        if (currentMapId == MAP_ROUTE21   && ny >= 40)   { loadMap(MAP_CINNABAR,  9,  1); return; }
        if (currentMapId == MAP_ROUTE21   && ny <  0)    { loadMap(MAP_PALLET,    9, 13); return; }

        // ---- BOUNDS ----
        if (nx < 0 || ny < 0 || nx >= currentMap.length || ny >= currentMap[0].length) return;

        // ---- SURF CHECK ----
        if (currentMap[nx][ny] == 5 && !canSurf) return;

        // ---- COLLISION ----
        if (currentMap[nx][ny] == 1 || currentMap[nx][ny] == 2 || currentMap[nx][ny] == 6) return;

        // ---- NPC COLLISION ----
        for (NPC n : npcs) {
            if (n.x == nx && n.y == ny) {
                if (n.type.equals("TRAINER") || n.type.equals("LEADER")) {
                    game.startBattle(n.party, true, n.name, n.id);
                } else if (n.type.equals("HEALER")) {
                    for (Pokemon p : GameLauncher.party) p.healFull();
                    JOptionPane.showMessageDialog(this, "Nurse Joy: Your Pokémon are fully healed!");
                }
                return;
            }
        }

        // ---- DOOR TILES ----
        int tile = currentMap[nx][ny];
        if (tile == 201) { loadMap(MAP_GYM_PEWTER,    7, 13); return; }
        if (tile == 204) { loadMap(MAP_GYM_CERULEAN,  7, 16); return; }
        if (tile == 205) { loadMap(MAP_FOREST,        10, 28); return; }
        if (tile == 208) { loadMap(MAP_GYM_VERMILION, 7, 13); return; }
        if (tile == 210) { loadMap(MAP_GYM_CELADON,   7, 13); return; }
        if (tile == 212) { loadMap(MAP_GYM_FUCHSIA,   7, 13); return; }
        if (tile == 213) { loadMap(MAP_GYM_CINNABAR,  7, 13); return; }
        if (tile == 203) { returnMapId = currentMapId; loadMap(MAP_CENTER, 5, 8); return; }
        if (tile == 202) { returnMapId = currentMapId; loadMap(MAP_MART,   5, 8); return; }
        if (tile == 214) { loadMap(MAP_WARDEN_HOUSE,  5, 10); return; }
        if (tile == 99 || tile == 98) {
            if      (tile == 98)                              loadMap(returnMapId,    14, 14);
            else if (currentMapId == MAP_GYM_PEWTER)    loadMap(MAP_PEWTER,     9, 10);
            else if (currentMapId == MAP_GYM_CERULEAN)  loadMap(MAP_CERULEAN,  11,  9);
            else if (currentMapId == MAP_GYM_VERMILION) loadMap(MAP_VERMILION,  7, 17);
            else if (currentMapId == MAP_GYM_CELADON)   loadMap(MAP_CELADON,   13, 21);
            else if (currentMapId == MAP_GYM_FUCHSIA)   loadMap(MAP_FUCHSIA,    7, 19);
            else if (currentMapId == MAP_GYM_CINNABAR)  loadMap(MAP_CINNABAR,  15,  9);
            else if (currentMapId == MAP_MT_MOON)        loadMap(MAP_ROUTE4,     2,  7);
            else if (currentMapId == MAP_WARDEN_HOUSE)   loadMap(MAP_FUCHSIA,   23,  9);
            return;
        }

        // ---- MOVE PLAYER ----
        playerX = nx; playerY = ny;
        repaint();

        if (currentMap[nx][ny] == 4 && Math.random() < 0.15) {
            game.startBattle(Pokemon.generateWild(4, 12), false, "Wild Pokemon", "WILD");
        } else if (currentMap[nx][ny] == 5 && Math.random() < 0.10) {
            game.startBattle(new Pokemon("Tentacool", "WATER", 20), false, "Wild Pokemon", "WILD");
        }
    }

    // -----------------------------------------------------------------------
    // INTERACT
    // -----------------------------------------------------------------------
    private void interact()
    {
        for (NPC n : npcs) {
            if (Math.abs(n.x - playerX) <= 1 && Math.abs(n.y - playerY) <= 1) {
                if (n.type.equals("OAK")) {
                    if (!GameLauncher.hasStarter) {
                        String[] starters = {"Charmander", "Squirtle", "Bulbasaur"};
                        int c = JOptionPane.showOptionDialog(this, "Choose!", "Oak", 0, 3, null, starters, 0);
                        if (c == 0) GameLauncher.party.add(new Pokemon("Charmander", "FIRE",  5));
                        if (c == 1) GameLauncher.party.add(new Pokemon("Squirtle",  "WATER", 5));
                        if (c == 2) GameLauncher.party.add(new Pokemon("Bulbasaur", "GRASS", 5));
                        if (c != -1) GameLauncher.hasStarter = true;
                    }
                } else if (n.type.equals("WARDEN")) {
                    if (!canSurf) {
                        JOptionPane.showMessageDialog(this, "Safari Warden: Thanks for finding my Gold Teeth!\n\n(You can now SURF on water tiles!)");
                        canSurf = true;
                    } else {
                        JOptionPane.showMessageDialog(this, "Safari Warden: Enjoy surfing around Kanto!");
                    }
                } else if (n.type.equals("MART")) {
                    String[] items = {"Potion - $300", "Poké Ball - $200", "Cancel"};
                    int choice = JOptionPane.showOptionDialog(this, "Welcome to the Poké Mart!", "Poké Mart", 0, 3, null, items, items[0]);
                    if (choice == 0 && GameLauncher.money >= 300) { GameLauncher.money -= 300; GameLauncher.bag.add("Potion");    JOptionPane.showMessageDialog(this, "Bought a Potion!"); }
                    if (choice == 1 && GameLauncher.money >= 200) { GameLauncher.money -= 200; GameLauncher.bag.add("Poké Ball"); JOptionPane.showMessageDialog(this, "Bought a Poké Ball!"); }
                }
                return;
            }
        }
    }

    // -----------------------------------------------------------------------
    // PAINT
    // -----------------------------------------------------------------------
    @Override
    protected void paintComponent(Graphics g)
    {
        super.paintComponent(g);
        int camX = (800 / 2) - (playerX * TILE_SIZE);
        int camY = (600 / 2) - (playerY * TILE_SIZE);

        for (int x = 0; x < currentMap.length; x++) {
            for (int y = 0; y < currentMap[0].length; y++) {
                int id = currentMap[x][y];
                Color c = Color.BLACK;
                if      (id == 0) c = new Color(50,  205,  50);
                else if (id == 4) c = new Color(0,   100,   0);
                else if (id == 3) c = Color.LIGHT_GRAY;
                else if (id == 1) c = Color.GRAY;
                else if (id == 2) c = new Color(178,  34,  34);
                else if (id == 5) c = new Color(30,  144, 255);
                else              c = new Color(200, 180, 140);
                g.setColor(c);
                g.fillRect(x * TILE_SIZE + camX, y * TILE_SIZE + camY, TILE_SIZE, TILE_SIZE);
            }
        }

        for (NPC n : npcs) {
            if      (n.type.equals("HEALER"))  g.setColor(Color.PINK);
            else if (n.type.equals("TRAINER")) g.setColor(Color.CYAN);
            else if (n.type.equals("LEADER"))  g.setColor(Color.MAGENTA);
            else if (n.type.equals("WARDEN"))  g.setColor(Color.YELLOW);
            else                               g.setColor(Color.ORANGE);
            g.fillOval(n.x * TILE_SIZE + camX + 5, n.y * TILE_SIZE + camY + 5, TILE_SIZE - 10, TILE_SIZE - 10);
        }

        if (currentMap[playerX][playerY] == 5) g.setColor(new Color(135, 206, 250));
        else g.setColor(Color.RED);
        g.fillOval(playerX * TILE_SIZE + camX + 5, playerY * TILE_SIZE + camY + 5, TILE_SIZE - 10, TILE_SIZE - 10);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.BOLD, 14));
        for (MapLabel l : labels) g.drawString(l.text, l.x * TILE_SIZE + camX, l.y * TILE_SIZE + camY);

        if (showPokedex) {
            g.setColor(new Color(0, 0, 0, 180));     g.fillRect(0, 0, 800, 600);
            g.setColor(new Color(220, 20, 60));       g.fillRoundRect(100, 50, 600, 500, 20, 20);
            g.setColor(Color.WHITE);                  g.drawRoundRect(100, 50, 600, 500, 20, 20);
            g.setColor(new Color(30, 30, 30));        g.fillRect(150, 150, 500, 300);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Monospaced", Font.BOLD, 36));
            g.drawString("POKÉDEX", 320, 110);
            g.setFont(new Font("Monospaced", Font.PLAIN, 20));
            g.setColor(Color.GREEN);
            g.drawString("001. Bulbasaur  [SEEN]",  170, 190);
            g.drawString("004. Charmander [OWNED]", 170, 220);
            g.setColor(Color.GRAY);
            g.drawString("007. Squirtle   [???]",   170, 250);
            g.drawString("025. Pikachu    [???]",   170, 280);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.ITALIC, 16));
            g.drawString("Press 'P' to Close", 330, 520);
        }
    }

    class NPC      { String name, type, id; int x, y; Pokemon party; NPC(String n, int x, int y, String t, String i){name=n;this.x=x;this.y=y;type=t;id=i;} }
    class MapLabel { String text; int x, y; MapLabel(String t, int x, int y){text=t;this.x=x;this.y=y;} }
}