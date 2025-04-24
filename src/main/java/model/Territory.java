package model;

import java.util.ArrayList;
import java.util.List;

public class Territory {
    private String territoryName;
    private Player owner;
    private int armies;
    private List<Territory> neighborTerritories;

    public Territory(String territoryName) {
        this.territoryName = territoryName;
        this.armies = 1; //default start
        this.neighborTerritories = new ArrayList<Territory>();
    }

    public void setOwner(Player owner){
        this.owner = owner;
    }

    public Player getOwner(){
        return owner;
    }

    public void addNeighbour(Territory neighbor){
        this.neighborTerritories.add(neighbor);
    }

    public List<Territory> getNeighbors() {
        return neighborTerritories;
    }

    public int getArmies() {
        return armies;
    }
    public void setArmies(int armies) {
        this.armies = armies;
    }
    public String getTerritoryName(){
        return territoryName;
    }
}
