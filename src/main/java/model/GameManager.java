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
        TEST_assignTerritories();
        setupNeighbors();
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

        for (Territory territory : territories) {
            int armyCount = random.nextInt(5) + 1;
            territory.setArmies(armyCount);
        }
    }

    public void TEST_assignTerritories() {
        Player player1 = players.get(0);
        Player player2 = players.get(1);

        // TEST MODU - TÜM ÜLKELER KIRMIZIYA VERİLDİ
        for (Territory territory : territories) {
            territory.setOwner(player1);
            player1.addTerritory(territory);
            territory.setArmies(1);
        }

        System.out.println("Test modu aktif! Tüm ülkeler Kırmızı'ya verildi.");
    }

    public void setupNeighbors() {
        // Venezuela - Brazil - Argentina (Güney Amerika)
        territories.get(1).addNeighbor(territories.get(2)); // Venezuela - Brazil
        territories.get(2).addNeighbor(territories.get(1)); // Brazil - Venezuela

        territories.get(1).addNeighbor(territories.get(3)); // Venezuela - Argentina
        territories.get(3).addNeighbor(territories.get(1)); // Argentina - Venezuela

        territories.get(2).addNeighbor(territories.get(3)); // Brazil - Argentina
        territories.get(3).addNeighbor(territories.get(2)); // Argentina - Brazil

        // Turkey - Middle East - China (Asya)
        territories.get(0).addNeighbor(territories.get(4)); // Turkey - Middle East
        territories.get(4).addNeighbor(territories.get(0)); // Middle East - Turkey

        territories.get(4).addNeighbor(territories.get(5)); // Middle East - China
        territories.get(5).addNeighbor(territories.get(4)); // China - Middle East
    }

    public void addArmyToTerritory(Territory territory) {
        int newArmyCount = territory.getArmies() + 1;
        territory.setArmies(newArmyCount);
    }

    public void attack(Territory attacker, Territory defender) {
        if (attacker.getArmies() < 2) {
            System.out.println("Saldırı için yeterli ordu yok.");
            return;
        }

        int attackerRoll = random.nextInt(6) + 1;
        int defenderRoll = random.nextInt(6) + 1;

        System.out.println("Saldırı: " + attacker.getTerritoryName() + " (Zar: " + attackerRoll + ") --> " +
                defender.getTerritoryName() + " (Zar: " + defenderRoll + ")");

        if (attackerRoll > defenderRoll) {
            // Savunan kaybediyor
            defender.setArmies(defender.getArmies() - 1);
            System.out.println(defender.getTerritoryName() + " kaybetti! Yeni ordu sayısı: " + defender.getArmies());

            // Eğer savunan ülkenin ordusu 0'a inerse, ele geçirilir
            if (defender.getArmies() <= 0) {
                Player attackerOwner = attacker.getOwner();
                defender.setOwner(attackerOwner);
                defender.setArmies(1);  // Ele geçirilen ülke 1 ordu ile başlar

                System.out.println(defender.getTerritoryName() + " ele geçirildi!");
                System.out.println(defender.getTerritoryName() + " artık " + attackerOwner.getName() + " oyuncusuna ait.");
            }

        } else {
            // Saldıran kaybediyor
            attacker.setArmies(attacker.getArmies() - 1);
            System.out.println(attacker.getTerritoryName() + " kaybetti! Yeni ordu sayısı: " + attacker.getArmies());
        }

        // Güncel durumları yazdır
        System.out.println("Durum: " + attacker.getTerritoryName() + " Ordu: " + attacker.getArmies() +
                " | " + defender.getTerritoryName() + " Ordu: " + defender.getArmies());
    }

    public boolean isGameOver(){
        if(territories.isEmpty()){
            return false;
        }
        Player winner = territories.get(0).getOwner();
        if(winner == null){ return false;}
        for(Territory territory : territories){
            if(territory.getOwner() != winner){
                return false;
            }

        }
        System.out.println("Oyun bitti, kazanan: " + winner.getName());
        return true;
    }

}
