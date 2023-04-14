package com.microtech.plugins

import com.android.build.gradle.api.BaseVariantOutput
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.microtech.plugins.extensions.MicrotechPluginExtension
import com.microtech.plugins.tasks.ResourceIdTask
import com.microtech.plugins.tasks.SaveAndInitResourceIdTxtTask
import com.microtech.plugins.utils.PUtils
import org.gradle.api.Plugin
import org.gradle.api.Project

class ResourcesIdPlugin: Plugin<Project> {

    private val tag = "[microtech] ResourcesIdPlugin"
    private lateinit var project: Project

    private fun logI(msg: String) {
        println("$tag $msg")
    }
    private fun logE(msg: String) {
        println("$tag $msg")
    }

    override fun apply(target: Project) {
        project = target

        project.extensions.create(MicrotechPluginExtension::class.java, PUtils.microtechPluginExtensionName, MicrotechPluginExtension::class.java)

        target.afterEvaluate { project ->

            logI("当前tasks: ${project.tasks.size}")
            val androidExtension = project.extensions.getByName("android") as BaseAppModuleExtension

            androidExtension.applicationVariants.all {
                logI("application变体： ${it.name}")
                logI("resourceIdTask 配置开始")

                val resourceIdTask: ResourceIdTask = project.tasks.create("microtechProcess${it.name}ResourceId", ResourceIdTask::class.java)
                resourceIdTask.variant = it
                val applicationId = PUtils.getApplicationId(project, it)
                if (applicationId.isNullOrEmpty()) {
                    logE("getApplicationId 为空 结束资源id固定")
                    return@all
                }
                resourceIdTask.applicationId = applicationId

                val processManifestTask = PUtils.getProcessManifestTask(project, it)
                if (processManifestTask == null) {
                    logE("processManifestTask 为空 结束资源id固定")
                    return@all
                }
                resourceIdTask.mustRunAfter(processManifestTask)

                val processResourcesTask = PUtils.getProcessResourcesTask(project, it)
                if (processResourcesTask == null) {
                    logE("processResourcesTask 为空 结束资源id固定")
                    return@all
                }
                processResourcesTask.dependsOn(resourceIdTask)

                val mergeResourcesTask = PUtils.getMergeResourcesTask(project, it)
                if (mergeResourcesTask == null) {
                    logE("mergeResourcesTask 为空 结束资源id固定")
                    return@all
                }
                resourceIdTask.dependsOn(mergeResourcesTask)

                logI("${it.name} resourceIdTask配置结束")


                // 打包后生成public.txt文件
                val genPublicTxtTask: SaveAndInitResourceIdTxtTask = project.tasks.create("microtechGen${it.name}PublicTxt", SaveAndInitResourceIdTxtTask::class.java)
                it.outputs.all { o: BaseVariantOutput ->
                    logI("outputs ${o.name} ${o.outputFile.absolutePath} ")
                    genPublicTxtTask.applicationVariant = it
                    genPublicTxtTask.variant = o
                    genPublicTxtTask.flavorName = it.flavorName
                    genPublicTxtTask.buildType = it.buildType
                    genPublicTxtTask.outputFolder = o.dirName
                    val assembleTask = PUtils.getAssembleTask(project, it)
                    logI("$assembleTask")

                    assembleTask?.doLast {
                        logE("assembleTask dolast  ")
                        genPublicTxtTask.justDoIt()
                    }

                }

            }
        }
    }

}