package com.microtech.plugins.utils

import com.android.build.gradle.api.ApplicationVariant
import org.gradle.api.Project
import org.gradle.api.Task
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

object PUtils {

    const val microtechPluginExtensionName = "microtechPlugin"

    fun getApplicationId(project: Project, variant: ApplicationVariant): String? {
        return variant.applicationId
    }

    fun getProcessManifestTask(project: Project, variant: ApplicationVariant): Task? {
        return project.tasks.findByName("process${variant.name.capitalize()}Manifest")
    }

    fun getProcessResourcesTask(project: Project, variant: ApplicationVariant): Task? {
        return project.tasks.findByName("process${variant.name.capitalize()}Resources")
    }

    fun getMergeResourcesTask(project: Project, variant: ApplicationVariant): Task? {
        return project.tasks.findByName("merge${variant.name.capitalize()}Resources")
    }

    fun getAssembleTask(project: Project, variant: ApplicationVariant): Task? {
        return project.tasks.findByName("assemble${variant.name.capitalize()}")
    }

    fun getCurDate(formatString: String = "yyyyMMddHHmmss"): String =
        SimpleDateFormat(formatString, Locale.CHINA).format(Date())


    fun isLegalFile(path: String?): Boolean {
        if (path == null) {
            return false
        }
        val file = File(path)
        return file.exists() && file.isFile && file.length() > 0
    }

    fun deleteFile(file: File?): Boolean {
        if (file == null) {
            return true
        }
        return if (file.exists()) {
            file.delete()
        } else true
    }
}