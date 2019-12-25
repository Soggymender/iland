# iland
java lwjgl 3 engine and open world



GENERAL
The top level project folders are an open world called "game" utilizing a lower level folder structure called "engine".

Engine subsystems each go in their own folder and ideally a one way dependency hierarchy will be maintained among those subsystems.

The main animation system should go in a "animation" subsystem folder. If you want to create one entirely from scratch you can create a second animation subsystem in some other subfolder.

The open world is a single world for testing engine features and should be a "playground". It's okay to load other worlds for testing specific features, or creating labeled "portals" to load them.


REFERENCES
The core engine framework is heavily based off of this free online book, with some deviations where it suites my tastes.
https://lwjglgamedev.gitbooks.io/3d-game-development-with-lwjgl/content/

I've implemented through chapter 7, then skipped to chapter 27ish and adapted ASSIMP FBX static mesh loading. We will use FBX animation assets, NOT MD5 as described in the book. Now I'm looping back to chapter 8 to continue with lighting etc.



MAVEN
Modify project settings and dependencies via Maven pom.xml. Get initial dependencies via Maven import.



DEPENDENCIES
Java 13 JDK - language
lwjgl 3 - hardware and OS access
JOML - 3d math
ASSIMP - asset importing



3D ASSETS & RESOURCES
Blender 2.79b (or newer compatible)

Save .blend files to subfolders of src/main/assets.
Export FBX to subfolders of src/main/assets next to sibling .blend files.
  export as text 6.1, not binary
  selected only
  Z up, Y forward (orientation doesn't seem to make a difference)

Download autodesk FBX converter:
http://images.autodesk.com/adsk/files/fbx20133_converter_win_x64.exe

Convert Blender's FBX 6.1 Text to FBX 2013 Text.
Conversion destination should be subfolders of src/main/resources.

This conversion is necessary because Blender only supports 6.1, while the engine uses ASSIMP which only supports 2013. Eventually a custom FBX importer will be written, but for now the conversion time is less than the coding time. Also, the official FBX SDK only natively supports Python and C++. Haven't found a reasonable Java binding.



EXPORT TROUBLESHOOTING
- If FBX Converter bakes in its own (incorrect) relative path to textures, it is because the textures referenced in Blender are no longer in the specified location. Reassign the textures or restore them, then re-export. This happened once after relocating the project files.



WORLD EDITOR
Voxel open world: I've experimented with a unique "capped voxel" system previously and the results were promising. Generate curved terrain, split the surfaces into block caps on XZ, fill the column below with minecraft-esque blocks.
Allow normally facetted indestructable props to be placed anywhere in the world. This gives a world similar to Terraria or Starbound, but 3D. The terrain can be kept "whole" in each chunk until it is "broken" or manipulated by user action.

Else: As for Blender, I've previously created a custom exporter that rips the scene down to object positions and asset references, along with an engine loader that rebuilds the scene approximation in-game. I liked the workflow and I'll consider that direction if I pass on voxels.



CRUNCH
There is no existing crunch process. For now load all Blender assets via FBX Text 2013. I'm considering an automated pre-crunch step that checks for FBX resources before crunched resources, crunches them, and then loads the crunched versions and deletes the FBX (the one in resource subfolder, not assets).
This will simplify engine usage.



AVATARS
I'm going to try to model an avatar similar to Nintendo Mii in Blender, and make it modular and customizable. If it works, I'll create a rig and test animation for it.

A game system can be made to customize Avatars and roll NPCs for the open world. Perhaps the animation system can use those assets.



ANIMATION subsystem
The animation subsystem should support 3D and "2.5d" animation. This shouldn't affect system implementation whatsoever, but just in case...

