package com.libraries.heiko.gamebook.controls;

import com.libraries.heiko.gamebook.GAnimation;
import com.libraries.heiko.gamebook.GameBook;
import com.libraries.heiko.gamebook.GameElement;
import com.libraries.heiko.gamebook.GamePage;
import com.libraries.heiko.gamebook.tools.Position;
import com.libraries.heiko.gamebook.tools.Tileset;

/**
 * Created by heiko on 13.04.2016.
 */
public class MapTile extends BaseSquare
{
	Tileset tileset;
	Position tilePosition;
	long lastAnimationUpdate;
	int frameDuration = 0;

	public MapTile(String a_id, GamePage a_page, GameBook a_book, GameElement a_parent)
	{
		super(a_id, a_page, a_book, a_parent);
		this.lastAnimationUpdate = 0;
		this.tilePosition = new Position(-1, -1);
		// TODO: Implement map with layers, tiles and collision-info
	}

	@Override
	public void SetTile(Tileset a_tileset, int a_x, int a_y)
	{
		this.tileset = a_tileset;
		this.tilePosition.SetPosition(a_x, a_y);
		super.SetTile(this.tileset, a_x, a_y);
	}

	public void SetTile(Tileset a_tileset, Position a_position)
	{
		this.tileset = a_tileset;
		this.tilePosition.SetPosition(a_position.x, a_position.y);
		super.SetTile(this.tileset, this.tilePosition.x, this.tilePosition.y);
	}

	@Override
	public void SetTilePosition(int a_x, int a_y)
	{
		this.tilePosition.SetPosition(a_x, a_y);
		super.SetTilePosition(this.tilePosition.x, this.tilePosition.y);
	}

	public void SetTilePosition(Position a_position)
	{
		this.tilePosition.SetPosition(a_position.x, a_position.y);
		super.SetTilePosition(this.tilePosition.x, this.tilePosition.y);
	}

	public void SetAnimationSpeed(int a_millisecPerFrame)
	{
		this.frameDuration = a_millisecPerFrame;
		if (this.frameDuration == 0)
			this.needsUpdate = false;
		else
			this.EnableUpdating();
	}

	// Draws the Tile on the framebuffer
	@Override
	protected void _Draw(float[] a_mvpMatrix)
	{
		this.DrawBasics(a_mvpMatrix);
	}

	@Override
	protected void _Update(long a_timeDelta, double a_timeFactor, long a_timePassed)
	{
		if (this.tileset == null || this.tileset.animationInfo[this.tilePosition.x][this.tilePosition.y].x < 0 || this.tileset.animationInfo[this.tilePosition.x][this.tilePosition.y].y < 0 || this.frameDuration <= 0)
			return;

		if ((a_timePassed - this.lastAnimationUpdate)/1000000 >= this.frameDuration)
		{
			this.SetTilePosition(this.tileset.animationInfo[this.tilePosition.x][this.tilePosition.y]);
			this.lastAnimationUpdate = a_timePassed;
		}
	}
}
