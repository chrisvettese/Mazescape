package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import chris.mazescape.Controls;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Projectile extends GameObject {
	public Projectile() {
		super();
		health = 34;
		texture = new Sprite[] {new Sprite(new Texture("shoot2.png")), new Sprite(new Texture("shoot1.png")), new Sprite(new Texture("shoot2.png"))};
		speed = 90;
		aSwitchTime = 0.04f;
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		texture[(int) Main.state.game.generation.animation.get(i)[Controls.INDEX]].setRotation(Main.state.game.generation.animation.get(i)[Controls.ROTATION]);
		super.draw(i, batch, px, py, playerIndex);
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.metal.index] >= 2;
		return has ? 9 : 0;
	}
	@Override
	public byte[] move(int index) {
		byte[] direction = new byte[2];
		float angle = Main.state.game.generation.animation.get(index)[Controls.ROTATION];
		if (angle >= 315 || angle <= 45) direction[Generation.X] = 1;
		else if (angle < 135) direction[Generation.Y] = 1;
		else if (angle < 225) direction[Generation.X] = -1;
		else direction[Generation.Y] = -1;
		return direction;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.metal.index] -= 2;
		itemCount[index] += 9;
	}
}