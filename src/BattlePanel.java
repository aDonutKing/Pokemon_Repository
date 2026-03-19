package PART2;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class BattlePanel extends JPanel
{
    private GameLauncher game;
    private Pokemon enemy;
    private boolean isTrainerBattle;

    private JTextArea logArea;
    private JLabel enemyStats, playerStats;
    private JProgressBar playerHPBar, enemyHPBar, playerXPBar;
    private JPanel menuPanel;
    private JButton btn1, btn2, btn3, btn4;
    private JButton backBtn;

    // --- Image Labels ---
    private JLabel playerImage, enemyImage;

    private int menuState = 0;

    // Move database: move name (lowercase) → Move object with real stats
    // Loaded once from your moves data file on first battle
    private java.util.Map<String, Move> moveDatabase = new java.util.HashMap<>();
    private boolean moveDatabaseLoaded = false;

    public BattlePanel(GameLauncher game)
    {
        this.game = game;
        setLayout(new BorderLayout());

        // --- Stats Header ---
        JPanel top = new JPanel(new GridLayout(1, 2, 20, 0));
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Enemy Side
        JPanel enemySide = new JPanel(new BorderLayout());
        enemyStats = new JLabel("Enemy");
        enemyStats.setFont(new Font("Arial", Font.BOLD, 18));
        enemyHPBar = new JProgressBar(0, 100);
        enemyHPBar.setForeground(Color.GREEN);
        enemySide.add(enemyStats, BorderLayout.NORTH);
        enemySide.add(enemyHPBar, BorderLayout.CENTER);

        // Player Side
        JPanel playerSide = new JPanel(new GridLayout(3, 1, 0, 2));
        playerStats = new JLabel("Player");
        playerStats.setFont(new Font("Arial", Font.BOLD, 18));

        playerHPBar = new JProgressBar(0, 100);
        playerHPBar.setForeground(Color.GREEN);

        playerXPBar = new JProgressBar(0, 100);
        playerXPBar.setForeground(Color.CYAN);
        playerXPBar.setPreferredSize(new Dimension(0, 5));

        playerSide.add(playerStats);
        playerSide.add(playerHPBar);
        playerSide.add(playerXPBar);

        top.add(playerSide);
        top.add(enemySide);
        add(top, BorderLayout.NORTH);

        // --- Battle Arena (Center) ---
        JPanel arenaPanel = new JPanel(new GridLayout(1, 2));

        playerImage = new JLabel("Player", SwingConstants.CENTER);
        enemyImage  = new JLabel("Enemy",  SwingConstants.CENTER);

        arenaPanel.add(playerImage);
        arenaPanel.add(enemyImage);
        add(arenaPanel, BorderLayout.CENTER);

        // --- Battle Log ---
        logArea = new JTextArea();
        logArea.setEditable(false);
        logArea.setFont(new Font("Monospaced", Font.PLAIN, 14));
        JScrollPane scroll = new JScrollPane(logArea);
        scroll.setPreferredSize(new Dimension(300, 0));
        add(scroll, BorderLayout.EAST);

        // --- Controls ---
        menuPanel = new JPanel(new GridLayout(2, 2, 5, 5));
        menuPanel.setPreferredSize(new Dimension(800, 150));

        btn1 = new JButton(); btn2 = new JButton();
        btn3 = new JButton(); btn4 = new JButton();

        btn1.addActionListener(e -> handleButton(0));
        btn2.addActionListener(e -> handleButton(1));
        btn3.addActionListener(e -> handleButton(2));
        btn4.addActionListener(e -> handleButton(3));

        menuPanel.add(btn1); menuPanel.add(btn2);
        menuPanel.add(btn3); menuPanel.add(btn4);

        // Control Wrapper & Back Button
        JPanel controlWrapper = new JPanel(new BorderLayout(5, 0));
        controlWrapper.add(menuPanel, BorderLayout.CENTER);

        backBtn = new JButton("BACK");
        backBtn.setFont(new Font("Arial", Font.BOLD, 14));
        backBtn.setPreferredSize(new Dimension(100, 150));
        backBtn.addActionListener(e -> {
            if (menuState == 1) showMainMenu();
        });
        controlWrapper.add(backBtn, BorderLayout.EAST);

        add(controlWrapper, BorderLayout.SOUTH);
    }

    // -----------------------------------------------------------------------
    // Determine wild Pokemon level from player's map position
    // -----------------------------------------------------------------------
    private int getAreaLevel()
    {
        int px = game.getWorldPanel().getPlayerX();
        int py = game.getWorldPanel().getPlayerY();

        int mapWidth  = game.getWorldPanel().getMapWidth();
        int mapHeight = game.getWorldPanel().getMapHeight();

        int centerX = mapWidth  / 2;
        int centerY = mapHeight / 2;

        int dist = Math.abs(px - centerX) + Math.abs(py - centerY);

        if (dist < 5)  return 3;
        if (dist < 10) return 6;
        if (dist < 20) return 10;
        if (dist < 30) return 18;
        return 28;
    }

    // -----------------------------------------------------------------------
    // Begin a new battle
    // -----------------------------------------------------------------------
    public void setupBattle(Pokemon e, boolean trainer, String name)
    {
        this.enemy          = e;
        this.isTrainerBattle = trainer;

        // Scale wild Pokemon to area level
        if (!trainer)
        {
            enemy.level  = getAreaLevel();
            enemy.maxHp  = 20 + enemy.level * 5;
            enemy.currentHp = enemy.maxHp;
        }

        // Load moves BEFORE healing so HP is already correct
        loadMovesetsFromFile(getActivePokemon());
        loadMovesetsFromFile(enemy);

        // FIX: only healFull on the enemy (player party keeps current HP)
        enemy.healFull();

        logArea.setText("Battle started against " + name + "!\n");

        showMainMenu();
        updateStats();
    }

    // -----------------------------------------------------------------------
    // Load full move database from your moves data file.
    // Expected CSV format (one move per line):
    //   index, InternalName, DisplayName, hex, power, type, category, accuracy, pp, ...
    // Example: 28,BITE,Bite,00F,60,DARK,Physical,100,25,30,00,0,abe,Tough,...
    //   col 0 = index, col 2 = display name, col 4 = power, col 5 = type, col 8 = pp
    // -----------------------------------------------------------------------
    private void loadMoveDatabase()
    {
        if (moveDatabaseLoaded) return;
        moveDatabaseLoaded = true;

        String userDir = System.getProperty("user.dir");
        // Add whatever filenames your moves file might have
        String[] possibleNames = { "moves.csv", "Moves.csv", "moves.txt", "Moves.txt", "PokemonMoves.csv" };
        File dbFile = null;

        for (String fname : possibleNames) {
            File f = new File(userDir, fname);
            if (f.exists()) { dbFile = f; break; }
        }
        if (dbFile == null) {
            for (String fname : possibleNames) {
                File f = new File(userDir + File.separator + "src", fname);
                if (f.exists()) { dbFile = f; break; }
            }
        }

        if (dbFile == null) {
            System.out.println("Move database file not found — moves will use default stats.");
            return;
        }

        System.out.println("Loading move database from: " + dbFile.getAbsolutePath());

        try (Scanner sc = new Scanner(dbFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Split on commas but respect quoted fields (descriptions contain commas)
                String[] col = splitCsv(line);

                // Need at least 9 columns: idx, internal, display, hex, power, type, category, acc, pp
                if (col.length < 9) continue;

                try {
                    String displayName = col[2].trim();                      // e.g. "Bite"
                    int    power       = Integer.parseInt(col[4].trim());    // e.g. 60
                    String type        = col[5].trim().toUpperCase();        // e.g. "DARK"
                    int    pp          = Integer.parseInt(col[8].trim());    // e.g. 25

                    // Cap PP at 40; status moves with 0 power keep pp as-is
                    if (pp <= 0) pp = 10;
                    if (pp > 40) pp = 40;

                    Move m = new Move(displayName, type, power, pp);
                    // Key by lowercase display name for case-insensitive lookup
                    moveDatabase.put(displayName.toLowerCase(), m);
                } catch (NumberFormatException e) {
                    // Skip malformed lines silently
                }
            }
            System.out.println("Move database loaded: " + moveDatabase.size() + " moves.");
        } catch (Exception e) {
            System.out.println("Error reading move database: " + e.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // CSV splitter that respects quoted fields containing commas.
    // e.g. 28,BITE,Bite,00F,60,DARK,Physical,100,25,...,"The target is bitten..."
    // -----------------------------------------------------------------------
    private String[] splitCsv(String line)
    {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;           // toggle quoted mode, don't add the quote char
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());      // end of field
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());              // last field
        return fields.toArray(new String[0]);
    }

    // -----------------------------------------------------------------------
    // Look up a move name in the database; return a Move with real stats.
    // Falls back to generic stats if the move isn't found.
    // -----------------------------------------------------------------------
    private Move createMoveFromDatabase(String moveName)
    {
        Move template = moveDatabase.get(moveName.toLowerCase());
        if (template != null) {
            // Return a fresh Move with the real stats (fresh AP = maxAp)
            return new Move(template.name, template.type, template.power, template.maxAp);
        }
        // Fallback — move not in database yet
        System.out.println("WARNING: '" + moveName + "' not found in move database, using defaults.");
        return new Move(moveName, "NORMAL", 40, 35);
    }

    // -----------------------------------------------------------------------
    // Load movesets from Movesets.txt
    // -----------------------------------------------------------------------
    private void loadMovesetsFromFile(Pokemon p)
    {
        if (p == null) return;

        // Make sure the move database is loaded before we look up stats
        loadMoveDatabase();

        String userDir = System.getProperty("user.dir");
        String[] possibleNames = { "Movesets.txt", "Movesets.txt.txt", "moves.txt" };
        File myFile = null;

        // Try project root
        for (String fname : possibleNames) {
            File testFile = new File(userDir, fname);
            if (testFile.exists()) { myFile = testFile; break; }
        }

        // Try src/ subfolder (Eclipse / IntelliJ)
        if (myFile == null) {
            for (String fname : possibleNames) {
                File testFile = new File(userDir + File.separator + "src", fname);
                if (testFile.exists()) { myFile = testFile; break; }
            }
        }

        if (myFile == null) {
            System.out.println("CRITICAL ERROR: Could not find Movesets.txt anywhere in " + userDir);
            return;
        }

        System.out.println("SUCCESS: Loading moves from " + myFile.getAbsolutePath());

        try (Scanner reader = new Scanner(myFile)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 1) continue;

                String pokeNameInFile = parts[0].trim();

                if (pokeNameInFile.equalsIgnoreCase(p.name)) {
                    p.moves.clear();

                    // Each move entry: moveName, levelRequired (pairs starting at index 1)
                    for (int i = 1; i + 1 < parts.length; i += 2) {
                        try {
                            String moveName = parts[i].trim();
                            int    levelReq = Integer.parseInt(parts[i + 1].trim());

                            if (p.level >= levelReq) {
                                // Look up real stats from the move database
                                p.moves.add(createMoveFromDatabase(moveName));
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("WARNING: Bad level value near index " + i + " for " + p.name);
                        }
                    }

                    // Keep only the 4 most recently learned moves
                    if (p.moves.size() > 4) {
                        List<Move> lastFour = new ArrayList<>(
                            p.moves.subList(p.moves.size() - 4, p.moves.size())
                        );
                        p.moves.clear();
                        p.moves.addAll(lastFour);
                    }
                    break; // Found the Pokemon — stop scanning
                }
            }
        } catch (Exception ex) {
            System.out.println("SYSTEM ERROR reading Movesets.txt: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // Menu helpers
    // -----------------------------------------------------------------------
    private void showMainMenu()
    {
        menuState = 0;
        backBtn.setVisible(false);

        btn1.setText("FIGHT");
        btn2.setText("BAG");
        btn3.setText("POKEMON");
        btn4.setText("RUN");
    }

    private void showMoveMenu()
    {
        menuState = 1;
        backBtn.setVisible(true);

        Pokemon p = getActivePokemon();

        btn1.setText(formatMoveText(p, 0));
        btn2.setText(formatMoveText(p, 1));
        btn3.setText(formatMoveText(p, 2));
        btn4.setText(formatMoveText(p, 3));
    }

    private String formatMoveText(Pokemon p, int index)
    {
        if (p == null || index >= p.moves.size()) return "-";
        Move m = p.moves.get(index);
        return m.name + " (" + m.currentAp + "/" + m.maxAp + ")";
    }

    private void handleButton(int index)
    {
        if (menuState == 0) {
            if (index == 0) showMoveMenu();
            if (index == 1) openBag();
            if (index == 2) switchPokemon(false);
            if (index == 3) tryRun();
        } else if (menuState == 1) {
            useMove(index);
        }
    }

    // -----------------------------------------------------------------------
    // Player uses a move
    // -----------------------------------------------------------------------
    private void useMove(int index)
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;
        if (index >= p.moves.size()) return;

        Move m = p.moves.get(index);

        if (m.currentAp <= 0) {
            logArea.append("No AP left for " + m.name + "!\n");
            return;
        }

        m.currentAp--;

        logArea.append(p.name + " used " + m.name + "!\n");

        double mult = Pokemon.getEffectiveness(m.type, enemy.type);
        int dmg = (int)((p.level * 2 * m.power / 20.0) * mult);
        if (m.power > 0 && dmg == 0) dmg = 1;

        enemy.takeDamage(dmg);

        if (mult > 1.0) logArea.append("It's super effective!\n");
        else if (mult < 1.0 && mult > 0) logArea.append("It's not very effective...\n");

        updateStats();
        showMoveMenu(); // FIX: refresh AP display after use

        if (enemy.currentHp <= 0) winBattle();
        else enemyTurn();
    }

    // -----------------------------------------------------------------------
    // Enemy takes a turn
    // -----------------------------------------------------------------------
    private void enemyTurn()
    {
        Pokemon p = getActivePokemon();
        if (p == null || p.currentHp <= 0) return;
        if (enemy.moves.isEmpty()) return;

        // FIX: pick a move that still has AP; skip exhausted ones
        List<Move> usable = new ArrayList<>();
        for (Move mv : enemy.moves) {
            if (mv.currentAp > 0) usable.add(mv);
        }

        if (usable.isEmpty()) {
            logArea.append("Enemy " + enemy.name + " has no moves left!\n");
            return;
        }

        Move m = usable.get(new Random().nextInt(usable.size()));
        m.currentAp--; // FIX: enemy moves now cost AP

        double mult = Pokemon.getEffectiveness(m.type, p.type);
        int dmg = (int)((enemy.level * 2 * m.power / 25.0) * mult);
        if (m.power > 0 && dmg == 0) dmg = 1;

        p.takeDamage(dmg);

        logArea.append("Enemy " + enemy.name + " used " + m.name + "!\n");
        if (mult > 1.0) logArea.append("It's super effective!\n");

        updateStats();

        if (p.currentHp <= 0) {
            logArea.append(p.name + " fainted!\n");
            checkPartyFainted();
        } else {
            showMainMenu();
        }
    }

    // -----------------------------------------------------------------------
    // Bag / items
    // -----------------------------------------------------------------------
    private void openBag()
    {
        if (GameLauncher.bag.isEmpty()) {
            logArea.append("Bag is empty!\n");
            return;
        }

        String[] items  = GameLauncher.bag.toArray(new String[0]);
        String   choice = (String) JOptionPane.showInputDialog(this, "Select Item", "Bag",
                          JOptionPane.PLAIN_MESSAGE, null, items, items[0]);

        if (choice == null) return; // Cancelled — no turn used

        if (choice.equals("Potion")) {
            Pokemon p = getActivePokemon();
            p.currentHp = Math.min(p.maxHp, p.currentHp + 20);
            GameLauncher.bag.remove("Potion");
            logArea.append("Used Potion on " + p.name + "!\n");
            updateStats();
            showMainMenu(); // FIX: return to main menu after item use
            enemyTurn();

        } else if (choice.equals("Poké Ball")) {
            if (isTrainerBattle) {
                logArea.append("You can't catch another trainer's Pokemon!\n");
            } else {
                GameLauncher.bag.remove("Poké Ball");
                logArea.append("You threw a Poké Ball...\n");

                double catchChance = 1.0 - ((double) enemy.currentHp / enemy.maxHp);
                if (Math.random() < catchChance || Math.random() < 0.3) {
                    logArea.append("Gotcha! " + enemy.name + " was caught!\n");
                    GameLauncher.party.add(enemy);
                    GameLauncher.registerCaught(enemy.name);
                    game.endBattle(true);
                } else {
                    logArea.append("Oh no! The Pokemon broke free!\n");
                    showMainMenu(); // FIX: show menu before enemy acts
                    enemyTurn();
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // HUD updates
    // -----------------------------------------------------------------------
    private void updateStats()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;

        playerStats.setText(p.name + " Lv" + p.level +
            " (" + p.currentHp + "/" + p.maxHp + ")");
        playerHPBar.setMaximum(p.maxHp);
        playerHPBar.setValue(p.currentHp);
        updateBarColor(playerHPBar);

        playerXPBar.setMaximum(p.xpToNext);
        playerXPBar.setValue(p.xp);

        if (enemy != null) {
            enemyStats.setText(enemy.name + " Lv" + enemy.level +
                " (" + enemy.currentHp + "/" + enemy.maxHp + ")");
            enemyHPBar.setMaximum(enemy.maxHp);
            enemyHPBar.setValue(enemy.currentHp);
            updateBarColor(enemyHPBar);
        }

        updateImages();
    }

    private void updateImages()
    {
        Pokemon p = getActivePokemon();
        if (p != null) {
            ImageIcon pIcon = loadPokemonImage(p.name);
            playerImage.setIcon(pIcon);
            playerImage.setText(pIcon == null ? p.name + " (No Image)" : "");
        }

        if (enemy != null) {
            ImageIcon eIcon = loadPokemonImage(enemy.name);
            enemyImage.setIcon(eIcon);
            enemyImage.setText(eIcon == null ? enemy.name + " (No Image)" : "");
        }
    }

    // FIX: Use a relative, cross-platform path instead of a hardcoded Windows path
    private ImageIcon loadPokemonImage(String name)
    {
        String lowerCaseName = name.toLowerCase();

        String[] searchPaths = {
            // Original network/school drive path — checked first
            "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".jpg",
            "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".png",
            // Relative fallbacks for running from project root
            "Pokemon Sprites" + File.separator + lowerCaseName + ".png",
            "Pokemon Sprites" + File.separator + lowerCaseName + ".jpg",
            "src" + File.separator + "Pokemon Sprites" + File.separator + lowerCaseName + ".png",
            "src" + File.separator + "Pokemon Sprites" + File.separator + lowerCaseName + ".jpg",
        };

        for (String path : searchPaths) {
            File imgFile = new File(path);
            if (imgFile.exists()) {
                ImageIcon icon = new ImageIcon(imgFile.getAbsolutePath());
                Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                return new ImageIcon(img);
            }
        }

        return null; // No image found
    }

    private void updateBarColor(JProgressBar bar)
    {
        if (bar.getMaximum() == 0) return;
        double percent = (double) bar.getValue() / bar.getMaximum();

        if      (percent > 0.5) bar.setForeground(Color.GREEN);
        else if (percent > 0.2) bar.setForeground(Color.YELLOW);
        else                    bar.setForeground(Color.RED);
    }

    // -----------------------------------------------------------------------
    // Switch Pokemon
    // -----------------------------------------------------------------------
    private void switchPokemon(boolean forced)
    {
        if (GameLauncher.party.size() < 2) {
            logArea.append("Only one Pokemon!\n");
            return;
        }

        String[] partyNames = new String[GameLauncher.party.size()];
        for (int i = 0; i < GameLauncher.party.size(); i++) {
            Pokemon pk = GameLauncher.party.get(i);
            partyNames[i] = pk.name + " (HP: " + pk.currentHp + "/" + pk.maxHp + ")";
        }

        // FIX: snapshot the active Pokemon by reference before the loop
        Pokemon activeBefore = getActivePokemon();

        while (true) {
            String choice = (String) JOptionPane.showInputDialog(this, "Switch to:", "Party",
                            JOptionPane.QUESTION_MESSAGE, null, partyNames, partyNames[0]);

            if (choice == null) {
                if (forced) {
                    JOptionPane.showMessageDialog(this, "You must choose a new Pokémon to continue!");
                    continue;
                } else return;
            }

            int selectedIndex = -1;
            for (int i = 0; i < partyNames.length; i++) {
                if (partyNames[i].equals(choice)) { selectedIndex = i; break; }
            }

            if (selectedIndex < 0) continue; // Shouldn't happen but guard anyway

            Pokemon selected = GameLauncher.party.get(selectedIndex);

            // FIX: compare by object identity, not by index 0
            if (selected == activeBefore) {
                if (forced) {
                    JOptionPane.showMessageDialog(this,
                        activeBefore.name + " fainted! Choose a different Pokémon.");
                    continue;
                } else {
                    logArea.append(activeBefore.name + " is already out!\n");
                    return;
                }
            }

            if (selected.currentHp <= 0) {
                JOptionPane.showMessageDialog(this, "That Pokemon has no energy!");
                if (forced) continue;
                else return;
            }

            // Perform the switch
            GameLauncher.party.remove(selectedIndex);
            GameLauncher.party.add(0, selected);

            loadMovesetsFromFile(selected); // Reload moves for switched-in Pokemon

            logArea.append("Go! " + getActivePokemon().name + "!\n");
            updateStats();
            showMainMenu();
            if (!forced) enemyTurn();
            break;
        }
    }

    private void checkPartyFainted()
    {
        boolean anyAlive = false;
        for (Pokemon p : GameLauncher.party) if (p.currentHp > 0) anyAlive = true;

        if (!anyAlive) game.endBattle(false);
        else           switchPokemon(true);
    }

    private void tryRun()
    {
        if (isTrainerBattle) logArea.append("Can't run!\n");
        else                 game.endBattle(true);
    }

    // -----------------------------------------------------------------------
    // Win battle & XP
    // -----------------------------------------------------------------------
    private void winBattle()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;

        int oldLevel = p.level;

        if (p.level < 100) {
            int xpGain = enemy.level * 10;
            p.gainXp(xpGain);
            logArea.append("Gained " + xpGain + " XP!\n");

            // FIX: clamp level AFTER gainXp (gainXp may have pushed past 100)
            if (p.level > 100) {
                p.level = 100;
                p.xp    = 0;
            }

            if (p.level > oldLevel) checkNewMoves(p);
            checkEvolution();
        } else {
            logArea.append(p.name + " is already at Max Level (100)!\n");
        }

        int moneyWon = isTrainerBattle ? 300 : 50;
        GameLauncher.money += moneyWon;
        logArea.append("You won $" + moneyWon + "!\n");

        updateStats();
        JOptionPane.showMessageDialog(this, "You Won! Gained $" + moneyWon);
        game.endBattle(true);
    }

    private void checkNewMoves(Pokemon p)
    {
        // Moves are handled by loadMovesetsFromFile based on level.
        // Re-load so any newly unlocked moves are picked up on level-up.
        loadMovesetsFromFile(p);
    }

    // FIX: learnMove now has a complete forget-move dialog when party is full
    private void learnMove(Pokemon p, Move newMove)
    {
        if (p.moves.size() < 4) {
            p.moves.add(newMove);
            logArea.append(p.name + " learned " + newMove.name + "!\n");
        } else {
            // Ask player which move to forget
            String[] moveNames = new String[p.moves.size()];
            for (int i = 0; i < p.moves.size(); i++) moveNames[i] = p.moves.get(i).name;

            String forget = (String) JOptionPane.showInputDialog(this,
                p.name + " wants to learn " + newMove.name + "!\nForget which move?",
                "Learn Move", JOptionPane.QUESTION_MESSAGE, null, moveNames, moveNames[0]);

            if (forget != null) {
                for (int i = 0; i < p.moves.size(); i++) {
                    if (p.moves.get(i).name.equals(forget)) {
                        p.moves.set(i, newMove);
                        logArea.append(p.name + " forgot " + forget +
                            " and learned " + newMove.name + "!\n");
                        break;
                    }
                }
            } else {
                logArea.append(p.name + " did not learn " + newMove.name + ".\n");
            }
        }
    }

    // -----------------------------------------------------------------------
    // Evolution check
    // -----------------------------------------------------------------------
    private void checkEvolution()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;

        String  oldName = p.name;
        boolean evolved = false;

        if (p.level >= 36) {
            if      (p.name.equals("Charmeleon")) { p.name = "Charizard";  evolved = true; }
            else if (p.name.equals("Wartortle"))  { p.name = "Blastoise";  evolved = true; }
            else if (p.name.equals("Ivysaur"))    { p.name = "Venusaur";   evolved = true; }
        } else if (p.level >= 16) {
            if      (p.name.equals("Charmander")) { p.name = "Charmeleon"; evolved = true; }
            else if (p.name.equals("Squirtle"))   { p.name = "Wartortle";  evolved = true; }
            else if (p.name.equals("Bulbasaur"))  { p.name = "Ivysaur";    evolved = true; }
        }

        if (evolved) {
            logArea.append("\nWhat? " + oldName + " is evolving!\n");

            // Boost stats on evolution
            p.maxHp    += 20;
            p.currentHp = p.maxHp;

            // Load moves for the new form
            loadMovesetsFromFile(p);

            JOptionPane.showMessageDialog(this,
                oldName + " has evolved into " + p.name + "!",
                "Evolution", JOptionPane.INFORMATION_MESSAGE);

            logArea.append(oldName + " became " + p.name + "!\n");
            updateStats();
        }
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------
    private Pokemon getActivePokemon()
    {
        if (GameLauncher.party.isEmpty()) return null;
        return GameLauncher.party.get(0);
    }
}