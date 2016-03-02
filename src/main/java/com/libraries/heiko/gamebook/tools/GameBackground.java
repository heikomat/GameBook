package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;

/**
 * Created by heiko on 28.02.2016.
 */
public class GameBackground
{
    public Bitmap image;
    Bitmap buffer;
    Canvas bufferCanvas;
    private int x = 0;
    private int y = 0;
    private int width = 0;
    private int height = 0;
    private boolean changed = false;

    private int posX = 0;
    private int posY = 0;
    private Rect dst = new Rect(0, 0, 0, 0);
    private Rect src = new Rect(0, 0, 0, 0);
    private BackgroundMode mode = BackgroundMode.TILE;

    private int xCount = 0;
    private int yCount = 0;
    private int horzCounter = 0;
    private int vertCounter = 0;

    private int srcX1 = 0;
    private int srcY1 = 0;
    private int srcX2 = 0;
    private int srcY2 = 0;
    private int dstX1 = 0;
    private int dstY1 = 0;
    private int dstX2 = 0;
    private int dstY2 = 0;

    public void MoveBy(int a_x, int a_y)
    {
        this.posX += a_y;
        this.posY += a_y;
        this.CheckBackgroundPos();
    }

    public void MoveTo(int a_x, int a_y)
    {
        this.posX = a_y;
        this.posY = a_y;
        this.CheckBackgroundPos();
    }

    private void CheckBackgroundPos()
    {
        if (this.image == null)
            return;

        if (this.mode == BackgroundMode.TILE)
        {
            if (this.posX > 0)
                this.posX = -(this.image.getWidth() - this.posX);

            if (this.posY > 0)
                this.posY = -(this.image.getHeight() - this.posY);

            if (this.posX + this.image.getWidth() <= 0)
                this.posX = this.posX%(-this.image.getWidth());

            if (this.posY + this.image.getWidth() <= 0)
                this.posY = this.posY%(-this.image.getHeight());
        }
        else if (this.mode == BackgroundMode.SINGLE)
            this.dst.set(this.x, this.y, this.image.getWidth(), this.image.getHeight());
        else if (this.mode == BackgroundMode.STRETCH)
            this.dst.set(this.x, this.y, this.width, this.height);

        this.changed = true;
    }

    public void SetBitmap(Bitmap a_bitmap)
    {
        image = a_bitmap;
        this.CheckBackgroundPos();
    }

    public void SetBackgroundMode(BackgroundMode a_mode)
    {
        this.mode = a_mode;
        this.CheckBackgroundPos();
    }

    public void SetBounds(int a_x, int a_y, int a_width, int a_height)
    {
        this.x = a_x;
        this.y = a_y;
        this.width = a_width;
        this.height = a_height;
        this.buffer = Bitmap.createBitmap(a_width, a_height, Bitmap.Config.RGB_565);
        if (this.bufferCanvas == null)
            this.bufferCanvas = new Canvas(this.buffer);

        this.CheckBackgroundPos();
    }

    public void Draw(int a_shaderProgram, int a_x, int a_y, int a_width, int a_height)
    {
        if (image == null)
            return;

        // TODO: Draw the background as a texture. Probably need the texture-target as parameter
        /*
        if (this.changed)
        {
            if (this.mode == BackgroundMode.SINGLE || this.mode == BackgroundMode.STRETCH)
                a_targetCanvas.drawBitmap(this.image, null, this.dst, null);
            else
            {
                this.xCount = (int) Math.ceil(((float) this.width - this.posX) / this.image.getWidth());
                this.yCount = (int) Math.ceil(((float) this.height - this.posY) / this.image.getHeight());
                for (this.horzCounter = 0; this.horzCounter < this.xCount; this.horzCounter++)
                {
                    this.srcX1 = 0;
                    this.dstX1 = this.posX + this.horzCounter * this.image.getWidth();

                    if (this.horzCounter == 0)
                    {
                        this.dstX1 = 0;
                        this.srcX1 = -this.posX;
                    }

                    this.srcX2 = this.image.getWidth();
                    this.dstX2 = this.dstX1 + (this.srcX2 - this.srcX1);
                    if (this.horzCounter == this.xCount - 1)
                    {
                        this.srcX2 = (this.width - (this.posX + this.horzCounter * this.image.getWidth())) - this.srcX1;
                        this.dstX2 = this.width;
                    }

                    for (this.vertCounter = 0; this.vertCounter < this.yCount; this.vertCounter++)
                    {
                        this.srcY1 = 0;
                        this.dstY1 = this.posY + this.vertCounter * this.image.getHeight();

                        if (this.vertCounter == 0)
                        {
                            this.dstY1 = 0;
                            this.srcY1 = -this.posY;
                        }

                        this.srcY2 = this.image.getHeight();
                        this.dstY2 = this.dstY1 + (this.srcY2 - this.srcY1);
                        if (this.vertCounter == this.yCount - 1)
                        {
                            this.srcY2 = (this.height - (this.posY + this.vertCounter * this.image.getHeight())) - this.srcY1;
                            this.dstY2 = this.height;
                        }

                        this.src.set(this.srcX1, this.srcY1, this.srcX2, this.srcY2);
                        this.dst.set(this.dstX1, this.dstY1, this.dstX2, this.dstY2);
                        this.bufferCanvas.drawBitmap(this.image, this.src, this.dst, null);
                    }
                }
            }
        }

        a_targetCanvas.drawBitmap(this.buffer, this.x, this.y, null);
        */
        this.changed = false;
    }
}

