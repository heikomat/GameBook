package com.libraries.heiko.gamebook.tools;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.opengl.GLES20;
import android.opengl.GLUtils;
import android.opengl.Matrix;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

public class GameFont
{
    public final static int CHAR_START = 32;                        // First Character (ASCII Code)
    public final static int CHAR_END = 126;                         // Last Character (ASCII Code)
    public final static int CHAR_CNT = (CHAR_END - CHAR_START) + 2; // Character Count (Including Character to use for Unknown)
    public final static int CHAR_NONE = 32;                         // Character to Use for Unknown (ASCII Code)
    public final static int CHAR_UNKNOWN = (CHAR_CNT - 1);          // Index of the Unknown Character

    //--Members--//
    AssetManager assets;                               // Asset Manager
    int fontPadX, fontPadY, fontSize;                  // Font Padding (Pixels; On Each Side, ie. Doubled on Both X+Y Axis)
    String fontFile;                                   // FontFile to load

    float[] color = new float[4];
    public boolean fontLoaded = false;

    int textureId = -1;                                     // Font Texture ID [NOTE: Public for Testing Purposes Only!]
    float charWidthMax = 0;                                // Character Width (Maximum; Pixels)
    float charHeight = 0;                                  // Character Height (Maximum; Pixels)
    final float[] charWidths;                          // Width of Each Character (Actual; Pixels)
    final Rect[] charBounds;                          // Width of Each Character (Actual; Pixels)
    float[][] charRgn;                           // Region of Each Character (Texture Coordinates)
    int cellWidth = 0, cellHeight = 0;                         // Character Cell Width/Height
    int columnCount = 0;                                // Number of Rows/Columns

    float scaleX = 1, scaleY = 1;                              // Font Scale (X,Y Axis)
    float spaceX = 0;                                      // Additional (X,Y Axis) Spacing (Unscaled)

    private int programHandle; 						   // OpenGL Program object
    private int mColorHandle;						   // Shader color handle
    private int mTextureUniformHandle;                 // Shader texture handle

    // cache-variables to prevent memory-allocations
    int tempWidth;
    int tempLen;

    private static final String vertexShaderCode =
            "uniform mat4 uMVPMatrix;      \n"     // An array representing the combined
                    + "attribute vec4 a_Position;     \n"     // Per-vertex position information we will pass in.
                    + "attribute vec2 a_TexCoordinate;\n"     // Per-vertex texture coordinate information we will pass in
                    + "varying vec2 v_TexCoordinate;  \n"   // This will be passed into the fragment shader.
                    + "void main()                    \n"     // The entry point for our vertex shader.
                    + "{                              \n"
                    + "   v_TexCoordinate = a_TexCoordinate; \n"
                    + "   gl_Position = uMVPMatrix * a_Position;   \n"     // gl_Position is a special variable used to store the final position.
                    + "}                              \n";


    private static final String fragmentShaderCode =
            "uniform sampler2D u_Texture;       \n"
                    + "precision mediump float;       \n"
                    + "uniform vec4 u_Color;          \n"
                    + "varying vec2 v_TexCoordinate;  \n"
                    + "void main()                    \n"
                    + "{                              \n"
                    + "   gl_FragColor = texture2D(u_Texture, v_TexCoordinate).w * u_Color;\n"
                    + "}                             \n";


    //SpriteBatcher
    int bufferIndex = 0;                                   // Vertex Buffer Start Index
    int numSprites = 0;                                    // Number of Sprites Currently in Buffer
    int maxSprites = 24;                                    // Maximum Sprites Allowed in Buffer
    private float[] mVPMatrix;							// View and projection matrix specified at begin
    private int mMVPMatricesHandle;							// shader handle of the MVP matrix array
    private float[] mMVPMatrix = new float[16];				// used to calculate MVP matrix of each sprite
    private float[] modelMatrix = new float[16];				// used to calculate MVP matrix of each sprite

    public final int vertexSize = 20;                       // Bytesize of a Single Vertex
    final FloatBuffer vertices;                          // Vertex Buffer
    final ShortBuffer indices;                         // Index Buffer
    private float[] vertexValues;				// used to calculate MVP matrix of each sprite

