import java.awt.Font;

import org.engine.core.Rect;
import org.engine.input.Mouse;
import org.joml.Vector2f;
import org.joml.Vector4f;

import org.engine.renderer.FontTexture;
import org.engine.scene.Entity;
import org.engine.IHud;
import org.engine.ui.Button;
import org.engine.ui.Canvas;
import org.engine.ui.Panel;
import org.engine.ui.Text;

import org.engine.renderer.Window;

public class Hud implements IHud {

    private Canvas canvas;
    private Panel bPanel;
    private Button button;
    private Panel rPanel;

    private static final int FONT_COLS = 16;
    private static final int FONT_ROWS = 16;

    private static final Font FONT = new Font("Arial", Font.PLAIN, 32);
    private static final String CHARSET = "ISO-8859-1";

    private Entity[] entities;

    private final Text fpsText;

    public Hud(Window window) throws Exception {

        FontTexture fontTexture = new FontTexture(FONT, CHARSET);

        canvas = new Canvas(window, new Vector2f(720, 480));
        bPanel = new Panel(canvas, canvas, new Rect(0, 0, -100, -100, true), new Rect(0, 0, 1, 1), new Vector2f(0, 0));
        button = new Button(canvas, bPanel, new Rect(100, 100, 100, 30),     new Rect(0, 0, 0, 0),       new Vector2f(0, 0));
        rPanel = new Panel(canvas, bPanel, new Rect(-100, 100, 100, 50),     new Rect(1, 0, 1, 0, true), new Vector2f(1, 0));

//        button.setDepth(bPanel.getDepth() + 0.01f);
  //      rPanel.setDepth(bPanel.getDepth() + 0.01f);

        // Setup a text box.
        fpsText = new Text("0.0", fontTexture);
        fpsText.getMesh().getMaterial().setDiffuseColor(new Vector4f(1, 1, 1, 1));

        entities = new Entity[]{/*canvas,*/ bPanel, button, rPanel, fpsText};
    }

    public void setStatusText(String statusText) {
        fpsText.setText(statusText);
    }

    @Override
    public Entity[] getEntities() {

        return entities;
    }

    public void input(Window window, Mouse mouse) {

        canvas.input(mouse);
    }

    public void updateSize(Window window) {

        canvas.updateSize(window);

        fpsText.setPosition(10f, window.getHeight() - 50f, 0);
    }
}
