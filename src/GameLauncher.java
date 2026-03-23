import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*; 
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameLauncher extends JFrame
{
    // --- GLOBAL STATE ---
    public static String playerName = "Red";
    public static List<Pokemon> party = new ArrayList<>();
    public static List<String> bag = new ArrayList<>();
    public static List<String> tms = new ArrayList<>(); // NEW: Added TM pocket for the inventory!
    public static int money = 500;
   
    // Pokedex State
    public static boolean[] pokedexSeen = new boolean[152];
    public static boolean[] pokedexCaught = new boolean[152];
   
    // Story Flags
    public static boolean hasStarter = false;
    public static boolean trainer1Defeated = false;
    public static boolean trainer2Defeated = false;
    public static boolean badgeRock = false;

    private CardLayout cardLayout = new CardLayout();
    private JPanel mainPanel = new JPanel(cardLayout);
    private MainMenuPanel mainMenuPanel; 
    private WorldPanel worldPanel;
    private BattlePanel battlePanel;
    private PokedexPanel pokedexPanel;
    private InventoryPanel inventoryPanel; // NEW: The Inventory Screen
   
    private String currentEnemyID = "";
    
    public GameLauncher()
    {
        // LOAD DATA FIRST
        PokemonData.loadFromFile("data/pokemon.txt");

        setTitle("Java Pokémon: Full Battle System");
        setSize(800, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        setupMenuBar();

        // Initialize Panels
        mainMenuPanel = new MainMenuPanel(this); 
        // NOTE: Make sure your WorldPanel and BattlePanel classes are defined elsewhere in your project!
        worldPanel = new WorldPanel(this);
        battlePanel = new BattlePanel(this);
        pokedexPanel = new PokedexPanel(this);
        inventoryPanel = new InventoryPanel(this); // NEW: Initialize the Bag

        // Add Panels to Layout
        mainPanel.add(mainMenuPanel, "MENU"); 
        mainPanel.add(worldPanel, "WORLD");
        mainPanel.add(battlePanel, "BATTLE");
        mainPanel.add(pokedexPanel, "DEX");
        mainPanel.add(inventoryPanel, "BAG"); // NEW: Add to CardLayout
       
        add(mainPanel);

        cardLayout.show(mainPanel, "MENU");
    }
    
    public void startGame(boolean isNewGame) 
    {
        if (isNewGame) 
        {
            // Starter Items
            if(bag.isEmpty()) 
            {
                bag.add("Potion"); bag.add("Potion"); bag.add("Poké Ball"); bag.add("Poké Ball");
                tms.add("TM01 - Tackle"); // A starter TM for testing
            }
            JOptionPane.showMessageDialog(this, "Welcome " + playerName + "!\nWild Pokemon only appear in DARK GRASS (Bushes)!\nVisit the Center to heal and Mart to buy items.\n\nPress 'P' to open Pokedex, 'B' to open your Bag!");
        }
        
        cardLayout.show(mainPanel, "WORLD");
        if (worldPanel != null) 
        {
            worldPanel.requestFocusInWindow(); 
        }
    }

    private void setupMenuBar() 
    {
        JMenuBar menuBar = new JMenuBar();
        JMenu fileMenu = new JMenu("File");
        
        JMenuItem saveItem = new JMenuItem("Save Game");
        JMenuItem loadItem = new JMenuItem("Load Game");

        saveItem.addActionListener(e -> saveGame());
        loadItem.addActionListener(e -> 
        {
            if(loadGame()) 
            {
                startGame(false); 
                if(worldPanel != null) 
                {
                    worldPanel.refreshMap(); 
                }
            }
        });

        fileMenu.add(saveItem);
        fileMenu.add(loadItem);
        menuBar.add(fileMenu);
        setJMenuBar(menuBar);
    }

    public void saveGame() 
    {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream("save.dat"))) 
        {
            SaveState state = new SaveState();
            state.playerName = playerName; 
            state.party = party;
            state.bag = bag;
            state.tms = tms; // NEW: Save TMs
            state.money = money;
            state.pokedexSeen = pokedexSeen;
            state.pokedexCaught = pokedexCaught;
            state.hasStarter = hasStarter;
            state.trainer1Defeated = trainer1Defeated;
            state.trainer2Defeated = trainer2Defeated;
            state.badgeRock = badgeRock;
            
            oos.writeObject(state);
            JOptionPane.showMessageDialog(this, "Game Saved Successfully!");
            if(worldPanel != null) worldPanel.requestFocusInWindow();
        } catch (Exception e) 
        {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving game: " + e.getMessage());
        }
    }

    public boolean loadGame() 
    {
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream("save.dat"))) 
        {
            SaveState state = (SaveState) ois.readObject();
            playerName = state.playerName; 
            party = state.party;
            bag = state.bag;
            
            // NEW: Safely load TMs (in case an old save file doesn't have them)
            if (state.tms != null) 
            {
                tms = state.tms;
            }
            
            money = state.money;
            pokedexSeen = state.pokedexSeen;
            pokedexCaught = state.pokedexCaught;
            hasStarter = state.hasStarter;
            trainer1Defeated = state.trainer1Defeated;
            trainer2Defeated = state.trainer2Defeated;
            badgeRock = state.badgeRock;
            
            JOptionPane.showMessageDialog(this, "Game Loaded Successfully! Welcome back, " + playerName + "!");
            return true;
        } catch (Exception e) 
        {
            JOptionPane.showMessageDialog(this, "No save file found or error loading.");
            return false;
        }
    }

    public void startBattle(Pokemon enemy, boolean isTrainer, String trainerName, String id)
    {
        this.currentEnemyID = id;
       
        for(int i=1; i<Pokedex.NAMES.length; i++) 
        {
            if(Pokedex.NAMES[i].equals(enemy.name)) 
            {
                pokedexSeen[i] = true;
                break;
            }
        }
       
        battlePanel.setupBattle(enemy, isTrainer, trainerName);
        cardLayout.show(mainPanel, "BATTLE");
        battlePanel.requestFocus();
    }

    public void endBattle(boolean won)
    {
        cardLayout.show(mainPanel, "WORLD");
        worldPanel.requestFocusInWindow();
       
        if(won)
        {
            if(currentEnemyID.equals("TRAINER1")) trainer1Defeated = true;
            if(currentEnemyID.equals("TRAINER2")) trainer2Defeated = true;
            if(currentEnemyID.equals("LEADER")) badgeRock = true;
            worldPanel.refreshMap();
        }
        else
        {
            JOptionPane.showMessageDialog(this, "You blacked out! Scurrying back to home...");
            worldPanel.respawn();
            for(Pokemon p : party) p.healFull();
        }
    }

    // --- NEW: Inventory Navigation Methods ---
    public void openInventory() 
    {
        inventoryPanel.refresh();
        cardLayout.show(mainPanel, "BAG");
        inventoryPanel.requestFocus();
    }

    public void closeInventory() 
    {
        cardLayout.show(mainPanel, "WORLD");
        if (worldPanel != null) worldPanel.requestFocusInWindow();
    }

    public void openPokedex() 
    {
        pokedexPanel.refresh();
        cardLayout.show(mainPanel, "DEX");
        pokedexPanel.requestFocus();
    }

    public void closePokedex() 
    {
        cardLayout.show(mainPanel, "WORLD");
        worldPanel.requestFocusInWindow();
    }
   
    public static void registerCaught(String name) 
    {
         for(int i=1; i<Pokedex.NAMES.length; i++) 
        {
            if(Pokedex.NAMES[i].equals(name)) 
            {
                pokedexCaught[i] = true;
                pokedexSeen[i] = true;
                break;
            }
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(() -> new GameLauncher().setVisible(true));
    }
}