    /*
        Function: GameFont
            prepares the font to be loaded

        Parameter:
            a_assets    - AssetManager  | The AssetManager that has the Font to load
            a_fontFile  - String        | Name of the .ttf or .otf file to load
            a_fontSize  - Integer       | Desired FontSize in px
            a_fontPadX  - Integer       | x-padding between characters
            a_fontPadY  - Integer       | y-padding between characters
     */
    public GameFont(AssetManager a_assets, String a_fontFile, int a_fontSize, int a_fontPadX, int a_fontPadY, int a_spaceX)
    {
        this.assets = a_assets;                           // Save the Asset Manager Instance
        this.charWidths = new float[CHAR_CNT];               // Create the Array of Character Widths
        this.charBounds = new Rect[CHAR_CNT];               // Create the Array of Character Widths
        this.charRgn = new float[CHAR_CNT][4];          // Create the Array of Character Regions

        // initialize remaining members
        this.fontPadX = a_fontPadX;
        this.fontPadY = a_fontPadY;
        this.fontSize = a_fontSize;
        this.fontFile = a_fontFile;
        this.spaceX = a_spaceX;

        // Sprite Batcher
        vertexValues = new float[20*maxSprites];
        ByteBuffer buffer = ByteBuffer.allocateDirect(this.maxSprites * 4 * vertexSize);  // Allocate Buffer for vertices (Max)
        this.vertices = buffer.order(ByteOrder.nativeOrder()).asFloatBuffer();           // Save Vertex Buffer

        buffer = ByteBuffer.allocateDirect(this.maxSprites * 6 * (Short.SIZE / 8));  // Allocate Buffer for Indices (MAX)
        this.indices = buffer.order(ByteOrder.nativeOrder()).asShortBuffer();       // Save Index Buffer

        this.indices.clear();
        for ( int i = 0; i < maxSprites; i++)
        {
            this.indices.put((short) (i*4 + 0));
            this.indices.put((short) (i*4 + 1));
            this.indices.put((short) (i*4 + 2));
            this.indices.put((short) (i*4 + 2));
            this.indices.put((short) (i*4 + 3));
            this.indices.put((short) (i*4 + 0));
        }
    }

