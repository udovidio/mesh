# Gentics Mesh - OrientDB Demo

The demo module contains the demo server runner and pom.xml build logic which generates the `mesh-demo.jar`. It also contains the `DockerFile` to build the final image.

The build process will clone the github demo project `https://github.com/gentics/mesh-angular-example.git` (demo branch) and add it to the demo dump zip which is part of the demo.
