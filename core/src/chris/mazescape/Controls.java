package chris.mazescape;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import chris.mazescape.gameobjects.GameObject;

public class Controls {
	/*Index is the current sprite to use, which is found in each GameObject's texture[] array
	 * Rotation is the rotation of each sprite.
	 * There are 2 timers used for animations. Timer 1 is used for the walking animation, timer 2 is used for
	 * the hurt animation, and timer 3 is used for gun firing (since when a gameobject carries a gun, it is actually
	 * part of the gameobject, not a separate item
	 */
	public static final byte INDEX = 0, ROTATION = 1, TIMER1 = 2, TIMER2 = 3, TIMER3 = 4;
	
	//Could be used for enemies walking
	private Sound step1;
	//Used for collision (this is my own Rectangle class since the Java rectangle doesn't work on android
	private Rectangle obj, box;
	
	public Controls() {
		step1 = Gdx.audio.newSound(Gdx.files.internal("step1.wav"));
		obj = new Rectangle();
		box = new Rectangle();
	}
	//Moves objects
	public void move(int uIndex) {
		final Generation gen = Main.state.game.generation;
		final int playerIndex = gen.types.indexOf(GameObject.player.index);
		final int X = Generation.X;
		final int Y = Generation.Y;
		final int index = Main.state.game.generation.uIndexList.indexOf(uIndex);
		final GameObject o = Game.object.objects.get(gen.types.get(index));
		gen.animation.get(index)[TIMER1] += Gdx.graphics.getDeltaTime();
		
		//Calls the movement code for enemy/player and gets the direction to move in
		byte[] direction = o.move(index);
		//Sets rotation based on above direction
		if (direction[X] == 0 && direction[Y] < 0) {
			gen.animation.get(index)[ROTATION] = 270;
		} else if (direction[X] == 0 && direction[Y] > 0) {
			gen.animation.get(index)[ROTATION] = 90;
		} else if (direction[X] < 0 && direction[Y] == 0) {
			gen.animation.get(index)[ROTATION] = 180;
		} else if (direction[X] > 0 && direction[Y] == 0) {
			gen.animation.get(index)[ROTATION] = 0;
		}
		// Calculates actual xdir and ydir based on possible xdir and ydir
		float[] dir;
		if (!Game.object.enemies.contains(gen.types.get(index)) && o.solid) {
			//Collision
			dir = collide(index, direction[X] * o.speed * Main.info.getDeltaTime(), direction[Y] * o.speed * Main.info.getDeltaTime());
		} else {
			//If object is an enemy
			int eIndex = gen.manager.enemies.indexOf(uIndex);
			dir = collide(index, direction[X] * gen.manager.speeds.get(eIndex) * Main.info.getDeltaTime(), direction[Y] * gen.manager.speeds.get(eIndex) * Main.info.getDeltaTime());
		}
		// Sets animation, moves object
		gen.coordinates.get(index)[X] = gen.coordinates.get(index)[X] + dir[X];
		gen.coordinates.get(index)[Y] = gen.coordinates.get(index)[Y] + dir[Y];
		if (dir[X] == 0 && dir[Y] == 0) {
			gen.animation.get(index)[INDEX] = 0;
			gen.animation.get(index)[TIMER1] = 0;
		}
		// Animation code
		if (gen.animation.get(index)[TIMER1] != 0) {
			if (gen.animation.get(index)[INDEX] == 0) {
				gen.animation.get(index)[INDEX] = 1;
			}
			else if (gen.animation.get(index)[TIMER1] > o.aSwitchTime) {
				//Step volume. Enemies farther away sound more quiet than closer ones
				float volume = 1 - (float) Math.sqrt(Math.pow(gen.coordinates.get(index)[X] - Main.camera.position.x, 2) + Math.pow(gen.coordinates.get(index)[Y] - Main.camera.position.y, 2)) / (float) Game.size / 16f;
				if (volume < 0) volume = 0;
				step1.setVolume(step1.play(), volume);
				gen.animation.get(index)[TIMER1] = 0;
				float distance = (float) Math.sqrt(Math.pow(gen.coordinates.get(index)[X] - gen.coordinates.get(playerIndex)[X], 2) + Math.pow(gen.coordinates.get(index)[Y] - gen.coordinates.get(playerIndex)[Y], 2));
				distance = distance / (Main.state.game.maze[X].length + Main.state.game.maze.length) / 1.5f;
				if (distance > 1) distance = 1;
				distance = 1 - distance;
				if (gen.animation.get(index)[INDEX] == 1) gen.animation.get(index)[INDEX] = 2;
				else gen.animation.get(index)[INDEX] = 1;
			}
		}
		//Calls projectile firing code
		if (o == GameObject.shooter) {
			gen.manager.fire(index);
		}
	}
	//Collision code
	public float[] collide(int index, float xdir, float ydir) {
		boolean[][] maze = Main.state.game.maze;
		Generation gen = Main.state.game.generation;
		GameObject o = Game.object.objects.get(gen.types.get(index));
		float[] direction = new float[] {xdir, ydir};
		float ox = gen.coordinates.get(index)[Generation.X] + direction[Generation.X];
		float oy = gen.coordinates.get(index)[Generation.Y] + direction[Generation.Y];
		int tIndex = (int) gen.animation.get(index)[INDEX];
		o.texture[tIndex].setRotation(gen.animation.get(index)[ROTATION]);
		o.texture[tIndex].setPosition(gen.coordinates.get(index)[Generation.X], gen.coordinates.get(index)[Generation.Y]);
		float[] v = o.texture[tIndex].getVertices();
		//Provides coordinates to the rectangle class
		obj.set(v[SpriteBatch.X1] + xdir, v[SpriteBatch.Y1] + ydir, v[SpriteBatch.X3] + xdir, v[SpriteBatch.Y3] + ydir);
		if (o == GameObject.player) {
			//System.out.println(obj.x + "," + obj.xwidth);
			//System.out.println("...   " +obj.y + "," + obj.yheight);
		}
		//Maze collision code. Enemies don't walk into walls so they don't need this
		if (!Game.object.enemies.contains(o.index)) {
			int x = (int) (ox / Game.size);
			int y = (int) (oy / Game.size);
			for (int bx = -1 + x; bx < 2 + x; bx ++) {
				for (int by = -1 + y; by < 2 + y; by++) {
					if (by < maze.length && by >= 0 && bx >= 0 && bx < maze[Generation.X].length && maze[by][bx]) {
						int boxX = bx * Game.size;
						int boxY = by * Game.size;
						//Sets rectangle based on x, y, width, and height
						box.set(boxX, boxY, Game.size, Game.size);
						//If the object and maze block collide
						if (Rectangle.intersects(box, obj)) {
							if (o == GameObject.projectile) gen.health.set(index, gen.health.get(index) - 1);
							return new float[] {0, 0};
						}
					}
				}
			}
		}
		//Object and object collision code
		for (int i = 0; i < gen.types.size; i++) {
			GameObject object = Game.object.objects.get(gen.types.get(i));
			if (object.solid && i != index && Math.abs(ox - gen.coordinates.get(i)[Generation.X]) < 60 && Math.abs(oy - gen.coordinates.get(i)[Generation.Y]) < 60) {
				boolean objectGround = Game.object.ground.contains(object.index);
				boolean oEnemy = Game.object.enemies.contains(o.index);
				if (!(oEnemy && object == GameObject.projectile) && !(o == GameObject.projectile && objectGround) && !(oEnemy && objectGround) && !(o == GameObject.match && !(object == GameObject.freeze || object == GameObject.explosive))) {
					int bIndex = (int) gen.animation.get(i)[INDEX];
					object.texture[bIndex].setRotation(gen.animation.get(i)[ROTATION]);
					object.texture[bIndex].setPosition(gen.coordinates.get(i)[Generation.X], gen.coordinates.get(i)[Generation.Y]);
					float[] vertices = object.texture[bIndex].getVertices();
					box.set(vertices[SpriteBatch.X1], vertices[SpriteBatch.Y1], vertices[SpriteBatch.X3], vertices[SpriteBatch.Y3]);
					//If the objects collide, these are the various things that 
					//happen depending on what the objects are
					if (Rectangle.intersects(box, obj)) {
						//If enemy hits player
						if (oEnemy && object == GameObject.player) {
							gen.health.set(i, gen.health.get(i) - 1);
							gen.animation.get(i)[TIMER2] = object.aSwitchTime;
						}
						//If projectile hits enemy or player
						else if (o == GameObject.projectile && (Game.object.enemies.contains(object.index) || object == GameObject.player)) {
							gen.health.set(index, -1);
							gen.health.set(i, gen.health.get(i) - 5);
							if (gen.animation.get(index)[Controls.TIMER3] == 100 && gen.health.get(i) <= 0) Main.state.game.mission.killedEnemy(Game.object.objects.get(gen.types.get(i)));
							gen.animation.get(i)[TIMER2] = object.aSwitchTime;
						}
						//Two projectiles collide
						else if (o == GameObject.projectile && object == GameObject.projectile) {
							gen.health.set(index, -1);
							gen.health.set(i, -1);
						}
						//Match collides with bomb
						else if (o == GameObject.match && (object == GameObject.freeze || object == GameObject.explosive)) {
							gen.animation.get(i)[Controls.INDEX] = 1;
							Main.state.game.delete(gen.uIndexList.get(index));
						}
						//Player collides with ground item
						else if (o == GameObject.player && objectGround) {
							if (object == GameObject.gun) {
								Main.state.game.item.add(object, 1);
								Main.state.game.item.add(GameObject.projectile, 3);
							}
							//Player picked up health
							else if (object == GameObject.healthPack || object == GameObject.largeHealthPack){
								if (gen.health.get(index) < o.health) {
									gen.health.set(index, gen.health.get(index) + object.health);
									if (gen.health.get(index) > o.health) gen.health.set(index, o.health);
								}
							} else {
								Main.state.game.item.add(object, 1);
							}
							//Deletes the object
							Main.state.game.delete(gen.uIndexList.get(i));
						}
						return new float[] {0, 0};
					}
				}
			}
		}
		return direction;
	}
	//Disposes of the step sound
	public void dispose() {
		step1.dispose();
	}
}