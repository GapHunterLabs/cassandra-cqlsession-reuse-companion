// Demo data for Cassandra CqlSession Reuse Companion -- used with
// `./gradlew runIde` to capture the real Marketplace screenshot. Open
// this file, the warning should appear on the CqlSession.builder()
// line inside saveUnsafely.

class OrderRepository {

    private final CqlSession session;

    OrderRepository() {
        // Built once, in the constructor -- NOT flagged.
        this.session = CqlSession.builder().build();
    }

    void saveUnsafely(Order order) {
        // A new session built here on every call -- FLAGGED. Each
        // instance spins up its own connection pools to every node.
        CqlSession session = CqlSession.builder().build();
        session.execute("INSERT INTO orders (id) VALUES (?)", order.getId());
    }

    void saveSafely(Order order) {
        // Reuses the instance built once in the constructor -- NOT
        // flagged.
        session.execute("INSERT INTO orders (id) VALUES (?)", order.getId());
    }
}