// --- NEW CLASS: INVENTORY PANEL ---
class InventoryPanel extends JPanel 
{
    private GameLauncher game;
    private DefaultListModel<String> listModel;
    private JList<String> itemList;
    private JComboBox<String> categoryBox;

    public InventoryPanel(GameLauncher game) 
    {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        // Header
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(34, 139, 34)); // Forest green
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
        JLabel title = new JLabel("BACKPACK", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        header.add(title, BorderLayout.CENTER);

        // Category Selector (Items vs TMs)
        String[] categories = {"Items", "TMs / HMs"};
        categoryBox = new JComboBox<>(categories);
        categoryBox.setFont(new Font("Monospaced", Font.BOLD, 16));
        categoryBox.addActionListener(e -> refresh());
        header.add(categoryBox, BorderLayout.SOUTH);
        add(header, BorderLayout.NORTH);

        // Item List
        listModel = new DefaultListModel<>();
        itemList = new JList<>(listModel);
        itemList.setFont(new Font("Monospaced", Font.PLAIN, 18));
        itemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        add(new JScrollPane(itemList), BorderLayout.CENTER);

        // Footer with Action Buttons
        JPanel footer = new JPanel();
        footer.setBackground(Color.DARK_GRAY);

        JButton useBtn = new JButton("USE");
        useBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        useBtn.addActionListener(e -> useSelectedItem());
        footer.add(useBtn);

        JButton backBtn = new JButton("CLOSE BAG");
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        backBtn.addActionListener(e -> game.closeInventory());
        footer.add(backBtn);

        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() 
    {
        listModel.clear();
        String selectedCategory = (String) categoryBox.getSelectedItem();

        if ("Items".equals(selectedCategory)) 
        {
            for (String item : GameLauncher.bag) 
            {
                listModel.addElement(item);
            }
        } else 
        {
            for (String tm : GameLauncher.tms) 
            {
                listModel.addElement(tm);
            }
        }
    }

    private void useSelectedItem() 
    {
        String selected = itemList.getSelectedValue();
        if (selected == null) 
        {
            JOptionPane.showMessageDialog(this, "Please select an item first!");
            return;
        }

        if (GameLauncher.party.isEmpty()) 
        {
            JOptionPane.showMessageDialog(this, "You have no Pokémon to use this on!");
            return;
        }

        // Ask which Pokemon to use it on
        String[] partyNames = new String[GameLauncher.party.size()];
        for (int i = 0; i < GameLauncher.party.size(); i++) 
        {
            partyNames[i] = GameLauncher.party.get(i).name + " (HP: " + GameLauncher.party.get(i).currentHp + "/" + GameLauncher.party.get(i).maxHp + ")";
        }

        String target = (String) JOptionPane.showInputDialog(this, "Use " + selected + " on which Pokémon?", 
                        "Select Target", JOptionPane.QUESTION_MESSAGE, null, partyNames, partyNames[0]);

        if (target != null) 
        {
            int targetIndex = -1;
            for (int i = 0; i < partyNames.length; i++) 
            {
                if (partyNames[i].equals(target)) targetIndex = i;
            }

            Pokemon p = GameLauncher.party.get(targetIndex);

            if (selected.equals("Potion")) 
            {
                if (p.currentHp == p.maxHp) 
                {
                    JOptionPane.showMessageDialog(this, p.name + "'s HP is already full!");
                    return;
                }
                p.currentHp = Math.min(p.maxHp, p.currentHp + 20);
                GameLauncher.bag.remove("Potion");
                JOptionPane.showMessageDialog(this, "Healed " + p.name + " for 20 HP!");
                refresh();
            } 
            else if (selected.equals("Poké Ball")) 
                {
                 JOptionPane.showMessageDialog(this, "Professor Oak's words echoed: There's a time and place for everything! (Use in battle)");
                } 
            else if (selected.startsWith("TM")) 
                {
                 JOptionPane.showMessageDialog(this, "TM Logic goes here! (Requires Move learning logic)");
                }
        }
    }
}

// --- MAIN MENU PANEL ---
class MainMenuPanel extends JPanel 
{
    public MainMenuPanel(GameLauncher game) 
    {
        setLayout(new GridBagLayout());
        setBackground(new Color(135, 206, 250)); 

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(10, 10, 10, 10);
        gbc.gridx = 0; 
        gbc.gridy = 0;

        JLabel title = new JLabel("JAVA POKÉMON");
        title.setFont(new Font("Monospaced", Font.BOLD, 48));
        title.setForeground(Color.DARK_GRAY);
        add(title, gbc);

        gbc.gridy++;
        JLabel nameLabel = new JLabel("Enter Your Name:");
        nameLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        add(nameLabel, gbc);

        gbc.gridy++;
        JTextField nameField = new JTextField(15);
        nameField.setFont(new Font("Monospaced", Font.PLAIN, 18));
        nameField.setHorizontalAlignment(JTextField.CENTER);
        add(nameField, gbc);

        gbc.gridy++;
        JButton newGameBtn = new JButton("NEW GAME");
        newGameBtn.setFont(new Font("Monospaced", Font.BOLD, 20));
        newGameBtn.addActionListener(e -> 
        {
            String typedName = nameField.getText().trim();
            if(!typedName.isEmpty()) 
            {
                GameLauncher.playerName = typedName;
            }
            game.startGame(true); 
        });
        add(newGameBtn, gbc);

        gbc.gridy++;
        JButton loadGameBtn = new JButton("LOAD GAME");
        loadGameBtn.setFont(new Font("Monospaced", Font.BOLD, 20));
        loadGameBtn.addActionListener(e -> 
        {
            if(game.loadGame()) 
            {
                game.startGame(false); 
            }
        });
        add(loadGameBtn, gbc);
    }
}

// --- SAVE STATE DATA ---
class SaveState implements Serializable 
{
    private static final long serialVersionUID = 1L; 
    String playerName; 
    List<Pokemon> party;
    List<String> bag;
    List<String> tms; // NEW: Save TM data
    int money;
    boolean[] pokedexSeen;
    boolean[] pokedexCaught;
    boolean hasStarter, trainer1Defeated, trainer2Defeated, badgeRock;
}

// --- POKEDEX PANEL ---
class PokedexPanel extends JPanel 
{
    private GameLauncher game;
    private DefaultListModel<String> listModel;
    private JList<String> pokemonList;
    private JLabel statsLabel;

