package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Freeze extends GameObject {
	private Color flashColour;
	
	public Freeze() {
		super();
		health = -1000;
		texture = new Sprite[] {new Sprite(new Texture("flash.png")), new Sprite(new Texture("flash_light.png"))};
		aSwitchTime = 12;
		flashColour = new Color(1, 0.93f, 0.85f, 0.40f);
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		Generation gen = Main.state.game.generation;
		if (gen.animation.get(i)[Controls.TIMER3] >= aSwitchTime) Main.state.game.delete(gen.uIndexList.get(i));
		if (gen.animation.get(i)[Controls.TIMER3] <= 3) {
			texture[0].setRotation(0);
			super.draw(i, batch, px, py, playerIndex);
		}
		if (gen.animation.get(i)[Controls.INDEX] == 1) gen.animation.get(i)[Controls.TIMER3] += Main.info.getDeltaTime();
	}
	//Renders the explosion
	@Override
	public void renderDraw(int i, ShapeRenderer renderer) {
		Generation gen = Main.state.game.generation;
		if (gen.animation.get(i)[Controls.TIMER3] >= 3) {
			float radius = 100 * (float) Math.sqrt(gen.animation.get(i)[Controls.TIMER3] - 3);
			renderer.setColor(flashColour);
			renderer.circle(gen.coordinates.get(i)[Generation.X] + texture[0].getWidth() / 2, gen.coordinates.get(i)[Generation.Y] + texture[0].getHeight() / 2, radius, 100);
			for (int e = 0; e < gen.manager.enemies.size; e++) {
				int eIndex = gen.uIndexList.indexOf(gen.manager.enemies.get(e));
				float distance = (float) Math.sqrt(Math.pow(gen.coordinates.get(i)[Generation.X] - gen.coordinates.get(eIndex)[Generation.X], 2) + Math.pow(gen.coordinates.get(i)[Generation.Y] - gen.coordinates.get(eIndex)[Generation.Y], 2));
				if (distance < radius) {
					gen.manager.targets.get(e)[Generation.X] = (short) (gen.coordinates.get(eIndex)[Generation.X] / Game.size);
					gen.manager.targets.get(e)[Generation.Y] = (short) (gen.coordinates.get(eIndex)[Generation.Y] / Game.size);
				}
			}
		}
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.metal.index] >= 1 && itemCount[GameObject.stick.index] >= 9 && itemCount[GameObject.match.index] >= 4;
		return has ? 1 : 0;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.metal.index] -= 1;
		itemCount[GameObject.stick.index] -= 9;
		itemCount[GameObject.match.index] -= 4;
		itemCount[index] += 1;
	}
}