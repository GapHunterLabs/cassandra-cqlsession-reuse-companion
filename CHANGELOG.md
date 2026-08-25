<!-- Keep a Changelog guide -> https://keepachangelog.com -->

# Cassandra CqlSession Reuse Companion Changelog

## [Unreleased]

## [0.1.0]

### Added

- Warning icon on a CqlSession.builder()....build() chain built inside
  a regular method instead of created once and reused -- matching
  DataStax's own best-practices docs: root session objects are
  expensive to create, reuse one per application.
- 100% static text/PSI analysis, Java and Kotlin, no network calls,
  no telemetry. Free.

[Unreleased]: https://github.com/GapHunterLabs/cassandra-cqlsession-reuse-companion/compare/0.1.0...HEAD
[0.1.0]: https://github.com/GapHunterLabs/cassandra-cqlsession-reuse-companion/commits/0.1.0
