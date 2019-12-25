import org.engine.Engine;
import org.engine.IGame;

public class Main {

    public static void main(String[] args) {
        try {
            boolean vSync = true;
            IGame gameLogic = new Game();
            Engine engine = new Engine("GAME", 600, 480, vSync, gameLogic);
            engine.run();
        } catch (Exception excp) {
            excp.printStackTrace();
            System.exit(-1);
        }
    }
}