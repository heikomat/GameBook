package com.libraries.heiko.gamebook.controls;

import android.graphics.Bitmap;
import android.graphics.Color;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.tools.GameBackground;

/**
 * Created by heiko on 23.02.2016.
 */
public class BaseElement extends GameElement
{
    private int borderRadius = 0;                       // Current border-radius
    private int backgroundColor = Color.TRANSPARENT;    // Current background-color
    private int borderColor = Color.TRANSPARENT;        // Current border-color
    private int borderWidth = 0;                        // Current border-width
    public GameBackground background;                   // Current background
    private boolean changed = true;                     // true: the element changed since the las draw, false: the element did not change since the last draw

    public BaseElement(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this.background = new GameBackground();
        this.SetBoxStyle(this.borderRadius, null, null, this.borderWidth);
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
            this.backgroundColor = Color.parseColor(a_backgroundColor);

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
    public void DrawBasics(int a_shaderProgram)
    {
        this.changed = false;
        // TODO: Draw the box with border

        this.background.Draw(a_shaderProgram, this.x, this.y, this.width, this.height);
    }

    // apply the mask of this element, so sub elements won't overflow
    public void _ApplyMask(int a_shaderProgram)
    {
        // TODO: Apply some kind of mask
    }
}
