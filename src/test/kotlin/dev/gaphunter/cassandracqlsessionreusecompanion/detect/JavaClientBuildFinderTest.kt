package dev.gaphunter.cassandracqlsessionreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class JavaClientBuildFinderTest : BasePlatformTestCase() {

    fun `test a session built inside a regular method is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                void save(Order order) {
                    CqlSession session = CqlSession.builder().build();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test a builder chain with intermediate calls is still flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                void save(Order order) {
                    CqlSession session = CqlSession.builder().addContactPoint(address).withKeyspace("orders").build();
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, JavaClientBuildFinder.findAll(file).size)
    }

    fun `test a session built inside a constructor is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                private final CqlSession session;
                OrderRepository() {
                    session = CqlSession.builder().build();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated builder call is never flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.java",
            """
            class OrderRepository {
                void save(Order order) {
                    Order built = Order.builder().build();
                }
            }
            """.trimIndent(),
        )
        assertTrue(JavaClientBuildFinder.findAll(file).isEmpty())
    }
}
