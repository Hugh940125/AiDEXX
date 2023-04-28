package com.microtech.plugins.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.api.BaseVariantOutput
import com.android.builder.model.BuildType
import com.microtech.plugins.utils.PUtils
import com.microtech.plugins.utils.convertRTxtToPublicTxt
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import java.nio.file.Files
import java.nio.file.StandardCopyOption

open class SaveAndInitResourceIdTxtTask: DefaultTask() {

    private val tag = "[microtech] SaveAndInitResourceIdTxtTask"

    @Internal
    lateinit var variant: BaseVariantOutput

    @Internal
    lateinit var applicationVariant: ApplicationVariant

    @Internal
    lateinit var flavorName: String

    @Internal
    lateinit var buildType: BuildType

    @Internal
    var outputFolder: String = ""

    private fun logI(msg: String) {
        println("$tag $msg")
    }
    private fun logE(msg: String) {
        println("$tag $msg")
    }


    @TaskAction
    fun justDoIt() {
        logI("开始生成public.txt buildType=${buildType.name} flavor=$flavorName")
        logI("$outputFolder ${variant.outputFile.absolutePath} ${variant.outputFile.exists()} ${variant.outputFile.name}")

        // 打包完成后转移R.txt 到目标目录
        val rSymbolFileDir = project.file("${project.buildDir.absolutePath}/intermediates/runtime_symbol_list")
        val rTxtFile = project.file("${rSymbolFileDir.absolutePath}/$flavorName${buildType.name}/R.txt")
        logI("r.txt=${rTxtFile.absolutePath} 是否存在${rTxtFile.exists()}")

        // todo 可配置
        val distDir = "${project.buildDir.parent}/dist"
        val flavorPath = if (flavorName.isEmpty()) "" else "/$flavorName"
        val targetSaveDirPath = "$distDir/${buildType.name}$flavorPath"

        val targetSaveDir = project.file(targetSaveDirPath)
        if (!targetSaveDir.exists()) {
            targetSaveDir.mkdirs()
        }

        val bakDir = project.file("$targetSaveDirPath/bak")
        if (!buildType.name.endsWith("release", true)) {
            bakDir.deleteRecursively()
        }
        if (!bakDir.exists()) {
            bakDir.mkdirs()
        }

        val apksDir = project.file("$targetSaveDirPath/apks")
        if (!buildType.name.endsWith("release", true)) {
            apksDir.deleteRecursively()
        }
        if (!apksDir.exists()) {
            apksDir.mkdirs()
        }

        val targetRTxtFile = project.file("$targetSaveDirPath/R.txt")
        if (targetRTxtFile.exists()) {
            val bakRTxtFile = project.file("${bakDir.absolutePath}/R.txt_${PUtils.getCurDate()}")
            targetRTxtFile.renameTo(bakRTxtFile)
            logI("备份 R.txt")
        }


        //获取当前最新apk包
        val prefix = variant.outputFile.name
            .take(variant.outputFile.name.lastIndexOf('.')) +
                "-${applicationVariant.versionCode}-${applicationVariant.versionName}-${PUtils.getCurDate()}"

        val targetApkFile = project.file("${apksDir.absolutePath}/$prefix.apk")
        Files.copy(variant.outputFile.toPath(), targetApkFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        logI("获取最新apk成功 ${targetApkFile.absolutePath} ")

        // 获取当前打包的R.txt
        Files.copy(rTxtFile.toPath(), targetRTxtFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
        if (targetRTxtFile.exists()) {
            logI("获取最新R.txt成功 ${targetRTxtFile.absolutePath} ")
        } else {
            logE("获取最新R.txt失败 请检查 ${rTxtFile.absolutePath}")
        }

        //R.txt转public.txt todo 如果多次打包的话
        val targetPublicTxtFile = project.file("$targetSaveDirPath/public.txt")
        val ret = convertRTxtToPublicTxt(rTxtFile, targetPublicTxtFile, applicationVariant.applicationId)
        if (!ret.first) {
            logE("public.txt文件生成失败：${ret.second}")
        } else {
            val distPublicTxtFile = project.file("$distDir/${applicationVariant.applicationId}-public.txt")
            Files.copy(targetPublicTxtFile.toPath(), distPublicTxtFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            logI("public.txt 文件生成成功 ${targetPublicTxtFile.absolutePath}")
        }
    }

}