package com.libraries.heiko.gamebook.tools;

/**
 * Created by heiko on 13.04.2016.
 */
public class Position
{
	public int x;
	public int y;
	public Position(int a_x, int a_y)
	{
		this.x = a_x;
		this.y = a_y;
	}

	public void SetPosition(int a_x, int a_y)
	{
		this.x = a_x;
		this.y = a_y;
	}
}
