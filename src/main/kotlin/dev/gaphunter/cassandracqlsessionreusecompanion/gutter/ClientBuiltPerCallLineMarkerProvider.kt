package dev.gaphunter.cassandracqlsessionreusecompanion.gutter

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbAware
import com.intellij.psi.PsiElement
import dev.gaphunter.cassandracqlsessionreusecompanion.detect.JavaClientBuildFinder
import dev.gaphunter.cassandracqlsessionreusecompanion.detect.KotlinClientBuildFinder
import dev.gaphunter.cassandracqlsessionreusecompanion.model.ClientBuildHit
import dev.gaphunter.cassandracqlsessionreusecompanion.review.ReviewPrompt

class ClientBuiltPerCallLineMarkerProvider : LineMarkerProviderDescriptor(), DumbAware {

    override fun getName(): String = "Cassandra CqlSession built inside a method"

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<*>? = null

    override fun collectSlowLineMarkers(elements: MutableList<out PsiElement>, result: MutableCollection<in LineMarkerInfo<*>>) {
        val file = elements.firstOrNull()?.containingFile ?: return
        val hits = when (file.language.id) {
            "JAVA" -> JavaClientBuildFinder.findAll(file)
            "kotlin" -> KotlinClientBuildFinder.findAll(file)
            else -> emptyList()
        }
        if (hits.isEmpty()) return

        val hitsByElement = hits.associateBy { it.callElement }
        for (element in elements) {
            val hit = hitsByElement[element] ?: continue
            result.add(buildMarker(hit))

            val path = file.virtualFile?.path ?: continue
            val lineNumber = file.viewProvider.document?.getLineNumber(element.textRange.startOffset) ?: -1
            ReviewPrompt.recordHit(file.project, "$path:$lineNumber")
        }
    }

    private fun buildMarker(hit: ClientBuildHit): LineMarkerInfo<PsiElement> {
        val tooltip = "CqlSession.builder()....build() is called here inside a method -- DataStax's own best " +
            "practices say this root object is expensive to create (it initializes and maintains connection " +
            "pools to every node in the cluster); create one session per application and reuse it for the " +
            "application's entire lifetime"
        return LineMarkerInfo(
            hit.callElement,
            hit.callElement.textRange,
            ClientReuseIcons.RISK,
            { _: PsiElement -> tooltip },
            null,
            GutterIconRenderer.Alignment.RIGHT,
            { tooltip },
        )
    }
}
