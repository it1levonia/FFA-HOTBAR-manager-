# Building

Use either Maven or Gradle. They are alternative builds of the same source; do not nest the project under another src folder.

Requirements: JDK 17, Maven 3.8+ **or** Gradle 8.x installed. Java output targets 8. No Maven/Gradle wrapper is included. Internet access is needed to resolve dependencies.

## Maven

```sh
mvn clean package
```

The plugin JAR is in `target/`. When Maven produces `original-*.jar`, deploy the final artifact instead of that unshaded intermediate.

## Gradle

```sh
gradle clean build
```

The plugin JAR is in `build/libs/`.

## Verification boundary

See VALIDATION.md for what was actually checked. A source compilation check is not an in-game test. Missing dependencies or API mismatches must be resolved before publishing a working-build claim.
