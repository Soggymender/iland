package org.tiland;

import org.engine.renderer.Color;

import org.tiland.Tile;

public class TileCatalog {

    public Tile tiles[];

    public TileCatalog() throws Exception {

        tiles = new Tile[Tile.Type.COUNT.ordinal()];

        tiles[Tile.Type.NONE.ordinal()] = new Tile(new Color(0.5f, 0.5f, 1.0f, 1.0f));
        tiles[Tile.Type.DIRT.ordinal()] = new Tile(new Color(0.0f, 1.0f, 0.0f, 1.0f));
    }
}