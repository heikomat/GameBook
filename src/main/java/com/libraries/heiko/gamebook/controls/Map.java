package com.libraries.heiko.gamebook.controls;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;

/**
 * Created by heiko on 12.04.2016.
 */
public class Map extends BaseSquare
{
	private int mapWidth, mapHeight, layerCount;
	private int cameraX = 0;
	private int cameraY = 0;
	private Sheet[][][] tiles;
	private boolean mapReady = false;
	public Map(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent, int a_mapWidth, int a_mapHeight, int a_layerCount)
	{
		super(a_id, a_page, a_book, a_parent);
		this.mapWidth = a_mapWidth;
		this.mapHeight = a_mapHeight;
		this.layerCount = a_layerCount;
		this.tiles = new Sheet[this.layerCount][this.mapWidth][this.mapHeight];
		// TODO: Implement map with layers, tiles and collision-info
	}

	@Override
	protected void _Draw(float[] a_mvpMatrix)
	{
		if (this.mapReady != true)
			return;

		// TODO: make use of the cameraX and cameraY, and only draw the necessary tiles
		// TODO: based on size and position (as this is still a subclass of GameElement)
		for (int i = 0; i < tiles.length; i++)
		{
			for (int j = 0; j < this.mapWidth; j++)
			{
				for (int k = 0; k < this.mapHeight; k++)
				{
					this.tiles[i][j][k].DrawBasics(a_mvpMatrix);
				}
			}
		}
	}

	public void Load(String a_mapInfo)
	{
		// TODO: implement map-loading
	}

	public String Save()
	{
		// TODO: implement map-saving
		return "";
	}
}
