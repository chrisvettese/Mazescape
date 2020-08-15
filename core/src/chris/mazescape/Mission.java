package chris.mazescape;

import chris.mazescape.gameobjects.GameObject;

import java.util.ArrayList;
import java.util.List;

/**Creates an objective of the game, partly depending on the level*/
public class Mission {
	//The main missions
	public static final byte KILL = 1, CREATE = 2, ESCAPE = 3, SURVIVE = 4;
	//If any mission has a win or lose timer
	public static final byte TIMER_WIN = 5, TIMER_LOSE = 6, NO_TIMER = 7;
	//mission is assigned to an above mission, and timer is assigned to an above timer
	public byte mission, timer;
	//If the maze is dark mode and if enemies should respawn
	public boolean isDark, respawnEnemies;
	//Number of enemies to kill, if it is the mission
	public int killNum;
	//Mission timer
	public float time, originalTime;
	//If the goal is to create a certain item or kill a certain enemy
	public GameObject itemToCreate, enemyToKill;
	//The mission described in a sentence
	public String description;
	
	//Sets mission based on level
	public Mission(int level) {
		setMission(level);
	}
	//Sets mission given specific information (loaded from a file)
	public Mission(List<String> info) {
		interpret(info);
	}
	/**If given list of strings, interpret each string as part of the mission*/
	private void interpret(List<String> info) {
		System.out.println(info);
		mission = Byte.parseByte(info.get(0));
		timer = Byte.parseByte(info.get(1));
		isDark = Boolean.parseBoolean(info.get(2));
		respawnEnemies = Boolean.parseBoolean(info.get(3));
		killNum = Integer.parseInt(info.get(4));
		time = Float.parseFloat(info.get(5));
		originalTime = Float.parseFloat(info.get(5));
		if (!info.get(6).equals("-1")) {
			itemToCreate = Game.object.objects.get(Integer.parseInt(info.get(6)));
		}
		if (!info.get(7).equals("-1")) {
			System.out.println(Game.object.objects);
			enemyToKill = Game.object.objects.get(Integer.parseInt(info.get(7)));
		}
		description = info.get(8);
	}
	/**Randomly selects a mission. They generally get harder as the level increases*/
	private void setMission(int level) {
		respawnEnemies = true;
		//Levels 1 to 7
		if (level < 8) {
			int i = Generation.randomInt(100, 1);
			if (i <= 25) {
				//Mission is to create an item
				mission = CREATE;
				i = Generation.randomInt(3, 1);
				if (level < 4) {
					switch (i) {
						case 1: itemToCreate = GameObject.match;
							break;
						case 2: itemToCreate = GameObject.projectile;
							break;
						default: itemToCreate = GameObject.saw;
							break;
					}
				} else {
					switch (i) {
						case 1: itemToCreate = GameObject.stick;
							break;
						case 2: itemToCreate = GameObject.freeze;
							break;
						default: itemToCreate = GameObject.explosive;
					}
				}
			}
			else if (i <= 68) {
				//Mission is to kill an enemy or a number of enemies
				mission = KILL;
			} else {
				//Mission is to survive for number of seconds
				mission = SURVIVE;
			}
		//Level is greater than 7 (no create item missions)
		} else {
			int i = Generation.randomInt(20, 1);
			if (i > 19) {
				mission = ESCAPE;
			}
			else if (i > 10) {
				mission = SURVIVE;
			} else {
				mission = KILL;
			}
		}
		if (mission == SURVIVE) {
			timer = TIMER_WIN;
		}
		else if (Generation.randomInt(100, 1) < 36) {
			timer = TIMER_LOSE;
		} else {
			timer = NO_TIMER;
		}

		if (timer == TIMER_LOSE) {
			time = 115 + (float) Math.sqrt(level) * 10 + Generation.randomInt(7, -3);
			if (mission == KILL && enemyToKill == null) {
				time += (killNum * 13);
			}
			originalTime = time;
		} else if (timer == TIMER_WIN) {
			time = 28 + (float) Math.sqrt(level) * 10 + Generation.randomInt(7, -3);
			originalTime = time;
		}
		
		if (mission == KILL) {
			int i = Generation.randomInt(10, 1);
			if (level < 2 && i < 7) {
				if (i <= 4) {
					enemyToKill = GameObject.enemy;
				} else {
					enemyToKill = GameObject.shooter;
				}
				respawnEnemies = false;
			}
			else if (level < 4 && i < 7) {
				if (i <= 1) {
					enemyToKill = GameObject.enemy;
				} else {
					enemyToKill = GameObject.shooter;
				}
				respawnEnemies = false;
			} else {
				killNum = 2 + (int) (level * 1.35) + Generation.randomInt(level / 3, -level / 4);
			}
		}
		
		int i = Generation.randomInt(10, 1);
		if (i <= 2 && level > 3) isDark = true;
		else isDark = false;
		description = getMission();
	}
	//Enemy has been killed
	public void killedEnemy(GameObject enemy) {
		System.out.println(enemy + ", " + enemyToKill);
		if (mission == KILL) {
			if (enemy == enemyToKill) killNum = -1000;
			else killNum -= 1;
		}
	}
	//Item has been created
	public void createdItem(GameObject item) {
		if (mission == CREATE) {
			System.out.println(item + ", " + itemToCreate);
			if (item.index == itemToCreate.index) killNum = -1000;
		}
	}
	/**Converts the mission into a sentence*/
	private String getMission() {
		String message;
		if (mission == KILL) {
			message = "Kill";
			if (!respawnEnemies) {
				String enemy;
				if (enemyToKill == GameObject.shooter) enemy = " Shooter";
				else enemy = "n Enemy";
				message += (" a" + enemy);
			} else {
				String enemies;
				if (killNum == 1) enemies = " enemy";
				else enemies = " enemies";
				message += (" " + killNum + enemies);
			}
		}
		else if (mission == CREATE) {
			message = "Create ";
			if (itemToCreate == GameObject.freeze) message += "a freeze";
			else if (itemToCreate == GameObject.explosive) message += "a bomb";
			else if (itemToCreate == GameObject.match) message += "matches";
			else if (itemToCreate == GameObject.saw) message += "a saw";
			else if (itemToCreate == GameObject.stick) message += "sticks";
			else if (itemToCreate == GameObject.projectile) message += "projectiles";
		}
		else if (mission == ESCAPE) {
			message = "Escape the maze";
		
		} else {
			message = "Survive for " + (int) originalTime + " seconds";
		}
		if (timer == TIMER_LOSE) {
			message += " in " + (int) originalTime + " seconds.";
		} else {
			message += ".";
		}
		return message;
	}
	/**Checks if the mission has been completed*/
	public boolean verify() {
		//If the current level has a timer, count down
		if ((timer == TIMER_WIN || timer == TIMER_LOSE) && !Main.state.game.buttons[Game.ITEMS].buttonState) {
			time -= Main.info.getDeltaTime();
		}
		//If mission is to kill certain enemy/number of enemies and it has been accomplished
		if (killNum == -1000 || (mission == KILL && enemyToKill == null && killNum <= 0)) return true;
		//If mission is to survive for amount of time
		if (timer == TIMER_WIN) {
			if (time <= 0) return true;
			return false;
		}
		//If mission is to escape the maze
		if (mission == ESCAPE) {
			float[] coordinates = Main.state.game.generation.coordinates.get(Main.state.game.generation.types.indexOf(GameObject.player.index));
			//Player has escaped the maze
			if (coordinates[Generation.X] < 0 || coordinates[Generation.X] > Main.state.game.maze[Generation.X].length * Game.size || coordinates[Generation.Y] < 0 || coordinates[Generation.Y] > Main.state.game.maze.length * Game.size) {
				return true;
			}
		}
		return false;
	}
	/**Converts the mission into list of strings*/
	public List<String> getSaveableMission() {
		List<String> save = new ArrayList<>();
		int itemToCreate = -1;
		int enemyToKill = -1;
		if (this.itemToCreate != null) itemToCreate = this.itemToCreate.index;
		if (this.enemyToKill != null) enemyToKill = this.enemyToKill.index;
		save.add(mission + "");
		save.add(timer + "");
		save.add(isDark + "");
		save.add(respawnEnemies + "");
		save.add(killNum + "");
		save.add(originalTime + "");
		save.add(itemToCreate + "");
		save.add(enemyToKill + "");
		save.add(description);
		return save;
	}
}