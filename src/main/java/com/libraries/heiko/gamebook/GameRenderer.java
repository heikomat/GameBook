package com.libraries.heiko.gamebook;

import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * Created by heiko on 28.02.2016.
 */
public class GameRenderer implements GLSurfaceView.Renderer
{
    private final float[] mvpMatrix = new float[16];           // holds the final mvp-matrix for the vertexShader-program (MVP Matrix = Model View Projection Matrix)
    private final float[] ProjectionMatrix = new float[16];    // used to make object not look streched due to screen-ratio
    private final float[] viewMatrix = new float[16];          // used to define where the camera is
    private long lastFrameTime = 0;                            // stores when the last frame finished rendering
    public boolean oglReady = false;                           // flag that indicates wether OpenGL is ready to be used

    // GameBook stuff
    private GameBook gamebook;                                  // Reference to the GameBook-Instance to call the _Draw function
    public GameRenderer(GameBook a_gamebook, int a_targetFramerate)
    {
        super();
        this.gamebook = a_gamebook;
    }

    public void onPause()
    {
    }

    public void onResume()
    {
    }

    public void onSurfaceCreated(GL10 a_gl, EGLConfig a_config)
    {
        this.oglReady = true;
        this.gamebook._OGLReady();

        // Make the Background black
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    }

    public void onSurfaceChanged(GL10 a_gl, int a_width, int a_height)
    {
        // TODO: Implement a displaySize-independent game-size
        // make OpenGL use the new screen-size
        GLES20.glViewport(0, 0, a_width, a_height);

        // define projection-matrix(orthoM) and set the camera-position (View matrix)
        Matrix.orthoM(ProjectionMatrix, 0, 0, a_width, 0, a_height, -1, 10);
        //Matrix.orthoM(ProjectionMatrix, 0, 0, a_width, 0, a_height, -1, 10);
        Matrix.setLookAtM(viewMatrix, 0, 0, 0, 1, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation, and pass the result to the transformation matrix of the vertexShader-program
        Matrix.multiplyMM(mvpMatrix, 0, ProjectionMatrix, 0, viewMatrix, 0);
    }

    // gets called every time a Frame can be drawn. Draws the current scene
    public void onDrawFrame(GL10 a_gl)
    {
        // Clear the BackGround and draws the enviroment
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.gamebook._Draw(this.mvpMatrix);

        // calculate draw-fps
        this.gamebook.lastDrawFPS = 1000000000/(System.nanoTime() - this.lastFrameTime);
        this.lastFrameTime = System.nanoTime();
    }
}
