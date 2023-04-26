package com.microtech.plugins.tasks

import com.android.build.gradle.api.ApplicationVariant
import com.android.build.gradle.internal.dsl.BaseAppModuleExtension
import com.microtech.plugins.extensions.MicrotechPluginExtension
import com.microtech.plugins.utils.PUtils
import com.microtech.plugins.utils.PUtils.isLegalFile
import com.microtech.plugins.utils.RDotTxtEntry
import com.microtech.plugins.utils.RType
import com.microtech.plugins.utils.readRTxt
import org.gradle.api.DefaultTask
import org.gradle.api.Task
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.impldep.com.google.common.collect.ImmutableList
import sun.misc.Unsafe
import java.lang.reflect.Field
import java.nio.file.Files
import java.nio.file.StandardCopyOption
import kotlin.reflect.full.memberProperties
import kotlin.reflect.jvm.javaField


open class ResourceIdTask: DefaultTask() {

    private val tag = "[microtech] resourceIdTask"

    @Internal
    lateinit var variant: ApplicationVariant

    @Input
    var applicationId: String = ""

    private fun log(msg: String) {
        project.logger.error("$tag $msg")
    }

    @TaskAction
    fun justDoIt() {

        log("开始执行 resourcesId task 未调通")

        val processResourcesTask1 = PUtils.getProcessResourcesTask(project, variant)
        log("processResourcesTask类型=${processResourcesTask1?.javaClass}")
        processResourcesTask1?.doFirst {

            val androidExtension = project.extensions.getByName("android") as BaseAppModuleExtension

            val aaptParams = androidExtension.aaptOptions.additionalParameters

            if (aaptParams != null) {
                if (!aaptParams.contains("--stable-ids")) {
                    log("开始配置 --stable-ids aaptParams=$aaptParams")
                } else {
                    initTmpPublic(applicationId)
                    log("aapt配置已经存在 --stable-ids 这里终止 aaptParams=$aaptParams")
                }
            } else {
                log("aaptParams 为空")
            }
        }


        return






        val microtechPluginExtension = project.extensions
            .getByName(PUtils.microtechPluginExtensionName) as MicrotechPluginExtension

//        val resourceMappingFile = microtechPluginExtension.rTxtFilePath.get()
        val resourceMappingFile = "/Users/guobo/Workspace/weitai/aidex-x/R.txt"
        log("R.txt=$resourceMappingFile")
        if (!isLegalFile(resourceMappingFile)) {
            log("$resourceMappingFile 文件不合法，无法固定资源id")
            return
        }

        val rTxtMap = readRTxt(resourceMappingFile)
        if (rTxtMap.isEmpty()) {
            log("$resourceMappingFile 文件中没有资源id，无法固定资源id")
            return
        }

//        val stableIdsFile = project.file(microtechPluginExtension.publicTxtFilePath.get())
        val stableIdsFile = project.file("/Users/guobo/Workspace/weitai/aidex-x/public.txt")
        log("stableIdFile=${stableIdsFile.absolutePath}")
        PUtils.deleteFile(stableIdsFile)
        val sortedLines = getSortedStableIds(rTxtMap)

        sortedLines.forEach {
            stableIdsFile.appendText("${it}\n")
        }
        log("成功排序并写入")

        val processResourcesTask = PUtils.getProcessResourcesTask(project, variant)
        log("processResourcesTask类型=${processResourcesTask?.javaClass}")
        processResourcesTask?.doFirst {

            val androidExtension = project.extensions.getByName("android") as BaseAppModuleExtension

            val aaptParams = androidExtension.aaptOptions.additionalParameters

            if (aaptParams != null) {
                if (!aaptParams.contains("--stable-ids")) {
                    log("开始配置 --stable-ids aaptParams=$aaptParams")
                    addStableIdsFileForAGP410(it, stableIdsFile.absolutePath)
//                    androidExtension.aaptOptions.additionalParameters("--stable-ids", stableIdsFile.absolutePath)
                } else {
                    log("aapt配置已经存在 --stable-ids 这里终止")
                }
            }

        } ?:let {
            log("processResourcesTask 获取失败，终止资源固定")
            return
        }

    }

