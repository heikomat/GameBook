package com.libraries.heiko.gamebook;

import android.opengl.Matrix;

/**
 * Created by heiko on 19.02.2016.
 */
public class GAnimation
{
    float[] currentTranslation = {1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1};  // the currently applied transition
    float[] temp;  // the currently applied transition
    public GAnimation()
    {
        this.temp = new float[16];
    }

    // Updates the animation
    public void Update(long a_timeDelta, double a_timeFactor)
    {
		// TODO: Rotation for the time beeing only works around the z-axis
		// TODO: Fix projection and animation, so that a frustom-projection can be used
		// TODO: And neat 2.5D-effects can be created
        Matrix.rotateM(this.currentTranslation, 0, 60 * ((float) a_timeDelta / 1000000000), 0, 0, 1);
    }

    // Applys the animation
    public void Apply(float[] a_mvpMatrix)
    {
        if (currentTranslation != null)
        {
            for (int i = 0; i < 16; i++)
            {
                this.temp[i] = a_mvpMatrix[i];
            }

            Matrix.multiplyMM(a_mvpMatrix, 0, this.temp, 0, this.currentTranslation, 0);
        }
    }
}
