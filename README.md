# GameBook
My attempt on an Android-Framework for 2D games

It's currently **under heavy development**, but i'll make a proper readme as soon as it's good eneough to actually do something with it,
but **the general gist is**:
  - It's in OpenGL ES 2, and supports 2D (orthographic) and 3D (perspective) rendering
  - You can switch between 2D (e.g top-down adventure) and 3D (e.g. 2.5D sidescroller) without changing your code
  - You have the GameBook (main) class
  - The GameBook has Pages
  - Pages have Elements
  - Elements have Subelements
  - You have different controls (like Label, Square and Cube), and every Control is a subclass of Element

**Already implemented features**:
  - 2D and 3D rendering
  - Screen-independent game-size
  - Subelement-hierarchy
  - Element-overflow-clipping
  - Font- and bitmap-usage
  - Independent update- and render-threads
  - Resource-manager for easy loading and usage of resources like fonts and images
  - Per-element animation system is already prepared

**Principles for the framework**:
  - **Zero per-frame-memory-allocation** (The framework does not use extra memory to update and draw a frame)
  - Highly optimised. I regularly profile the framework to make sure, that no unnecessary work is done

As stated before, i'll make a proper readme, when the framework has matured a little. But for now, here's my current test-sourcecode, so you can see how i'm currently using it:
```Java
package com.gametest.heiko.gametest;

import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

import com.libraries.heiko.gamebook.*;
import com.libraries.heiko.gamebook.controls.*;
import com.libraries.heiko.gamebook.tools.GameFont;
import com.libraries.heiko.gamebook.tools.Tileset;

public class GameActivity extends Activity
{
	GameBook gb;
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		//turn off title
		requestWindowFeature(Window.FEATURE_NO_TITLE);

		//set fullscreen
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

		// create new GameBook
		this.gb = new GameBook(this);
		this.gb.SetGameOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
		int gameHeight = 1080;
		int gameWidth = this.gb.SetGameHeight(gameHeight);
		setContentView(gb);

		Tileset water = gb.resources.AddTileset("water", R.drawable.water, 128, 128);
		Tileset forest = gb.resources.AddTileset("forest", R.drawable.grassland, 128, 128);
		Tileset waterfall = gb.resources.AddTileset("waterfall", R.drawable.waterfall, 128, 128);
		water.SetAnimationByPattern(0, 4, 4);
		waterfall.SetAnimationByPattern(4, 0, 4);

		for (int i = 0; i < 4; i++)
		{
			for (int j = 0; j < 3; j++)
			{
				waterfall.SetAnimationInfo(i + 12, j, i, j + 3);
				waterfall.SetAnimationInfo(i + 12, j + 3, i, j);
			}
		}

		GamePage p1 = gb.AddPage("Test1", true);
		MapLayer ground = (MapLayer) p1.AddMapLayer("ground", null, 0, 0, (int) Math.ceil((float) gameWidth/128), (int) Math.ceil((float) gameHeight/128), 128, 128);
		MapLayer lvl1 = (MapLayer) p1.AddMapLayer("level_1", null, 0, 0, (int) Math.ceil((float) gameWidth/128), (int) Math.ceil((float) gameHeight/128), 128, 128);
		MapLayer lvl2 = (MapLayer) p1.AddMapLayer("level_2", null, 0, 0, (int) Math.ceil((float) gameWidth/128), (int) Math.ceil((float) gameHeight/128), 128, 128);
		lvl1.SetDrawOrder(1);
		lvl2.SetDrawOrder(2);
		
		for (int i = 0; i < ground.mapWidth; i++)
		{
			for (int j = 0; j < ground.mapHeight; j++)
			{
				ground.SetTile(i, j, forest, 1, 1);
			}
		}

		lvl1.SetTile(0, 0, forest, 9, 5);
		ground.SetTile(1, 0, forest, 0, 2);
		ground.SetTile(2, 0, forest, 4, 1);
		lvl1.SetTile(2, 0, forest, 3, 8);
		lvl2.SetTile(3, 0, forest, 0, 12);
		lvl2.SetTile(4, 0, forest, 1, 12);
		lvl2.SetTile(5, 0, forest, 2, 12);
		lvl2.SetTile(6, 0, forest, 3, 12);
		lvl2.SetTile(7, 0, forest, 4, 12);
		ground.SetTile(7, 0, forest, 10, 8);
		ground.SetTile(8, 0, forest, 12, 8);
		ground.SetTile(9, 0, forest, 12, 8);
		ground.SetTile(10, 0, forest, 13, 8);
		lvl2.SetTile(11, 0, forest, 0, 12);
		lvl2.SetTile(12, 0, forest, 1, 12);
		lvl2.SetTile(13, 0, forest, 2, 12);
		lvl2.SetTile(14, 0, forest, 3, 12);

		lvl1.SetTile(0, 1, forest, 9, 6);
		ground.SetTile(1, 1, forest, 0, 3);
		ground.SetTile(2, 1, forest, 4, 2);
		lvl1.SetTile(3, 1, forest, 13, 4);
		lvl2.SetTile(3, 1, forest, 0, 13);
		lvl2.SetTile(4, 1, forest, 1, 13);
		lvl2.SetTile(5, 1, forest, 2, 13);
		lvl2.SetTile(6, 1, forest, 3, 13);
		lvl2.SetTile(7, 1, forest, 4, 13);
		ground.SetTile(7, 1, forest, 10, 8);
		ground.SetTile(8, 1, forest, 12, 8);
		ground.SetTile(9, 1, forest, 12, 8);
		ground.SetTile(10, 1, forest, 13, 8);
		lvl2.SetTile(11, 1, forest, 0, 13);
		lvl2.SetTile(12, 1, forest, 1, 13);
		lvl2.SetTile(13, 1, forest, 2, 13);
		lvl2.SetTile(14, 1, forest, 3, 13);

		ground.SetTile(1, 2, forest, 0, 1);
		ground.SetTile(2, 2, forest, 4, 1);
		lvl1.SetTile(3, 2, forest, 15, 3);
		lvl2.SetTile(3, 2, forest, 0, 14);
		lvl2.SetTile(4, 2, forest, 1, 14);
		lvl2.SetTile(5, 2, forest, 2, 14);
		lvl2.SetTile(6, 2, forest, 3, 14);
		ground.SetTile(7, 2, forest, 10, 8);
		ground.SetTile(8, 2, forest, 12, 8);
		ground.SetTile(9, 2, forest, 12, 8);
		ground.SetTile(10, 2, forest, 13, 8);
		lvl1.SetTile(11, 2, forest, 0, 14);
		lvl1.SetTile(12, 2, forest, 1, 14);
		lvl1.SetTile(13, 2, forest, 2, 14);
		lvl1.SetTile(14, 2, forest, 3, 14);
		lvl2.SetTile(12, 2, forest, 0, 11);
		lvl2.SetTile(13, 2, forest, 1, 11);
		lvl2.SetTile(14, 2, forest, 2, 11);

		ground.SetTile(0, 3, forest, 2, 1);
		ground.SetTile(1, 3, forest, 0, 3);
		ground.SetTile(2, 3, forest, 4, 1);
		lvl2.SetTile(3, 3, forest, 0, 15);
		lvl2.SetTile(4, 3, forest, 1, 15);
		lvl2.SetTile(5, 3, forest, 2, 15);
		lvl2.SetTile(6, 3, forest, 3, 15);
		ground.SetTile(7, 3, forest, 10, 8);
		ground.SetTile(8, 3, forest, 12, 8);
		ground.SetTile(9, 3, forest, 12, 8);
		ground.SetTile(10, 3, forest, 13, 8);
		lvl1.SetTile(11, 3, forest, 0, 15);
		lvl1.SetTile(12, 3, forest, 1, 15);
		lvl1.SetTile(13, 3, forest, 2, 15);
		lvl1.SetTile(14, 3, forest, 3, 15);
		lvl2.SetTile(12, 3, forest, 0, 12);
		lvl2.SetTile(13, 3, forest, 1, 12);
		lvl2.SetTile(14, 3, forest, 2, 12);

		ground.SetTile(0, 4, forest, 0, 0);
		ground.SetTile(1, 4, forest, 6, 1);
		ground.SetTile(2, 4, forest, 4, 1);
		ground.SetTile(3, 4, forest, 6, 7);
		ground.SetTile(4, 4, forest, 7, 7);
		ground.SetTile(5, 4, forest, 3, 9);
		ground.SetTile(6, 4, forest, 2, 9);
		ground.SetTile(7, 4, waterfall, 0, 0);
		ground.SetTile(8, 4, waterfall, 1, 0);
		ground.SetTile(9, 4, waterfall, 2, 0);
		ground.SetTile(10, 4, waterfall, 3, 0);
		ground.SetTileAnimationSpeed(7, 4, 100);
		ground.SetTileAnimationSpeed(8, 4, 100);
		ground.SetTileAnimationSpeed(9, 4, 100);
		ground.SetTileAnimationSpeed(10, 4, 100);
		ground.SetTile(11, 4, forest, 8, 7);
		ground.SetTile(12, 4, forest, 9, 7);
		lvl2.SetTile(12, 4, forest, 0, 13);
		lvl2.SetTile(13, 4, forest, 1, 13);
		lvl2.SetTile(14, 4, forest, 2, 13);

		ground.SetTile(0, 5, forest, 0, 2);
		ground.SetTile(1, 5, forest, 1, 3);
		ground.SetTile(2, 5, forest, 4, 1);
		ground.SetTile(3, 5, forest, 6, 8);
		lvl1.SetTile(4, 5, forest, 7, 8);
		lvl1.SetTile(5, 5, forest, 3, 10);
		lvl1.SetTile(6, 5, forest, 2, 10);
		lvl1.SetTile(7, 5, waterfall, 0, 1);
		lvl1.SetTile(8, 5, waterfall, 1, 1);
		lvl1.SetTile(9, 5, waterfall, 2, 1);
		lvl1.SetTile(10, 5, waterfall, 3, 1);
		lvl1.SetTileAnimationSpeed(7, 5, 100);
		lvl1.SetTileAnimationSpeed(8, 5, 100);
		lvl1.SetTileAnimationSpeed(9, 5, 100);
		lvl1.SetTileAnimationSpeed(10, 5, 100);
		lvl1.SetTile(11, 5, forest, 8, 8);
		lvl1.SetTile(12, 5, forest, 9, 8);
		lvl2.SetTile(13, 5, forest, 1, 14);
		lvl2.SetTile(14, 5, forest, 2, 14);

		ground.SetTile(0, 6, forest, 0, 3);
		ground.SetTile(1, 6, forest, 2, 3);
		ground.SetTile(2, 6, forest, 4, 2);
		ground.SetTile(3, 6, forest, 4, 7);
		lvl1.SetTile(4, 6, forest, 5, 7);
		lvl2.SetTile(5, 6, forest, 3, 7);
		lvl2.SetTile(6, 6, forest, 3, 7);
		ground.SetTile(7, 6, water, 6, 0);
		ground.SetTile(8, 6, water, 7, 0);
		ground.SetTile(9, 6, water, 7, 0);
		ground.SetTile(10, 6, water, 9, 0);
		ground.SetTileAnimationSpeed(7, 6, 200);
		ground.SetTileAnimationSpeed(8, 6, 200);
		ground.SetTileAnimationSpeed(9, 6, 200);
		ground.SetTileAnimationSpeed(10, 6, 200);
		lvl1.SetTile(11, 6, forest, 0, 8);
		ground.SetTile(12, 6, forest, 1, 8);
		lvl2.SetTile(13, 6, forest, 1, 15);
		lvl2.SetTile(14, 6, forest, 2, 15);
		lvl1.SetTile(7, 6, waterfall, 0, 2);
		lvl1.SetTile(8, 6, waterfall, 1, 2);
		lvl1.SetTile(9, 6, waterfall, 2, 2);
		lvl1.SetTile(10, 6, waterfall, 3, 2);
		lvl1.SetTileAnimationSpeed(7, 6, 100);
		lvl1.SetTileAnimationSpeed(8, 6, 100);
		lvl1.SetTileAnimationSpeed(9, 6, 100);
		lvl1.SetTileAnimationSpeed(10, 6, 100);

		ground.SetTile(0, 7, forest, 0, 3);
		ground.SetTile(1, 7, forest, 2, 3);
		ground.SetTile(2, 7, forest, 4, 1);
		ground.SetTile(3, 7, forest, 4, 7);
		lvl1.SetTile(4, 7, forest, 5, 7);
		lvl1.SetTile(5, 7, forest, 3, 7);
		lvl1.SetTile(6, 7, forest, 3, 7);
		lvl2.SetTile(4, 7, forest, 0, 11);
		lvl2.SetTile(5, 7, forest, 1, 11);
		lvl2.SetTile(6, 7, forest, 2, 11);
		lvl2.SetTile(7, 7, forest, 3, 11);
		lvl2.SetTile(8, 7, forest, 4, 11);
		ground.SetTile(7, 7, water, 6, 1);
		ground.SetTile(8, 7, water, 2, 1);
		ground.SetTile(9, 7, water, 2, 1);
		ground.SetTile(10, 7, water, 9, 1);
		ground.SetTileAnimationSpeed(7, 7, 200);
		ground.SetTileAnimationSpeed(8, 7, 200);
		ground.SetTileAnimationSpeed(9, 7, 200);
		ground.SetTileAnimationSpeed(10, 7, 200);
		lvl1.SetTile(11, 7, forest, 0, 8);
		ground.SetTile(12, 7, forest, 1, 7);

		ground.SetTile(0, 8, forest, 0, 3);
		ground.SetTile(1, 8, forest, 2, 3);
		ground.SetTile(2, 8, forest, 4, 1);
		ground.SetTile(3, 8, forest, 4, 7);
		lvl1.SetTile(4, 8, forest, 5, 7);
		lvl2.SetTile(4, 8, forest, 0, 12);
		lvl2.SetTile(5, 8, forest, 1, 12);
		lvl2.SetTile(6, 8, forest, 2, 12);
		lvl2.SetTile(7, 8, forest, 3, 12);
		lvl2.SetTile(8, 8, forest, 4, 12);
		ground.SetTile(7, 8, water, 6, 1);
		ground.SetTile(8, 8, water, 8, 1);
		ground.SetTile(9, 8, water, 8, 1);
		ground.SetTile(10, 8, water, 9, 1);
		ground.SetTileAnimationSpeed(7, 8, 200);
		ground.SetTileAnimationSpeed(8, 8, 200);
		ground.SetTileAnimationSpeed(9, 8, 200);
		ground.SetTileAnimationSpeed(10, 8, 200);
		lvl1.SetTile(11, 8, forest, 0, 8);
		ground.SetTile(12, 8, forest, 1, 9);
		ground.SetTile(13, 8, forest, 2, 9);
		ground.SetTile(14, 8, forest, 9, 7);
	}
}
```

This creates the following output (image is massively scaled down for the filesizes sake):

![preview](http://dev.wtf/demo.gif)
