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
    }

    // Applys the animation
    public void Apply(Canvas a_targetCanvas)
    {
        if (currentTranslation != null)
            a_targetCanvas.concat(currentTranslation);
    }
}
