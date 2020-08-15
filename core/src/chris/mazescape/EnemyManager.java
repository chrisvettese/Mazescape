package chris.mazescape;

import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.utils.FloatArray;
import com.badlogic.gdx.utils.IntArray;
import chris.mazescape.gameobjects.GameObject;
import chris.mazescape.gameobjects.Player;

import java.util.ArrayList;
import java.util.List;

/**Pathfinding code and projectiles. Uses a separate thread so it doesn't lag the game.
 * Based on the A-star algorithm
 * 
 * IntArray and FloatArray are the same as ArrayList<Integer> and ArrayList<Float> in terms of function.
 * They are more efficient though (important for pathfinding) because they use an array of ints or floats
 * instead of the objects Integer and Float
 */
public class EnemyManager implements Runnable {
	private int X = Generation.X, Y = Generation.Y;
	private int size = Game.size;
	private Generation gen;

	// uIndexes of enemies
	public IntArray enemies = new IntArray();
	// Coordinates the enemies are moving towards
	public List<short[]> targets = new ArrayList<>();
	//Enemy speeds
	public FloatArray speeds = new FloatArray();
	//For shooters, time to wait before firing again
	public FloatArray timer = new FloatArray();

	// Pathfinding lists
	//Possible spots for the enemies to move to that aren't walls
	private List<short[]> spots = new ArrayList<>();
	//Spots that haven't been checked yet
	private IntArray openList = new IntArray();
	//Spots that have been checked already
	private IntArray closedList = new IntArray();
	//gCost + the distance from the player
	private IntArray fCosts = new IntArray();
	//Cost from the enemy's current position to the current spot
	private IntArray gCosts = new IntArray();
	//The previous spot of the current spot. If you found kept following the previous spots you would get back to
	//the enemy again.
	private IntArray previousIndex = new IntArray();

	private Thread thread;
	// When false the pathfinding thread ends
	private volatile boolean runThread = true;

