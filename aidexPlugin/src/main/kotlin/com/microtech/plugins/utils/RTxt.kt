package com.microtech.plugins.utils

import org.gradle.internal.impldep.com.google.common.collect.ComparisonChain
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.util.*
import java.util.regex.Pattern

fun convertRTxtToPublicTxt(rTxtFile: File, publicTxtFile: File, applicationId: String): Pair<Boolean,String> {
    val resourceMappingFile = rTxtFile.absolutePath
    if (!PUtils.isLegalFile(resourceMappingFile)) {
        return false to "R.txt 文件不存在"
    }

    val rTxtMap = readRTxt(resourceMappingFile)
    if (rTxtMap.isEmpty()) {
        return false to "R.txt文件内容为空"
    }

    PUtils.deleteFile(publicTxtFile)
    val sortedLines = getSortedStableIds(rTxtMap,applicationId)

    sortedLines.forEach {
        publicTxtFile.appendText("${it}\n")
    }
    return true to ""
}

enum class RType {
    ANIM, ANIMATOR, ARRAY, ATTR, BOOL, COLOR, DIMEN, DRAWABLE, FONT, FRACTION, ID, INTEGER, INTERPOLATOR, LAYOUT, MENU, MIPMAP, PLURALS, RAW, STRING, STYLE, STYLEABLE, TRANSITION, XML, NAVIGATION;

    override fun toString(): String {
        return super.toString().lowercase(Locale.getDefault())
    }
}
enum class IdType {
    INT, INT_ARRAY;

    override fun toString(): String {
        return if (this == INT) {
            "int"
        } else "int[]"
    }

    companion object {
        fun from(raw: String): IdType {
            if (raw == "int") {
                return INT
            } else if (raw == "int[]") {
                return INT_ARRAY
            }
            throw IllegalArgumentException(String.format("'%s' is not a valid ID type.", raw))
        }
    }
}

class RDotTxtEntry constructor(val idType: IdType,
                               val type: RType,
                               val name: String,
                               var idValue: String): Comparable<RDotTxtEntry> {

    private val TEXT_SYMBOLS_LINE = Pattern.compile("(\\S+) (\\S+) (\\S+) (.+)")


    override fun compareTo(that: RDotTxtEntry): Int {
        return ComparisonChain.start().compare(type, that.type).compare(name, that.name)
            .result()
    }

    override fun equals(obj: Any?): Boolean {
        if (obj !is RDotTxtEntry) {
            return false
        }
        return Objects.equals(type, obj.type) && Objects.equals(
            name, obj.name
        )
    }

    override fun hashCode(): Int {
        return Arrays.hashCode(arrayOf<Any>(type, name))
    }

    override fun toString(): String {
        return "RDotTxtEntry"
    }

}

fun readRTxt(rTxtFullFilename: String): Map<RType, MutableSet<RDotTxtEntry>?> {

    val rTypeResourceMap: MutableMap<RType, MutableSet<RDotTxtEntry>?> =
        HashMap<RType, MutableSet<RDotTxtEntry>?>()
    if (PUtils.isLegalFile(rTxtFullFilename)) {
        var bufferedReader: BufferedReader? = null
        try {
            val textSymbolLine = Pattern.compile("(\\S+) (\\S+) (\\S+) (.+)")
            bufferedReader =
                BufferedReader(InputStreamReader(FileInputStream(rTxtFullFilename)))
            var line: String? = null
            while (bufferedReader.readLine().also { line = it } != null) {
                val matcher = textSymbolLine.matcher(line)
                if (matcher.matches()) {
                    val idType: IdType = IdType.from(matcher.group(1))
                    val rType: RType =
                        RType.valueOf(matcher.group(2).uppercase(Locale.getDefault()))
                    val name = matcher.group(3)
                    val idValue = matcher.group(4)
                    val rDotTxtEntry = RDotTxtEntry(idType, rType, name, idValue)
                    var hashSet: MutableSet<RDotTxtEntry>? = null
                    if (rTypeResourceMap.containsKey(rType)) {
                        hashSet = rTypeResourceMap[rType]
                    } else {
                        hashSet = HashSet<RDotTxtEntry>()
                        rTypeResourceMap[rType] = hashSet
                    }
                    hashSet!!.add(rDotTxtEntry)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            bufferedReader?.close()
        }
    }
    return rTypeResourceMap
}

fun getSortedStableIds(rTypeResourceMap: Map<RType, MutableSet<RDotTxtEntry>?> , applicationId: String): ArrayList<String> {
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