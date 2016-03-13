package com.libraries.heiko.gamebook.controls;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.Matrix;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.tools.GameBackground;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by heiko on 23.02.2016.
 */
public class BaseElement extends GameElement
{
    private int borderRadius = 0;                       // Current border-radius
    private int tempColor;
    private float[] backgroundColor = {0, 0, 0, 0};     // Current background-color

    private int borderColor = Color.TRANSPARENT;        // Current border-color
    private int borderWidth = 0;                        // Current border-width
    public GameBackground background;                   // Current background
    private boolean changed = true;                     // true: the element changed since the las draw, false: the element did not change since the last draw

    private int tempHandle;

    // OpenGL stuff
    public int shaderProgram;
    public final String vertexShaderCode   = "uniform mat4 uMVPMatrix; attribute vec4 vPosition; void main() { gl_Position = uMVPMatrix * vPosition; }";
    public final String fragmentShaderCode = "precision mediump float; uniform vec4 vColor; void main() { gl_FragColor = vColor; }";

    private float coords[] = {0, 0, 0,  0, 0, 0,  0, 0, 0,  0, 0, 0};
    private short drawOrder[] = {0, 1, 2, 0, 2, 3};
    private FloatBuffer vertexBuffer;
    private ShortBuffer drawListBuffer;
    ByteBuffer bb;
    ByteBuffer dlb;

    public BaseElement(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this.background = new GameBackground();
        this.SetBoxStyle(this.borderRadius, null, null, this.borderWidth);

        this.bb = ByteBuffer.allocateDirect(this.coords.length * 4);
        this.bb.order(ByteOrder.nativeOrder());
        this.dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        this.dlb.order(ByteOrder.nativeOrder());

        this._posToVertices();
    }

    public void _OGLReady()
    {
        // Create an empty OpenGL ES Program, Load the Shaders and add them to the program and create (compile) it
        this.shaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.shaderProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode));
        GLES20.glAttachShader(this.shaderProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));
        GLES20.glLinkProgram(this.shaderProgram);
    }

    public void SetSize(int a_width, int a_height)
    {
        this.width = a_width;
        this.height = a_height;
        this._posToVertices();
    }

    public void SetPosition(int a_x, int a_y)
    {
        this.x = a_x;
        this.y = a_y;
        this._posToVertices();
    }

    private void _posToVertices()
    {
        // top left
        this.coords[0] = this.x;
        this.coords[1] = this.y;

        // bottom left
        this.coords[3] = this.x;
        this.coords[4] = this.y + this.height;

        // bottom right
        this.coords[6] = this.x + this.width;
        this.coords[7] = this.y + this.height;

        // top right
        this.coords[9] = this.x + this.width;
        this.coords[10] = this.y;

        // create a floating point buffer from the ByteBuffer, add the coordinates to it,
        // and make it read from the beginning of the buffer
        this.vertexBuffer = bb.asFloatBuffer();
        this.vertexBuffer.put(this.coords);
        this.vertexBuffer.position(0);

        // initialize byte buffer for the draw list
        drawListBuffer = this.dlb.asShortBuffer();
        drawListBuffer.put(this.drawOrder);
        drawListBuffer.position(0);
    }

    /*
        Function: SetBoxStyle
            Sets the style of the box drawn by DrawBasics

        Parameter:
            a_borderRadius      - int       | the border-radius the box will have
            a_backgroundColor   - String    | the background-color the box will have (e.g. "#FFFFFF")
            a_borderColor       - String    | the border-color the box will have (e.g. "#0000FF");
            a_borderWidth       - int       | the width the border of the box will have
    */
    public void SetBoxStyle(int a_borderRadius, String a_backgroundColor, String a_borderColor, int a_borderWidth)
    {
        this.borderRadius = a_borderRadius;
        this.borderWidth = a_borderWidth;
        if (a_backgroundColor != null)
        {
            this.tempColor = Color.parseColor(a_backgroundColor);
            this.backgroundColor[3] = (float)(tempColor >> 24) / 0xFF;
            this.backgroundColor[0] = (float)((tempColor >> 16) & 0xFF) / 0xFF;
            this.backgroundColor[1] = (float)((tempColor >> 8) & 0xFF) / 0xFF;
            this.backgroundColor[2] = (float)(tempColor & 0xFF) / 0xFF;
            System.out.println("alpha: " + this.backgroundColor[3]);
        }

        if (a_borderColor != null)
            this.borderColor = Color.parseColor(a_borderColor);
    }

    /*
        Function: SetBackgroundImage
            Sets the background-bitmap of the object

        Parameter:
            a_bitmap    - Bitmap    | the backgroundImage to set
    */
    public void SetBackgroundImage(Bitmap a_bitmap)
    {
        this.background.SetBitmap(a_bitmap);
        this.background.SetBounds(this.x, this.y, this.width, this.height);
    }

    /*
        Function: DrawBasics
            Draws the box as currently set up

        Parameter:
            a_shaderProgram  - int    | The ShaderProgram to use
    */
    public void DrawBasics(float[] a_mvpMatrix)
    {
        if (this.shaderProgram == 0)
            return;

        this.changed = false;
        // TODO: Draw the box with border

        // Make OpenGL use the newly created program
        GLES20.glUseProgram(this.shaderProgram);

        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);

        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.shaderProgram, "uMVPMatrix"), 1, false, a_mvpMatrix, 0);

        this.tempHandle = GLES20.glGetAttribLocation(this.shaderProgram, "vPosition");
        GLES20.glEnableVertexAttribArray(this.tempHandle);
        GLES20.glVertexAttribPointer(this.tempHandle, 3, GLES20.GL_FLOAT, false, 12, vertexBuffer);
        GLES20.glUniform4fv(GLES20.glGetUniformLocation(this.shaderProgram, "vColor"), 1, this.backgroundColor, 0);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        this.background.Draw(this.shaderProgram, this.x, this.y, this.width, this.height);
    }

    // apply the mask of this element, so sub elements won't overflow
    public void _ApplyMask(float[] a_mvpMatrix)
    {
        // TODO: Apply some kind of mask
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
