package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

public class Stick extends GameObject {
	public Stick() {
		health = -1000;
		texture = new Sprite[] {new Sprite(new Texture("stick1.png")), new Sprite(new Texture("stick2.png"))};
	}
	@Override
	public int canMake(int[] itemCount) {
		boolean has = itemCount[GameObject.log.index] >= 2 &&  itemCount[GameObject.saw.index] >= 1;
		return has ? 7 : 0;
	}
	@Override
	public void createItem(int[] itemCount) {
		itemCount[GameObject.log.index] -= 2;
		itemCount[GameObject.saw.index] -= 1;
		itemCount[index] += 7;
	}
}