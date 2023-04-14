package com.microtech.plugins.extensions

import org.gradle.api.provider.Property

interface MicrotechPluginExtension {

    var rTxtFilePath: Property<String>
    var publicTxtFilePath: Property<String>

}