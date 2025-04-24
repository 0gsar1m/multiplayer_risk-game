package model;

import javax.smartcardio.Card;
import java.util.ArrayList;
import java.util.List;

public class Player
{
    private String name;
    private List<Territory> ownedTerritories ;
    private int availableArmies;

    public Player(String name){
    this.name = name;
    ownedTerritories = new ArrayList<Territory>();
    availableArmies = 0;
    }

    public void addTerritory(Territory territory){
        ownedTerritories.add(territory);
    }

    public void removeTerritory(Territory territory){
        ownedTerritories.remove(territory);
    }
    public String getName(){
        return name;
    }
    public List<Territory> getOwnedTerritories(){
        return ownedTerritories;
    }
    public int getAvailableArmies(){
        return availableArmies;
    }
    public void setAvailableArmies(int availableArmies){
        this.availableArmies = availableArmies;
    }
}
