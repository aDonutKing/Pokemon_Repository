package PART2;
import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.Scanner;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;


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
        enemyImage = new JLabel("Enemy", SwingConstants.CENTER);
       
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


    public void setupBattle(Pokemon e, boolean trainer, String name)
    {
        this.enemy = e;
        this.isTrainerBattle = trainer;


        // --- ADDED: Load Movesets from text file ---
        loadMovesetsFromFile(getActivePokemon());
        loadMovesetsFromFile(enemy);


        enemy.healFull();
        logArea.setText("Battle started against " + name + "!\n");
        showMainMenu();
        updateStats();
    }


    // --- NEW: Method to read Movesets.txt ---
private void loadMovesetsFromFile(Pokemon p) {
    if (p == null) return;


    String userDir = System.getProperty("user.dir");
    String[] possibleNames = {"Movesets.txt", "Movesets.txt.txt", "moves.txt"};
    File myFile = null;


    // Try to find the file in the project folder
    for (String name : possibleNames) {
        File testFile = new File(userDir, name);
        if (testFile.exists()) {
            myFile = testFile;
            break;
        }
    }


    // If still not found, try looking in the 'src' folder (common for Eclipse/IntelliJ)
    if (myFile == null) {
        for (String name : possibleNames) {
            File testFile = new File(userDir + File.separator + "src", name);
            if (testFile.exists()) {
                myFile = testFile;
                break;
            }
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
            String pokeNameInFile = parts[0].trim();


            if (pokeNameInFile.equalsIgnoreCase(p.name)) {
                p.moves.clear();
               
                // Read Move Name and Level Required
                for (int i = 1; i < parts.length - 1; i += 2) {
                    try {
                        String moveName = parts[i].trim();
                        int levelReq = Integer.parseInt(parts[i+1].trim());


                        if (p.level >= levelReq) {
                            p.moves.add(new Move(moveName, "Normal", 40, 35));
                        }
                    } catch (Exception e) { /* Skip bad formatting */ }
                }
               
                // Keep the 4 most recent moves
                if (p.moves.size() > 4) {
                    java.util.List<Move> lastFour = new java.util.ArrayList<>(
                        p.moves.subList(p.moves.size() - 4, p.moves.size())
                    );
                    p.moves.clear();
                    p.moves.addAll(lastFour);
                }
                break;
            }
        }
    } catch (Exception e) {
        System.out.println("SYSTEM ERROR: " + e.getMessage());
    }
}
    private void showMainMenu()
    {
        menuState = 0;
        backBtn.setVisible(false);
       
        btn1.setText("FIGHT");
        btn2.setText("BAG");
        btn3.setText("POKEMON");
        btn4.setText("RUN");
    }


    private void showMoveMenu() {
        menuState = 1;
        backBtn.setVisible(true);
       
        Pokemon p = getActivePokemon();
       
        btn1.setText(formatMoveText(p, 0));
        btn2.setText(formatMoveText(p, 1));
        btn3.setText(formatMoveText(p, 2));
        btn4.setText(formatMoveText(p, 3));
    }


    private String formatMoveText(Pokemon p, int index) {
        if (index >= p.moves.size()) return "-";
        Move m = p.moves.get(index);
        return m.name + " (" + m.currentAp + "/" + m.maxAp + ")";
    }


    private void handleButton(int index) {
        if (menuState == 0) {
            if(index == 0) showMoveMenu();
            if(index == 1) openBag();
            if(index == 2) switchPokemon(false);
            if(index == 3) tryRun();
        } else if (menuState == 1) {
            useMove(index);
        }
    }


    private void useMove(int index) {
        Pokemon p = getActivePokemon();
        if (p == null) return;


        if(index >= p.moves.size()) return;


        Move m = p.moves.get(index);


        if (m.currentAp <= 0) {
            logArea.append("No AP left for " + m.name + "!\n");
            return;
        }


        m.currentAp--;


        logArea.append(p.name + " used " + m.name + "!\n");
        showMoveMenu();


        double mult = Pokemon.getEffectiveness(m.type, enemy.type);
        int dmg = (int)((p.level * 2 * m.power / 20) * mult);
        if (m.power > 0 && dmg == 0) dmg = 1;


        enemy.takeDamage(dmg);


        if(mult > 1.0) logArea.append("It's super effective!\n");


        updateStats();


        if(enemy.currentHp <= 0) winBattle();
        else enemyTurn();
    }


    private void enemyTurn()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;
        if(p.currentHp <= 0) return;


        if (enemy.moves.isEmpty()) return;


        Move m = enemy.moves.get(new Random().nextInt(enemy.moves.size()));


        double mult = Pokemon.getEffectiveness(m.type, p.type);
        int dmg = (int)((enemy.level * 2 * m.power / 25) * mult);
        if (m.power > 0 && dmg == 0) dmg = 1;


        p.takeDamage(dmg);


        logArea.append("Enemy " + enemy.name + " used " + m.name + "!\n");


        updateStats();


        if(p.currentHp <= 0)
        {
            logArea.append(p.name + " fainted!\n");
            checkPartyFainted();
        } else
        {
            showMainMenu();
        }
    }


    private void openBag()
    {
        if(GameLauncher.bag.isEmpty())
        {
            logArea.append("Bag is empty!\n");
            return;
        }


        String[] items = GameLauncher.bag.toArray(new String[0]);
        String choice = (String)JOptionPane.showInputDialog(this, "Select Item", "Bag",
                        JOptionPane.PLAIN_MESSAGE, null, items, items[0]);
       
        if(choice != null) {
            if(choice.equals("Potion"))
            {
                getActivePokemon().currentHp = Math.min(getActivePokemon().maxHp, getActivePokemon().currentHp + 20);
                GameLauncher.bag.remove("Potion");
                logArea.append("Used Potion on " + getActivePokemon().name + "!\n");
                updateStats();
                enemyTurn();
            }
            else if(choice.equals("Poké Ball"))
                {
                if(isTrainerBattle)
                {
                    logArea.append("You can't catch another trainer's Pokemon!\n");
                } else
                {
                    GameLauncher.bag.remove("Poké Ball");
                    logArea.append("You threw a Poké Ball...\n");
                   
                    double catchChance = 1.0 - ((double)enemy.currentHp / enemy.maxHp);
                    if(Math.random() < catchChance || Math.random() < 0.3)
                    {
                        logArea.append("Gotcha! " + enemy.name + " was caught!\n");
                        GameLauncher.party.add(enemy);
                        GameLauncher.registerCaught(enemy.name);
                        game.endBattle(true);
                    } else
                    {
                        logArea.append("Oh no! The Pokemon broke free!\n");
                        enemyTurn();
                    }
                }
            }
        }
    }


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
   
    private void updateImages() {
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


    private ImageIcon loadPokemonImage(String name) {
        String lowerCaseName = name.toLowerCase();
        String filePath = "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".jpg";
       
        File imgFile = new File(filePath);
        if (!imgFile.exists()) return null;
       
        ImageIcon icon = new ImageIcon(filePath);
        Image img = icon.getImage().getScaledInstance(150, 150, Image.SCALE_SMOOTH);
        return new ImageIcon(img);
    }


    private void updateBarColor(JProgressBar bar)
    {
        if (bar.getMaximum() == 0) return;
        double percent = (double)bar.getValue() / bar.getMaximum();


        if (percent > 0.5) bar.setForeground(Color.GREEN);
        else if (percent > 0.2) bar.setForeground(Color.YELLOW);
        else bar.setForeground(Color.RED);
    }


    private void switchPokemon(boolean forced) {
        if(GameLauncher.party.size() < 2) {
            logArea.append("Only one Pokemon!\n");
            return;
        }
       
        String[] partyNames = new String[GameLauncher.party.size()];
        for(int i = 0; i < GameLauncher.party.size(); i++) {
            Pokemon pk = GameLauncher.party.get(i);
            partyNames[i] = pk.name + " (HP: " + pk.currentHp + "/" + pk.maxHp + ")";
        }
       
        while (true) {
            String choice = (String)JOptionPane.showInputDialog(this, "Switch to:", "Party",
                            JOptionPane.QUESTION_MESSAGE, null, partyNames, partyNames[0]);
           
            if(choice == null) {
                if (forced) {
                    JOptionPane.showMessageDialog(this, "You must choose a new Pokémon to continue!");
                    continue;
                } else return;
            }


            int selectedIndex = -1;
            for(int i = 0; i < partyNames.length; i++) {
                if(partyNames[i].equals(choice)) { selectedIndex = i; break; }
            }


            if(selectedIndex == 0) {
                if (forced) {
                    JOptionPane.showMessageDialog(this, getActivePokemon().name + " fainted! Choose a different Pokémon.");
                    continue;
                } else {
                    logArea.append(getActivePokemon().name + " is already out!\n");
                    return;
                }
            } else if(GameLauncher.party.get(selectedIndex).currentHp <= 0) {
                JOptionPane.showMessageDialog(this, "That Pokemon has no energy!");
                if (forced) continue;
                else return;
            } else {
                Pokemon chosen = GameLauncher.party.remove(selectedIndex);
                GameLauncher.party.add(0, chosen);
               
                // --- RELOAD MOVES for the newly switched Pokemon ---
                loadMovesetsFromFile(chosen);
               
                logArea.append("Go! " + getActivePokemon().name + "!\n");
                updateStats();
                showMainMenu();
                if (!forced) enemyTurn();
                break;
            }
        }
    }


    private void checkPartyFainted() {
        boolean anyAlive = false;
        for(Pokemon p : GameLauncher.party) if(p.currentHp > 0) anyAlive = true;
       
        if(!anyAlive) game.endBattle(false);
        else switchPokemon(true);
    }


    private void tryRun() {
        if(isTrainerBattle) logArea.append("Can't run!\n");
        else game.endBattle(true);
    }


    private void winBattle()
    {
        Pokemon p = getActivePokemon();
        if (p == null) return;


        int oldLevel = p.level;


        if (p.level < 100) {
            int xpGain = enemy.level * 10;
            p.gainXp(xpGain);
            logArea.append("Gained " + xpGain + " XP!\n");


            if (p.level > 100) {
                p.level = 100;
                p.xp = 0;
            }


            if(p.level > oldLevel) checkNewMoves(p);
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


    private void checkNewMoves(Pokemon p) {
        // You can leave your level-up logic here or move it to a text file!
        // But the loadMovesetsFromFile will handle the initial set.
    }


    private void learnMove(Pokemon p, Move newMove) {
        if(p.moves.size() < 4) {
            p.moves.add(newMove);
            logArea.append(p.name + " learned " + newMove.name + "!\n");
        } else {
            // Option dialog for forgetting moves (existing logic)
        }
    }


private void checkEvolution() {
    Pokemon p = getActivePokemon();
    if (p == null) return;

    String userDir = System.getProperty("user.dir");
    File evoFile = new File(userDir, "evolutions.txt");
    if (!evoFile.exists()) {
        evoFile = new File(userDir + File.separator + "src", "evolutions.txt");
    }

    if (!evoFile.exists()) return;

    try (Scanner reader = new Scanner(evoFile)) {
        while (reader.hasNextLine()) {
            String line = reader.nextLine().trim();
            if (line.isEmpty()) continue;

            String[] parts = line.split(",");
            if (parts.length < 4) continue;

            String baseForm = parts[0].trim();
            String nextForm = parts[1].trim();
            String method = parts[2].trim(); // "Level", "Thunder Stone", etc.
            String requirement = parts[3].trim(); // The number "16" or "0"

            // 1. Check if the Name matches
            if (p.name.equalsIgnoreCase(baseForm)) {
                
                // 2. ONLY try to parse the number if the method is "Level"
                if (method.equalsIgnoreCase("Level")) {
                    int levelReq = Integer.parseInt(requirement);

                    if (p.level >= levelReq) {
                        evolvePokemon(p, nextForm);
                        return; 
                    }
                } 
                // 3. If you want to add Stone evolutions later, you'd add an "else if" here
            }
        }
    } catch (Exception e) {
        System.out.println("DEBUG: Evolution Error: " + e.getMessage());
    }
}

// Helper method to keep the code clean
private void evolvePokemon(Pokemon p, String nextForm) {
    String oldName = p.name;
    p.name = nextForm;
    p.maxHp += 20;
    p.currentHp = p.maxHp;

    loadMovesetsFromFile(p);
    updateStats();

    logArea.append("\nWhat? " + oldName + " is evolving!\n");
    JOptionPane.showMessageDialog(this, oldName + " evolved into " + p.name + "!");
    
    checkEvolution(); // Check for stage 3
}

    private Pokemon getActivePokemon() {
        if (GameLauncher.party == null || GameLauncher.party.isEmpty()) return null;
        return GameLauncher.party.get(0);
    }
} // Final closing bracket for BattlePanel class