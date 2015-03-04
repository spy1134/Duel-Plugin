package com.github.spy1134.duelplugin;

import org.bukkit.Location;

public class Arena {

    private Location posOne, posTwo;

    public Arena() {
        posOne = null;
        posTwo = null;
    }

    public Arena(Location posOne, Location posTwo) {
        this.posOne = posOne;
        this.posTwo = posTwo;
    }

    public Location getPosOne() {
        return posOne;
    }

    public Location getPosTwo() {
        return posTwo;
    }

    public void setPosOne(Location loc) {
        this.posOne = loc;
    }

    public void setPosTwo(Location loc) {
        this.posTwo = loc;
    }

    /*
     Checks if a given location is within the bounds of this cube.
     @returns false if the positions haven't both been set.
     public boolean isInCube(Location loc) {
     Double xMin, xMax, yMin, yMax, zMin, zMax;
        
     if (posOne == null || posTwo== null)
     return false;
        
     // Get X range of cube.
     if(posOne.getX() < posTwo.getX()) {
     xMin = posOne.getX();
     xMax = posTwo.getX();
     } else {
     xMin = posTwo.getX();
     xMax = posOne.getX();
     }
        
     // Get Y range of cube.
     if(posOne.getY() < posTwo.getY()) {
     yMin = posOne.getY();
     yMax = posTwo.getY();
     } else {
     yMin = posTwo.getY();
     yMax = posOne.getY();
     }
        
     // Get Z range of cube.
     if(posOne.getZ() < posTwo.getZ()) {
     zMin = posOne.getZ();
     zMax = posTwo.getZ();
     } else {
     zMin = posTwo.getZ();
     zMax = posOne.getZ();
     }
        
     // Check if the given location is within X Y and Z bounds.
     if(loc.getX() < xMin || loc.getX() > xMax)
     return false;
        
     else if(loc.getY() < yMin || loc.getY() > yMax)
     return false;
        
     else if(loc.getZ() < zMin || loc.getZ() > zMax)
     return false;
        
     // If we made it here then the location is within bounds.
     return true;
     }
     */
}
