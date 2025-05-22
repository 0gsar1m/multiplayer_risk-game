package model;

import javafx.scene.control.Button;
import javafx.scene.control.Label;

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
   // private boolean isPostAttackReinforcePhase = false;
    private Territory targetTerritory;
    private Territory selectedTerritory;
    private boolean isReinforcementPhase = false;

    private GameManager() {
        territories = new ArrayList<>();
        players = new ArrayList<>();
        random = new Random();
        setupDummyPlayers();
        setupTerritories();
        assignTerritories();
        setupNeighbors();
        startReinforcementPhase(getCurrentPlayer()); // Takviye başlasın
    }

    private PostAttackReinforceListener postAttackReinforceListener;


    public void setPostAttackReinforceListener(PostAttackReinforceListener listener) {
        this.postAttackReinforceListener = listener;
    }

    public void postAttackReinforce(Territory attacker, Territory conquered) {
        int maxReinforce = attacker.getArmies() - 1;

        System.out.println("Takviye aşaması: " + attacker.getTerritoryName() + " ile " + conquered.getTerritoryName());
        System.out.println(attacker.getTerritoryName() + " ülkesinden " + conquered.getTerritoryName() + " ülkesine " + maxReinforce + " ordu takviyesi yapılabilir.");

        // 🧠 Fazı GUI tarafına *her halükârda* bildirmeliyiz
        //isPostAttackReinforcePhase = true;

        // 🧠 Eğer iki tarafta da sadece 1'er ordu varsa, takviye mümkün değildir
        if (attacker.getArmies() == 1 && conquered.getArmies() == 1) {
            System.out.println("⚠️ Her iki ülkede 1'er ordu kaldı. Takviye yapılamaz, faz atlanıyor.");
            if (postAttackReinforceListener != null) {
                postAttackReinforceListener.onPostAttackReinforce(null, null);
            }
            return;
        }

        // 🧠 Kaynak ülkedeki ordu sayısı 1'in altındaysa da takviye yapılamaz
        if (maxReinforce < 1) {
            System.out.println("⚠️ Taşınacak ordu yok. Takviye aşaması atlanıyor.");
            if (postAttackReinforceListener != null) {
                postAttackReinforceListener.onPostAttackReinforce(null, null);
            }
            return;
        }

        selectedTerritory = attacker;
        targetTerritory = conquered;

        // 🔄 GUI'yi bilgilendir
        if (postAttackReinforceListener != null) {
            System.out.println("🧩 postAttackReinforceListener çağrılıyor");
            postAttackReinforceListener.onPostAttackReinforce(attacker, conquered);
        }
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

                    // Yalnızca 1 asker otomatik geçsin
                    attacker.setArmies(attacker.getArmies() - 1);
                    defender.setArmies(1);


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
        territories.clear(); // Önceki ülkeleri temizle
        /*territories.add(new Territory("Venezuela", 318, 615, "South America"));
        territories.add(new Territory("Brazil", 420, 692, "South America"));
        territories.add(new Territory("Argentina", 368, 794, "South America"));*/
        territories.add(new Territory("North Africa", 603, 501, "Africa"));
        territories.add(new Territory("Egypt", 736, 510, "Africa"));
        territories.add(new Territory("Congo", 695, 620, "Africa"));
        territories.add(new Territory("East Africa", 757, 638, "Africa"));
        territories.add(new Territory("South Africa", 720, 730, "Africa"));
        territories.add(new Territory("Madagascar", 814, 704, "Africa"));
    }


    public void setupNeighbors() {
        /*// Venezuela - Brazil - Argentina (Güney Amerika)
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
        argentina.addNeighbor(brazil);*/
        Territory northAfrica = getTerritoryByName("North Africa");
        Territory egypt = getTerritoryByName("Egypt");
        Territory congo = getTerritoryByName("Congo");
        Territory eastAfrica = getTerritoryByName("East Africa");
        Territory southAfrica = getTerritoryByName("South Africa");
        Territory madagascar = getTerritoryByName("Madagascar");

        northAfrica.addNeighbor(egypt);
        northAfrica.addNeighbor(congo);

        egypt.addNeighbor(northAfrica);
        egypt.addNeighbor(eastAfrica);
        egypt.addNeighbor(congo);

        congo.addNeighbor(northAfrica);
        congo.addNeighbor(eastAfrica);
        congo.addNeighbor(southAfrica);
        congo.addNeighbor(egypt);

        eastAfrica.addNeighbor(egypt);
        eastAfrica.addNeighbor(congo);
        eastAfrica.addNeighbor(southAfrica);
        eastAfrica.addNeighbor(madagascar);

        southAfrica.addNeighbor(congo);
        southAfrica.addNeighbor(eastAfrica);
        southAfrica.addNeighbor(madagascar);

        madagascar.addNeighbor(eastAfrica);
        madagascar.addNeighbor(southAfrica);
    }

    public void setupDummyPlayers() {
        Player player1 = new Player("Kırmızı");
        Player player2 = new Player("Mavi");
        players.add(player1);
        players.add(player2);
    }

    public void assignTerritories() {
        // Temiz başlangıç
        for (Player player : players) {
            player.getOwnedTerritories().clear();
        }

        // Kontrollü atama
        Player red = players.get(0);
        Player blue = players.get(1);

        // İlk 3 ülkeyi kırmızıya
        for (int i = 0; i < 3 && i < territories.size(); i++) {
            Territory t = territories.get(i);
            t.setOwner(red);
            red.addTerritory(t);
            t.setArmies(random.nextInt(3) + 1);
        }

        // Sonraki 3 ülkeyi maviye
        for (int i = 3; i < 6 && i < territories.size(); i++) {
            Territory t = territories.get(i);
            t.setOwner(blue);
            blue.addTerritory(t);
            t.setArmies(random.nextInt(3) + 1);
        }

        System.out.println("Ülkeler başarıyla dağıtıldı:");
        for (Player p : players) {
            System.out.println(p.getName() + " -> " + p.getOwnedTerritories().size() + " ülke");
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
     *
     * @param from   Takviye yapılacak kaynak ülke
     * @param to     Takviye yapılacak hedef ülke
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

    private Territory getTerritoryByName(String name) {
        for (Territory territory : territories) {
            if (territory.getTerritoryName().equals(name)) {
                return territory;
            }
        }
        return null;
    }

}