    private fun addStableIdsFileForAGP410(processResourcesTask: Task, stableIdsFilePath: String) {
        val taskClass = Class.forName("com.android.build.gradle.internal.res.LinkApplicationAndroidResourcesTask")
        log("addStableIdsFileForAGP410  taskClass成功获取 $taskClass")

        processResourcesTask::class.memberProperties.forEach {
//            log("${it.name} $it")
        }

//        taskClass.declaredFields.forEach {
//            log("LinkApplicationAndroidResourcesTask field: ${it.name} ${it.type}")
//        }
//        taskClass.fields.forEach {
//            log("LinkApplicationAndroidResourcesTask field: ${it.name} ${it.type}")
//        }
//        taskClass.declaredMethods.forEach {
//            log("LinkApplicationAndroidResourcesTask method: ${it.name} ${it.returnType}")
//        }
//        taskClass.methods.forEach {
//            log("LinkApplicationAndroidResourcesTask method: ${it.name} ${it.returnType}")
//        }

        var aaptAdditionalParametersField =  processResourcesTask::class.memberProperties.stream()
            .filter{ it.name == "aaptAdditionalParameters"}
            .findAny().get().javaField

        processResourcesTask::class.memberProperties.forEach {
            if (it.name == "aaptAdditionalParameters") {
                aaptAdditionalParametersField = it.javaField
                log(" aaptAdditionalParametersField ${aaptAdditionalParametersField}")
            }
        }


        log(" aaptAdditionalParametersField : $aaptAdditionalParametersField --- ${aaptAdditionalParametersField?.canAccess(processResourcesTask)}")

//        val aaptAdditionalParametersField: Field = taskClass.getDeclaredField("aaptAdditionalParameters")
        aaptAdditionalParametersField!!.isAccessible = true
        log(" aaptAdditionalParametersField : $aaptAdditionalParametersField --- ${aaptAdditionalParametersField?.canAccess(processResourcesTask)}")


        val aaptAdditionalParameters = aaptAdditionalParametersField?.get(processResourcesTask)
        log("addStableIdsFileForAGP410  aaptAdditionalParameters成功获取 $aaptAdditionalParameters")

        val abstractPropertyClass = Class.forName("org.gradle.api.internal.provider.AbstractProperty")
        val valueField = abstractPropertyClass.getDeclaredField("value")
        valueField.isAccessible = true
        val listPropertyValue = valueField.get(aaptAdditionalParameters)
        log("addStableIdsFileForAGP410  listPropertyValue成功获取 $listPropertyValue")

        val fixedSupplierClass = Class.forName("org.gradle.api.internal.provider.AbstractCollectionProperty${"$"}FixedSupplier")
        val fixedSupplierValueField = fixedSupplierClass.getDeclaredField("value")
        fixedSupplierValueField.isAccessible = true
        val supplierValue = fixedSupplierValueField.get(listPropertyValue) as Collection<String>
        log("addStableIdsFileForAGP410  supplierValue成功获取 $supplierValue")

        val builder = ImmutableList.Builder<String>()
        builder.addAll(supplierValue.iterator())
        builder.add("--stable-ids")
        builder.add(stableIdsFilePath)
        val newSupplierValue = builder.build()
        log("addStableIdsFileForAGP410  新的newSupplierValue构建成功 $newSupplierValue")

        replaceFinalField(fixedSupplierClass, "value", listPropertyValue, newSupplierValue)
        log("addStableIdsFileForAGP410 aaptAdditionalParameters 写入完成")
    }


    private fun replaceFinalField(clazz: Class<*>, fieldName: String, instance: Any, fieldValue: Any) {
        var currClazz: Class<*> = clazz
        var field: Field
        while (true) {
            try {
                field = currClazz.getDeclaredField(fieldName)
                log("replaceFinalField field=$field")
                break
            } catch ( e: NoSuchFieldException) {
                if (currClazz == Any::class.java) {
                        throw e
                } else {
                    currClazz = currClazz.superclass
                }
            }
        }
        val unsafeField = Unsafe::class.java.getDeclaredField("theUnsafe")
        unsafeField.isAccessible = true
        val unsafe: Unsafe = unsafeField.get(null) as Unsafe
        log("replaceFinalField Unsafe=$unsafe")
        val fieldOffset: Long = unsafe.objectFieldOffset(field)
        log("replaceFinalField fieldOffset=$fieldOffset")
        unsafe.putObject(instance, fieldOffset, fieldValue)

    }

    private fun getSortedStableIds(rTypeResourceMap: Map<RType, MutableSet<RDotTxtEntry>?> ): ArrayList<String> {
        val sortedLines =  ArrayList<String>()
        rTypeResourceMap.forEach { (key, entries) ->
            entries?.forEach {
                // 当前只支持string资源
                if (it.type == RType.STRING || it.type == RType.ARRAY) {
                    sortedLines.add("${applicationId}:${it.type}/${it.name} = ${it.idValue}")
                }
            }
        }
        sortedLines.sort()
        return sortedLines
    }


    /**
     * 由于flavor中无法根据包名配置public.txt 所以在这里打包前把内容复制到一个指定文件
     * 把当前包名对应的public.txt内容写入tmp-public.txt中
     */
    private fun initTmpPublic(applicationId: String) {
        val distDir = "${project.buildDir.parent}/dist"
        val targetPublicTxtFile = project.file("$distDir/${applicationId}-public.txt")
        val distPublicTxtFile = project.file("$distDir/public.txt")
        if (targetPublicTxtFile.exists()) {
            if (distPublicTxtFile.exists()) {
                distPublicTxtFile.delete()
            } else {
                distPublicTxtFile.mkdirs()
            }
            Files.copy(targetPublicTxtFile.toPath(), distPublicTxtFile.toPath(), StandardCopyOption.REPLACE_EXISTING)
            log("${applicationId}-public.txt 内容成功写入tmp-public.txt")
        }

    }

}