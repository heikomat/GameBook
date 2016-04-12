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
    private final float[] mvpMatrix = new float[16];            // holds the final mvp-matrix for the vertexShader-program (MVP Matrix = Model View Projection Matrix)
    private final float[] projectionMatrix = new float[16];     // used to make object not look streched due to screen-ratio
    private final float[] viewMatrix = new float[16];           // used to define where the camera is
    private long lastFrameTime = 0;                             // stores when the last frame finished rendering
    private int displayWidth = 0;                               // stores the current DisplayWidth, so the projection-matrix can be changed later on
    private int displayHeight = 0;                              // stores the current DisplayHeight, so the projection-matrix can be changed later on
    private RenderMode renderMode = RenderMode.TWOD;			// stores the current renderMode
    public boolean oglReady = false;                           	// flag that indicates wether OpenGL is ready to be used

    // screen-position and size
    float left = 0;
    float right = 0;
    float bottom = 0;
    float top = 0;
    float width = 0;
    float height = 0;
    float horzVertexRatio = 0;
    float vertVertexRatio = 0;

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
        // Make the Background black
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    }

    public void onSurfaceChanged(GL10 a_gl, int a_width, int a_height)
    {
        this.oglReady = true;
        this.gamebook.OGLReady();

        this.displayWidth = a_width;
        this.displayHeight = a_height;

        // make OpenGL use the new screen-size
        GLES20.glViewport(0, 0, this.displayWidth, this.displayHeight);
        this.SetRenderMode(this.renderMode);
    }

    // Sets the RenderMode (2D or 3D)
    public void SetRenderMode(RenderMode a_renderMode)
    {
        if (a_renderMode != null)
            this.renderMode = a_renderMode;

        if (!this.oglReady)
            return;

        // define projection-matrix and set the camera-position (View matrix)
        float ratio = (float) this.gamebook.gameWidth / this.gamebook.gameHeight;
        this.left = -ratio;
        this.right = ratio;
        this.bottom = -1;
        this.top = 1;
        this.width = this.right - this.left;
        this.height = this.top - this.bottom;
        this.horzVertexRatio = this.width/this.gamebook.gameWidth;
        this.vertVertexRatio = this.height/this.gamebook.gameHeight;

        if (this.renderMode == RenderMode.TWOD)
            Matrix.orthoM(projectionMatrix, 0,   this.left, this.right,   this.bottom, this.top,   -1, 10);
        else if (this.renderMode == RenderMode.THREED)
            Matrix.frustumM(projectionMatrix, 0,  this.left, this.right,   this.bottom, this.top, 1, 100);
        else
            throw new Error("unknown render-mode " + a_renderMode);

        Matrix.setLookAtM(viewMatrix, 0,   0, 0, 1,   0, 0, 0,   0, 1, 0);
        Matrix.multiplyMM(mvpMatrix, 0, projectionMatrix, 0, viewMatrix, 0);

        // In 3D-Mode, Elements are drawn on a z-level that is one away from the camera
        // To make Elements with the lowest z-level the correct size on the screen,
        // everything needs to be scaled up by 2
        if (this.renderMode == RenderMode.THREED)
            Matrix.scaleM(mvpMatrix, 0, 2, 2, 1);

        this.gamebook.UpdateScreenDimensions(this.horzVertexRatio, this.vertVertexRatio);
    }

    // gets called every time a Frame can be drawn. Draws the current scene
    public void onDrawFrame(GL10 a_gl)
    {
        // Clear the BackGround and draws the enviroment
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.gamebook.Draw(this.mvpMatrix);

        // calculate draw-fps
        this.gamebook.lastDrawFPS = 1000000000/(System.nanoTime() - this.lastFrameTime);
        this.lastFrameTime = System.nanoTime();
    }

    public enum RenderMode {TWOD, THREED}
}