    public PokedexPanel(GameLauncher game) 
    {
        this.game = game;
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(220, 20, 60)); 
        header.setBorder(BorderFactory.createEmptyBorder(15, 10, 15, 10));
       
        JLabel title = new JLabel("POKÉDEX", SwingConstants.CENTER);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Monospaced", Font.BOLD, 28));
        header.add(title, BorderLayout.CENTER);
       
        statsLabel = new JLabel("Seen: 0  |  Owned: 0", SwingConstants.CENTER);
        statsLabel.setForeground(Color.WHITE);
        statsLabel.setFont(new Font("Monospaced", Font.BOLD, 16));
        header.add(statsLabel, BorderLayout.SOUTH);

        add(header, BorderLayout.NORTH);

        listModel = new DefaultListModel<>();
        pokemonList = new JList<>(listModel);
        pokemonList.setFont(new Font("Monospaced", Font.PLAIN, 18));
        pokemonList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        pokemonList.setFixedCellHeight(30);
       
        JScrollPane scrollPane = new JScrollPane(pokemonList);
        add(scrollPane, BorderLayout.CENTER);

        JPanel footer = new JPanel();
        footer.setBackground(Color.DARK_GRAY);
        JButton backBtn = new JButton("CLOSE POKEDEX");
        backBtn.setBackground(Color.WHITE);
        backBtn.setForeground(Color.BLACK);
        backBtn.setFont(new Font("Monospaced", Font.BOLD, 16));
        backBtn.addActionListener(e -> game.closePokedex());
        footer.add(backBtn);
        add(footer, BorderLayout.SOUTH);
    }

