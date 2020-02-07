package org.tiland;

import org.engine.scene.Entity;

import org.tiland.Tile;

public class Cell extends Entity {

    public Tile.Type tileType = Tile.Type.NONE;

    public Cell(Tile.Type type) {
        this.tileType = type;
    }
}