    /*
        Function: Load
            Loads the font with the settings set in the constructor
    */
    public boolean Load()
    {
        // Program erstellen
        this.programHandle = GLES20.glCreateProgram();

        // Shader attachen
        GLES20.glAttachShader(this.programHandle, this._LoadShader(GLES20.GL_VERTEX_SHADER, vertexShaderCode));
        GLES20.glAttachShader(this.programHandle, this._LoadShader(GLES20.GL_FRAGMENT_SHADER, fragmentShaderCode));

        GLES20.glBindAttribLocation(this.programHandle, 1, "a_Position");
        GLES20.glBindAttribLocation(this.programHandle, 2, "a_TexCoordinate");
        GLES20.glBindAttribLocation(this.programHandle, 3, "a_MVPMatrixIndex");
        GLES20.glLinkProgram(this.programHandle);

        // Initialize the color and texture handles
        this.mColorHandle = GLES20.glGetUniformLocation(this.programHandle, "u_Color");
        this.mTextureUniformHandle = GLES20.glGetUniformLocation(this.programHandle, "u_Texture");
        this.mMVPMatricesHandle = GLES20.glGetUniformLocation(programHandle, "uMVPMatrix");

        // load the font and setup paint instance for drawing
        Typeface tf = Typeface.createFromAsset(this.assets, this.fontFile);  // Create the Typeface from Font File
        Paint paint = new Paint();                      // Create Android Paint Instance
        paint.setAntiAlias(true);                     // Enable Anti Alias
        paint.setTextSize(this.fontSize);                      // Set Text Size
        paint.setColor(0xffffffff);                   // Set ARGB (White, Opaque)
        paint.setTypeface(tf);                        // Set Typeface

        // get font metrics
        Paint.FontMetrics fm = paint.getFontMetrics();  // Get Font Metrics
        this.charHeight = (float)Math.ceil( Math.abs( fm.bottom ) + Math.abs( fm.top ) );  // Calculate Font Height

        // determine the width of each character (including unknown character)
        // also determine the maximum character width
        char[] s = new char[1];                 // Create Character Array
        this.charWidthMax = 0;                  // Reset Character Width/Height Maximums
        float[] textWidth = new float[1];
        int count = 0;                          // Array Counter

        for (char c = (char) this.CHAR_START; c <= this.CHAR_END; c++)  // FOR Each Character
        {
            s[0] = c;                                    // Set Character
            paint.getTextWidths(s, 0, 1, textWidth);           // Get Character Bounds
            this.charWidths[count] = textWidth[0];      // Get Width

            if (this.charWidths[count] > this.charWidthMax) // IF Width Larger Than Max Width
                this.charWidthMax = this.charWidths[count]; // Save New Max Width
            count++;                                        // Advance Array Counter
        }

        s[0] = (char) this.CHAR_NONE;                               // Set Unknown Character
        paint.getTextWidths(s, 0, 1, textWidth);           // Get Character Bounds
        this.charWidths[count] = textWidth[0];      // Get Width

        if (this.charWidths[count] > this.charWidthMax)           // IF Width Larger Than Max Width
            this.charWidthMax = this.charWidths[count];              // Save New Max Width

        count++;

        // find the maximum size, validate, and setup cell sizes
        this.cellWidth = (int) this.charWidthMax + ( 2 * this.fontPadX );  // Set Cell Width
        this.cellHeight = (int) this.charHeight + ( 2 * this.fontPadY );  // Set Cell Height

        int textureSize =  (int) Math.ceil(Math.sqrt(this.cellWidth * this.cellHeight * count));
        this.columnCount = (int) Math.floor(textureSize/this.cellWidth);               // Calculate Number of Columns
        int rowCount = (int) Math.ceil((float) count/this.columnCount);

        // if the texturesize is too small (because it's calculated using the required area,
        // ignoring that i can't put half a character on one line, and the other half on the next)
        // increase the textureSize by adding one row or one column, depending on what will
        // increase the textureSize by a smaller amount
        if (rowCount > Math.floor(textureSize/this.cellHeight))
            textureSize = Math.min(rowCount * this.cellHeight, (this.columnCount + 1) * this.cellWidth);

        // make the textureSize a power of 2
        textureSize = (int) Math.pow(2, Math.ceil(Math.log(textureSize)/Math.log(2)));
        this.columnCount = (int) Math.floor(textureSize / this.cellWidth);

        // create an empty bitmap (alpha only)
        Bitmap bitmap = Bitmap.createBitmap(textureSize, textureSize, Bitmap.Config.ALPHA_8);  // Create Bitmap
        Canvas canvas = new Canvas( bitmap );           // Create Canvas for Rendering to Bitmap
        bitmap.eraseColor( 0x00000000 );                // Set Transparent Background (ARGB)

        int column = 0;
        int row = 0;
        float textXOffset = this.fontPadX;                             // Set Start Position (X)
        float textYOffset = this.cellHeight - (float) Math.ceil(Math.abs(fm.descent)) - this.fontPadY - 1;  // Set Start Position (Y)
        for (int i = 0; i < this.CHAR_CNT; i++)
        {
            // Set Character to Draw
            if (i == this.CHAR_CNT)
                s[0] = (char) this.CHAR_NONE;
            else
                s[0] = (char) (column + row*this.columnCount + this.CHAR_START);

            // Draw Character
            canvas.drawText(s, 0, 1, textXOffset + column * this.cellWidth, textYOffset + row * this.cellHeight, paint);

            // Create Region for Character
            this.charRgn[i][0] = ((float) column * this.cellWidth) / textureSize;
            this.charRgn[i][1] = (float) row * this.cellHeight / textureSize;
            this.charRgn[i][2] = this.charRgn[i][0] + ((float) (this.cellWidth) / textureSize);
            this.charRgn[i][3] = this.charRgn[i][1] + ((float) (this.cellHeight) / textureSize);

            column++;
            if (column == this.columnCount)
            {
                column = 0;
                row++;
            }
        }

        this.textureId = this.LoadTexture(bitmap);

        // return success
        this.fontLoaded = true;
        return true;                                    // Return Success
    }

