package com.libraries.heiko.gamebook.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

/**
 * Created by heiko on 19.02.2016.
 */
public class Label extends CanvasElement
{
    private Paint paint;                                // current Paint
    private int fontSize = 75;                          // current font-size
    private int fontColor = Color.WHITE;                // current font-color
    private Paint.Align textAlign = Paint.Align.LEFT;   // current text-alignment

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
        this.SetStyle(this.fontSize, "#FFFFFF", Paint.Align.LEFT);
        this.value = a_text;
    }

    /*
        Function: SetStyle
            Sets the style of the label

        Parameter
            a_fontSize  - int           | the font-size the label will use
            a_fontColor - String        | the font-color the label will have (e.g. "#FFFFFF")
            a_textAlign - Paint.Align   | the text-align the label will have (e.g. Paint.Align.LEFT);
    */
    public void SetStyle(int a_fontSize, String a_fontColor, Paint.Align a_textAlign)
    {
        this.fontSize = a_fontSize;
        this.fontColor = Color.parseColor(a_fontColor);
        this.textAlign = a_textAlign;

        this.paint.setColor(this.fontColor);
        this.paint.setTextSize(this.fontSize);
        this.paint.setTextAlign(this.textAlign);
    }

    // Draws the Label on the framebuffer
    public void _Draw(Canvas a_targetCanvas)
    {
        a_targetCanvas.drawText((String) this.value, this.x, this.y, this.paint);
    }
}
