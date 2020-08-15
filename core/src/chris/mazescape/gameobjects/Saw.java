package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Saw extends GameObject {
	public Saw() {
		super();
		texture = new Sprite[] {new Sprite(new Texture("saw.png"))};
		health = -1000;
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.rock.index] >= 1 && itemCount[GameObject.metal.index] >= 4 && itemCount[GameObject.log.index] >= 1;
		return has ? 1 : 0;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.rock.index] -= 1;
		itemCount[GameObject.metal.index] -= 4;
		itemCount[GameObject.log.index] -= 1;
		itemCount[index] += 1;
	}
}