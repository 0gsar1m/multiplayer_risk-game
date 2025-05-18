package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public class GameManager {
    private static GameManager instance;
    private List<Player> players;
    private int currentPlayerIndex = 0;
    private List<Territory> territories;
    private Random random;
    private int reinforcementArmy = 0;
    private int tempReinforcement = 0;



    private GameManager() {
        territories = new ArrayList<>();
        players = new ArrayList<>();
        random = new Random();
        setupDummyPlayers();
        setupTerritories();
        assignTerritories();
        setupNeighbors();
    }


    public void attack(Territory attacker, Territory defender) {

        if (attacker.getOwner() == defender.getOwner()) {
            System.out.println("Aynı oyuncunun ülkesi! Saldırı yapılamaz.");
            return;
        }

        if (attacker.getArmies() < 2) {
            System.out.println(attacker.getTerritoryName() + " ülkesinde saldırı için yeterli ordu yok.");
            return;
        }

        List<Integer> attackerRolls = new ArrayList<>();
        List<Integer> defenderRolls = new ArrayList<>();

        int attackerDice = Math.min(3, attacker.getArmies() - 1); // En fazla 3 zar
        int defenderDice = Math.min(2, defender.getArmies());     // En fazla 2 zar

        Random random = new Random();

        for (int i = 0; i < attackerDice; i++) {
            attackerRolls.add(random.nextInt(6) + 1);
        }
        for (int i = 0; i < defenderDice; i++) {
            defenderRolls.add(random.nextInt(6) + 1);
        }

        attackerRolls.sort(Collections.reverseOrder());
        defenderRolls.sort(Collections.reverseOrder());

        System.out.println("Saldıran zarlar: " + attackerRolls);
        System.out.println("Savunan zarlar: " + defenderRolls);

        int comparisons = Math.min(attackerDice, defenderDice);

        for (int i = 0; i < comparisons; i++) {
            int attackRoll = attackerRolls.get(i);
            int defendRoll = defenderRolls.get(i);

            if (attackRoll > defendRoll) {
                defender.setArmies(defender.getArmies() - 1);
                System.out.println(defender.getTerritoryName() + " kaybetti! Kalan ordu: " + defender.getArmies());

                if (defender.getArmies() <= 0) {
                    defender.setOwner(attacker.getOwner());
                    defender.setArmies(1);
                    attacker.setArmies(attacker.getArmies() - 1);
                    System.out.println(defender.getTerritoryName() + " ele geçirildi!");
                    postAttackReinforce(attacker, defender);
                    return;
                }

            } else {
                attacker.setArmies(attacker.getArmies() - 1);
                System.out.println(attacker.getTerritoryName() + " kaybetti! Kalan ordu: " + attacker.getArmies());
            }
        }

        System.out.println("Saldırı tamamlandı.");
    }


    /**
     * Saldırı sonrası takviye aşaması
     */
    public void postAttackReinforce(Territory attacker, Territory conquered) {
        if (attacker.getArmies() <= 1) {
            System.out.println("Takviye yapılacak yeterli ordu yok.");
            return;
        }

        int maxReinforce = attacker.getArmies() - 1;
        System.out.println("Takviye aşaması: " + attacker.getTerritoryName() + " ile " + conquered.getTerritoryName());
        System.out.println(attacker.getTerritoryName() + " ülkesinden " + conquered.getTerritoryName() + " ülkesine " + maxReinforce + " ordu takviyesi yapılabilir.");
    }


    /**
     * Kullanıcıdan girilen asker sayısı kadar takviye yapar.
     */
    public void executePostAttackReinforce(Territory source, Territory target, int armies) {
        int maxReinforce = source.getArmies() - 1;

        if (armies <= 0 || armies > maxReinforce) {
            System.out.println("Geçersiz takviye miktarı. Maksimum: " + maxReinforce);
            return;
        }

        source.setArmies(source.getArmies() - armies);
        target.setArmies(target.getArmies() + armies);
        System.out.println(source.getTerritoryName() + " ülkesinden " + target.getTerritoryName() + " ülkesine " + armies + " ordu gönderildi.");
    }


    private List<Integer> rollDice(int count) {
        List<Integer> rolls = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            rolls.add(random.nextInt(6) + 1);
        }
        return rolls;
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

    public int getTempReinforcement() {
        return tempReinforcement;
    }

    public void nextTurn() {
        currentPlayerIndex = (currentPlayerIndex + 1) % players.size();
        startReinforcementPhase(getCurrentPlayer());
    }

    public void setupTerritories() {
        territories.add(new Territory("Venezuela", 318, 615, "South America"));
        territories.add(new Territory("Brazil", 420, 692, "South America"));
        territories.add(new Territory("Argentina", 368, 794, "South America"));
    }

    public void setupNeighbors() {
        // Venezuela - Brazil - Argentina (Güney Amerika)
        Territory venezuela = territories.get(0);
        Territory brazil = territories.get(1);
        Territory argentina = territories.get(2);

        // Venezuela komşuları
        venezuela.addNeighbor(brazil);
        venezuela.addNeighbor(argentina);

        // Brazil komşuları
        brazil.addNeighbor(venezuela);
        brazil.addNeighbor(argentina);

        // Argentina komşuları
        argentina.addNeighbor(venezuela);
        argentina.addNeighbor(brazil);
    }


    public void setupDummyPlayers() {
        Player player1 = new Player("Kırmızı");
        Player player2 = new Player("Mavi");
        players.add(player1);
        players.add(player2);
    }

    public void assignTerritories() {
        // Kırmızı oyuncuya Venezuela veriliyor
        players.get(0).addTerritory(territories.get(0));
        territories.get(0).setOwner(players.get(0));

        // Mavi oyuncuya Brazil ve Argentina veriliyor
        players.get(1).addTerritory(territories.get(1));
        territories.get(1).setOwner(players.get(1));

        players.get(1).addTerritory(territories.get(2));
        territories.get(2).setOwner(players.get(1));

        // Ordu sayıları belirleniyor (1-5 arası rastgele)
        for (Territory territory : territories) {
            territory.setArmies(random.nextInt(5) + 1);
        }
    }


    public void startReinforcementPhase(Player player) {
        int ownedTerritories = player.getOwnedTerritories().size();
        reinforcementArmy = Math.max(3, ownedTerritories / 3);
        tempReinforcement = reinforcementArmy;
        System.out.println(player.getName() + " oyuncusu " + tempReinforcement + " ordu alacak.");
    }
    /**
     * Saldırı sonrası takviye aşamasında, iki ülke arasında takviye yapılmasını sağlar.
     * @param from Takviye yapılacak kaynak ülke
     * @param to Takviye yapılacak hedef ülke
     * @param armies Gönderilecek ordu sayısı
     */
    public void reinforceBetweenTerritories(Territory from, Territory to, int armies) {
        if (from.getOwner() != to.getOwner()) {
            System.out.println("Takviye sadece kendi ülkeleriniz arasında yapılabilir.");
            return;
        }

        if (armies < 1 || armies >= from.getArmies()) {
            System.out.println("Takviye için geçersiz sayı.");
            return;
        }

        from.setArmies(from.getArmies() - armies);
        to.setArmies(to.getArmies() + armies);

        System.out.println(from.getTerritoryName() + " ülkesinden " + to.getTerritoryName() + " ülkesine " + armies + " ordu takviyesi yapıldı.");
    }




    public void reinforceTerritory(Territory territory, int armies) {
        if (armies <= 0 || armies > tempReinforcement) {
            System.out.println("Hatalı ordu sayısı: " + armies);
            return;
        }

        territory.setArmies(territory.getArmies() + armies);
        tempReinforcement -= armies;

        System.out.println(territory.getTerritoryName() + " ülkesine " + armies + " ordu eklendi. Kalan takviye: " + tempReinforcement);
    }
    /**
     * Oyunun bitip bitmediğini kontrol eder.
     */
    public boolean isGameOver() {
        Player potentialWinner = territories.get(0).getOwner();

        for (Territory territory : territories) {
            if (territory.getOwner() != potentialWinner) {
                return false;  // Farklı bir oyuncu varsa oyun bitmemiştir
            }
        }
        return true;
    }

    /**
     * Oyunu kazanan oyuncuyu döner.
     */
    public Player getWinner() {
        if (isGameOver()) {
            return territories.get(0).getOwner();
        }
        return null;
    }

}
