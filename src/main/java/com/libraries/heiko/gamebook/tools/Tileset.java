package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;

/**
 * Created by heiko on 11.04.2016.
 */
public class Tileset
{
	public Bitmap tileImage;
	public int tileWidth, tileHeight, columns, rows;
	public float widthRatio, heightRatio;
	public boolean[][] collisionInfo;
	public Tileset(Bitmap a_image, int a_tileWidth, int a_tileHeight)
	{
		this.tileImage = a_image;
		this.tileWidth = a_tileWidth;
		this.tileHeight = a_tileHeight;
		this.widthRatio = this.tileImage.getWidth() / this.tileWidth;
		this.heightRatio = this.tileImage.getWidth() / this.tileHeight;
		this.columns = (int) ((float) this.tileImage.getWidth()/this.tileWidth);
		this.rows = (int) ((float) this.tileImage.getHeight()/this.tileHeight);
		this.collisionInfo = new boolean[this.columns][this.rows];
	}

	/*
		Function: SetCollisionInfo
			Sets wether a tile in this tileset is solid or not

		Parameter:
			a_x		- Integer	| x-position of the tile
			a_y		- Integer	| y-position of the tile
			a_solid	- boolean	| true: the tile will be solid, false: the tile will not be solid
	*/
	public void SetCollisionInfo(int a_x, int a_y, boolean a_solid)
	{
		if (a_x >= this.columns || a_y >= this.rows)
			return;

		this.collisionInfo[a_x][a_y] = a_solid;
	}

	/*
		Function: SetCollisionInfo
			Sets the collisionInfo of this tileset

		Parameter:
			a_collisionInfo	- boolean[][]	| booleanArray with the collision-info
	*/
	public void SetCollisionInfo(boolean[][] a_collisionInfo)
	{
		for (int i = 0; i < this.columns && i < a_collisionInfo.length; i++)
		{
			for (int j = 0; j < this.rows && j < a_collisionInfo[0].length; j++)
			{
				this.collisionInfo[i][j] = a_collisionInfo[i][j];
			}
		}
	}

	/*
		Function: GetXPosition
			retrieves the x-position of a tile in pixel

		Parameter:
			a_x	- Integer	| x-index of the tile
	*/
	public int GetXPosition(int a_x)
	{
		return this.tileWidth * a_x;
	}

	/*
		Function: GetYPosition
			retrieves the y-position of a tile in pixel

		Parameter:
			a_y	- Integer	| y-index of the tile
	*/
	public int GetYPosition(int a_y)
	{
		return this.tileHeight * a_y;
	}
}
