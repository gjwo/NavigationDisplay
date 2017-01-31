# NavigationDisplay
This program is the client side of RPISensors, it provides a number of ways to access sensor information including:
1. A Compass
2. A Rotating Cube
3. A live chart of roll pitch & yaw
4. A radar display

It also provides access to the logging system, a means of starting and stopping subsystems, and a means of controlling the tank's motors.


The underlying system uses the Remote Method Interface (RMI) to communicate with the server.

Compiling does have dependencies on RPISensors, JfreeChart, and j3d. see
https://github.com/gjwo/RPISensors
http://jfree.org/jfreechart/index.html
https://java.net/projects/java3d
