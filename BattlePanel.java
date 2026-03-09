package PART2;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Random;

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
        enemy.healFull(); 
        logArea.setText("Battle started against " + name + "!\n");
        showMainMenu();
        updateStats(); 
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
            if(index == 2) switchPokemon();
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
        
        // Sync images every time stats update
        updateImages();
    }
    
    // --- Image Loading Methods ---
    private void updateImages() {
        Pokemon p = getActivePokemon();
        if (p != null) {
            ImageIcon pIcon = loadPokemonImage(p.name);
            playerImage.setIcon(pIcon);
            // If no image is found, show the name text instead
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
        // Converts name to lowercase and looks for .jpg
        String lowerCaseName = name.toLowerCase();
        if(lowerCaseName.equals("farfetch-d")) lowerCaseName = "farfetchd"; // Handle special case
        String filePath = "T:\\HS\\Student\\Computer Science\\Software Engineering\\Pokemon Sprites\\" + lowerCaseName + ".jpg";
        
        File imgFile = new File(filePath);
        if (!imgFile.exists()) {
            return null; 
        }
        
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

    private void switchPokemon() {
        if(GameLauncher.party.size() < 2) {
            logArea.append("Only one Pokemon!\n");
            return;
        }
        String[] partyNames = new String[GameLauncher.party.size()];
        for(int i = 0; i < GameLauncher.party.size(); i++) {
            Pokemon pk = GameLauncher.party.get(i);
            partyNames[i] = pk.name + " (HP: " + pk.currentHp + "/" + pk.maxHp + ")";
        }
        String choice = (String)JOptionPane.showInputDialog(this, "Switch to:", "Party",
                        JOptionPane.QUESTION_MESSAGE, null, partyNames, partyNames[0]);
        if(choice != null) {
            int selectedIndex = -1;
            for(int i = 0; i < partyNames.length; i++) {
                if(partyNames[i].equals(choice)) { selectedIndex = i; break; }
            }
            if(selectedIndex == 0) {
                logArea.append(getActivePokemon().name + " is already out!\n");
            } else if(GameLauncher.party.get(selectedIndex).currentHp <= 0) {
                logArea.append("That Pokemon has no energy!\n");
            } else {
                Pokemon chosen = GameLauncher.party.remove(selectedIndex);
                GameLauncher.party.add(0, chosen);
                logArea.append("Go! " + getActivePokemon().name + "!\n");
                updateStats();
                showMainMenu(); 
                enemyTurn();
            }
        }
    }

    private void checkPartyFainted() {
        boolean anyAlive = false;
        for(Pokemon p : GameLauncher.party) if(p.currentHp > 0) anyAlive = true;
        if(!anyAlive) game.endBattle(false);
        else switchPokemon();
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

        int xpGain = enemy.level * 10;
        p.gainXp(xpGain);
        logArea.append("Gained " + xpGain + " XP!\n");

        if(p.level > oldLevel) {
            checkNewMoves(p);
        }

        checkEvolution();

        GameLauncher.money += isTrainerBattle ? 300 : 50;

        updateStats();

        JOptionPane.showMessageDialog(this, "You Won!");
        game.endBattle(true);
    }

    private void checkNewMoves(Pokemon p) {
        String n = p.name;
        
        if(n.equals("Bulbasaur") || n.equals("Ivysaur") || n.equals("Venusaur")) {
            if(p.level == 1 && p.moves.isEmpty()) {
                learnMove(p, new Move("Tackle", "Normal", 40, 35));
                learnMove(p, new Move("Growl", "Normal", 0, 40));
            }
            if(p.level == 7) learnMove(p, new Move("Leech Seed", "Grass", 20, 10));
            if(p.level == 13) learnMove(p, new Move("Vine Whip", "Grass", 45, 25));
            if(p.level == 20) learnMove(p, new Move("Poison Powder", "Poison", 0, 35));
            if(p.level == 27) learnMove(p, new Move("Razor Leaf", "Grass", 55, 25));
            if(p.level == 48) learnMove(p, new Move("Solar Beam", "Grass", 120, 10));
        }
        else if(n.equals("Charmander") || n.equals("Charmeleon") || n.equals("Charizard")) {
             if(p.level == 1 && p.moves.isEmpty()) {
                learnMove(p, new Move("Scratch", "Normal", 40, 35));
                learnMove(p, new Move("Growl", "Normal", 0, 40));
            }
            if(p.level == 7) learnMove(p, new Move("Ember", "Fire", 40, 25));
            if(p.level == 13) learnMove(p, new Move("Metal Claw", "Steel", 50, 35));
            if(p.level == 16) learnMove(p, new Move("Dragon Rage", "Dragon", 40, 10)); 
            if(p.level == 30) learnMove(p, new Move("Flamethrower", "Fire", 90, 15));
        }
        else if(n.equals("Squirtle") || n.equals("Wartortle") || n.equals("Blastoise")) {
             if(p.level == 1 && p.moves.isEmpty()) {
                learnMove(p, new Move("Tackle", "Normal", 40, 35));
                learnMove(p, new Move("Tail Whip", "Normal", 0, 40));
            }
            if(p.level == 7) learnMove(p, new Move("Water Gun", "Water", 40, 25));
            if(p.level == 13) learnMove(p, new Move("Bubble", "Water", 40, 30));
            if(p.level == 18) learnMove(p, new Move("Bite", "Dark", 60, 25));
            if(p.level == 30) learnMove(p, new Move("Hydro Pump", "Water", 110, 5));
        }
    }

    private void learnMove(Pokemon p, Move newMove) {
        if(p.moves.size() < 4) {
            p.moves.add(newMove);
            logArea.append(p.name + " learned " + newMove.name + "!\n");
            JOptionPane.showMessageDialog(this, p.name + " learned " + newMove.name + "!");
        } else {
            String[] options = {
                p.moves.get(0).name,
                p.moves.get(1).name,
                p.moves.get(2).name,
                p.moves.get(3).name,
                "Do not learn"
            };

            int choice = JOptionPane.showOptionDialog(this,
                p.name + " wants to learn " + newMove.name + ".\nBut " + p.name + " already knows 4 moves!\nSelect a move to forget:",
                "Learn " + newMove.name + "?",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null, options, options[4]);

            if(choice >= 0 && choice < 4) {
                String oldMove = p.moves.get(choice).name;
                p.moves.set(choice, newMove);
                
                logArea.append("Forgot " + oldMove + " and learned " + newMove.name + "!\n");
                JOptionPane.showMessageDialog(this, "1, 2, and... Poof! " + oldMove + " was forgotten.\nAnd " + newMove.name + " was learned!");
            } else {
                logArea.append(p.name + " did not learn " + newMove.name + ".\n");
            }
        }
    }

    private void checkEvolution() {
        Pokemon p = getActivePokemon();
        String oldName = p.name;
        boolean evolved = false;

        if (p.level >= 30) {
            if (p.name.equals("Charmeleon")) { p.name = "Charizard"; evolved = true; }
            else if (p.name.equals("Wartortle")) { p.name = "Blastoise"; evolved = true; }
            else if (p.name.equals("Ivysaur")) { p.name = "Venusaur"; evolved = true; }
        }
        else if (p.level >= 15) {
            if (p.name.equals("Charmander")) { p.name = "Charmeleon"; evolved = true; }
            else if (p.name.equals("Squirtle")) { p.name = "Wartortle"; evolved = true; }
            else if (p.name.equals("Bulbasaur")) { p.name = "Ivysaur"; evolved = true; }
        }

        if (evolved) {
            logArea.append("\nWhat? " + oldName + " is evolving!\n");
            
            JOptionPane.showMessageDialog(this,
                oldName + " has evolved into " + p.name + "!",
                "Evolution", JOptionPane.INFORMATION_MESSAGE);

            p.maxHp += 20;
            p.currentHp = p.maxHp;
            
            logArea.append(oldName + " became " + p.name + "!\n");
            updateStats(); 
        }
    }

    private Pokemon getActivePokemon()
    {
        if (GameLauncher.party.isEmpty())
        {
            JOptionPane.showMessageDialog(this, "You don't have a Pokémon yet!");
            game.endBattle(false);
            return null;
        }
        return GameLauncher.party.get(0);
    }
}