    public void refresh() 
    {
        listModel.clear();
        int seenCount = 0;
        int caughtCount = 0;

        for (int i = 1; i < Pokedex.NAMES.length; i++) 
        {
            String entry;
            String num = String.format("%03d", i);
            String name = Pokedex.NAMES[i];

            if (GameLauncher.pokedexCaught[i]) 
            {
                entry = " " + num + "   " + name + "  [O]"; 
                seenCount++;
                caughtCount++;
            } else if (GameLauncher.pokedexSeen[i]) 
            {
                entry = " " + num + "   " + name;
                seenCount++;
            } else 
            {
                entry = " " + num + "   ----------";
            }
            listModel.addElement(entry);
        }
        statsLabel.setText("Seen: " + seenCount + "  |  Owned: " + caughtCount);
    }
}

// --- DATA CLASSES ---
class Pokedex
{
    public static final String[] NAMES =
    {
        "MissingNo",
        "Bulbasaur", "Ivysaur", "Venusaur", "Charmander", "Charmeleon", "Charizard",
        "Squirtle", "Wartortle", "Blastoise", "Caterpie", "Metapod", "Butterfree",
        "Weedle", "Kakuna", "Beedrill", "Pidgey", "Pidgeotto", "Pidgeot",
        "Rattata", "Raticate", "Spearow", "Fearow", "Ekans", "Arbok",
        "Pikachu", "Raichu", "Sandshrew", "Sandslash", "NidoranF", "Nidorina",
        "Nidoqueen", "NidoranM", "Nidorino", "Nidoking", "Clefairy", "Clefable",
        "Vulpix", "Ninetales", "Jigglypuff", "Wigglytuff", "Zubat", "Golbat",
        "Oddish", "Gloom", "Vileplume", "Paras", "Parasect", "Venonat",
        "Venomoth", "Diglett", "Dugtrio", "Meowth", "Persian", "Psyduck",
        "Golduck", "Mankey", "Primeape", "Growlithe", "Arcanine", "Poliwag",
        "Poliwhirl", "Poliwrath", "Abra", "Kadabra", "Alakazam", "Machop",
        "Machoke", "Machamp", "Bellsprout", "Weepinbell", "Victreebel", "Tentacool",
        "Tentacruel", "Geodude", "Graveler", "Golem", "Ponyta", "Rapidash",
        "Slowpoke", "Slowbro", "Magnemite", "Magneton", "Farfetch'd", "Doduo",
        "Dodrio", "Seel", "Dewgong", "Grimer", "Muk", "Shellder",
        "Cloyster", "Gastly", "Haunter", "Gengar", "Onix", "Drowzee",
        "Hypno", "Krabby", "Kingler", "Voltorb", "Electrode", "Exeggcute",
        "Exeggutor", "Cubone", "Marowak", "Hitmonlee", "Hitmonchan", "Lickitung",
        "Koffing", "Weezing", "Rhyhorn", "Rhydon", "Chansey", "Tangela",
        "Kangaskhan", "Horsea", "Seadra", "Goldeen", "Seaking", "Staryu",
        "Starmie", "Mr. Mime", "Scyther", "Jynx", "Electabuzz", "Magmar",
        "Pinsir", "Tauros", "Magikarp", "Gyarados", "Lapras", "Ditto",
        "Eevee", "Vaporeon", "Jolteon", "Flareon", "Porygon", "Omanyte",
        "Omastar", "Kabuto", "Kabutops", "Aerodactyl", "Snorlax", "Articuno",
        "Zapdos", "Moltres", "Dratini", "Dragonair", "Dragonite", "Mewtwo", "Mew"
    };

