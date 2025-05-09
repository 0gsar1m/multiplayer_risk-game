package model;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static GameManager instance;
    private List<Player> players;
    private int currentPlayerIndex = 0;
    private List<Territory> territories;
    private Random random;

    private GameManager() {
        territories = new ArrayList<>();
        players = new ArrayList<>();
        random = new Random();
        setupDummyPlayers();
        setupTerritories();
        assignTerritories();
    }

    public static GameManager getInstance() {
        if (instance == null) {
            instance = new GameManager();
        }
        return instance;
    }

    public List<Territory> getTerritories() {
        return territories;
    }

    public Player getCurrentPlayer() {
        return players.get(currentPlayerIndex);
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        System.out.println("Sıra: " + getCurrentPlayer().getName());
    }

    public void setupTerritories() {
        territories.add(new Territory("Turkey", 773, 421, "Asia"));
        territories.add(new Territory("Venezuela", 318, 615, "South America"));
        territories.add(new Territory("Brazil", 420, 692, "South America"));
        territories.add(new Territory("Argentina", 368, 794, "South America"));
        territories.add(new Territory("Middle East", 773, 421, "Asia"));
        territories.add(new Territory("China", 996, 370, "Asia"));
    }

    public void setupDummyPlayers() {
        Player player1 = new Player("Kırmızı");
        Player player2 = new Player("Mavi");
        players.add(player1);
        players.add(player2);
    }

    public void assignTerritories() {
        players.get(0).addTerritory(territories.get(0)); // Turkey - Kırmızı
        territories.get(0).setOwner(players.get(0));

        players.get(0).addTerritory(territories.get(1)); // Venezuela - Kırmızı
        territories.get(1).setOwner(players.get(0));

        players.get(1).addTerritory(territories.get(2)); // Brazil - Mavi
        territories.get(2).setOwner(players.get(1));

        players.get(1).addTerritory(territories.get(3)); // Argentina - Mavi
        territories.get(3).setOwner(players.get(1));

        // Ordu sayısını rastgele atıyoruz
        for (Territory territory : territories) {
            int armyCount = random.nextInt(5) + 1;
            territory.setArmies(armyCount);
        }
    }

    public void addArmyToTerritory(Territory territory) {
        int newArmyCount = territory.getArmies() + 1;
        territory.setArmies(newArmyCount);
        System.out.println("Ordu eklendi: " + territory.getTerritoryName() + " - Yeni Ordu Sayısı: " + newArmyCount);
    }
}
