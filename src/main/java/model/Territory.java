package model;

import java.util.ArrayList;
import java.util.List;

public class Territory {
    private String territoryName;
    private double x;
    private double y;
    private String continent;
    private Player owner;
    private int armies;
    private List<Territory> neighbors;

    public Territory(String territoryName, double x, double y, String continent) {
        this.territoryName = territoryName;
        this.x = x;
        this.y = y;
        this.continent = continent;
        this.armies = 1;
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(Territory neighbor) {
        neighbors.add(neighbor);
    }

    public List<Territory> getNeighbors() {
        return neighbors;
    }

    public String getTerritoryName() {
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

    public Player getOwner() {
        return owner;
    }

    public void setOwner(Player owner) {
        this.owner = owner;
    }

    public int getArmies() {
        return armies;
    }

    public void setArmies(int armies) {
        this.armies = armies;
    }
}
