package org.engine.anim.info;

import org.engine.anim.info.Clip;
import org.junit.jupiter.api.Test;

public class ClipTest {

    @Test
    public void buildClipTest() {
        Clip clip = new Clip();
        clip.name = "Test Clip";
    }

}
