package com.libraries.heiko.gamebook.controls;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLUtils;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

/**
 * Created by heiko on 23.02.2016.
 */
public class BaseSquare extends GameElement
{
    private int borderRadius = 0;                       // Current border-radius
    private float[] backgroundColor;                    // Current background-color
    private float backgroundWidth;                      // Current width of the background-image in pixels
    private float backgroundHeight;                     // Current height of the background-image in pixels

    private int borderColor = Color.TRANSPARENT;        // Current border-color
    private int borderWidth = 0;                        // Current border-width

	// cache-variables to prevent memory-allocations
	private int tempColor;								// used by SetBoxStyle to parse new colors

    // OpenGL stuff
    private int colorShaderProgram;						// the ShaderProgram to use when only a backgroundColor is set
    private int imageShaderProgram;						// the ShaderProgram to use when only a backgroundImage is set
    private int colorAndImageShaderProgram;				// the ShaderProgram to use when both backgroundColor and backgroundImage are set
    protected int stencilProgram;                          // the ShaderProgram to use when applying the mask
    protected int shaderProgram;							// the currently used shaderProgram based on the set backgroundColor and backgroundImage
														// needs to be public, so subclasses can decide not to call DrawBasics if not needed

	// Variables necessary for positioning the vertices
    protected float coords[] =                                                                // The coordinates of the 6 vertices of the rect
    {                                                                                       // The values represent the whole screen. they only
            0, 0, -(this.zIndex + 1),                                                       // get ues once in the constructor, and will be overwritten
            0, 1, -(this.zIndex + 1),                                                       // with the actual position directly afterwards
            1, 1, -(this.zIndex + 1),
            1, 0, -(this.zIndex + 1)
    };

	private FloatBuffer vertexBuffer;														// Buffer holding the coordinates from coords
	private int vertexPositionHandle = 0;													// Handle to vPosition in the vertexShaders
    private int stencilVertexPositionHandle = 0;                                            // Handle to vPosition in the stencil-vertexShader

    // Variables necessary for fullscreen-stencil-reset
    private FloatBuffer fullscreenVertexBuffer;											    // Buffer holding the coordinates from coords

	// Variables necessary to determine the drawOrder of the vertices
	private short drawOrder[] = {0, 1, 2, 0, 2, 3};											// The order in which to draw the vertices
	private ShortBuffer drawListBuffer;														// Buffer holding the draworder

	// Variables necessary to draw the texture (aka backgroundImage, if there is one set)
	private int[] textureIDs = new int[1];													// Array holding the pointer to the background-texture
	private float[] texturePositions = {0.0f, 1.0f, 0.0f, 0.0f, 1.0f, 0.0f, 1.0f, 1.0f};	// Positions of the texture
	private FloatBuffer texturePositionBuffer;												// Buffer holding the texture-positions
	private int texturePositionHandle = 0;													// Handle to s_texture in the fragmentShaders
	private Bitmap backgroundBitmap;														// The Bitmap to use as texture (aka backgroundImage)

	// The texture-shader to use, when a background-texture is set
    private final String textureVertexShaderCode  =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "attribute vec2 a_texCoord;" +
        "varying vec2 v_texCoord;" +
        "void main()" +
        "{" +
        "   gl_Position = uMVPMatrix * vPosition;" +
        "   v_texCoord = a_texCoord;" +
        "}";

	// The texture-shader to use, when no background-texture is set
    private final String colorVertexShaderCode =
        "uniform mat4 uMVPMatrix;" +
        "attribute vec4 vPosition;" +
        "void main()" +
        "{" +
        "   gl_Position = uMVPMatrix * vPosition;" +
        "}";

	// The fragment-shader to use, when only a background-color, but no background-texture is set
    private final String colorFragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main()" +
        "{" +
        "   gl_FragColor = vColor;" +
        "}";

	// The fragment-shader to use, when only a background-texture, but no background-color is set
    private final String imageFragmentShaderCode =
        "precision mediump float;" +
        "varying vec2 v_texCoord;" +
        "uniform sampler2D s_texture;" +
        "void main()" +
        "{" +
        "   vec4 tex = texture2D (s_texture, v_texCoord);" +
        "   gl_FragColor = vec4(tex.r, tex.g, tex.b, 1.0 - tex.a);" +
        "}";

