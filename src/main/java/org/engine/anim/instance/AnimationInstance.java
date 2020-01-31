package org.engine.anim.instance;

import org.engine.anim.info.Skeleton;
import org.engine.anim.info.Clip;

public class AnimationInstance {
    // Info References
    protected Skeleton skeleton = null;
    protected Clip currentClip = null;

    // Instance Data
    public BoneInstance[] boneInstances;

    AnimationInstance(Skeleton s) {
        skeleton = s;

        boneInstances = new BoneInstance[skeleton.boneList.length];
    }


}