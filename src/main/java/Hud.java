import org.joml.Vector4f;
import org.engine.scene.Entity;
import org.engine.IHud;
import org.engine.TextBox;

import org.engine.renderer.Window;

public class Hud implements IHud {
    private static final int FONT_COLS = 16;
    private static final int FONT_ROWS = 16;

    private static final String FONT_TEXTURE = "src/main/resources/textures/font_texture.png";

    private Entity[] entities;

    private final TextBox statusTextBox;

    public Hud(String statusText) throws Exception {
        this.statusTextBox = new TextBox(statusText, FONT_TEXTURE, FONT_COLS, FONT_ROWS);
        this.statusTextBox.getMesh().getMaterial().setAmbientColor(new Vector4f(1, 1, 1, 1));
        entities = new Entity[]{statusTextBox};
    }

    public void setStatusText(String statusText) {
        this.statusTextBox.setText(statusText);
    }

    @Override
    public Entity[] getEntities() {
        return entities;
    }

    public void updateSize(Window window) {
        this.statusTextBox.setPosition(10f, window.getHeight() - 50f, 0);
    }
}
