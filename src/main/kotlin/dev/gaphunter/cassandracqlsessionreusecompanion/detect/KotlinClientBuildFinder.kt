package dev.gaphunter.cassandracqlsessionreusecompanion.detect

import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.PsiTreeUtil
import dev.gaphunter.cassandracqlsessionreusecompanion.model.ClientBuildHit
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtConstructor
import org.jetbrains.kotlin.psi.KtDotQualifiedExpression
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtNamedFunction
import org.jetbrains.kotlin.psi.KtTreeVisitorVoid

/** Kotlin counterpart of [JavaClientBuildFinder]. */
object KotlinClientBuildFinder {

    fun findAll(file: PsiFile): List<ClientBuildHit> {
        if (file !is KtFile) return emptyList()
        val hits = mutableListOf<ClientBuildHit>()
        file.accept(object : KtTreeVisitorVoid() {
            override fun visitDotQualifiedExpression(expression: KtDotQualifiedExpression) {
                super.visitDotQualifiedExpression(expression)
                hitForBuilderBuild(expression)?.let { hits += it }
            }
        })
        return hits
    }

    private fun hitForBuilderBuild(expression: KtDotQualifiedExpression): ClientBuildHit? {
        val buildCall = expression.selectorExpression as? KtCallExpression ?: return null
        if (buildCall.calleeExpression?.text != "build") return null

        // The chain between `CqlSession.builder()` and `.build()` may have
        // any number of intermediate calls -- matching the full receiver
        // text's start avoids re-walking each intermediate call by hand.
        if (!expression.receiverExpression.text.startsWith("CqlSession.builder(")) return null

        return hitIfNotInConstructor(expression)
    }

    private fun hitIfNotInConstructor(element: PsiElement): ClientBuildHit? {
        if (PsiTreeUtil.getParentOfType(element, KtConstructor::class.java) != null) return null
        if (PsiTreeUtil.getParentOfType(element, KtNamedFunction::class.java) == null) return null
        return ClientBuildHit(leafOf(element))
    }

    private fun leafOf(element: PsiElement): PsiElement {
        var current = element
        while (current.firstChild != null) current = current.firstChild
        return current
    }
}
