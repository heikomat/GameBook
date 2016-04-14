package com.libraries.heiko.gamebook.tools;

import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLUtils;

/**
 * Created by heiko on 11.04.2016.
 */
public class Tileset
{
	// TODO: Things are too public here. make them more private
	public float widthRatio, heightRatio;
	public Bitmap tileImage;
	public int tileWidth, tileHeight, columns, rows;
	public Position[][] animationInfo;
	public boolean textureCreated = false;
	public int[] textureIDs = new int[1];													// Array holding the pointer to the background-texture

	boolean[][] collisionInfo;
	private boolean temp;
	public Tileset(Bitmap a_image, int a_tileWidth, int a_tileHeight)
	{
		this.tileImage = a_image;
		this.tileWidth = a_tileWidth;
		this.tileHeight = a_tileHeight;
		this.widthRatio = this.tileImage.getWidth() / this.tileWidth;
		this.heightRatio = this.tileImage.getHeight() / this.tileHeight;
		this.columns = (int) ((float) this.tileImage.getWidth()/this.tileWidth);
		this.rows = (int) ((float) this.tileImage.getHeight()/this.tileHeight);
		this.collisionInfo = new boolean[this.columns][this.rows];
		this.animationInfo = new Position[this.columns][this.rows];
		for (int i = 0; i < this.columns; i++)
		{
			for (int j = 0; j < this.rows ; j++)
			{
				this.animationInfo[i][j] = new Position(-1, -1);
			}
		}
	}

	public void CreateTexture()
	{
		// Bind texture to texturename
		GLES20.glGenTextures(1, this.textureIDs, 0);
		GLES20.glBindTexture(GLES20.GL_TEXTURE_2D, this.textureIDs[0]);

		// Set filtering
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MIN_FILTER, GLES20.GL_NEAREST);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_MAG_FILTER, GLES20.GL_NEAREST);

		// Set wrapping mode
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_S, GLES20.GL_CLAMP_TO_EDGE);
		GLES20.glTexParameteri(GLES20.GL_TEXTURE_2D, GLES20.GL_TEXTURE_WRAP_T, GLES20.GL_CLAMP_TO_EDGE);

		// Load the bitmap into the bound texture.
		GLUtils.texImage2D(GLES20.GL_TEXTURE_2D, 0, this.tileImage, 0);
		this.textureCreated = true;
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
		Function: SetCollisionInfo
			Sets the position of the next animation-tile for this tile

		Parameter:
			a_x		- Integer	| x-position of the tile
			a_y		- Integer	| y-position of the tile
			a_nextX	- Integer	| x-position of the tile that represents the next animation-frame
			a_nextY	- Integer	| y-position of the tile that represents the next animation-frame
	*/
	public void SetAnimationInfo(int a_x, int a_y, int a_nextX, int a_nextY)
	{
		if (a_x >= this.columns || a_y >= this.rows)
			return;

		this.animationInfo[a_x][a_y].SetPosition(a_nextX, a_nextY);
	}

	/*
		Function: SetAnimationInfo
			Sets the animationInfo of this tileset

		Parameter:
			a_collisionInfo	- boolean[][]	| booleanArray with the collision-info
	*/
	public void SetAnimationInfo(Position[][] a_animationInfo)
	{
		for (int i = 0; i < this.columns && i < a_animationInfo.length; i++)
		{
			for (int j = 0; j < this.rows && j < a_animationInfo[0].length; j++)
			{
				this.animationInfo[i][j] = a_animationInfo[i][j];
			}
		}
	}

	/*
		Function: SetAnimationByPattern
			Sets the animationInfo of this tileset using a pattern

		Parameter:
			a_xDifference	- Integer	| the difference in x-position between a tile and its next frame
			a_yDifference	- Integer	| the difference in y-position between a tile and its next frame
			a_frameCount	- Integer	| the number of frames per animation
	*/
	public void SetAnimationByPattern(int a_xDifference, int a_yDifference, int a_frameCount)
	{
		for (int i = 0; i < this.columns; i++)
		{
			for (int j = 0; j < this.rows; j++)
			{
				this.animationInfo[i][j].SetPosition(-1, -1);
			}
		}

		for (int i = 0; i < this.columns; i++)
		{
			for (int j = 0; j < this.rows; j++)
			{
				temp = true;
				for (int k = 0; k < a_frameCount - 1 && temp == true; k++)
				{
					// if there already is an animation, or the target-tile is out of bounds, the animation is invalid
					if (this.animationInfo[i + k*a_xDifference][j + k*a_yDifference].x >= 0 ||
						this.animationInfo[i + k*a_xDifference][j + k*a_yDifference].y >= 0 ||
						i + (k + 1)*a_xDifference >= this.columns || j + (k + 1)*a_yDifference >= this.rows)
					{
						temp = false;
						break;
					}

					this.animationInfo[i + k*a_xDifference][j + k*a_yDifference].SetPosition(i + (k + 1)*a_xDifference, j + (k + 1)*a_yDifference);
				}

				// the last frame loops back to the first one
				if (temp == true && this.animationInfo[i + (a_frameCount - 1)*a_xDifference][j + (a_frameCount - 1)*a_yDifference].x < 0 &&
									this.animationInfo[i + (a_frameCount - 1)*a_xDifference][j + (a_frameCount - 1)*a_yDifference].y < 0)
				{
					this.animationInfo[i + (a_frameCount - 1) * a_xDifference][j + (a_frameCount - 1) * a_yDifference].SetPosition(i, j);
				}
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
