package com.libraries.heiko.gamebook.controls;

import android.graphics.Color;
import android.graphics.Paint;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

/**
 * Created by heiko on 19.02.2016.
 */
public class Label extends BaseElement
{
    private Paint paint;                                // current Paint
    private int fontSize = 75;                          // current font-size
    private int fontColor = Color.WHITE;                // current font-color

    public Label(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this._Init("");
    }

    public Label(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent, String a_text)
    {
        super(a_id, a_page, a_book, a_parent);
        this._Init(a_text);
    }

    // Initializes the default values of the label
    public void _Init(String a_text)
    {
        this.paint = new Paint();
        this.paint.setStyle(Paint.Style.FILL);
        this.SetStyle(this.fontSize, "#FFFFFF");
        this.value = a_text;
    }

    /*
        Function: SetStyle
            Sets the style of the label

        Parameter:
            a_fontSize  - int           | the font-size the label will use
            a_fontColor - String        | the font-color the label will have (e.g. "#FFFFFF")
    */
    public void SetStyle(int a_fontSize, String a_fontColor)
    {
        this.fontSize = a_fontSize;
        this.fontColor = Color.parseColor(a_fontColor);
    }

    // Draws the Label on the framebuffer
    public void _Draw(int a_shaderProgram)
    {
        // TODO: Draw the text
    }
}
