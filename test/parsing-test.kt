package org.ice1000.tt

import com.intellij.testFramework.ParsingTestCase
import org.ice1000.tt.psi.MiniTTParserDefinition

class MiniTTParsingTest : ParsingTestCase("parse/minitt", "minitt", MiniTTParserDefinition()) {
	override fun getTestDataPath() = "testData"
	fun `testparse-only`() = doTest(true)
	fun `testlocal-binding`() = doTest(true)
	fun testreference() = doTest(true)
	fun testsimple() = doTest(true)
	fun testsubtyping() = doTest(true)
	fun `testsum-literals`() = doTest(true)
	fun `testsyntacic-sugar`() = doTest(true)
	fun testunicode() = doTest(true)
	fun testunivese() = doTest(true)
}