    public static final String[] TYPES =
    {
        "NORMAL",
        "GRASS", "GRASS", "GRASS", "FIRE", "FIRE", "FIRE",
        "WATER", "WATER", "WATER", "BUG", "BUG", "BUG",
        "BUG", "BUG", "BUG", "NORMAL", "NORMAL", "NORMAL",
        "NORMAL", "NORMAL", "NORMAL", "NORMAL", "POISON", "POISON",
        "ELECTRIC", "ELECTRIC", "GROUND", "GROUND", "POISON", "POISON",
        "POISON", "POISON", "POISON", "POISON", "NORMAL", "NORMAL",
        "FIRE", "FIRE", "NORMAL", "NORMAL", "POISON", "POISON",
        "GRASS", "GRASS", "GRASS", "BUG", "BUG", "BUG",
        "BUG", "GROUND", "GROUND", "NORMAL", "NORMAL", "WATER",
        "WATER", "FIGHTING", "FIGHTING", "FIRE", "FIRE", "WATER",
        "WATER", "WATER", "PSYCHIC", "PSYCHIC", "PSYCHIC", "FIGHTING",
        "FIGHTING", "FIGHTING", "GRASS", "GRASS", "GRASS", "WATER",
        "WATER", "ROCK", "ROCK", "ROCK", "FIRE", "FIRE",
        "WATER", "WATER", "ELECTRIC", "ELECTRIC", "NORMAL", "NORMAL",
        "NORMAL", "WATER", "WATER", "POISON", "POISON", "WATER",
        "WATER", "GHOST", "GHOST", "GHOST", "ROCK", "PSYCHIC",
        "PSYCHIC", "WATER", "WATER", "ELECTRIC", "ELECTRIC", "GRASS",
        "GRASS", "GROUND", "GROUND", "FIGHTING", "FIGHTING", "NORMAL",
        "POISON", "POISON", "GROUND", "GROUND", "NORMAL", "GRASS",
        "NORMAL", "WATER", "WATER", "WATER", "WATER", "WATER",
        "WATER", "PSYCHIC", "BUG", "ICE", "ELECTRIC", "FIRE",
        "BUG", "NORMAL", "WATER", "WATER", "WATER", "NORMAL",
        "NORMAL", "WATER", "ELECTRIC", "FIRE", "NORMAL", "WATER",
        "WATER", "ROCK", "ROCK", "ROCK", "NORMAL", "ICE",
        "ELECTRIC", "FIRE", "DRAGON", "DRAGON", "DRAGON", "PSYCHIC", "PSYCHIC"
    };
}

