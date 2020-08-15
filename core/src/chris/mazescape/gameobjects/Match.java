package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class Match extends GameObject {
	public Match() {
		super();
		health = -1000;
		texture = new Sprite[] {new Sprite(new Texture("match.png"))};
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.rock.index] >= 1 && itemCount[GameObject.stick.index] >= 2 && itemCount[GameObject.woodchips.index] >= 3;
		return has ? 2 : 0;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.rock.index] -= 1;
		itemCount[GameObject.stick.index] -= 2;
		itemCount[GameObject.woodchips.index] -= 3;
		itemCount[index] += 2;
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		texture[0].setRotation(0);
		super.draw(i, batch, px, py, playerIndex);
	}
}