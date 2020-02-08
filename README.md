# iland
java lwjgl 3 engine and test "games"


GENERAL
The project currently consists of three packages: engine, iland, and tiland.

engine is light weight 3d game engine.

iland is a 3d 3rd person "open world"

tiland is a 2.5d side scroller

Engine subsystems each go in their own package and ideally a one way dependency hierarchy will be maintained among those subsystems.

The iland open world is a single world for testing engine features and should be a "playground". It's okay to load other worlds for testing specific features, or creating labeled "portals" to load them.

tiland is for verifying that 2.5d methodologies cooperate well with the engine.

There are hidden test GUIs behind the Tab key.

REFERENCES
The core engine framework is heavily based off of this free online book, with some deviations where it suites my tastes. It is slowly evolving quite a bit away from that foundation, especially the scene, shader, and UI systems.
https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/

I've implemented through chapter 7, then skipped to chapter 27ish and adapted ASSIMP FBX static mesh loading. We will use FBX animation assets, NOT MD5 as described in the book. Now I'm looping back to chapter 8 to continue with lighting etc.

For short term purposes it may be suitable to load MD5 only if through ASSIMP, since swapping MD5 out for FBX should be "plug and play".

iland Game.java includes an FBX scene loading technique whereby a scene FBX is loaded, custom properties allow the engine scene loader to callback to game.java to instantiate the scene entities as custom game types. Terrain is the only current example.



MAVEN
Modify project settings and dependencies via Maven pom.xml. Get initial dependencies via Maven import.



DEPENDENCIES
Java 13 JDK - language
lwjgl 3 - hardware and OS access
JOML - 3d math
ASSIMP - asset importing
GLFW - input devices



3D ASSETS & RESOURCES

I started with Blender 2.79b but there were a lot of hoops and problems with their old outdated FBX support. Blender 2.8+ solves all of them, so I'm doing a big update on this section.

Use Blender 2.8 (or newer compatible)

Save .blend files to subfolders of src/main/assets and copy or process them to src/main/resources. Resources subfolders for iland and tiland specific files exist, push stuff down there. I haven't made a similar subfolder structure for /assets yet.

Export FBX to subfolders of src/main/assets next to sibling .blend files.
  selected only for individual assets, or whole scene for scene loading.
  Z up, Y forward (orientation doesn't seem to make a difference)

All resource paths and filenames must be all lower case to maintain Linux compatibility. FBX will bake mixed-case file references, but the code will toLowerCase() filenames - at least for textures where it has been a problem. All code paths and filenames must be lower case only.

Blender allows "custom properties" to be specified on objects. These are the main method for shuttling metadata into the game scene. We can use these to give a scene object a data type, name, etc, and the scene loader can instantiate the correct type of game object to represent it. 

It's also import to make sure instance of the same mesh are actually being instanced. The scene does not necessarily have to be used for the mesh data that it contains - just as visual references and metadata.



EXPORT TROUBLESHOOTING
- If FBX Converter bakes in its own (incorrect) relative path to textures, it is because the textures referenced in Blender are no longer in the specified location. Reassign the textures or restore them, then re-export. This happened once after relocating the project files.



WORLD EDITOR
Voxel open world: I've experimented with a unique "capped voxel" system previously and the results were promising. Generate curved terrain, split the surfaces into block caps on XZ, fill the column below with minecraft-esque blocks.
Allow normally facetted indestructable props to be placed anywhere in the world. This gives a world similar to Terraria or Starbound, but 3D. The terrain can be kept "whole" in each chunk until it is "broken" or manipulated by user action.

Else: As for Blender, I've previously created a custom exporter that rips the scene down to object positions and asset references, along with an engine loader that rebuilds the scene approximation in-game. I liked the workflow and I'll consider that direction if I pass on voxels.

update* I've implemented a very early framework for this, you can see it in iland/game.java's scene loading callbacks. Terrain is the only supported type.



CRUNCH
There is no existing crunch process. For now load all Blender assets via FBX. I'm considering an automated pre-crunch step that checks for FBX resources before crunched resources, crunches them, and then loads the crunched versions and deletes the FBX (the one in resource subfolder, not assets).
This will simplify engine usage.



AVATARS
I'm going to try to model an avatar similar to Nintendo Mii in Blender, and make it modular and customizable. If it works, I'll create a rig and test animation for it.

A game system can be made to customize Avatars and roll NPCs for the open world. Perhaps the animation system can use those assets.



ANIMATION subsystem
The animation subsystem should support 3D and "2.5d" animation. This shouldn't affect system implementation whatsoever, but just in case...

