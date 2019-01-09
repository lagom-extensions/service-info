#### Lagom Service Info
Project is quite similar with spring actuator, but for Lagom based scala projects

##### Library dependencies used
- sbt build info
- sbt sbt-git
- spring actuator (jdbc for database checks)
- set of provided libraries required for specific health indicators

#### Development integration notes
##### 1. include dependency on service info plugin
TODO should first be published, for more clean docs

##### 2. Custom build info properties support
All values required to be strings. Payload will contains all such properties plus two default service and version.
