package dev.gaphunter.cassandracqlsessionreusecompanion.detect

import com.intellij.psi.JavaRecursiveElementWalkingVisitor
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.PsiMethod
import com.intellij.psi.PsiMethodCallExpression
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.cassandracqlsessionreusecompanion.model.ClientBuildHit

/**
 * Finds `CqlSession.builder()....build()` (DataStax Java driver for
 * Apache Cassandra) construction chains written inside a
 * non-constructor method body -- DataStax's own best-practices
 * documentation states: "These root objects are expensive to create
 * because they initialize and maintain connection pools to every node
 * in a cluster... Create one session instance for each application,
 * and then reuse that session for the entire lifetime of the
 * application." Building one inside a regular method means a brand
 * new connection pool to every cluster node on every call.
 *
 * **v0.1 scope, stated honestly:** matches by simple text, not real
 * type resolution, so it works whether the real DataStax driver jar is
 * on the classpath or not -- an unrelated `CqlSession` class from a
 * different library is a possible (rare) false positive. Only the
 * "build from scratch" shape is flagged; a session obtained by
 * reference from an existing shared instance/dependency injection is
 * never flagged.
 */
object JavaClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : JavaRecursiveElementWalkingVisitor() {
            override fun visitMethodCallExpression(expression: PsiMethodCallExpression) {
                super.visitMethodCallExpression(expression)
                hitForBuilderBuild(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForBuilderBuild(buildCall: PsiMethodCallExpression): ClientBuildHit? {
        if (buildCall.methodExpression.referenceName != "build") return null
        val qualifier = buildCall.methodExpression.qualifierExpression ?: return null
        // The chain between `CqlSession.builder()` and `.build()` may have
        // any number of `.addContactPoint(...)`/`.withKeyspace(...)` etc.
        // calls -- matching the full qualifier text's start avoids
        // re-walking each intermediate call by hand (same fix as the real
        // bug found in apache-httpclient-reuse-companion #38).
        if (!qualifier.text.startsWith("CqlSession.builder(")) return null
        return hitIfNotInConstructor(buildCall)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        val containingMethod = PsiTreeUtil.getParentOfType(element, PsiMethod::class.java) ?: return null
        if (containingMethod.isConstructor) return null
        return ClientBuildHit(leafOf(element))
    }

    /** Descends to a real leaf PSI element -- LineMarkerInfo must never anchor on a composite node (SDK_GOTCHAS.md SS20). */
    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