	// The fragment-shader to use, when both background-color and background-texture are set
    private final String colorAndImageFragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "varying vec2 v_texCoord;" +
        "uniform sampler2D s_texture;" +
        "void main()" +
        "{" +
        "   vec4 texture = texture2D(s_texture, v_texCoord);" +
        "   vec4 bgColor = vColor;" +
        "   bgColor.a = bgColor.a - clamp(bgColor.a + texture.a - 1.0, 0.0, bgColor.a);" +
        "   texture.a = 1.0 - (texture.a - clamp(vColor.a + texture.a - 1.0, 0.0, texture.a));" +
        "   gl_FragColor = vec4(bgColor.r*bgColor.a/vColor.a + texture.r," +		// Dividing by old alpha, multiplying by new one
        "						bgColor.g*bgColor.a/vColor.a + texture.g, " +
        "						bgColor.b*bgColor.a/vColor.a + texture.b, " +
        "						bgColor.a * texture.a);" +
        "}";

    // The fragment-shader to use when editing the current stencil
    private final String stencilFragmentShaderCode =
        "precision mediump float;" +
        "uniform vec4 vColor;" +
        "void main()" +
        "{" +
        "   gl_FragColor = vec4(0.0, 0.0, 0.0, 1.0);" +
        "}";

    public BaseSquare(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this.SetBoxStyle(this.borderRadius, null, null, this.borderWidth);

		// initialize vertexbuffer and texturePositionBuffer
        ByteBuffer vertexByteBuffer = ByteBuffer.allocateDirect(this.coords.length * 4);
        this.vertexBuffer = vertexByteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

        vertexByteBuffer = ByteBuffer.allocateDirect(this.coords.length * 4);
        this.fullscreenVertexBuffer = vertexByteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();
        this.fullscreenVertexBuffer.put(this.coords).position(0);

		ByteBuffer textureByteBuffer = ByteBuffer.allocateDirect(texturePositions.length * 4);
		this.texturePositionBuffer = textureByteBuffer.order(ByteOrder.nativeOrder()).asFloatBuffer();

        // initialize byte buffer for the draw list
        ByteBuffer dlb = ByteBuffer.allocateDirect(drawOrder.length * 2);
        this.drawListBuffer = dlb.order(ByteOrder.nativeOrder()).asShortBuffer();
        this.drawListBuffer.put(this.drawOrder).position(0);
        this.PosToVertices();

        if (this.book.gameRenderer.oglReady == true)
            this._OGLReady();
    }

