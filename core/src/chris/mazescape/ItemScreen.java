package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import chris.mazescape.gameobjects.GameObject;

/**Displays items the player currently has, the mission, and any items the player has the resources to create*/
public class ItemScreen {
	//Coordinates for rendering the item screen correctly
	private final Vector3 itemScreenLocation;
	private final Color itemScreenCover;
	public int[] itemCount;
	private final float[] itemScreen = {Main.info.width * Main.camera.zoom + 10, -Main.info.height * Main.camera.zoom - 10};
	//Array of buttons for any items the player can create
	private final Button[] createButtons;
	//Array of buttons for any items the player can equip
	private final Button[] selectButtons;
	//Item "equipped" by the player (like holding the gun or an explosive)
	public int holdingIndex; 
	
	public ItemScreen() {
		holdingIndex = -1;
		itemScreenCover = new Color(0.05f, 0.05f, 0.05f, 0.72f);
		itemScreenLocation = new Vector3();
		itemCount = new int[Game.object.objects.size];
		createButtons = new Button[10];
		//Creates one button so the other buttons can copy its properties
		createButtons[0] = new Button(0, 0, Button.RECT, Button.ONE_CLICK, 27, 10, new Color(145/255f, 145/255f, 145/255f, 170/255f), new Color(90/255f, 90/255f, 90/255f, 170/255f), null);
		createButtons[0].useCamera = false;
		selectButtons = new Button[10];
	}
	/**Adds an item to the list of items collected by the player*/
	public void add(GameObject item, int count) {
		int index = Game.object.objects.indexOf(item, true);
		itemCount[index] += count;
	}
	/**Rendering code for the item screen*/
	public void draw(SpriteBatch batch, ShapeRenderer renderer, boolean displayItemScreen) {
		Main.camera.unproject(itemScreenLocation.set(-5, -5, 0));
		Gdx.gl.glEnable(GL20.GL_BLEND);
		Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
		renderer.begin(ShapeType.Filled);
		if (displayItemScreen) {
			renderer.setColor(itemScreenCover);
			renderer.rect(itemScreenLocation.x, itemScreenLocation.y, itemScreen[Generation.X], itemScreen[Generation.Y]);
		}
		Main.state.game.buttons[Game.ITEMS].render(renderer);
		renderer.end();
		batch.begin();
		Main.state.game.buttons[Game.ITEMS].draw(batch);
		if (displayItemScreen) {
			Main.camera.unproject(itemScreenLocation.set(62 / Main.camera.zoom, 35 / Main.camera.zoom, 0));
			float x = itemScreenLocation.x;
			float y = itemScreenLocation.y;
			if (holdingIndex >= 0) batch.draw(Game.object.objects.get(holdingIndex).texture[0], x - 54, y + 2, Game.object.objects.get(holdingIndex).texture[0].getWidth() * 1.3f, Game.object.objects.get(holdingIndex).texture[0].getHeight() * 1.3f);
			float scaleX = Main.font.getScaleX();
			float scaleY = Main.font.getScaleY();
			Main.font.draw(batch, "Your Items:", x - 24, y + 26);
			Main.font.draw(batch, "Create:", x + Main.info.width * Main.camera.zoom - 150, y + 26);
			Main.font.getData().setScale(0.5f, 0.5f);
			Main.font.draw(batch, Main.state.game.mission.description, x - 55, y - 105);
			Main.font.getData().setScale(0.4f, 0.4f);
			//Displays items the player has
			for (int i = 0; i < itemCount.length; i++) {
				if (itemCount[i] > 0 && i != holdingIndex) {
					GameObject o = Game.object.objects.get(i);
					batch.draw(o.texture[0].getTexture(), x, y, o.texture[0].getWidth() / 1.5f, o.texture[0].getHeight() / 1.5f);
					//Displays number of each item the player has
					Main.font.draw(batch, " x " + ((Integer) itemCount[i]).toString(), x + 11, y + 7);
					y -= 13;
				}
			}
			Array<GameObject> createItems = new Array<>();
			IntArray itemCounts = new IntArray();
			//Checks each item to see if the player has the resources to make it
			for (int i = 0; i < itemCount.length; i++) {
				int amount = Game.object.objects.get(i).canMake(itemCount);
				if (amount > 0) {
					itemCounts.add(amount);
					createItems.add(Game.object.objects.get(i));
				}
			}
			x += Main.info.width * Main.camera.zoom - 136;
			y = itemScreenLocation.y;
			//Renders the items the player can create
			for (int i = 0; i < createItems.size; i++) {
				GameObject o = createItems.get(i);
				batch.draw(o.texture[0].getTexture(), x, y, o.texture[0].getWidth() / 1.5f, o.texture[0].getHeight() / 1.5f);
				//Displays number of each item that will be created
				Main.font.draw(batch, " x " + ((Integer) itemCounts.get(i)), x + 11, y + 7);
				y -= 13;
			}
			batch.end();
			Gdx.gl.glEnable(GL20.GL_BLEND);
			Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
			renderer.begin(ShapeType.Filled);
			x = itemScreenLocation.x - 1;
			y = itemScreenLocation.y - 1;
			//Places buttons on top of all the items the player can select to hold
			int selectIndex = 0;
			for (int i = 0; i < itemCount.length; i++) {
				if (itemCount[i] > 0 && i != holdingIndex) {
					if (selectButtons[selectIndex] == null) {
						//Initializes button if it has not been created yet
						selectButtons[selectIndex] = new Button(0, 0, Button.RECT, Button.ONE_CLICK, 27, 10, new Color(145/255f, 145/255f, 145/255f, 170/255f), new Color(90/255f, 90/255f, 90/255f, 170/255f), null);
						selectButtons[selectIndex].useCamera = false;
					}
					selectButtons[selectIndex].set(x, y);
					//If player wants to hold that item
					if (selectButtons[selectIndex].isPressed()) {
						holdingIndex = i;
					}
					selectButtons[selectIndex].render(renderer);
					y -= 13;
					selectIndex += 1;
				}
			}
			x += Main.info.width * Main.camera.zoom - 135;
			y = itemScreenLocation.y - 1;
			//Repeats above, but for all the items the player can create
			for (int i = 0; i < createItems.size; i++) {
				if (createButtons[i] == null) {
					createButtons[i] = new Button(0, 0, Button.RECT, Button.ONE_CLICK, 27, 10, new Color(145/255f, 145/255f, 145/255f, 170/255f), new Color(90/255f, 90/255f, 90/255f, 170/255f), null);
					createButtons[i].useCamera = false;
				}
				createButtons[i].set(x, y);
				//If player wants to create that item
				if (createButtons[i].isPressed()) {
					createItems.get(i).createItem(itemCount);
					//Lets mission know that the player has created an item
					Main.state.game.mission.createdItem(createItems.get(i));
				}
				createButtons[i].render(renderer);
				y -= 13;
			}
			renderer.setColor(createButtons[0].normal);
			renderer.rect(itemScreenLocation.x - 56, itemScreenLocation.y, 20, 20);
			renderer.end();
			//Restores font to original size
			Main.font.getData().setScale(scaleX, scaleY);
		}
		else {
			batch.end();
		}
	}
}