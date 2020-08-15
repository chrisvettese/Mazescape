package chris.mazescape.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector3;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

public class Player extends GameObject {
	public float shootTimer = 0.5f;
	private float placeTimer = 0.32f;
	public Vector3 playerInfo, placeItem;
	
	public Player() {
		super();
		health = 100;
		texture = new Sprite[] {new Sprite(new Texture("playerstill.png")), new Sprite(new Texture("playermove1.png")), new Sprite(new Texture("playermove2.png"))};
		speed = 40;
		aSwitchTime = 0.26f;
		playerInfo = new Vector3();
		placeItem = new Vector3();
	}
	@Override
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		Generation gen = Main.state.game.generation;
		texture[(int) gen.animation.get(i)[Controls.INDEX]].setRotation(gen.animation.get(i)[Controls.ROTATION]);
		super.draw(i, batch, px, py, playerIndex);
		if (placeTimer <= 0.32f) {
			placeTimer += Gdx.graphics.getDeltaTime();
		}
		if (Main.state.game.item.itemCount[Game.object.objects.indexOf(GameObject.gun, true)] > 0 && Main.state.game.item.holdingIndex == GameObject.gun.index) {
			((Gun) GameObject.gun).draw(batch, px, py, playerIndex, i, this, GameObject.gun);
		}
		else if (Main.state.game.item.holdingIndex == GameObject.freeze.index || Main.state.game.item.holdingIndex == GameObject.match.index || Main.state.game.item.holdingIndex == GameObject.explosive.index) {
			GameObject o = Game.object.objects.get(Main.state.game.item.holdingIndex);
			((Gun) GameObject.gun).draw(batch, px, py, playerIndex, i, this, o);
		}
	}
	@Override
	public byte[] move(int index) {
		Generation gen = Main.state.game.generation;
		byte[] direction = new byte[2];
		if (Main.state.game.buttons[Game.MOVE].pressButton) {
			float angle = (float) Math.toDegrees(Math.atan2(Gdx.input.getY() - Main.state.game.buttons[Game.MOVE].y, Gdx.input.getX() - Main.state.game.buttons[Game.MOVE].x));
			if (angle < 0) angle += 360;
			if (angle >= 315 || angle <= 45) direction[Generation.X] = 1;
			else if (angle < 135) direction[Generation.Y] = -1;
			else if (angle < 225) direction[Generation.X] = -1;
			else direction[Generation.Y] = 1;
			Main.camera.unproject(playerInfo.set((float) Math.cos(Math.toRadians(angle)) * Main.state.game.buttons[Game.MOVE].width / Main.camera.zoom + Main.state.game.buttons[Game.MOVE].x, (float) Math.sin(Math.toRadians(angle)) * Main.state.game.buttons[Game.MOVE].width / Main.camera.zoom + Main.state.game.buttons[Game.MOVE].y, 0));
		}
		Main.camera.position.set(gen.coordinates.get(index)[Generation.X] + GameObject.player.texture[0].getWidth() / 2, gen.coordinates.get(index)[Generation.Y] + GameObject.player.texture[0].getHeight() / 2, 0);
		Main.camera.update();
		
		int count = 0;
		for (int i = 0; i < 10; i++) {
			if (Gdx.input.isTouched(i)) count += 1;
		}
		if ((count == 1 && !Main.state.game.buttons[Game.MOVE].pressButton && !(Main.state.game.buttons[Game.ITEMS].buttonState || Main.state.game.buttons[Game.ITEMS].pressButton)) || count >= 2) {
			int hIndex = Main.state.game.item.holdingIndex;
			if (hIndex == GameObject.gun.index) gen.manager.fire(index);
			else if (placeTimer > 0.32f && (hIndex == GameObject.freeze.index || hIndex == GameObject.match.index || hIndex == GameObject.explosive.index)) {
				placeTimer = 0;
				GameObject o = Game.object.objects.get(hIndex);
				Main.state.game.item.itemCount[o.index] -= 1;
				if (Main.state.game.item.itemCount[o.index] <= 0) Main.state.game.item.holdingIndex = -1;
				Main.camera.unproject(placeItem.set(Gdx.input.getX(), Gdx.input.getY(), 0));
				gen.add(placeItem.x, placeItem.y, o.index);
				if (hIndex == GameObject.match.index) {
					Main.state.game.control.collide(gen.types.size - 1, 0, 0);
				}
			}
		}
		return direction;
	}
}