# Mazescape
Mazescape is a cross-platform maze exploration game for desktop and Android. The player is placed in a randomly generated maze, and must complete a given mission. Missions can include surviving for a certain amount of time, killing enemies, or creating items. Items are spread out around the ground. There are weapons, materials, and health. Gathering certain items allows the player to upgrade to other items.

The game has enemies that use the A* pathfinding algorithm to track down the player. Some enemies can shoot the player, others can only hurt the player by contact. The missions, which are randomly generated, get progressively more difficult.

## Setup
### From the Release
The game can be run on desktop computers or Android, by downloading the jar or APK from the release ([https://github.com/chrisvettese/Mazescape/releases](https://github.com/chrisvettese/Mazescape/releases))
### From the Source Code
The game can be built by cloning the repository, and running `gradlew desktop:dist` (for desktop) or `gradlew android:assembleDebug` (for Android - debug mode only) in the top level folder. This will generate a jar file that can be run (desktop) or an APK file that can be installed (Android).

Video demonstration: [https://www.youtube.com/watch?v=xHNZFTduW2w](https://www.youtube.com/watch?v=xHNZFTduW2w)

![The player attacking an enemy.](https://raw.githubusercontent.com/chrisvettese/Mazescape/master/i2.png)
The player attacking an enemy.
