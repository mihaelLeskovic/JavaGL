# Bachelor degree final thesis
## 3D plane simulator

- implemented in Java, using LWJGL; implements its own 3D graphics and physics engine
- offers compatibility for Windows and Linux (tested) and potentially for macOS (untested, but compiled and packaged)

HOW TO USE:
- download relevant plane_simulator_[OS].zip file
- unpack it
- in terminal, position yourself in the unpacked directory (where the plane_simulator.jar is)
- use command: 'java -jar plane_simulator.jar' to run

CONTROLS:
- T to toggle engine
- 0-9 to choose engine power level
- WASDQE for pitch, yaw and roll control

ADDITIONAL ARGUMENTS:
- used in typical argument fashion: 'java -jar plane_simulator.jar [space-split arguments]'
'-enableHitboxes' - make hitboxes visible
'-enableFreecam' - disable simulation mode and control the camera in a freecam mode; WSADQE for controls, C for culling, V to set the mouse free
