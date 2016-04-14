package com.libraries.heiko.gamebook.tools;

import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.controls.MapLayer;
import com.libraries.heiko.gamebook.controls.MapTile;

/**
 * Created by heiko on 12.04.2016.
 * basically just a collection of MapLayer for easy synchronous camera-movement
 */
public class Map
{
	private int mapWidth, mapHeight;
	private int cameraX = 0;
	private int cameraY = 0;
	private GameStack<MapLayer> layer;
	private GameStack<MapLayer> tempLayer;

	public Map(int a_mapWidth, int a_mapHeight)
	{
		this.mapWidth = a_mapWidth;
		this.mapHeight = a_mapHeight;
		this.layer = new GameStack<MapLayer>();
	}

	public void AddLayer(MapLayer a_layer)
	{
		layer.push(a_layer);
	}

	public void SetCameraPos(int a_x, int a_y)
	{
		this.tempLayer = this.layer;
		while (this.tempLayer.content != null)
		{
			this.tempLayer.content.SetPosition(a_x, a_y);
			this.tempLayer = this.tempLayer.next;
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
