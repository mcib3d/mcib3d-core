# MCIB3D
The mcib3d library is the core for most of the mcib3d plugins developed. This is a joint effort to
create robust library for 3D images and objects for analysis. Main developers are Thomas Boudier and
Jean Ollion. 

Please follow these instructions to download and compile the source code : 

`git clone https://framagit.org/mcib3d/mcib3d-core.git`

`mvn package`

You then should be able to open the project in your favorite IDE. 

If you want to include this library as part as your maven project, use this dependency : 

```
<dependency>
		<groupId>org.framagit.mcib3d</groupId>
		<artifactId>mcib3d-core</artifactId>
		<version>4.0.1</version>		
</dependency>
```

and 

```
<repository>	
	<id>jitpack.io</id>
	<url>https://jitpack.io</url>
</repository>
```


  
  If you use MCIB library or plugins in your experiments, please cite : 
  J. Ollion, J. Cochennec, F. Loll, C. Escudé, and T. Boudier (2013). 
  TANGO: a generic tool for high-throughput 3D image analysis for studying nuclear organization.
  Bioinformatics 29(14):1840-1. doi: 10.1093/bioinformatics/btt276.
  
  Documentations are available on the wiki  : 
  https://mcib3d.frama.io/3d-suite-imagej/

