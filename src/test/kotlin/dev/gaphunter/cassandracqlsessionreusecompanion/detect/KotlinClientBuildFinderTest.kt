package dev.gaphunter.cassandracqlsessionreusecompanion.detect

import com.intellij.testFramework.fixtures.BasePlatformTestCase

class KotlinClientBuildFinderTest : BasePlatformTestCase() {

    fun `test a session built inside a regular function is flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                fun save(order: Order) {
                    val session = CqlSession.builder().build()
                }
            }
            """.trimIndent(),
        )
        assertEquals(1, KotlinClientBuildFinder.findAll(file).size)
    }

    fun `test a session built as a class property is not flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                val session = CqlSession.builder().build()
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }

    fun `test an unrelated builder call is never flagged`() {
        val file = myFixture.configureByText(
            "OrderRepository.kt",
            """
            class OrderRepository {
                fun save(order: Order) {
                    val built = Order.builder().build()
                }
            }
            """.trimIndent(),
        )
        assertTrue(KotlinClientBuildFinder.findAll(file).isEmpty())
    }
}
