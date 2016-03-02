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
    // OpenGL stuff
    private int shaderProgram;
    private final String vertexShaderCode   = "uniform mat4 uMVPMatrix; attribute vec4 vPosition; void main() { gl_Position = uMVPMatrix * vPosition; }";
    private final String fragmentShaderCode = "precision mediump float; uniform vec4 vColor; void main() { gl_FragColor = vColor; }";
    private final float[] mMVPMatrix = new float[16];           // holds the final mvp-matrix for the vertexShader-program (MVP Matrix = Model View Projection Matrix)
    private final float[] mProjectionMatrix = new float[16];    // used to make object not look streched due to screen-ratio
    private final float[] mViewMatrix = new float[16];          // used to define where the camera is

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
        // Create an empty OpenGL ES Program, Load the Shaders and add them to the program and create (compile) it
        this.shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.shaderProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode));
        GLES20.glAttachShader(this.shaderProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));
        GLES20.glLinkProgram(this.shaderProgram);

        // Make OpenGL use the newly created program
        GLES20.glUseProgram(this.shaderProgram);

        // Make the Background black
        GLES20.glClearColor(0.3f, 0.3f, 0.3f, 1.0f);
    }

    public void onSurfaceChanged(GL10 a_gl, int a_width, int a_height)
    {
        // make OpenGL use the new screen-size
        GLES20.glViewport(0, 0, a_width, a_height);

        // define projection-matrix(frustumM) and set the camera-position (View matrix)
        float ratio = (float) a_width / a_height;
        Matrix.frustumM(mProjectionMatrix, 0, -ratio, ratio, -1, 1, 3, 7);
        Matrix.setLookAtM(mViewMatrix, 0, 0, 0, -3, 0f, 0f, 0f, 0f, 1.0f, 0.0f);

        // Calculate the projection and view transformation, and pass the result to the transformation matrix of the vertexShader-program
        Matrix.multiplyMM(mMVPMatrix, 0, mProjectionMatrix, 0, mViewMatrix, 0);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.shaderProgram, "uMVPMatrix"), 1, false, mMVPMatrix, 0);
    }

    // gets called every time a Frame can be drawn. Draws the current scene
    public void onDrawFrame(GL10 a_gl)
    {
        // Clear the BackGround and draws the enviroment
        long startTime = System.nanoTime();

        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        this.gamebook._Draw(this.shaderProgram);

        this.gamebook.lastDrawFPS = 1000000000/(System.nanoTime() - startTime);
    }

    // creates a shader of a given type an compiles a given sourceCode into it
    private static int _LoadShader(int a_type, String a_shaderCode)
    {
        int shader = GLES20.glCreateShader(a_type);
        GLES20.glShaderSource(shader, a_shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }
}