class Move implements Serializable
{
    private static final long serialVersionUID = 1L;
    String name, type;
    int power;
    int maxAp;      
    int currentAp;  

    public Move(String n, String t, int p, int ap)
    {
        name = n; type = t; power = p; maxAp = ap; currentAp = ap;
    }
}

class Pokemon implements Serializable
{
    private static final long serialVersionUID = 1L;
    String name, type;
    int maxHp, currentHp, level, xp, xpToNext;
    int attack, defense, spAtk, spDef, speed;
    java.util.List<Move> moves = new java.util.ArrayList<>();

    public Pokemon(String name, String type, int lvl) 
        {
            this.name = name;
            this.type = type;
            this.level = lvl;

            this.xp = 0;
            this.xpToNext = lvl * 15;

            learnBaseMoves();
        }
    public static Pokemon generateWild(int minLvl, int maxLvl)
    {
        Random r = new Random();
        int lvl = minLvl + r.nextInt(maxLvl - minLvl + 1);

        Pokemon base = PokemonData.allPokemon.get(
            r.nextInt(PokemonData.allPokemon.size())
        );

        Pokemon wild = new Pokemon(base.name, base.type, lvl);

        // Copy stats
        wild.maxHp = base.maxHp;
        wild.currentHp = base.maxHp;
        wild.attack = base.attack;
        wild.defense = base.defense;
        wild.spAtk = base.spAtk;
        wild.spDef = base.spDef;
        wild.speed = base.speed;

        return wild;
    }
    private void learnBaseMoves()
    {
        moves.clear();
        moves.add(new Move("Tackle", "NORMAL", 35, 35));

        if(type.equals("FIRE"))
        {
            moves.add(new Move("Ember", "FIRE", 40, 25));
            if(level >= 12) moves.add(new Move("Flamethrower", "FIRE", 90, 15));
        }
        else if(type.equals("WATER"))
        {
            moves.add(new Move("Water Gun", "WATER", 40, 25));
            if(level >= 12) moves.add(new Move("Bubblebeam", "WATER", 65, 20));
        }
        else if(type.equals("GRASS"))
        {
            moves.add(new Move("Vine Whip", "GRASS", 45, 25));
            if(level >= 12) moves.add(new Move("Razor Leaf", "GRASS", 55, 25));
        }
        else if(type.equals("ELECTRIC"))
        {
            moves.add(new Move("Thundershock", "ELECTRIC", 40, 30));
            if(level >= 12) moves.add(new Move("Thunderbolt", "ELECTRIC", 90, 15));
        }
        else if(type.equals("ROCK") || type.equals("GROUND"))
        {
            moves.add(new Move("Rock Throw", "ROCK", 50, 15));
            if(level >= 12) moves.add(new Move("Earthquake", "GROUND", 100, 10));
        }
        else if(type.equals("PSYCHIC"))
        {
            moves.add(new Move("Confusion", "PSYCHIC", 50, 25));
            if(level >= 12) moves.add(new Move("Psychic", "PSYCHIC", 90, 10));
        }
        else if(type.equals("BUG"))
        {
            moves.add(new Move("String Shot", "BUG", 10, 40));
            if(level >= 10) moves.add(new Move("Twin Needle", "BUG", 25, 20));
        }
        else if(type.equals("GHOST")) 
        {
            moves.add(new Move("Lick", "GHOST", 30, 30));
            if(level >= 12) moves.add(new Move("Shadow Ball", "GHOST", 80, 15));
        }
        else if(type.equals("FIGHTING"))
        {
            moves.add(new Move("Low Kick", "FIGHTING", 40, 20));
            if(level >= 12) moves.add(new Move("Karate Chop", "FIGHTING", 50, 25));
        }
        else if(type.equals("POISON")) 
        {
            moves.add(new Move("Poison Sting", "POISON", 15, 35));
            if(level >= 12) moves.add(new Move("Sludge", "POISON", 65, 20));
        }
        else if(type.equals("ICE")) 
        {
            moves.add(new Move("Ice Shard", "ICE", 40, 30));
            if(level >= 12) moves.add(new Move("Ice Beam", "ICE", 90, 10));
        }
        else if(type.equals("DRAGON")) 
        {
            moves.add(new Move("Twister", "DRAGON", 40, 20));
            if(level >= 12) moves.add(new Move("Dragon Claw", "DRAGON", 80, 15));
        }
        else 
        { 
            if(level >= 10) moves.add(new Move("Quick Attack", "NORMAL", 40, 30));
        }
    }