	// Starts pathfinding for a new enemy
	public void addEnemy(int uIndex) {
		gen = Main.state.game.generation;
		int index = gen.uIndexList.indexOf(uIndex);
		targets.add(new short[] {(short) (gen.coordinates.get(index)[X] / Game.size), (short) (gen.coordinates.get(index)[Y] / Game.size)});
		enemies.add(uIndex);
		GameObject o = Game.object.objects.get(gen.types.get(index));
		speeds.add(o.speed + (float) Math.random() * (o.maxSpeed - o.speed));
		timer.add(0.7f);
	}
	//Removes an enemy
	public void removeEnemy(int uIndex) {
		int index = enemies.indexOf(uIndex);
		try {
		targets.remove(index);
		enemies.removeIndex(index);
		speeds.removeIndex(index);
		timer.removeIndex(index);
		} catch (Exception e) {
			System.out.println(targets.size());
			System.out.println(enemies.size);
		}
	}
	// Finds next direction (up, down, left, or right) the enemy should move in
	public void findTarget(int uIndex) {
		int enemyIndex = enemies.indexOf(uIndex);
		int index = gen.uIndexList.indexOf(uIndex);
		short ex = (short) (gen.coordinates.get(index)[X] / size);
		short ey = (short) (gen.coordinates.get(index)[Y] / size);
		short px = (short) (gen.coordinates.get(gen.types.indexOf(GameObject.player.index))[X] / size);
		short py = (short) (gen.coordinates.get(gen.types.indexOf(GameObject.player.index)) [Y] / size);
		short[] target = calculatePath(ex, ey, px, py);
		targets.get(enemyIndex)[X] = target[X];
		targets.get(enemyIndex)[Y] = target[Y];
	}
	//Checks if it is time to fire a projectile (for player or enemy) and if it is, places the projectile
	public void fire(int index) {
		int eIndex = enemies.indexOf(gen.uIndexList.get(index));
		if (eIndex != -1) {
			if (timer.get(eIndex) <= 0) {
				float rotation = gen.animation.get(index)[Controls.ROTATION];
				float[] location = getShotPos(index, false);
				gen.add(location[Generation.X], location[Generation.Y], GameObject.projectile.index);
				gen.animation.get(gen.animation.size() - 1)[Controls.ROTATION] = rotation;
				gen.animation.get(index)[Controls.TIMER3] = 0.2f;
				timer.set(eIndex, 0.7f);
			}
			timer.set(eIndex, timer.get(eIndex) - Main.info.getDeltaTime());
		} else {
			//If it is time to shoot, and the player has a gun with at least one shot left
			if (((Player) GameObject.player).shootTimer <= 0 && Main.state.game.item.itemCount[GameObject.projectile.index] > 0 && Main.state.game.item.itemCount[GameObject.gun.index] > 0) {
				float rotation = gen.animation.get(index)[Controls.ROTATION];
				float[] location = getShotPos(index, false);
				gen.add(location[Generation.X], location[Generation.Y], GameObject.projectile.index);
				Main.state.game.item.itemCount[GameObject.projectile.index] -= 1;
				gen.animation.get(gen.animation.size() - 1)[Controls.ROTATION] = rotation;
				//When player shoots, the projectile's timer3 value is set to 100 to distinguish from enemy projectiles
				gen.animation.get(gen.animation.size() - 1)[Controls.TIMER3] = 100;
				gen.animation.get(index)[Controls.TIMER3] = 0.2f;
				((Player) GameObject.player).shootTimer = 0.5f;
			}
			((Player) GameObject.player).shootTimer -= Main.info.getDeltaTime();
		}
	}
	//Finds the correct position of the shot/gun
	public float[] getShotPos(int index, boolean gun) {
		GameObject o = Game.object.objects.get(gen.types.get(index));
		Sprite s = o.texture[(int) gen.animation.get(index)[Controls.INDEX]];
		float rotation = gen.animation.get(index)[Controls.ROTATION];
		float ox = gen.coordinates.get(index)[Generation.X];
		float oy = gen.coordinates.get(index)[Generation.Y];
		float x = 0, y = 0;
		float sx = 0, sy = 0, gx = 0;
		sx = GameObject.projectile.texture[0].getWidth();
		sy = GameObject.projectile.texture[0].getHeight();
		if (gun) {
			gx = GameObject.gun.texture[0].getWidth();
		}
		if (rotation == 0) {
			x = ox + s.getWidth() - gx;
			y = oy + s.getHeight() / 5f;
		} else if (rotation == 90) {
			x = ox + s.getWidth() - s.getWidth() / 5f - sx / 2;
			y = oy + s.getHeight() + sy - gx;
		} else if (rotation == 180) {
			x = ox - sx + gx;
			y = oy + s.getHeight() - s.getHeight() / 5f - sy;
		} else {
			x = ox + s.getWidth() / 5f - sy;
			y = oy - sx + gx;
		}
		return new float[] {x, y};
	}

