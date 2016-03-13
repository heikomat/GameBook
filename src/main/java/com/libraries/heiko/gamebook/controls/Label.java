package com.libraries.heiko.gamebook.controls;

import android.graphics.Color;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.tools.GameFont;

/**
 * Created by heiko on 19.02.2016.
 */
public class Label extends BaseElement
{
    private GameFont font;                    // current font
    private int fontColor = Color.WHITE;    // current font-color

    public Label(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent, GameFont a_font)
    {
        super(a_id, a_page, a_book, a_parent);
        this._Init(a_font, "");
    }

    public Label(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent, GameFont a_font, String a_text)
    {
        super(a_id, a_page, a_book, a_parent);
        this._Init(a_font, a_text);
    }

    // Initializes the default values of the label
    public void _Init(GameFont a_font, String a_text)
    {
        this.font = a_font;
        this.SetColor("#FFFFFF");
        this.value = a_text;
    }

    /*
        Function: SetStyle
            Sets the style of the label

        Parameter:
            a_fontSize  - int           | the font-size the label will use
            a_fontColor - String        | the font-color the label will have (e.g. "#FFFFFF")
    */
    public void SetColor(String a_fontColor)
    {
        this.fontColor = Color.parseColor(a_fontColor);
    }

    // Draws the Label on the framebuffer
    public void _Draw(float[] a_mvpMatrix)
    {
        if (this.font.fontLoaded == false)
            return;

        this.font.Begin( 1.0f, 1.0f, 1.0f, 1.0f, a_mvpMatrix );             // Begin Text Rendering (Set Color BLUE)
        this.font.Draw( (String) this.value, this.x, this.y, 0, 0, 0, 0);   // Draw Test String
        this.font.End();
    }
}
