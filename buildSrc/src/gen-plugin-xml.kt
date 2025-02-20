package org.ice1000.tt.gradle

import org.intellij.lang.annotations.Language

fun LanguageUtilityGenerationTask.pluginXml(nickname: String) {
	pluginXmlDir.mkdirs()
	val pluginXml = pluginXmlDir.resolve("plugin-$nickname-generated.xml")
	@Language("XML")
	val pluginXmlContent = """
<idea-plugin>
	<extensions defaultExtensionNs='com.intellij'>
		<liveTemplateContext implementation="org.ice1000.tt.editing.${languageName}DefaultContext"/>
		<configurationType implementation="org.ice1000.tt.execution.${languageName}RunConfigurationType"/>
		<runConfigurationProducer implementation="org.ice1000.tt.execution.${languageName}RunConfigurationProducer"/>
		<lang.refactoringSupport language="$languageName" implementationClass="org.ice1000.tt.editing.InplaceRenameRefactoringSupportProvider"/>

		<projectConfigurable
			groupId="language"
			id="TT.$languageName.Configurable"
			displayName="$languageName"
			instance="org.ice1000.tt.project.${languageName}ProjectConfigurable"/>
	</extensions>

	<project-components>
		<component>
			<implementation-class>org.ice1000.tt.project.${languageName}ProjectSettingsService</implementation-class>
		</component>
	</project-components>
</idea-plugin>
"""
	pluginXml.apply { if (!exists()) createNewFile() }.writeText(pluginXmlContent)
	val pluginCss = pluginXmlDir.resolve("$languageName-template.css")

	pluginCss.apply { if (!exists()) createNewFile() }.writeText(buildString {
		appendln("/* Token types. */")
		highlightTokenPairs.forEach { (name, highlight) ->
			appendln(".$languageName .${constantPrefix}_$name {")
			when (highlight) {
				"KEYWORD" -> appendln("color: #000080;").appendln("font-weight: bold;")
				"LINE_COMMENT", "BLOCK_COMMENT" -> appendln("color: #808080;").appendln("font-style: italic;")
				"IDENTIFIER" -> appendln("color: #000000;")
				"STRING" -> appendln("color: #008000;")
				"NUMBER" -> appendln("color: #0000FF;")
				"METADATA" -> appendln("color: #808000;")
			}
			appendln("}")
		}
		appendln()
		appendln("/* Standard attributes. */")
		appendln(".$languageName a { text-decoration: none; }")
		appendln(".$languageName :target { border: 2px solid #ECFAEB; background-color: #ECFAEB; }")
		appendln(".$languageName a[href]:hover { color: #0000FF; text-decoration: underline; text-decoration-color: #0000FF; }")
	})
}
