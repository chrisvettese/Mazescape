package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Flashlight extends GameObject {
	public Flashlight() {
		super();
		health = -1000;
		solid = false;
		texture = new Sprite[] {new Sprite(new Texture("flashlight.png"))};
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		Generation generation = Main.state.game.generation;
		generation.coordinates.get(i)[Generation.X] = px * Game.size;
		generation.coordinates.get(i)[Generation.Y] = py * Game.size;
		texture[0].setRotation(generation.animation.get(playerIndex)[Controls.ROTATION]);
		super.draw(i, batch, px, py, playerIndex);
	}
}