    public void gainXp(int amount)
    {
        this.xp += amount;
        if(this.xp >= xpToNext)
        {
            level++; xp = 0; xpToNext = level * 15; maxHp += 6; currentHp = maxHp;
            JOptionPane.showMessageDialog(null, name + " grew to Level " + level + "!");
        }
    }

    public void healFull()
    {
        currentHp = maxHp;
        for(Move m : moves) m.currentAp = m.maxAp;
    }
   
    public void takeDamage(int amt) { currentHp -= amt; if(currentHp < 0) currentHp = 0; }

    public static double getEffectiveness(String mType, String tType)
    {
        if(mType.equals("FIRE")) return (tType.equals("GRASS") || tType.equals("ICE") || tType.equals("BUG")) ? 2.0 : (tType.equals("WATER") || tType.equals("ROCK") || tType.equals("FIRE")) ? 0.5 : 1.0;
        if(mType.equals("WATER")) return (tType.equals("FIRE") || tType.equals("ROCK") || tType.equals("GROUND")) ? 2.0 : (tType.equals("GRASS") || tType.equals("WATER")) ? 0.5 : 1.0;
        if(mType.equals("GRASS")) return (tType.equals("WATER") || tType.equals("ROCK") || tType.equals("GROUND")) ? 2.0 : (tType.equals("FIRE") || tType.equals("GRASS") || tType.equals("BUG")) ? 0.5 : 1.0;
        if(mType.equals("ELECTRIC")) return (tType.equals("WATER") || tType.equals("FLYING")) ? 2.0 : (tType.equals("GROUND") || tType.equals("GRASS")) ? 0.5 : 1.0;
        if(mType.equals("ICE")) return (tType.equals("GRASS") || tType.equals("GROUND") || tType.equals("DRAGON")) ? 2.0 : (tType.equals("FIRE") || tType.equals("ICE")) ? 0.5 : 1.0;
        if(mType.equals("FIGHTING")) return (tType.equals("NORMAL") || tType.equals("ROCK")) ? 2.0 : (tType.equals("PSYCHIC") || tType.equals("FLYING")) ? 0.5 : 1.0;
        if(mType.equals("PSYCHIC")) return (tType.equals("FIGHTING") || tType.equals("POISON")) ? 2.0 : (tType.equals("PSYCHIC")) ? 0.5 : 1.0;
        if(mType.equals("GROUND")) return (tType.equals("FIRE") || tType.equals("ELECTRIC") || tType.equals("ROCK")) ? 2.0 : (tType.equals("GRASS") || tType.equals("BUG")) ? 0.5 : 1.0;
        if(mType.equals("FLYING")) return (tType.equals("GRASS") || tType.equals("FIGHTING") || tType.equals("BUG")) ? 2.0 : (tType.equals("ELECTRIC") || tType.equals("ROCK")) ? 0.5 : 1.0;
        return 1.0; 
    }
}

class PokemonData
{
    static List<Pokemon> allPokemon = new ArrayList<>();

    public static void loadFromFile(String filePath) 
    {
        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(new FileInputStream(filePath), "UTF-8"))) 
        {
            String line;

            while ((line = br.readLine()) != null) 
            {
                String[] p = line.split(",");

                if (p.length < 11) continue; // safety check

                String name = p[1];

                String type1 = p[3].toUpperCase();
                String type2 = p[4].isEmpty() ? "" : "/" + p[4].toUpperCase();
                String type = type1 + type2;

                int hp = Integer.parseInt(p[5]);
                int atk = Integer.parseInt(p[6]);
                int def = Integer.parseInt(p[7]);
                int spAtk = Integer.parseInt(p[8]);
                int spDef = Integer.parseInt(p[9]);
                int speed = Integer.parseInt(p[10]);

                Pokemon mon = new Pokemon(name, type1, 5);

                mon.maxHp = hp;
                mon.currentHp = hp;

                mon.attack = atk;
                mon.defense = def;
                mon.spAtk = spAtk;
                mon.spDef = spDef;
                mon.speed = speed;

                allPokemon.add(mon);
            }

            System.out.println("Loaded " + allPokemon.size() + " Pokémon!");

        } 
        catch (Exception e) 
        {
            e.printStackTrace();
        }
    }
}