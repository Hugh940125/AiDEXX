package com.microtech.aidexx.utils.blankj

import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import com.microtech.aidexx.common.getContext
import java.io.FileOutputStream

object ResourceUtils {

    private const val BUFFER_SIZE = 8192

    /**
     * Return the drawable by identifier.
     *
     * @param id The identifier.
     * @return the drawable by identifier
     */
    fun getDrawable(@DrawableRes id: Int): Drawable? {
        return ContextCompat.getDrawable(getContext(), id)
    }

    /**
     * Return the id identifier by name.
     *
     * @param name The name of id.
     * @return the id identifier by name
     */
    fun getIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "id", getContext().packageName)
    }

    /**
     * Return the string identifier by name.
     *
     * @param name The name of string.
     * @return the string identifier by name
     */
    fun getStringIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "string", getContext().packageName)
    }

    /**
     * Return the color identifier by name.
     *
     * @param name The name of color.
     * @return the color identifier by name
     */
    fun getColorIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "color", getContext().packageName)
    }

    /**
     * Return the dimen identifier by name.
     *
     * @param name The name of dimen.
     * @return the dimen identifier by name
     */
    fun getDimenIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "dimen", getContext().packageName)
    }

    /**
     * Return the drawable identifier by name.
     *
     * @param name The name of drawable.
     * @return the drawable identifier by name
     */
    fun getDrawableIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "drawable", getContext().packageName)
    }

    /**
     * Return the mipmap identifier by name.
     *
     * @param name The name of mipmap.
     * @return the mipmap identifier by name
     */
    fun getMipmapIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "mipmap", getContext().packageName)
    }

    /**
     * Return the layout identifier by name.
     *
     * @param name The name of layout.
     * @return the layout identifier by name
     */
    fun getLayoutIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "layout", getContext().packageName)
    }

    /**
     * Return the style identifier by name.
     *
     * @param name The name of style.
     * @return the style identifier by name
     */
    fun getStyleIdByName(name: String?): Int {
        return getContext().resources.getIdentifier(name, "style", getContext().packageName)
    }


    /**
     * Copy the file from assets.
     *
     * @param assetsFilePath The path of file in assets.
     * @param destFilePath   The path of destination file.
     * @return `true`: success<br></br>`false`: fail
     */
    fun copyFileFromAssets(assetsFilePath: String, destFilePath: String): Boolean =
        kotlin.runCatching {
            getContext().assets.open(assetsFilePath).use { input ->
                FileOutputStream(destFilePath).use {  output ->
                    // 缓冲区
                    val buffer = ByteArray(BUFFER_SIZE)
                    var read: Int

                    // 将输入流读取到缓冲区并写入输出流
                    while (input.read(buffer).also { read = it } != -1) {
                        output.write(buffer, 0, read)
                    }
                    output.flush()
                    true
                }
            }
        }.getOrNull() ?: false

}