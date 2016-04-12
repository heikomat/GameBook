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
import com.libraries.heiko.gamebook.controls.BaseSquare;
import com.libraries.heiko.gamebook.controls.Sheet;
import com.libraries.heiko.gamebook.tools.GameFont;

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
		this.gb.SetGameOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		this.gb.SetGameWidth(720);
		setContentView(gb);

		gb.resources.AddImage("testImage", R.drawable.test80);
		gb.resources.AddImage("rauchen", R.drawable.rauchen);
		gb.resources.AddTileset("testTiles", R.drawable.test80, 40, 40);

		GameFont font = gb.resources.AddFont("roboto", "roboto.ttf", 25);
		GamePage p1 = gb.AddPage("Test1", true);

		String parent = null;
		for (int i = 0; i < 60; i++)
		{
			p1.AddLabel("Test1_" + i, null, 25 + i * 5, 500 + i * 5, font, "Hallo, das ist ein Test");
			if (i == 0)
				((BaseSquare) p1.GetElement("Test1_" + i)).SetBoxStyle(0, "#BB00FF00", "#FF9999FF", 1);

			Sheet sheet = (Sheet) p1.AddSheet("TestSheet_" + i, parent, 5, 10, 80, 80);
			if (i == 0)
			{
				sheet.HideOverflow(true);
				sheet.SetBoxStyle(0, "#33FF0000", "#FF9999FF", 1);
				sheet.SetBackground(gb.resources.GetImage("rauchen"));
				sheet.SetSize(400, 400);
				sheet.SetBackgroundDimensions(50, 50, 300, 300);
				sheet.SetPosition(5, 5);
			}
			else
			{
				sheet.SetTile(gb.resources.GetTileset("testTiles"), 1, 0);
				sheet.SetBoxStyle(0, "#3300FFFF", "#FF9999FF", 1);
			}
			parent = "TestSheet_" + i;
		}

		gb.gameRenderer.SetRenderMode(GameRenderer.RenderMode.THREED);
		Sheet sheet = (Sheet) p1.AddSheet("TestSheet_7000", "TestSheet_0", 50, 282, 160, 160);
		sheet.SetBackground(gb.resources.GetImage("testImage"));
		sheet.SetBackgroundSize(35, 35);
		sheet.SetBackgroundRepeat(true);
	}
}
```

This creates the following output (currently every Element is rotating around itself in all three axis, due to a demo-code in the GAnimation-class. That demo-code will be removed in the future):

![preview](http://dev.wtf/demo.png)
