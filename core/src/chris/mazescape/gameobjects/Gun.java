package chris.mazescape.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Gun extends GameObject {
	public Gun() {
		super();
		health = -1000;
		texture = new Sprite[] {new Sprite(new Texture("gun.png")), new Sprite(new Texture("gunfire.png"))};
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		int tIndex = (int) Main.state.game.generation.animation.get(i)[Controls.INDEX];
		texture[tIndex].setOriginCenter();
		texture[tIndex].setRotation(Main.state.game.generation.animation.get(i)[Controls.ROTATION]);
		super.draw(i, batch, px, py, playerIndex);
	}
	//Use this method to make an enemy or player carry an object. To draw gun on the ground, call above method
	public void draw(SpriteBatch batch, float px, float py, int playerIndex, int objectIndex, GameObject o, GameObject drawObject) {
		byte textureIndex = 0;
		Generation generation = Main.state.game.generation;
		if (drawObject == GameObject.gun && generation.animation.get(objectIndex)[Controls.TIMER3] > 0) {
			textureIndex = 1;
			generation.animation.get(objectIndex)[Controls.TIMER3] -= Gdx.graphics.getDeltaTime();
		}
		drawObject.texture[textureIndex].setOriginCenter();
		int rotation = (int) generation.animation.get(objectIndex)[Controls.ROTATION];
		float[] location = generation.manager.getShotPos(objectIndex, true);
		drawObject.texture[textureIndex].setPosition(location[Generation.X], location[Generation.Y]);
		drawObject.texture[textureIndex].setRotation(rotation);
		float alpha = 1;
		if (Main.state.game.mission.isDark) {
			alpha = Main.state.game.draw.getAlpha(generation.coordinates.get(objectIndex)[Generation.X] / Game.size, px, generation.coordinates.get(objectIndex)[Generation.Y] / Game.size, py, playerIndex);
		}
		drawObject.texture[textureIndex].setColor(0, 0, 0, 1);
		drawObject.texture[textureIndex].draw(batch);
		drawObject.texture[textureIndex].setColor(1, 1, 1, alpha);
		drawObject.texture[textureIndex].draw(batch);
	}
}