    /*
        Function: Begin
            Initiates font-rendering

        Parameter:
            a_red       - float     | Red-part of the desired font-color
            a_green     - float     | Green-part of the desired font-color
            a_blue      - float     | Blue-part of the desired font-color
            a_alpha     - float     | Alpha-part of the desired font-color
            a_vpMatrix  - float[]   | view-projection-matrix to use
    */
    public void Begin(float a_red, float a_green, float a_blue, float a_alpha, float[] a_vpMatrix)
    {
        GLES20.glUseProgram(this.programHandle); // specify the program to use

        // No culling of back faces and Depth-testing
        GLES20.glDisable(GLES20.GL_CULL_FACE);
        GLES20.glDisable(GLES20.GL_DEPTH_TEST);

        // Enable blending
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_ONE, GLES20.GL_ONE);

        // TODO: Alpha seems not to be working yet. Color works
        color[0] = a_red;
        color[1] = a_green;
        color[2] = a_blue;
        color[3] = a_alpha;
        GLES20.glUniform4fv(mColorHandle, 1, color, 0);
        GLES20.glEnableVertexAttribArray(mColorHandle);
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);  // Set the active texture unit to texture unit 0
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureId); // Bind the texture to this unit

        // Tell the texture uniform sampler to use this texture in the shader by binding to texture unit 0
        GLES20.glUniform1i(mTextureUniformHandle, 0);

        this.BeginBatch();                             // Reset Buffer Index (Empty)
        this.mVPMatrix = a_vpMatrix;
    }

    /*
        Function: Draw
            Renders a string

        Parameter:
            a_text          - String    | The String to render
            a_x             - float     | x-position of the bottom-left cordner of the text
            a_y             - float     | y-position of the bottom-left cordner of the text
            a_z             - float     | z-position of the bottom-left cordner of the text
            a_rotateAngleX  - float     | rotates the text around the x-axis by a_rotateAngleX degrees
            a_rotateAngleY  - float     | rotates the text around the y-axis by a_rotateAngleY degrees
            a_rotateAngleZ  - float     | rotates the text around the z-axis by a_rotateAngleZ degrees
    */
    public void Draw(String a_text, float a_x, float a_y, float a_z, float a_rotateAngleX, float a_rotateAngleY, float a_rotateAngleZ)
    {
        float chrHeight = this.cellHeight * this.scaleY;          // Calculate Scaled Character Height
        float chrWidth = this.cellWidth * this.scaleX;            // Calculate Scaled Character Width
        int len = a_text.length();                        // Get String Length
        a_x += ( chrWidth / 2.0f ) - ( this.fontPadX * this.scaleX );  // Adjust Start X
        a_y += ( chrHeight / 2.0f ) - ( this.fontPadY * this.scaleY );  // Adjust Start Y

        // create a model matrix based on x, y and angleDeg
        Matrix.setIdentityM(this.modelMatrix, 0);
        Matrix.translateM(this.modelMatrix, 0, a_x, a_y, a_z);

        if (a_rotateAngleX != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleX, 0, 0, 1);

        if (a_rotateAngleY != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleY, 1, 0, 0);

        if (a_rotateAngleZ != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleZ, 0, 1, 0);

        float letterX = 0;

        chrWidth /= 2;
        chrHeight /= 2;
        for (int i = 0; i < len; i++)
        {
            int c = (int) a_text.charAt(i) - this.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (i < 0 || i >= this.CHAR_CNT)                // IF Character Not In Font
                c = this.CHAR_UNKNOWN;                         // Set to Unknown Character Index

            // Draw the buffer when it's full
            if (numSprites == maxSprites)
            {
                this.EndBatch();
                this.BeginBatch();
            }

            this.vertexValues[bufferIndex++] = letterX - chrWidth;
            this.vertexValues[bufferIndex++] = -chrHeight;
            this.vertexValues[bufferIndex++] = this.charRgn[c][0];
            this.vertexValues[bufferIndex++] = this.charRgn[c][3];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = letterX + chrWidth;
            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = this.charRgn[c][2];
            this.vertexValues[bufferIndex++] = this.charRgn[c][3];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = chrHeight;
            this.vertexValues[bufferIndex++] = this.charRgn[c][2];
            this.vertexValues[bufferIndex++] = this.charRgn[c][1];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 16];
            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = this.charRgn[c][0];
            this.vertexValues[bufferIndex++] = this.charRgn[c][1];
            bufferIndex++;
            numSprites++;

            letterX += (this.charWidths[c] + this.spaceX ) * this.scaleX;    // Advance X Position by Scaled Character Width
        }
    }

    /*
        Function: Draw
            Renders a char-array. Is faster than the string-version, because charAt() isn't needed

        Parameter:
            a_text          - char[]    | The text to render.
            a_x             - float     | x-position of the bottom-left cordner of the text
            a_y             - float     | y-position of the bottom-left cordner of the text
            a_z             - float     | z-position of the bottom-left cordner of the text
            a_rotateAngleX  - float     | rotates the text around the x-axis by a_rotateAngleX degrees
            a_rotateAngleY  - float     | rotates the text around the y-axis by a_rotateAngleY degrees
            a_rotateAngleZ  - float     | rotates the text around the z-axis by a_rotateAngleZ degrees
    */
    public void Draw(char[] a_text, float a_x, float a_y, float a_z, float a_rotateAngleX, float a_rotateAngleY, float a_rotateAngleZ)
    {
        float chrHeight = this.cellHeight * this.scaleY;          // Calculate Scaled Character Height
        float chrWidth = this.cellWidth * this.scaleX;            // Calculate Scaled Character Width
        a_x += ( chrWidth / 2.0f ) - ( this.fontPadX * this.scaleX );  // Adjust Start X
        a_y += ( chrHeight / 2.0f ) - ( this.fontPadY * this.scaleY );  // Adjust Start Y

        // create a model matrix based on x, y and angleDeg
        Matrix.setIdentityM(this.modelMatrix, 0);
        Matrix.translateM(this.modelMatrix, 0, a_x, a_y, a_z);

        if (a_rotateAngleX != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleX, 0, 0, 1);

        if (a_rotateAngleY != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleY, 1, 0, 0);

        if (a_rotateAngleZ != 0)
            Matrix.rotateM(this.modelMatrix, 0, a_rotateAngleZ, 0, 1, 0);

        float letterX = 0;

        chrWidth /= 2;
        chrHeight /= 2;
        for (int i = 0; i < a_text.length; i++)
        {
            int c = (int) a_text[i] - this.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (i < 0 || i >= this.CHAR_CNT)                // IF Character Not In Font
                c = this.CHAR_UNKNOWN;                         // Set to Unknown Character Index

            // Draw the buffer when it's full
            if (numSprites == maxSprites)
            {
                this.EndBatch();
                this.BeginBatch();
            }

            this.vertexValues[bufferIndex++] = letterX - chrWidth;
            this.vertexValues[bufferIndex++] = -chrHeight;
            this.vertexValues[bufferIndex++] = this.charRgn[c][0];
            this.vertexValues[bufferIndex++] = this.charRgn[c][3];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = letterX + chrWidth;
            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = this.charRgn[c][2];
            this.vertexValues[bufferIndex++] = this.charRgn[c][3];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = chrHeight;
            this.vertexValues[bufferIndex++] = this.charRgn[c][2];
            this.vertexValues[bufferIndex++] = this.charRgn[c][1];
            bufferIndex++;

            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 16];
            this.vertexValues[bufferIndex++] = this.vertexValues[bufferIndex - 6];
            this.vertexValues[bufferIndex++] = this.charRgn[c][0];
            this.vertexValues[bufferIndex++] = this.charRgn[c][1];
            bufferIndex++;
            numSprites++;

            letterX += (this.charWidths[c] + this.spaceX ) * this.scaleX;    // Advance X Position by Scaled Character Width
        }
    }

    /*
        Function: End
            Finishes the Batch-rendering of characters and Renders the text to the screen
    */
    public void End()
    {
        this.EndBatch();
        GLES20.glDisableVertexAttribArray(mColorHandle);
    }

    // Stars the batch-rendering
    private void BeginBatch()
    {
        this.numSprites = 0;                                 // Empty Sprite Counter
        this.bufferIndex = 0;
        this.vertices.clear();                          // Remove Existing vertices
    }

    // Finishes the batch-rendering
    private void EndBatch()
    {
        if (this.numSprites <= 0)
            return;

        // bind MVP matrices array to shader
        Matrix.multiplyMM(this.mMVPMatrix, 0, this.mVPMatrix , 0, this.modelMatrix, 0);
        vertices.put(this.vertexValues, 0, this.bufferIndex - 1);
        GLES20.glUniformMatrix4fv(this.mMVPMatricesHandle, 1, false, this.mMVPMatrix, 0);
        GLES20.glEnableVertexAttribArray(this.mMVPMatricesHandle);

        // bind vertex position pointer
        GLES20.glVertexAttribPointer(1, 2, GLES20.GL_FLOAT, false, this.vertexSize, vertices.position(0));
        GLES20.glEnableVertexAttribArray(1);

        // bind texture position pointer
        // Set Vertex Buffer to Texture Coords (NOTE: position based on whether color is also specified)
        GLES20.glVertexAttribPointer(2, 2, GLES20.GL_FLOAT, false, this.vertexSize, vertices.position(2));
        GLES20.glEnableVertexAttribArray(2);

        GLES20.glDrawElements(GLES20.GL_TRIANGLES, this.numSprites * 6, GLES20.GL_UNSIGNED_SHORT, indices.position(0));
        GLES20.glDisableVertexAttribArray(2);
    }

    // Loads the font-texture
    private static int LoadTexture(Bitmap a_bitmap)
    {
        final int[] textureHandle = new int[1];
        GLES20.glGenTextures(1, textureHandle, 0);

        if (textureHandle[0] == 0)
            throw new RuntimeException("Error loading texture.");

        // Bind to the texture in OpenGL
        GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, textureHandle[0]);

        // Set filtering
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_LINEAR);
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);  // Set U Wrapping
        GLES20.glTexParameterf(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);  // Set V Wrapping

        // Load the bitmap into the bound texture.
        GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, a_bitmap, 0);

        // Recycle the bitmap, since its data has been loaded into OpenGL.
        a_bitmap.recycle();
        return textureHandle[0];
    }

    // creates a shader of a given type an compiles a given sourceCode into it
    private static int _LoadShader(int a_type, String a_shaderCode)
    {
        int shader = GLES20.glCreateShader(a_type);
        GLES20.glShaderSource(shader, a_shaderCode);
        GLES20.glCompileShader(shader);
        return shader;
    }

    /*
        Function: TextWidth
            Gets the width of a given text if it were to be drawn with this font.

        Parameter:
            a_text  - String    | The text to render

        Returns:
            Integer -> - The witdth of the text, if it were to be drawn with this font
    */
    public int TextWidth(String a_text)
    {
        this.tempWidth = 0;
        this.tempLen = a_text.length();
        for (int i = 0; i < this.tempLen; i++)
        {
            int c = (int) a_text.charAt(i) - this.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (i < 0 || i >= this.CHAR_CNT)                // IF Character Not In Font
                c = this.CHAR_UNKNOWN;                         // Set to Unknown Character Index

            this.tempWidth += (this.charWidths[c] + this.spaceX ) * this.scaleX;    // Advance X Position by Scaled Character Width
        }
        return tempWidth;
    }

    /*
        Function: TextWidth
            Gets the width of a given text if it were to be drawn with this font.
            Is faster than the string-version, because charAt() isn't needed.

        Parameter:
            a_text  - char[]    | The text to render

        Returns:
            Integer -> - The witdth of the text, if it were to be drawn with this font
    */
    public int TextWidth(char[] a_text)
    {
        this.tempWidth = 0;
        for (int i = 0; i < a_text.length; i++)
        {
            int c = (int) a_text[i] - this.CHAR_START;  // Calculate Character Index (Offset by First Char in Font)
            if (i < 0 || i >= this.CHAR_CNT)                // IF Character Not In Font
                c = this.CHAR_UNKNOWN;                         // Set to Unknown Character Index

            this.tempWidth += (this.charWidths[c] + this.spaceX ) * this.scaleX;    // Advance X Position by Scaled Character Width
        }
        return tempWidth;
    }

    public int TextHeight()
    {
        return this.cellHeight;
    }
}