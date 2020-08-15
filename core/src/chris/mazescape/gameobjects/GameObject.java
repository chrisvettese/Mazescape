package chris.mazescape.gameobjects;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ShortArray;
import chris.mazescape.Controls;
import chris.mazescape.Game;
import chris.mazescape.Generation;
import chris.mazescape.Main;

/**A class for the various game objects*/
public class GameObject {
	public short health;
	//Solid objects stop other moving objects when colliding
	public boolean solid = true;
	//Textures (1 or more) for each object
	public Sprite[] texture;
	//Lists of objects used for getObject() method
	public Array<GameObject> objects;
	//Lists of various types of objects
	public ShortArray enemies;
	public ShortArray moves;
	public ShortArray ground;
	public static GameObject player, enemy, flashlight, gun, projectile, shooter, stick, rock, metal, healthPack, largeHealthPack, match, woodchips, log, saw, freeze, explosive;
	//Range of speeds of the enemy
	public float speed, maxSpeed;
	//Walking animation speed and firing speed
	public float aSwitchTime;
	//Index used to distinguish each object
	public short index;
	
	public void load() {
		objects = new Array<>();
		enemies = new ShortArray();
		moves = new ShortArray();
		ground = new ShortArray();
		
		enemy = new Enemy();
		register(enemy, true, true, false);
		shooter = new Shooter();
		register(shooter, true, true, false);
		flashlight = new Flashlight();
		register(flashlight, false, false, false);
		gun = new Gun();
		register(gun, false, false, true);
		projectile = new Projectile();
		register(projectile, true, false, false);
		stick = new Stick();
		register(stick, false, false, true);
		rock = new GameObject();
		rock.health = -1000;
		rock.texture = new Sprite[] {new Sprite(new Texture("rock1.png")), new Sprite(new Texture("rock2.png"))};
		rock.index = (short) Game.object.objects.size;
		register(rock, false, false, true);
		metal = new GameObject();
		metal.health = -1000;
		metal.texture = new Sprite[] {new Sprite(new Texture("metal.png"))};
		register(metal, false, false, true);
		match = new Match();
		register(match, false, false, true);
		healthPack = new GameObject();
		healthPack.health = 7;
		healthPack.texture = new Sprite[] {new Sprite(new Texture("healthpack_small.png"))};
		register(healthPack, false, false, true);
		largeHealthPack = new GameObject();
		largeHealthPack.health = 17;
		largeHealthPack.texture = new Sprite[] {new Sprite(new Texture("healthpack_large.png"))};
		register(largeHealthPack, false, false, true);
		woodchips = new GameObject();
		woodchips.health = -1000;
		woodchips.texture = new Sprite[] {new Sprite(new Texture("woodchips.png"))};
		register(woodchips, false, false, true);
		log = new GameObject();
		log.health = -1000;
		log.texture = new Sprite[] {new Sprite(new Texture("log.png"))};
		register(log, false, false, true);
		saw = new Saw();
		register(saw, false, false, true);
		player = new Player();
		register(player, true, false, false);
		freeze = new Freeze();
		register(freeze, false, false, false);
		explosive = new Explosive();
		register(explosive, false, false, false);
	}
	/**Adds object to the appropriate list, assigns it an index*/
	private void register(GameObject obj, boolean moves, boolean enemy, boolean ground) {
		obj.index = (short) Game.object.objects.size;
		objects.add(obj);
		if (moves) this.moves.add(obj.index);
		if (enemy) this.enemies.add(obj.index);
		if (ground) this.ground.add(obj.index);
	}
	/**Override this to make the item createable*/
	public int canMake(int[] itemCount) {
		return 0;
	}
	/**Override this to customize rendering code*/
	public void draw(int i, SpriteBatch batch, float px, float py, int playerIndex) {
		Generation generation = Main.state.game.generation;
		int textureIndex = (int) generation.animation.get(i)[Controls.INDEX];
		float alpha = 1;
		if (Main.state.game.mission.isDark) {
			alpha = Main.state.game.draw.getAlpha(generation.coordinates.get(i)[Generation.X] / Game.size, px, generation.coordinates.get(i)[Generation.Y] / Game.size, py, playerIndex);
		}
		texture[textureIndex].setPosition(generation.coordinates.get(i)[Generation.X], generation.coordinates.get(i)[Generation.Y]);
		texture[textureIndex].setColor(0, 0, 0, 1);
		texture[textureIndex].draw(batch);
		
		if (Main.state.game.generation.animation.get(i)[Controls.TIMER2] > 0) {
			Main.state.game.generation.animation.get(i)[Controls.TIMER2] -= Main.info.getDeltaTime();
			texture[textureIndex].setColor(1, 0.2f, 0.2f, alpha);
		} else {
			texture[textureIndex].setColor(1, 1, 1, alpha);
		}
		texture[textureIndex].draw(batch);
	}
	/**Override this to adjust player inventory when an item is created*/
	public void createItem(int[] itemCount) { }
	/**Override this to set movement code*/
	public byte[] move(int index) {
		return null;
	}
	/**Override this for additional rendering*/
	public void renderDraw(int i, ShapeRenderer renderer) { };
	/**Disposes of all the textures*/
	public void dispose() {
		if (objects != null) {
			for (int i = 0; i < objects.size; i++) {
				for (int j = 0; j < objects.get(i).texture.length; j++) {
					objects.get(i).texture[j].getTexture().dispose();
				}
			}
		}
	}
}