package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;

/**
 * Created by heiko on 11.04.2016.
 */
public class Tileset
{
	public Bitmap tileImage;
	public int tileWidth, tileHeight;
	public float widthRatio, heightRatio;
	public Tileset(Bitmap a_image, int a_tileWidth, int a_tileHeight)
	{
		this.tileImage = a_image;
		this.tileWidth = a_tileWidth;
		this.tileHeight = a_tileHeight;
		this.widthRatio = this.tileImage.getWidth() / this.tileWidth;
		this.heightRatio = this.tileImage.getWidth() / this.tileHeight;
	}

	public int GetXPosition(int a_x)
	{
		return this.tileWidth * a_x;
	}

	public int GetYPosition(int a_y)
	{
		return this.tileHeight * a_y;
	}
}
