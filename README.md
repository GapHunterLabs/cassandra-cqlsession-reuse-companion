# Cassandra CqlSession Reuse Companion

Gutter warning icon on a `CqlSession.builder()....build()` construction
chain (DataStax Java driver for Apache Cassandra) written inside a
regular method body — DataStax's own best practices documentation
states: "These root objects are expensive to create because they
initialize and maintain connection pools to every node in a
cluster... Create one session instance for each application, and then
reuse that session for the entire lifetime of the application."

## Why it exists

`CqlSession session = CqlSession.builder().build();` compiles fine and
returns a working session — call it once per request handler and every
single call quietly spins up new connection pools to every node in the
cluster, instead of reusing the one session instance the application
already has.

## Why built this way

- **100% static text/PSI analysis** — matches the builder chain by
  simple text, so it works whether the real DataStax driver jar is on
  the classpath or not. Java and Kotlin.
- **Confirmed gap**: JetBrains supports Cassandra only via Database
  Tools (data/CQL browsing), and the one third-party Cassandra plugin
  found is a syntax-highlighting tool — neither checks `CqlSession`
  reuse in application code.

## v0.1 scope — stated honestly, not exhaustively

Only flags the direct `CqlSession.builder()....build()` chain. Never
flags a session obtained by reference from an existing shared
instance/dependency injection, or a call inside a constructor
(legitimate "create once" locations). Matches by simple text, not real
type resolution.

## Usage

Open any Java/Kotlin file using the DataStax Cassandra driver. A
session built inside a regular method shows a warning icon.

## Enterprise / Team Licensing

Need enterprise features, custom rules, or team licensing? Contact us at
**gaphunterlabs@gmail.com**.

## Development

```
./gradlew test           # unit tests
./gradlew buildPlugin    # generates build/distributions/*.zip
./gradlew verifyPlugin   # checks compatibility against real IDEs
```

## License

Apache-2.0. See `LICENSE`.
