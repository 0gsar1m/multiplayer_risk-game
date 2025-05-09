package model;

import java.util.ArrayList;
import java.util.List;

public class Territory {
    private String territoryName;
    private Player owner;
    private int armies;
    private List<Territory> neighborTerritories;
    private double x;
    private double y;
    private String continent;

    public Territory(String territoryName, double x, double y, String continent) {
        this.territoryName = territoryName;
        this.x = x;
        this.y = y;
        this.continent = continent;
        this.armies = 1; //default start
        this.neighborTerritories = new ArrayList<>();
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

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public String getContinent() {
        return continent;
    }
}
