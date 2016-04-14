package com.libraries.heiko.gamebook.controls;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.tools.Tileset;

/**
 * Created by heiko on 13.04.2016.
 */
public class MapLayer extends GameElement
{
	public int mapWidth, mapHeight;
	private int tileWidth, tileHeight;
	private int cameraX = 0;
	private int cameraY = 0;
	private MapTile[] tiles;
	private MapTile tempTile;
	public MapLayer(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent, int a_mapWidth, int a_mapHeight, int a_tileWidth, int a_tileHeight)
	{
		super(a_id, a_page, a_book, a_parent);
		this.mapWidth = a_mapWidth;
		this.mapHeight = a_mapHeight;
		this.tileWidth = a_tileWidth;
		this.tileHeight = a_tileHeight;
		this.tiles = new MapTile[this.mapWidth * this.mapHeight];
		for (int i = 0; i < tiles.length; i++)
		{
			tempTile = new MapTile(a_id + "_tile" + i, a_page, a_book, this);
			tempTile.SetPosition((i - (i/this.mapWidth)*this.mapWidth) * this.tileWidth, ((i/this.mapWidth) * this.tileHeight));
			tempTile.SetSize(this.tileWidth, this.tileHeight);
			tiles[i] = (MapTile) this.AddChild(tempTile);
		}
	}

	public void SetTile(int a_x, int a_y, Tileset a_tileset, int a_tilesetX, int a_tilesetY)
	{
		tiles[a_x + a_y*this.mapWidth].SetTile(a_tileset, a_tilesetX, a_tilesetY);
	}

	public void SetTileAnimationSpeed(int a_tileX, int a_tileY, int a_millisecPerFrame)
	{
		tiles[a_tileX + a_tileY*this.mapWidth].SetAnimationSpeed(a_millisecPerFrame);
	}
}
