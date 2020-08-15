package chris.mazescape.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Explosive extends GameObject {
	private Color[] explode;
	
	public Explosive() {
		super();
		health = -1000;
		texture = new Sprite[] {new Sprite(new Texture("explosive.png")), new Sprite(new Texture("explosive_light.png"))};
		aSwitchTime = 8;
		explode = new Color[] {new Color(1, 0.1f, 0.1f, 0.25f), new Color(1, 0.5f, 0.1f, 0.25f)};
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		Generation gen = Main.state.game.generation;
		if (gen.animation.get(i)[Controls.TIMER3] >= aSwitchTime) Main.state.game.delete(gen.uIndexList.get(i));
		if (gen.animation.get(i)[Controls.TIMER3] <= 3) {
			texture[0].setRotation(0);
			super.draw(i, batch, px, py, playerIndex);
		}
		if (gen.animation.get(i)[Controls.INDEX] == 1) gen.animation.get(i)[Controls.TIMER3] += Gdx.graphics.getDeltaTime();
	}
	@Override
	public void renderDraw(int i, ShapeRenderer renderer) {
		Generation gen = Main.state.game.generation;
		if (gen.animation.get(i)[Controls.TIMER3] >= 3) {
			float radius = 140 * (float) Math.sqrt(gen.animation.get(i)[Controls.TIMER3] - 3);
			int colour = 0;
			do {
				if (colour == 0) colour = 1;
				else colour = 0;
				renderer.setColor(explode[colour]);
				float tempRadius = radius + Generation.randomInt(12, 0) - 6;
				if (tempRadius <= 0) tempRadius = 1;
				renderer.circle(gen.coordinates.get(i)[Generation.X] + texture[0].getWidth() / 2, gen.coordinates.get(i)[Generation.Y] + texture[0].getHeight() / 2, tempRadius, 100);
				radius -= 10;
			} while (radius > 6);
			radius = 140 * (float) Math.sqrt(gen.animation.get(i)[Controls.TIMER3] - 3);
			for (int e = 0; e < gen.manager.enemies.size; e++) {
				int eIndex = gen.uIndexList.indexOf(gen.manager.enemies.get(e));
				float distance = (float) Math.sqrt(Math.pow(gen.coordinates.get(i)[Generation.X] - gen.coordinates.get(eIndex)[Generation.X], 2) + Math.pow(gen.coordinates.get(i)[Generation.Y] - gen.coordinates.get(eIndex)[Generation.Y], 2));
				if (distance < radius) {
					gen.health.set(eIndex, gen.health.get(eIndex) - 1);
					if (gen.health.get(eIndex) <= 0) Main.state.game.mission.killedEnemy(Game.object.objects.get(gen.types.get(eIndex)));
				}
			}
			int X = Generation.X;
			int Y = Generation.Y;
			float[] coordinates = {gen.coordinates.get(i)[X] / Game.size, gen.coordinates.get(i)[Y] / Game.size};
			float r = 140 * (float) Math.sqrt(gen.animation.get(i)[Controls.TIMER3] - 3) / Game.size;
			float distance;
			for (int x = (int) (coordinates[X] - r); x < coordinates[X] + r; x++) {
				for (int y = (int) (coordinates[Y] - r); y < coordinates[Y] + r; y++) {
					if (x >= 0 && x < Main.state.game.maze[X].length && y >= 0 && y < Main.state.game.maze.length && Main.state.game.maze[y][x]) {
						distance = (float) Math.sqrt(Math.pow(x - coordinates[X], 2) + Math.pow(y - coordinates[Y], 2));
						if (distance < r) {
							Main.state.game.maze[y][x] = false;
						}
					}
				}
			}
		}
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.freeze.index] >= 1 && itemCount[GameObject.woodchips.index] >= 9 && itemCount[GameObject.metal.index] >= 1;
		return has ? 1 : 0;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.metal.index] -= 1;
		itemCount[GameObject.woodchips.index] -= 9;
		itemCount[GameObject.freeze.index] -= 1;
		itemCount[index] += 1;
	}
}