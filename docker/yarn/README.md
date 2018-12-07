#### Objective: The objective of this script is to generate a sample hadoop cluster in Docker environment.
#### Pre-requisites: Docker needs to be installed on the machine. The script to do ensure that  can be found here: 
    SparkFHE/scripts/install_docker_ce.bash

#### Instructions:

1. **Make a Docker image for Hadoop Cluster**
This step does not need to be repeated again
```bash
sudo bash build_docker_image.bash
```
2. **Setup Hadoop Cluster in Docker Containers**
We also need to specify the number of nodes in cluster as a parameter. The number should be greater than 2.The URL for hadoop cluster should be generated in the end. Open that to view details about job and cluster.
```bash
sudo bash generate_cluster.bash 5
```

3. **Stop hadoop cluster and clean the scene**
The URL will not work from this point. Repeat From Step 2 onwards if required.
```bash
sudo bash stop_cluster.bash
```
