package chris.mazescape;

import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.ShortArray;
import chris.mazescape.gameobjects.GameObject;

import java.util.ArrayList;
import java.util.List;

/**Generates the items, enemies, maze, and stores the information about them in lists
 * ShortArray and IntArray are the functionally the same as ArrayList, but more efficient
 */
public class Generation {
	//Used to access x and y values stored in short or float arrays
	public static final int X = 0, Y = 1;
	//Size of the maze
	private static int mazeHeight, mazeLength;
	//Pathfinding class
	public EnemyManager manager;
	//Coordinates of each item
	public List<float[]> coordinates;
	//The type of each item
	public ShortArray types;
	//Used for animations
	//0 = INDEX, 1 = ROTATION, 2 = TIMER1, 3 = TIMER2, 4 = TIMER3
	public List<float[]> animation;
	//The uIndex of each item remains the same as it changes position in the list.
	//This allows the pathfinding class to get the index of all the enemies even if they are moved around
	public IntArray uIndexList;
	//Health of each item
	public IntArray health;
	
	//Keeps increasing. The next uIndex to be assigned to an item (all of them must be unique)
	private int currentIndex;

	public Generation() {
		coordinates = new ArrayList<>();
		types = new ShortArray();
		animation = new ArrayList<>();
		uIndexList = new IntArray();
		health = new IntArray();
		
		currentIndex = Integer.MIN_VALUE;
		
		manager = new EnemyManager();
	}
	/**Adds a new item. Index is the GameObject index of the item*/
	public void add(float x, float y, short index) {
		float[] coords = {x, y};
		coordinates.add(coords);
		types.add(index);
		animation.add(new float[5]);
		uIndexList.add(currentIndex);
		GameObject o = Game.object.objects.get(index);
		health.add(o.health);
		if (Game.object.enemies.contains(index)) manager.addEnemy(currentIndex);
		else if (o != GameObject.gun && Game.object.ground.contains(index)) {
			animation.get(animation.size() - 1)[Controls.INDEX] = randomInt(o.texture.length, 0);
		}
		currentIndex += 1;
	}
	/**Removes an item, based on uIndex since the real index can change*/
	public void remove(int uIndex) {
		int index = uIndexList.indexOf(uIndex);
		//Equivalent to remove(index) in ArrayList
		health.removeIndex(index);
		if (Game.object.enemies.contains(types.get(index))) manager.removeEnemy(uIndex);
		coordinates.remove(index);
		types.removeIndex(index);
		animation.remove(index);
		uIndexList.removeIndex(index);
	}
	/**Generates the maze maze */
	public static boolean[][] generateMaze() {
		boolean[][] maze = new boolean[randomInt(30, 30)][randomInt(30, 30)];
		//Creates border around the maze
		for (int i = 0; i < maze[X].length; i++) {
			for (int j = 0; j < maze.length; j++) {
				maze[j][i] = true;
			}
		}
		//Number of passes. Larger maze = more loops
		int i = (int) (maze[X].length * maze.length / 2);
		while (i >= 0) {
			//Finds a point in the maze, chooses a direction (left, right, up, or down), and chooses a length
			//to extend in that direction. That becomes a walkable path in the maze
			int x = randomInt(maze[X].length - 2, 1);
			int y = randomInt(maze.length - 2, 1);
			int dir = randomInt(4, 0);
			int length = randomInt(9, 1);
			if (dir == 0 || dir == 1) {
				for (int k = 0; k < length; k++) {
					if (dir == 0 && k + x < maze[X].length - 1) maze[y][k + x] = false;
					else if (dir == 1 && k + y < maze.length - 1) maze[k + y][x] = false;
				}
			} else {
				for (int k = 0; k > -length; k--) {
					if (dir == 2 && k > 0) maze[y][k + x] = false;
					else if (dir == 3 && k > 0) maze[k + y][x] = false;
				}
			}
			i -= 1;
		}
		mazeHeight = maze.length;
		mazeLength = maze[X].length;
		return maze;
	}
	/**Spreads out items around the maze, adds the player and enemies*/
	public void addStartingObjects() {
		spawnItems();
		placeRandom(GameObject.player.index);
		if (Main.state.game.mission.isDark) {
			//Makes player carry a flashlight if the maze is in dark mode
			add(coordinates.get(coordinates.size() - 1)[X], coordinates.get(coordinates.size() - 1)[Y], GameObject.flashlight.index);
		}
	}
	//Spawns items
	private void spawnItems() {
		int count = mazeLength * mazeHeight;
		//Places guns around the maze
		for (int i = 0; i < count / 180; i++) {
			placeRandom(GameObject.gun.index);
		}
		//Spawns 3 enemies in the maze
		for (int i = 0; i < 3; i++) {
			int e = randomInt(2, 0);
			if (e == 0) placeRandom(GameObject.enemy.index);
			else placeRandom(GameObject.shooter.index);
		}
		//Spawns items
		for (int i = 0; i < count / 1; i++) {
			int e = randomInt(1000, 0);
			if (e <= 200) placeRandom(GameObject.rock.index);
			else if (e <= 250) placeRandom(GameObject.stick.index);
			else if (e <= 320) placeRandom(GameObject.log.index);
			else if (e <= 340) placeRandom(GameObject.woodchips.index);
			else if (e <= 350) placeRandom(GameObject.match.index);
			else if (e <= 385) placeRandom(GameObject.metal.index);
			else if (e <= 389) placeRandom(GameObject.largeHealthPack.index);
			else if (e <= 403) placeRandom(GameObject.healthPack.index);
		}
	}
	/**Returns a random integer in the specified range based on Math.random()*/
	public static int randomInt(int range, int min) {
		return (int) (Math.random() * range + min);
	}
	/**If there are 2 or less enemies in the world, and enemies should respawn, then spawn a new enemy*/
	public void spawnEnemies() {
		//If enemies should respawn
		if (Main.state.game.mission.respawnEnemies) {
			if (Main.state.game.generation.manager.enemies.size < 3) {
				int e = randomInt(2, 0);
				if (e == 0) placeRandom(GameObject.enemy.index);
				else placeRandom(GameObject.shooter.index);
			}
		}
	}
	/**
	 * Returns true if there is an object at the specified location
	 */
	public boolean contains(float x, float y) {
		for (int i = 0; i < coordinates.size(); i++){
			if (coordinates.get(i)[X] == x && coordinates.get(i)[Y] == y) return true;
		}
		return false;
	}
	/**
	 * Places an object at a random location in the maze, that is not a wall or another object
	 */
	private void placeRandom(short index) {
		int x = 0, y = 0;
		// Finds open spot in maze
		do {
			x = randomInt(Main.state.game.maze[X].length - 2, 0);
			y = randomInt(Main.state.game.maze.length - 2, 0);
		} while (Main.state.game.maze[y][x] || contains(x * Game.size, y * Game.size));
		// Places object in the maze
		x *= Game.size;
		y *= Game.size;
		if (index == GameObject.player.index) {
			x += 1;
			y += 1;
		}
		add(x, y, index);
	}
}