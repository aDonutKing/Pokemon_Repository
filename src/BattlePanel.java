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

    // Move database: move name (lowercase) → Move template with real stats
    // Loaded once from Moves.txt on first battle
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
        this.enemy           = e;
        this.isTrainerBattle = trainer;

        // Scale wild Pokemon to area level
        if (!trainer)
        {
            enemy.level     = getAreaLevel();
            enemy.maxHp     = 20 + enemy.level * 5;
            enemy.currentHp = enemy.maxHp;
        }

        // Load moves using the real move database
        loadMovesetsFromFile(getActivePokemon());
        loadMovesetsFromFile(enemy);

        // Heal enemy only (player keeps current HP)
        enemy.healFull();

        logArea.setText("Battle started against " + name + "!\n");
        showMainMenu();
        updateStats();
    }

    // -----------------------------------------------------------------------
    // Load full move database from Moves.txt
    // Format: index,INTERNALNAME,DisplayName,hex,power,type,category,accuracy,pp,...
    // Example: 28,BITE,Bite,00F,60,DARK,Physical,100,25,...
    //   col 2 = display name, col 4 = power, col 5 = type, col 8 = pp
    // -----------------------------------------------------------------------
    private void loadMoveDatabase()
    {
        if (moveDatabaseLoaded) return;
        moveDatabaseLoaded = true;

        String userDir = System.getProperty("user.dir");
        String[] possibleNames = { "Moves.txt", "moves.txt", "Moves.csv", "moves.csv", "PokemonMoves.csv" };
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
            System.out.println("Move database not found — moves will use default stats.");
            return;
        }

        System.out.println("Loading move database from: " + dbFile.getAbsolutePath());

        try (Scanner sc = new Scanner(dbFile)) {
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                // Use quote-aware splitter so descriptions with commas don't shift columns
                String[] col = splitCsv(line);
                if (col.length < 9) continue;

                try {
                    String displayName = col[2].trim();
                    int    power       = Integer.parseInt(col[4].trim());
                    String type        = col[5].trim().toUpperCase();
                    int    pp          = Integer.parseInt(col[8].trim());

                    if (pp <= 0) pp = 10;
                    if (pp > 40) pp = 40;

                    Move m = new Move(displayName, type, power, pp);

                    // Store under exact lowercase name  e.g. "sand-attack"
                    moveDatabase.put(displayName.toLowerCase(), m);

                    // Also store under normalized key (no spaces, no hyphens, lowercase)
                    // e.g. "sand-attack" → "sandattack", "ThunderShock" → "thundershock"
                    // This lets Movesets.txt use "Sand Attack" or "Thunder Shock" and still match
                    String normalized = displayName.toLowerCase()
                                                   .replace(" ", "")
                                                   .replace("-", "");
                    moveDatabase.put(normalized, m);
                } catch (NumberFormatException ex) {
                    // Skip malformed lines silently
                }
            }
            System.out.println("Move database loaded: " + moveDatabase.size() + " moves.");
        } catch (Exception ex) {
            System.out.println("Error reading move database: " + ex.getMessage());
        }
    }

    // -----------------------------------------------------------------------
    // CSV splitter that respects quoted fields (descriptions contain commas)
    // -----------------------------------------------------------------------
    private String[] splitCsv(String line)
    {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString());
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    // -----------------------------------------------------------------------
    // Look up a move in the database by name; fallback to defaults if missing
    // -----------------------------------------------------------------------
    private Move createMoveFromDatabase(String moveName)
    {
        // Try exact lowercase match first  e.g. "bite"
        Move template = moveDatabase.get(moveName.toLowerCase());

        // If not found, try normalized (strip spaces & hyphens)  e.g. "sand attack" → "sandattack"
        if (template == null) {
            String normalized = moveName.toLowerCase().replace(" ", "").replace("-", "");
            template = moveDatabase.get(normalized);
        }

        if (template != null) {
            return new Move(template.name, template.type, template.power, template.maxAp);
        }

        System.out.println("WARNING: '" + moveName + "' not in move database, using defaults.");
        return new Move(moveName, "NORMAL", 40, 35);
    }

    // -----------------------------------------------------------------------
    // Load movesets from Movesets.txt
    // Format per line: PokemonName,MoveName,LevelReq,MoveName,LevelReq,...
    // -----------------------------------------------------------------------
    private void loadMovesetsFromFile(Pokemon p)
    {
        if (p == null) return;

        loadMoveDatabase(); // Ensure database is ready first

        String userDir = System.getProperty("user.dir");
        String[] possibleNames = { "Movesets.txt", "Movesets.txt.txt" };
        File myFile = null;

        for (String fname : possibleNames) {
            File testFile = new File(userDir, fname);
            if (testFile.exists()) { myFile = testFile; break; }
        }
        if (myFile == null) {
            for (String fname : possibleNames) {
                File testFile = new File(userDir + File.separator + "src", fname);
                if (testFile.exists()) { myFile = testFile; break; }
            }
        }

        if (myFile == null) {
            System.out.println("CRITICAL ERROR: Could not find Movesets.txt in " + userDir);
            return;
        }

        try (Scanner reader = new Scanner(myFile)) {
            while (reader.hasNextLine()) {
                String line = reader.nextLine().trim();
                if (line.isEmpty()) continue;

                String[] parts = line.split(",");
                if (parts.length < 1) continue;

                if (parts[0].trim().equalsIgnoreCase(p.name)) {
                    p.moves.clear();

                    for (int i = 1; i + 1 < parts.length; i += 2) {
                        try {
                            String moveName = parts[i].trim();
                            int    levelReq = Integer.parseInt(parts[i + 1].trim());

                            if (p.level >= levelReq) {
                                p.moves.add(createMoveFromDatabase(moveName));
                            }
                        } catch (NumberFormatException ex) {
                            System.out.println("WARNING: Bad level near index " + i + " for " + p.name);
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
                    break;
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

        if (mult > 1.0)             logArea.append("It's super effective!\n");
        else if (mult < 1.0 && mult > 0) logArea.append("It's not very effective...\n");

        updateStats();
        showMoveMenu(); // Refresh AP display

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

        // Only pick moves that still have AP
        List<Move> usable = new ArrayList<>();
        for (Move mv : enemy.moves) {
            if (mv.currentAp > 0) usable.add(mv);
        }

        if (usable.isEmpty()) {
            logArea.append("Enemy " + enemy.name + " has no moves left!\n");
            return;
        }

        Move m = usable.get(new Random().nextInt(usable.size()));
        m.currentAp--;

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
            showMainMenu();
            enemyTurn();

        } else if (choice.equals("Poké Ball")) {
            if (isTrainerBattle) {
                logArea.append("You can't catch another trainer's Pokemon!\n");
            } else {
                GameLauncher.bag.remove("Poké Ball");
                logArea.append("You threw a Poké Ball...\n");

                double hpPercent      = (double) enemy.currentHp / (double) enemy.maxHp;
                double shakeSuccessProb = Math.max(1.0 - hpPercent, 0.3);
                final double finalProb  = shakeSuccessProb;

                // Animate 3 shakes with 1-second delays using a Swing Timer
                javax.swing.Timer shakeTimer = new javax.swing.Timer(1000,
                    new java.awt.event.ActionListener() {
                        int shakeCount = 0;

                        @Override
                        public void actionPerformed(java.awt.event.ActionEvent e) {
                            shakeCount++;

                            if (Math.random() >= finalProb) {
                                // Failed a shake — break out
                                ((javax.swing.Timer) e.getSource()).stop();
                                logArea.append("Oh no! The Pokemon broke free!\n");
                                showMainMenu();
                                enemyTurn();
                                return;
                            }

                            if (shakeCount < 3) {
                                logArea.append("Shake " + shakeCount + "...\n");
                            } else {
                                // Passed all 3 shakes — caught!
                                ((javax.swing.Timer) e.getSource()).stop();
                                logArea.append("Gotcha! " + enemy.name + " was caught!\n");
                                GameLauncher.party.add(enemy);
                                GameLauncher.registerCaught(enemy.name);
                                game.endBattle(true);
                            }
                        }
                    });

                shakeTimer.start();
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

    private ImageIcon loadPokemonImage(String name)
    {
        String lowerCaseName = name.toLowerCase();

        String[] searchPaths = {
            // School network drive — checked first
            "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".jpg",
            "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".png",
            // Relative fallbacks
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

        return null;
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

            if (selectedIndex < 0) continue;

            Pokemon selected = GameLauncher.party.get(selectedIndex);

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

            GameLauncher.party.remove(selectedIndex);
            GameLauncher.party.add(0, selected);

            loadMovesetsFromFile(selected);

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

            if (p.level > 100) { p.level = 100; p.xp = 0; }

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
        loadMovesetsFromFile(p); // Re-load to pick up any newly level-gated moves
    }

    private void learnMove(Pokemon p, Move newMove)
    {
        if (p.moves.size() < 4) {
            p.moves.add(newMove);
            logArea.append(p.name + " learned " + newMove.name + "!\n");
        } else {
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
    // Evolution database: pokemon name (lowercase) → EvolutionEntry
    // Loaded once from evolutions.txt (or Evolutions.txt)
    //
    // File format — one evolution per line:
    //   BaseName,EvolvesInto,Method,Requirement
    //
    // Examples:
    //   Charmander,Charmeleon,Level,16
    //   Poliwhirl,Poliwrath,Level,36
    //   Eevee,Vaporeon,Water Stone,0
    //
    // Only "Level" evolutions are triggered automatically in battle.
    // Stone evolutions can be added later via the bag/item system.
    // -----------------------------------------------------------------------
    private static class EvolutionEntry {
        String evolvesInto;
        String method;        // e.g. "Level", "Water Stone", "Thunder Stone"
        int    levelRequired; // only used when method is "Level"
        EvolutionEntry(String into, String method, int level) {
            this.evolvesInto   = into;
            this.method        = method;
            this.levelRequired = level;
        }
    }

    private java.util.Map<String, EvolutionEntry> evolutionDatabase = new java.util.HashMap<>();
    private boolean evolutionDatabaseLoaded = false;

    private void loadEvolutionDatabase()
    {
        if (evolutionDatabaseLoaded) return;
        evolutionDatabaseLoaded = true;

        String userDir = System.getProperty("user.dir");
        String[] possibleNames = { "evolutions.txt", "Evolutions.txt", "Evolution.txt" };
        File evoFile = null;

        for (String fname : possibleNames) {
            File f = new File(userDir, fname);
            if (f.exists()) { evoFile = f; break; }
        }
        if (evoFile == null) {
            for (String fname : possibleNames) {
                File f = new File(userDir + File.separator + "src", fname);
                if (f.exists()) { evoFile = f; break; }
            }
        }

        if (evoFile == null) {
            System.out.println("evolutions.txt not found — using built-in evolutions only.");
            loadBuiltInEvolutions();
            return;
        }

        System.out.println("Loading evolutions from: " + evoFile.getAbsolutePath());

        try (Scanner sc = new Scanner(evoFile)) {
            int count = 0;
            while (sc.hasNextLine()) {
                String line = sc.nextLine().trim();
                if (line.isEmpty() || line.startsWith("#")) continue;

                String[] parts = line.split(",");

                // Support both 3-column (BaseName,EvolvesInto,Level) and
                // 4-column (BaseName,EvolvesInto,Method,Requirement) formats
                try {
                    String baseName    = parts[0].trim();
                    String evolvesInto = parts[1].trim();
                    String method      = "Level"; // default
                    int    levelReq    = 0;

                    if (parts.length >= 4) {
                        // 4-column format: BaseName,EvolvesInto,Method,Requirement
                        method   = parts[2].trim();
                        if (method.equalsIgnoreCase("Level")) {
                            levelReq = Integer.parseInt(parts[3].trim());
                        }
                    } else if (parts.length == 3) {
                        // 3-column format: BaseName,EvolvesInto,Level
                        levelReq = Integer.parseInt(parts[2].trim());
                    }

                    evolutionDatabase.put(baseName.toLowerCase(),
                                         new EvolutionEntry(evolvesInto, method, levelReq));
                    count++;
                } catch (NumberFormatException ex) {
                    System.out.println("WARNING: Bad entry in evolutions.txt: " + line);
                }
            }
            System.out.println("Evolution database loaded: " + count + " entries.");
        } catch (Exception ex) {
            System.out.println("Error reading evolutions.txt: " + ex.getMessage());
            loadBuiltInEvolutions();
        }
    }

    // Fallback used when evolutions.txt is missing
    private void loadBuiltInEvolutions()
    {
        evolutionDatabase.put("charmander", new EvolutionEntry("Charmeleon", "Level", 16));
        evolutionDatabase.put("charmeleon", new EvolutionEntry("Charizard",  "Level", 36));
        evolutionDatabase.put("squirtle",   new EvolutionEntry("Wartortle",  "Level", 16));
        evolutionDatabase.put("wartortle",  new EvolutionEntry("Blastoise",  "Level", 36));
        evolutionDatabase.put("bulbasaur",  new EvolutionEntry("Ivysaur",    "Level", 16));
        evolutionDatabase.put("ivysaur",    new EvolutionEntry("Venusaur",   "Level", 36));
        System.out.println("Using built-in evolutions (starters only).");
    }

    // -----------------------------------------------------------------------
    // Check if the active Pokemon should evolve after a battle
    // -----------------------------------------------------------------------
    private void checkEvolution()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;

        loadEvolutionDatabase();

        EvolutionEntry entry = evolutionDatabase.get(p.name.toLowerCase());
        if (entry == null) return;                          // Doesn't evolve
        if (!entry.method.equalsIgnoreCase("Level")) return; // Not a level evolution
        if (p.level < entry.levelRequired) return;          // Not high enough yet

        evolvePokemon(p, entry.evolvesInto);
    }

    // -----------------------------------------------------------------------
    // Perform the evolution and check for a further evolution (stage 3 chains)
    // e.g. Caterpie → Metapod → Butterfree in one battle
    // -----------------------------------------------------------------------
    private void evolvePokemon(Pokemon p, String nextForm)
    {
        String oldName = p.name;
        p.name      = nextForm;
        p.maxHp    += 20;
        p.currentHp = p.maxHp;

        loadMovesetsFromFile(p); // Load moves for the new form
        updateStats();           // Refresh image to new form immediately

        logArea.append("\nWhat? " + oldName + " is evolving!\n");
        JOptionPane.showMessageDialog(this,
            oldName + " evolved into " + p.name + "!",
            "Evolution", JOptionPane.INFORMATION_MESSAGE);
        logArea.append(oldName + " became " + p.name + "!\n");

        // Recursively check if the new form also evolves at this level
        // (handles stage-3 chains like Caterpie lv7 → Metapod lv10 → Butterfree)
        checkEvolution();
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
