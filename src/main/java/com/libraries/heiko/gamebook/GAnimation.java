package com.libraries.heiko.gamebook;

import android.opengl.Matrix;

/**
 * Created by heiko on 19.02.2016.
 */
class GAnimation
{
    private float[] currentTranslation = {1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 1, 0,  0, 0, 0, 1};  // the currently applied transition
    private float[] temp;  // the currently applied transition
    public GAnimation()
    {
        this.temp = new float[16];
    }

    // Updates the animation
    void Update(long a_timeDelta, double a_timeFactor)
    {
        Matrix.rotateM(this.currentTranslation, 0, 30 * ((float) a_timeDelta / 1000000000), 1, 1, 1);
    }

    // Applys the animation
    void Apply(float[] a_mvpMatrix)
    {
        if (currentTranslation == null)
			return;

		for (int i = 0; i < 16; i++)
		{
			this.temp[i] = a_mvpMatrix[i];
		}
		Matrix.multiplyMM(a_mvpMatrix, 0, this.temp, 0, this.currentTranslation, 0);
    }
}
