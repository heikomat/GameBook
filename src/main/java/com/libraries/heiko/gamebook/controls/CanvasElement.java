package com.libraries.heiko.gamebook.controls;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Region;

import com.libraries.heiko.gamebook.GAnimation;
import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

/**
 * Created by heiko on 23.02.2016.
 */
public class CanvasElement extends GameElement
{
    private int borderRadius = 0;                       // Current border-radius
    private int backgroundColor = Color.TRANSPARENT;    // Current background-color
    private int borderColor = Color.TRANSPARENT;        // Current border-color
    private int borderWidth = 0;                        // Current border-width
    private Paint paint;                                // Current Paint

    private boolean changed = true;                     // true: the element changed since the las draw, false: the element did not change since the last draw

    // cache-variables to prevent memory-allocations
    private Path lastPath;                              // The last path used to draw the box with round corners
    private Path tempPath;                              // used to cache a path

    public CanvasElement(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
    {
        super(a_id, a_page, a_book, a_parent);
        this.lastPath = new Path();
        this.tempPath = new Path();
        this.paint = new Paint();
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
        Function: DrawBasics
            Draws the box as currently set up

        Parameter:
            a_targetCanvas  - Canvas    | The canvas on which to draw the box
    */
    public void DrawBasics(Canvas a_targetCanvas)
    {
        if (this.borderRadius > 0)
        {
            if (this.changed == true)
                this.lastPath = this._RoundedRect(this.x, this.y, this.width, this.height, this.borderRadius, this.borderRadius);

            this.changed = false;
            this.paint.setStyle(Paint.Style.FILL);
            if (this.backgroundColor != Color.TRANSPARENT)
            {
                this.paint.setColor(this.backgroundColor);
                a_targetCanvas.drawPath(this.lastPath, this.paint);
            }

            if (this.borderColor != Color.TRANSPARENT && this.borderWidth > 0)
            {
                this.paint.setStyle(Paint.Style.STROKE);
                this.paint.setColor(this.borderColor);
                this.paint.setStrokeWidth(this.borderWidth);
                a_targetCanvas.drawPath(this.lastPath, this.paint);
            }
        }
        else
        {
            if (this.backgroundColor != Color.TRANSPARENT)
            {
                this.paint.setStyle(Paint.Style.FILL);
                this.paint.setColor(this.backgroundColor);
                a_targetCanvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, this.paint);
            }

            if (this.borderColor != Color.TRANSPARENT && this.borderWidth > 0)
            {
                this.paint.setStyle(Paint.Style.STROKE);
                this.paint.setColor(this.borderColor);
                this.paint.setStrokeWidth(this.borderWidth);
                a_targetCanvas.drawRect(this.x, this.y, this.x + this.width, this.y + this.height, this.paint);
            }
        }
    }

    // applys the mask of this element, so sub elements won't overflow
    public void _ApplyMask(Canvas a_targetCanvas)
    {
        if (this.borderRadius > 0)
        {
            if (this.changed == true)
                this.lastPath = this._RoundedRect(this.x, this.y, this.width, this.height, this.borderRadius, this.borderRadius);

            this.changed = false;
            a_targetCanvas.clipPath(this.lastPath);
        }
        else
            a_targetCanvas.clipRect(this.x, this.y, this.x + this.width, this.y + this.height, Region.Op.INTERSECT);
    }

    // creates a path for a box with round corners
    public Path _RoundedRect(float a_x, float a_y, float a_width, float a_height, float a_rx, float a_ry)
    {
        this.tempPath.rewind();
        if (a_rx < 0) a_rx = 0;
        if (a_ry < 0) a_ry = 0;
        if (a_rx > a_width/2) a_rx = a_width/2;
        if (a_ry > a_height/2) a_ry = a_height/2;
        float widthWithoutCorners = (a_width - (2 * a_rx));
        float heightWithoutCorners = (a_height - (2 * a_ry));

        this.tempPath.moveTo(a_x + a_width, a_y + a_ry);
        this.tempPath.rQuadTo(0, -a_ry, -a_rx, -a_ry);//top-right corner
        this.tempPath.rLineTo(-widthWithoutCorners, 0);
        this.tempPath.rQuadTo(-a_rx, 0, -a_rx, a_ry); //top-left corner
        this.tempPath.rLineTo(0, heightWithoutCorners);
        this.tempPath.rQuadTo(0, a_ry, a_rx, a_ry);//bottom-left corner
        this.tempPath.rLineTo(widthWithoutCorners, 0);
        this.tempPath.rQuadTo(a_rx, 0, a_rx, -a_ry); //bottom-right corner
        this.tempPath.rLineTo(0, -heightWithoutCorners);

        this.tempPath.close();//Given close, last lineto can be removed.
        return this.tempPath;
    }
}
