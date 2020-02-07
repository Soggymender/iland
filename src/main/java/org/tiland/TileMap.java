package org.tiland;

import org.engine.scene.Scene;

import org.tiland.Cell;
import org.tiland.Tile;

public class TileMap {

    TileCatalog tileCatalog = null;

    private static final int WIDTH = 4;
    private static final int HEIGHT = 4;

    private Cell cells[][];

    private Scene scene = null;

    public TileMap(Scene scene) throws Exception {
 
        this.scene = scene;

        tileCatalog = new TileCatalog();

        cells = new Cell[WIDTH][HEIGHT];

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                cells[x][y] = new Cell(Tile.Type.DIRT);
                cells[x][y].setMesh(tileCatalog.tiles[cells[x][y].tileType.ordinal()].mesh);

                cells[x][y].setPosition(x, -y, 0.0f);
            }
        }

        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {
                scene.addEntityMeshes(cells[x][y]);
            }
        }
    }

    public void generate() {

        /*
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < WIDTH; x++) {

            }
        }
        */
    }        

}