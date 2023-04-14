package com.microtech.build.gradle

import org.gradle.api.Project
import org.gradle.testfixtures.ProjectBuilder
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.File

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {

        val project: Project = ProjectBuilder.builder()
            .withProjectDir(File("/Users/guobo/Workspace/weitai/aidex-x/app/"))
            .build()

        project.pluginManager.apply("com.microtech.plugins.resourcesId")

        assertEquals(4, 2 + 2)
    }
}