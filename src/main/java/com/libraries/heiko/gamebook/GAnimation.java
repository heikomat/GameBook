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
        currentTranslation.postRotate(((float) 30*a_timeDelta)/1000000000);
        //currentTranslation.postRotate(180);
    }

    // Applys the animation
    public void Apply(Canvas a_targetCanvas)
    {
        a_targetCanvas.concat(currentTranslation);
    }
}
