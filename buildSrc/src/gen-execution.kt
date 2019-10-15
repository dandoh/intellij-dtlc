package org.ice1000.tt.gradle

import org.ice1000.tt.gradle.json.LangData
import org.intellij.lang.annotations.Language
import java.io.File

fun LangData.execution(nickname: String, configName: String, outDir: File) {
	@Language("java")
	val runConfigFactoryJava = """
	package $basePackage.execution;

	import com.intellij.execution.configurations.ConfigurationFactory;
	import com.intellij.openapi.project.Project;
	import org.jetbrains.annotations.NotNull;
	
	public class ${languageName}RunConfigurationFactory extends ConfigurationFactory {
		public ${languageName}RunConfigurationFactory(${languageName}RunConfigurationType type) {
			super(type);
		}
		
		@NotNull
		@Override
		public ${languageName}RunConfiguration createTemplateConfiguration(@NotNull Project project) {
			return new ${languageName}RunConfiguration(project, this);
		}
	}
	""".trimIndent()

	@Language("java")
	val runConfigTypeJava = """
	package $basePackage.execution;

	import com.intellij.execution.configurations.ConfigurationFactory;
	import com.intellij.execution.configurations.ConfigurationType;
	import icons.TTIcons;
	import org.ice1000.tt.TTBundle;
	import org.jetbrains.annotations.Nls;
	import org.jetbrains.annotations.NotNull;

	import javax.swing.*;

	public class ${languageName}RunConfigurationType implements ConfigurationType {

		ConfigurationFactory factory;
		private ConfigurationFactory[] factories;
		private static ${languageName}RunConfigurationType instance;
		
		synchronized public static ${languageName}RunConfigurationType getInstance() {
			if (instance == null) {
				instance = new ${languageName}RunConfigurationType();
			}
			return instance;
		}

		private ${languageName}RunConfigurationType() {
			factory = new ${languageName}RunConfigurationFactory(this);
			factories = new ConfigurationFactory[]{factory};
		}

		@NotNull
		@Override
		public String getDisplayName() {
			return TTBundle.message("$nickname.name");
		}

		@Nls
		@Override
		public String getConfigurationTypeDescription() {
			return TTBundle.message("$nickname.run-config.description");
		}

		@Override
		public Icon getIcon() {
			return TTIcons.$constantPrefix;
		}

		@NotNull
		@Override
		public String getId() {
			return "${constantPrefix}_RUN_CONFIG_ID";
		}

		@Override
		public ConfigurationFactory[] getConfigurationFactories() {
			return factories;
		}
	}
		
	""".trimIndent()

	@Language("kotlin")
	val runConfig = """
	package $basePackage.execution

	import com.intellij.execution.Executor
	import com.intellij.execution.actions.ConfigurationContext
	import com.intellij.execution.actions.LazyRunConfigurationProducer
	import com.intellij.execution.actions.RunConfigurationProducer
	import com.intellij.execution.configurations.ConfigurationFactory
	import com.intellij.execution.configurations.ConfigurationType
	import com.intellij.execution.runners.ExecutionEnvironment
	import com.intellij.openapi.actionSystem.CommonDataKeys
	import com.intellij.openapi.fileChooser.FileChooserDescriptorFactory
	import com.intellij.openapi.project.Project
	import com.intellij.openapi.util.JDOMExternalizerUtil
	import com.intellij.openapi.util.Ref
	import com.intellij.openapi.util.io.FileUtilRt
	import com.intellij.psi.PsiElement
	import icons.TTIcons
	import $basePackage.${languageName}FileType
	import $basePackage.TTBundle
	import $basePackage.execution.ui.InterpretedRunConfigurationEditorImpl
	import $basePackage.project.*
	import $basePackage.validateExe
	import org.jdom.Element


	class ${languageName}RunConfiguration(
		project: Project,
		factory: ConfigurationFactory
	) : InterpretedRunConfiguration<${languageName}CommandLineState>(project, factory, TTBundle.message("$nickname.name")) {
		var ${configName}Executable = project.${configName}Settings.settings.exePath
		init { $runConfigInit }

		override fun getState(executor: Executor, environment: ExecutionEnvironment) = ${languageName}CommandLineState(this, environment)
		override fun getConfigurationEditor() = ${languageName}RunConfigurationEditor(this, project)

		override fun readExternal(element: Element) {
			super.readExternal(element)
			JDOMExternalizerUtil.readField(element, "${configName}Executable").orEmpty().let { ${configName}Executable = it }
		}

		override fun writeExternal(element: Element) {
			super.writeExternal(element)
			JDOMExternalizerUtil.writeField(element, "${configName}Executable", ${configName}Executable)
		}
	}

	class ${languageName}RunConfigurationEditor(
		configuration: ${languageName}RunConfiguration,
		project: Project
	) : InterpretedRunConfigurationEditorImpl<${languageName}RunConfiguration>(project) {
		init {
			targetFileField.addBrowseFolderListener(TTBundle.message("$nickname.ui.run-config.select-$nickname-file"),
				TTBundle.message("$nickname.ui.run-config.select-$nickname-file.description"),
				project,
				FileChooserDescriptorFactory.createSingleFileDescriptor(${languageName}FileType))
			exePathField.addBrowseFolderListener(TTBundle.message("$nickname.ui.project.select-compiler"),
				TTBundle.message("$nickname.ui.project.select-compiler.description"),
				project,
				FileChooserDescriptorFactory.createSingleFileOrExecutableAppDescriptor())
			resetEditorFrom(configuration)
		}

		override fun resetEditorFrom(s: ${languageName}RunConfiguration) {
			super.resetEditorFrom(s)
			exePathField.text = s.${configName}Executable
		}

		override fun applyEditorTo(s: ${languageName}RunConfiguration) {
			super.applyEditorTo(s)
			s.${configName}Executable = exePathField.text
		}
	}

	class ${languageName}RunConfigurationProducer : LazyRunConfigurationProducer<${languageName}RunConfiguration>() {
		override fun getConfigurationFactory() = ${languageName}RunConfigurationType.getInstance().factory
		override fun isConfigurationFromContext(
			configuration: ${languageName}RunConfiguration, context: ConfigurationContext) =
			configuration.targetFile == context.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)?.path

		override fun setupConfigurationFromContext(
			configuration: ${languageName}RunConfiguration, context: ConfigurationContext, ref: Ref<PsiElement>): Boolean {
			val file = context.dataContext.getData(CommonDataKeys.VIRTUAL_FILE)
			if (file?.fileType != ${languageName}FileType) return false
			val config = context.project.${configName}SettingsNullable ?: return false
			configuration.targetFile = file.path
			configuration.workingDir = context.project.basePath.orEmpty()
			configuration.name = FileUtilRt.getNameWithoutExtension(configuration.targetFile)
				.takeLastWhile { it != '/' && it != '\\' }
			val existPath = config.settings.exePath
			if (validateExe(existPath)) configuration.${configName}Executable = existPath
			else {
				val exePath = ${configName}Path ?: return true
				if (validateExe(exePath)) configuration.${configName}Executable = exePath
			}
			return true
		}
	}
	"""
	@Language("kotlin")
	val cliState = """
	package $basePackage.execution

	import com.intellij.execution.runners.ExecutionEnvironment

	class ${languageName}CommandLineState(
		configuration: ${languageName}RunConfiguration,
		env: ExecutionEnvironment
	) : InterpretedCliState<${languageName}RunConfiguration>(configuration, env) {
		override fun ${languageName}RunConfiguration.pre(params: MutableList<String>) {
			params += ${configName}Executable
		}
	}
	"""
	val exe = outDir.resolve("execution").apply { mkdirs() }
	exe.resolve("$nickname-generated.kt").writeText(runConfig)
	exe.resolve("${languageName}RunConfigurationFactory.java").writeText((runConfigFactoryJava))
	exe.resolve("${languageName}RunConfigurationType.java").writeText((runConfigTypeJava))
	if (generateCliState) exe.resolve("$nickname-cli-state.kt").writeText(cliState)
}