    @Override
    protected void _OGLReady()
    {
        // Create an empty OpenGL ES Program, Load the Shaders and add them to the program and create (compile) it
        this.colorShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.colorShaderProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, this.colorVertexShaderCode));
        GLES20.glAttachShader(this.colorShaderProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, this.colorFragmentShaderCode));
        GLES20.glLinkProgram(this.colorShaderProgram);

        this.imageShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.imageShaderProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, this.textureVertexShaderCode));
        GLES20.glAttachShader(this.imageShaderProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, this.imageFragmentShaderCode));
        GLES20.glLinkProgram(this.imageShaderProgram);

        this.colorAndImageShaderProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.colorAndImageShaderProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, this.textureVertexShaderCode));
        GLES20.glAttachShader(this.colorAndImageShaderProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, this.colorAndImageFragmentShaderCode));
        GLES20.glLinkProgram(this.colorAndImageShaderProgram);

        this.stencilProgram = GLES20.glCreateProgram();
        GLES20.glAttachShader(this.stencilProgram, this._LoadShader(GLES20.GL_VERTEX_SHADER, this.colorVertexShaderCode));
        GLES20.glAttachShader(this.stencilProgram, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, this.stencilFragmentShaderCode));
        GLES20.glLinkProgram(this.stencilProgram);
        this.stencilVertexPositionHandle = GLES20.glGetAttribLocation(this.stencilProgram, "vPosition");

        // Create a texture and get its ID
        GLES20.glGenTextures(1, this.textureIDs, 0);
        this.UpdateShaderProgram();
    }

    @Override
    public void SetSize(int a_width, int a_height)
    {
        super.SetSize(a_width, a_height);
        this.PosToVertices();
    }

    @Override
    public void SetPosition(int a_x, int a_y)
    {
        super.SetPosition(a_x, a_y);
        this.PosToVertices();
    }

    private void PosToVertices()
    {
        // top left
        this.coords[0] = this.vectorX;
        this.coords[1] = this.vectorY;

        // bottom left
        this.coords[3] = this.vectorX;
        this.coords[4] = this.vectorY + this.vectorHeight;

        // bottom right
        this.coords[6] = this.vectorX + this.vectorWidth;
        this.coords[7] = this.vectorY + this.vectorHeight;

        // top right
        this.coords[9] = this.vectorX + this.vectorWidth;
        this.coords[10] = this.vectorY;

        // create a floating point buffer from the ByteBuffer, add the coordinates to it,
        // and make it read from the beginning of the buffer
        this.vertexBuffer.put(this.coords).position(0);
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
		// TODO: Implement actual usage of borderRadius and borderWidth
        this.borderRadius = a_borderRadius;
        this.borderWidth = a_borderWidth;
        if (a_backgroundColor != null)
        {
            if (this.backgroundColor == null)
                this.backgroundColor = new float[4];

            this.tempColor = Color.parseColor(a_backgroundColor);

            this.backgroundColor[3] = (float)((tempColor >> 24) & 0xFF) / 0xFF;
            this.backgroundColor[0] = ((float)((tempColor >> 16) & 0xFF) / 0xFF) * this.backgroundColor[3];
            this.backgroundColor[1] = ((float)((tempColor >> 8) & 0xFF) / 0xFF) * this.backgroundColor[3];
            this.backgroundColor[2] = ((float)(tempColor & 0xFF) / 0xFF) * this.backgroundColor[3];
            this.backgroundColor[3] = 1.0f - this.backgroundColor[3];
        }

        if (a_borderColor != null)
            this.borderColor = Color.parseColor(a_borderColor);

        this.UpdateShaderProgram();
    }

    /*
        Function: SetBackgroundImage
            Sets the background-bitmap of the object

        Parameter:
            a_bitmap    - Bitmap    | the backgroundImage to set
            a_width     - float		| The width in pixel the texture should get
            a_height    - float		| The height in pixel the texture should get
    */
    public void SetBackground(Bitmap a_bitmap, float a_width, float a_height)
    {
        this.backgroundBitmap = a_bitmap;
        this.UpdateShaderProgram();

        this.SetBackgroundSize(a_width, a_height);
    }

    /*
        Function: SetBackgroundImage
            Sets the background-bitmap of the object

        Parameter:
            a_bitmap    - Bitmap    | the backgroundImage to set
    */
    public void SetBackground(Bitmap a_bitmap)
    {
        this.SetBackground(a_bitmap, this.width, this.height);
    }

	/*
		Function: SetBackgroundSize
			Sets the size of the background-texture

		Parameter:
			a_width		- float	| The width in pixel the texture should get
			a_height	- float	| The height in pixel the texture should get
	*/
    public void SetBackgroundSize(float a_width, float a_height)
    {
        if (a_width == 0 || a_height == 0)
            throw new Error("Backgroundwidth or backgroundheight of " + this.id + " cannot be set to 0");

        this.backgroundWidth = a_width;
        this.backgroundHeight = a_height;

        // Set the new texture-position based on the new texture-size
		// TODO: Allow the setting of the actual position, not only the Size!
		this.texturePositions[1] = this.height / this.backgroundHeight;
		this.texturePositions[4] = this.width / this.backgroundWidth;
		this.texturePositions[6] = this.width / this.backgroundWidth;
		this.texturePositions[7] = this.height / this.backgroundHeight;
		this.texturePositionBuffer.put(this.texturePositions).position(0);
    }

    // Sets the shader-program to use, depending on the backgroundImage and backgroundColor currently set
    private void UpdateShaderProgram()
    {
        if (this.book.gameRenderer.oglReady == false)
        {
            this.shaderProgram = 0;
            return;
        }

        if (this.backgroundColor != null && this.backgroundBitmap == null)
            this.shaderProgram = this.colorShaderProgram;
        else if (this.backgroundColor == null && this.backgroundBitmap != null)
            this.shaderProgram = this.imageShaderProgram;
        else if (this.backgroundColor != null && this.backgroundBitmap != null)
            this.shaderProgram = this.colorAndImageShaderProgram;
        else
            this.shaderProgram = 0;

		if (this.shaderProgram == 0)
			return;

		// Get the vertex-position handle of the current program and enable its usage
		this.vertexPositionHandle = GLES20.glGetAttribLocation(this.shaderProgram, "vPosition");

        if (this.backgroundBitmap != null)
        {
            // Bind texture to texturename
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIDs[0]);

            // Set filtering
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

            // Set wrapping mode
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_REPEAT);
            GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_REPEAT);

            // Load the bitmap into the bound texture.
            GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.backgroundBitmap, 0);

            // Get handle to texture coordinates
            this.texturePositionHandle = GLES20.glGetAttribLocation(this.shaderProgram, "a_texCoord");
        }
    }

    /*
        Function: DrawBasics
            Draws the box as currently set up

        Parameter:
            a_shaderProgram  - int    | The ShaderProgram to use
    */
    protected void DrawBasics(float[] a_mvpMatrix)
    {
        if (this.shaderProgram == 0)
            return;

        // Make use of the current Program and the currently set stencil
		GLES20.glUseProgram(this.shaderProgram);

        // Enable blending for premultiplied alpha
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_SRC_ALPHA);

		// Set the view-projecton-matrix and the vertex-position
		GLES20.glEnableVertexAttribArray(this.vertexPositionHandle);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.shaderProgram, "uMVPMatrix"), 1, false, a_mvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.vertexPositionHandle, 3, GLES20.GL_FLOAT, false, 12, this.vertexBuffer);

        // Set Background color
        if (this.backgroundColor != null)
			GLES20.glUniform4fv(GLES20.glGetUniformLocation(this.shaderProgram, "vColor"), 1, this.backgroundColor, 0);

        // Set background image
        if (this.backgroundBitmap != null)
        {
			// Bind the Texture and set its coordinates
            GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
            GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIDs[0]);
			GLES20.glEnableVertexAttribArray(this.texturePositionHandle);
			GLES20.glVertexAttribPointer(this.texturePositionHandle, 2, GLES20.GL_FLOAT, false, 0, this.texturePositionBuffer);
        }

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
		GLES20.glDisableVertexAttribArray(this.vertexPositionHandle);

		if (this.backgroundBitmap != null)
			GLES20.glDisableVertexAttribArray(this.texturePositionHandle);
    }

    @Override
    // apply the mask of this element, so sub elements won't overflow
    protected int _ApplyMask(float[] a_mvpMatrix, int a_zIndex)
    {
        if (this.stencilProgram == 0)
            return a_zIndex;

        GLES20.glUseProgram(this.stencilProgram);

        // Initialize Stencil-manipulation
        GLES20.glEnable(GLES20.GL_STENCIL_TEST);
        GLES20.glColorMask(false, false, false, false);
        GLES20.glStencilMask(0xFF);

        if (a_zIndex == 255)
        {
            // TODO: Test, if this actually works
            // flip the zIndex, because the maximum depth has been reached
            // who the fuck uses more than 255 GUI-Elements inside one another?
            GLES20.glStencilFunc(GLES20.GL_EQUAL, a_zIndex, 0xFF);
            GLES20.glStencilOp(GLES20.GL_ZERO, GLES20.GL_ZERO, 5);

            // clear the whole stencil-buffer with 0, except where its 255, set it to 1 there
            GLES20.glEnableVertexAttribArray(this.stencilVertexPositionHandle);
            GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.stencilProgram, "uMVPMatrix"), 1, false, a_mvpMatrix, 0);
            GLES20.glVertexAttribPointer(this.stencilVertexPositionHandle, 3, GLES20.GL_FLOAT, false, 12, this.fullscreenVertexBuffer);
            GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);
            a_zIndex = 1;
        }

        // make it increase the stencil value by one for the whole area
        GLES20.glStencilFunc(GLES20.GL_ALWAYS, 0, 0xFF);
        GLES20.glStencilOp(GLES20.GL_KEEP, GLES20.GL_KEEP, GLES20.GL_INCR);

        // Set the view-projecton-matrix and the vertex-position
        GLES20.glEnableVertexAttribArray(this.stencilVertexPositionHandle);
        GLES20.glUniformMatrix4fv(GLES20.glGetUniformLocation(this.stencilProgram, "uMVPMatrix"), 1, false, a_mvpMatrix, 0);
        GLES20.glVertexAttribPointer(this.stencilVertexPositionHandle, 3, GLES20.GL_FLOAT, false, 12, this.vertexBuffer);
        GLES20.glDrawElements(GLES20.GL_TRIANGLES, drawOrder.length, GLES20.GL_UNSIGNED_SHORT, drawListBuffer);

        // switch back to regular non-stencil rendering
        GLES20.glColorMask(true, true, true, true);
        return a_zIndex + 1;
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