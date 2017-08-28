package com.mkobit.jenkins.pipelines

import org.assertj.core.api.Assertions.assertThat
import org.eclipse.jgit.api.Git
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Tag
import org.junit.jupiter.api.Test
import testsupport.NotImplementedYet
import testsupport.build
import testsupport.resourceText
import testsupport.writeRelativeFile
import java.io.File

@Tag("integration")
internal class IntegrationTestSourceIntegrationTest {

  private lateinit var projectDir: File

  @BeforeEach
  internal fun setUp() {
    projectDir = createTempDir().apply { deleteOnExit() }
  }

  @Disabled("may not be artifacts but file dependencies with current hack")
  @Test
  internal fun `Jenkins Pipeline Shared Groovy Libraries Plugin JAR available in integrationTestCompileClasspath configuration`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript() + """
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier

tasks.create('printOutDependencies') {
  doFirst {
    final configuration = configurations.getByName('integrationTestCompileClasspath')
    configuration.resolvedConfiguration.resolvedArtifacts.each { ResolvedArtifact artifact ->
      final ModuleVersionIdentifier identifier = artifact.moduleVersion.id
      println("Artifact: ${'$'}{identifier.group}:${'$'}{identifier.name}:${'$'}{artifact.extension}")
    }
  }
}
"""
    }

    val buildResult: BuildResult = build(projectDir, "printOutDependencies")

    assertThat(buildResult.output).contains("Artifact: org.jenkins-ci.plugins.workflow:workflow-cps-global-lib:jar")
  }

  @Disabled("may not be artifacts but file dependencies with current hack")
  @Test
  internal fun `Jenkins Pipeline Shared Groovy Libraries Plugin HPI available in integrationRuntimeClasspath configuration`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript() + """
import org.gradle.api.artifacts.ResolvedArtifact
import org.gradle.api.artifacts.ModuleVersionIdentifier

tasks.create('printOutDependencies') {
  doFirst {
    final configuration = configurations.getByName('integrationTestRuntimeClasspath')
    configuration.resolvedConfiguration.resolvedArtifacts.each { ResolvedArtifact artifact ->
      final ModuleVersionIdentifier identifier = artifact.moduleVersion.id
      println("Artifact: ${'$'}{identifier.group}:${'$'}{identifier.name}:${'$'}{artifact.extension}")
    }
  }
}
"""
    }

    val buildResult: BuildResult = build(projectDir, "printOutDependencies")

    assertThat(buildResult.output).contains("Artifact: org.jenkins-ci.plugins.workflow:workflow-cps-global-lib:hpi")
  }

  @NotImplementedYet
  @Test
  internal fun `no HPI artifacts are available in integrationTestImplementation configuration`() {
  }

  @Test
  internal fun `can compile integration test sources that use Jenkins libraries`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript()
    }

    projectDir.writeRelativeFile("test", "integration", "groovy", "com", "mkobit", fileName = "ImportClassesCompilationTest.groovy") {
      resourceText("com/mkobit/ImportClassesCompilationTest.groovy")
    }

    val buildResult: BuildResult = build(projectDir, "compileIntegrationTestGroovy", "-s", "-i")

    val task = buildResult.task(":compileIntegrationTestGroovy")
    assertThat(task?.outcome)
      .describedAs("integrationTestCompileGroovy task outcome")
      .withFailMessage("Build output: ${buildResult.output}")
      .isNotNull()
      .isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  internal fun `can use @JenkinsRule in integration tests`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript()
    }

    projectDir.writeRelativeFile("test", "integration", "groovy", "com", "mkobit", fileName = "JenkinsRuleUsageTest.groovy") {
      resourceText("com/mkobit/JenkinsRuleUsageTest.groovy")
    }

    val buildResult: BuildResult = build(projectDir, "integrationTest", "-s", "-i")

    val task = buildResult.task(":integrationTest")
    assertThat(task?.outcome)
      .describedAs("integrationTest task outcome")
      .withFailMessage("Build output: ${buildResult.output}")
      .isNotNull()
      .isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  internal fun `WorkflowJob can be created and executed in integration tests`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript()
    }

    projectDir.writeRelativeFile("test", "integration", "groovy", "com", "mkobit", fileName = "WorkflowJobUsageTest.groovy") {
      resourceText("com/mkobit/WorkflowJobUsageTest.groovy")
    }

    val buildResult: BuildResult = build(projectDir, "integrationTest", "-s", "-i")

    val task = buildResult.task(":integrationTest")
    assertThat(task?.outcome)
      .describedAs("integrationTest task outcome")
      .withFailMessage("Build output: ${buildResult.output}")
      .isNotNull()
      .isEqualTo(TaskOutcome.SUCCESS)
  }

  @Test
  internal fun `can set up Global Pipeline Library and use them in an integration test`() {
    projectDir.writeRelativeFile(fileName = "build.gradle") {
      groovyBuildScript()
    }

    projectDir.writeRelativeFile("src", "com", "mkobit", fileName = "LibHelper.groovy") {
      """
package com.mkobit

class LibHelper {
  private script
  LibHelper(script) {
    this.script = script
  }

  void sayHelloTo(String name) {
    script.echo "LibHelper says hello to ${'$'}name!"
  }
}
"""
    }

    projectDir.writeRelativeFile("test", "integration", "groovy", "com", "mkobit", fileName = "JenkinsGlobalLibraryUsageTest.groovy") {
      resourceText("com/mkobit/JenkinsGlobalLibraryUsageTest.groovy")
    }

    Git.init().setDirectory(projectDir).call().use {
      it.add().addFilepattern(".").call()
      it.commit().setMessage("Commit all the files").setAuthor("Mr. Manager", "mrmanager@example.com").call()
    }

    val buildResult: BuildResult = build(projectDir, "integrationTest", "-s", "-i")

    val task = buildResult.task(":integrationTest")
    assertThat(task?.outcome)
      .describedAs("integrationTest task outcome")
      .withFailMessage("Build output: ${buildResult.output}")
      .isNotNull()
      .isEqualTo(TaskOutcome.SUCCESS)
  }

  @NotImplementedYet
  @Test
  internal fun `can use 'stage' step in test`() {
  }

  @NotImplementedYet
  @Test
  internal fun `can use 'sh' step in test`() {
  }

  @NotImplementedYet
  @Test
  internal fun `can use 'node' step in test`() {
  }

  @NotImplementedYet
  @Test
  internal fun `GlobalLibraries is available for configuration in integration tests`() {
  }

  @NotImplementedYet
  @Test
  internal fun `integration test output for Jenkins Test Harness is in the build directory`() {
  }

  // TODO: this should be tested but may or may not be needed. I have a feeling classloader errors
  // will happen in pipeline code if those classes are available.
  @NotImplementedYet
  @Test
  internal fun `cannot use classes from main source code in integration test`() {
  }

  @NotImplementedYet
  @Test
  internal fun `can use declared plugin dependencies in integration test`() {
  }
}
