package org.ice1000.tt.psi

import com.intellij.openapi.progress.ProgressIndicatorProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.ResolveState
import com.intellij.psi.scope.PsiScopeProcessor

val PsiElement.elementType get() = node.elementType

/**
 * @return if a declaration <strong>not</strong> found.
 */
fun treeWalkUp(
	processor: PsiScopeProcessor,
	entrance: PsiElement,
	maxScope: PsiElement?,
	state: ResolveState = ResolveState.initial()): Boolean {
	if (!entrance.isValid) return false
	var prevParent = entrance
	var scope: PsiElement? = entrance

	while (scope != null) {
		ProgressIndicatorProvider.checkCanceled()
		if (!scope.processDeclarations(processor, state, prevParent, entrance)) return false
		if (scope == maxScope) break
		prevParent = scope
		scope = prevParent.context
	}
	return true
}
