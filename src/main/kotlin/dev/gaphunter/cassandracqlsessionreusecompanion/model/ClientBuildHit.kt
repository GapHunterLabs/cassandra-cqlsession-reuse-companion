package dev.gaphunter.cassandracqlsessionreusecompanion.model

import com.intellij.psi.PsiElement

/** One `CqlSession.builder()....build()` call found inside a non-constructor method body. */
data class ClientBuildHit(val callElement: PsiElement)
