package com.libraries.heiko.gamebook;

import android.graphics.Canvas;
import android.graphics.Matrix;

/**
 * Created by heiko on 19.02.2016.
 */
public class GAnimation
{
    Matrix currentTranslation;  // the currently applied transition
    public GAnimation()
    {
        currentTranslation = new Matrix();
    }

    // Updates the animation
    public void Update(long a_timeDelta, double a_timeFactor)
    {
        this.currentTranslation.postRotate(60*((float) a_timeDelta/1000000000));
    }

    // Applys the animation
    public void Apply(int a_shaderProgram)
    {
        if (currentTranslation != null)
        {
            // TODO: Check how to correctly handle animations in OpenGL
        }
    }
}