	// A* pathfinding algorithm
	private short[] calculatePath(int x1, int y1, int x2, int y2) {
		/* Nodes: spots contains grid locations of the node, fCost = gCost +
		hCost, gCosts are stored in list for efficiency in calculating gCosts
		of child nodes. previousIndex leads to parent node.*/
		spots = new ArrayList<>();
		fCosts = new IntArray();
		gCosts = new IntArray();
		previousIndex = new IntArray();

		// List of nodes that have been created but not yet considered
		openList = new IntArray();
		// List of nodes that have been checked and removed from openList
		closedList = new IntArray();

		//After 900 loops, the enemy is either stuck or too far from the player, so it will give up finding a path
		int cap = 900;
		//The first node, at the coordinate of the enemy
		addNode(x1, y1, -1, x2, y2, 0);
		//While there has been less than 900 loops and there are still spots left to check
		while (cap >= 0 && openList.size > 0) {
			cap -= 1;
			//Finds the "cheapest" spot (the one that is the most in the direction of the player)
			int cheapestNode = cheapest();
			int currentNode = openList.get(cheapestNode);
			//Removes the cheapest node from the openList since it is now being checked
			openList.removeIndex(cheapestNode);
			//If the spot being checked is the player
			if (spots.get(currentNode)[X] == (short) x2 && spots.get(currentNode)[Y] == (short) y2)
				//Path has been found
				return findFirstTarget(currentNode);
			else {
				//Spot has been checked and is not the player. Adding to closed list and adding any other 
				//spots around this spot (up, down, left, or right from it) to the open list
				if (!closedList.contains(currentNode)) {
					closedList.add(currentNode);
					checkSides(currentNode, x2, y2);
				}
			}
		}
		//Returns the current location of the enemy, or no movement at all
		return new short[] { (short) x1, (short) y1 };
	}
	/**
	 * Returns the index of the spot with the lowest fCost
	 * @return
	 */
	private int cheapest() {
		int index = 0;
		for (int i = 0; i < openList.size; i++) {
			if (fCosts.get(openList.get(i)) < fCosts.get(openList.get(index))) index = i;
		}
		return index;
	}
	/**
	 * Given the spot index of the player, it will follow each previous index (parent node) back until it gets to
	 * the first move the enemy should make to go towards the player
	 * @return
	 */
	private short[] findFirstTarget(int index) {
		int i = index;
		if (previousIndex.get(index) != -1) {
			while (previousIndex.get(previousIndex.get(i)) != -1) {
				i = previousIndex.get(i);
			}
		}
		return new short[] {spots.get(i)[X], spots.get(i)[Y]};
	}
	/**
	 * Adds spots around this one to the open list if possible
	 */
	private void checkSides(int index, int tx, int ty) {
		short x = spots.get(index)[X];
		short y = spots.get(index)[Y];
		checkSide((short) (x + 1), y, index, tx, ty);
		checkSide((short) (x - 1), y, index, tx, ty);
		checkSide(x, (short) (y + 1), index, tx, ty);
		checkSide(x, (short) (y - 1), index, tx, ty);
	}
	/**
	 * Adds specified spot to the open list if possible
	 */
	private void checkSide(short x, short y, int parent, int tx, int ty) {
		if (!Main.state.game.maze[y][x]) {
			if (!closedList.contains(getSpot((short) x, (short) y))) {
				addNode(x, y, parent, tx, ty, gCosts.get(parent) + size);
			}
		}
	}
	/**
	 * Adds spot/node to the list, calculates cost
	 */
	private void addNode(int x, int y, int parentIndex, int x2, int y2, int gCost) {
		short[] list = {(short) x, (short) y};
		spots.add(list);
		previousIndex.add(parentIndex);
		gCosts.add(gCost);
		fCosts.add(gCost + (int) Math.sqrt(Math.pow(Math.abs(x2 - x) * size, 2) + Math.pow(Math.abs(y2 - y), 2)));
		openList.add(getSpot((short) x, (short) y));
	}
	/**
	 * Given x and y, returns the index of the spot
	 */
	private int getSpot(short x, short y) {
		short[] list = {x, y};
		return indexOf(list);
	}
	/**
	 * Given an array, returns index of the spot in the spots list
	 */
	private int indexOf(short[] array) {
		for (int i = 0; i < spots.size(); i++) {
			short[] array2 = spots.get(i);
			if (array[Generation.X] == array2[Generation.X] && array[Generation.Y] == array2[Generation.Y]) return i;
		}
		return -1;
	}
	/**
	 * Starts a new thread
	 */
	public void startThread() {
		if (thread == null) {
			gen = Main.state.game.generation;
			thread = new Thread(this);
			thread.start();
		}
	}
	/**
	 * Ends the thread
	 */
	public void stopThread() {
		runThread = false;
		try {
			thread.join();
		} catch (Exception e) {
		}
	}
	/**
	 * This is the game loop for enemies. It runs separately from the main loop.
	 */
	public void run() {
		while (runThread) {
			//Loops through the list of registered enemies and finds paths from them to the player
			for (int i = 0; i < enemies.size; i++) {
				try {
					if (i < targets.size()) {
						short[] target = targets.get(i);
						int uIndex = enemies.get(i);
						int index = gen.uIndexList.indexOf(uIndex);
						float ex = gen.coordinates.get(index)[X];
						float ey = gen.coordinates.get(index)[Y];
						if (Math.abs(ex - (target[X] * size)) <= 1 && Math.abs(ey - (target[Y] * size)) <= 1) {
							gen.coordinates.get(index)[X] = (float) target[X] * size;
							gen.coordinates.get(index)[Y] = (float) target[Y] * size;
							findTarget(uIndex);
						}
					}
				} catch (Exception e) {
				}
			}
		}
	}
}