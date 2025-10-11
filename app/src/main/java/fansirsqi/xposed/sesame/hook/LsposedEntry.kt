package fansirsqi.xposed.sesame.hook

import de.robv.android.xposed.XposedBridge
import fansirsqi.xposed.sesame.data.General
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModule
import io.github.libxposed.api.XposedModuleInterface


class LsposedEntry(
    base: XposedInterface,
    param: XposedModuleInterface.ModuleLoadedParam

) : XposedModule(base, param) {
    val tag = "LsposedEntry"
    private val processName = param.processName
    var customHooker: ApplicationHook? = null


    init {
        customHooker = ApplicationHook()
        customHooker?.xposedInterface = base
        // 将框架提供的 base 接口实例传递给逻辑核心，连接 Hook 进程与框架功能。
        XposedBridge.log("$tag: Initialized for process $processName")


        val baseFw = "${base.frameworkName} \n${base.frameworkVersion} \n${base.applicationInfo} \n${base.frameworkVersionCode}"
        XposedBridge.log("LspEntry: Framework from base: $baseFw ")
    }

    /**
     * 当模块作用域内的应用进程启动时，框架会回调此方法。
     */
    override fun onPackageLoaded(param: XposedModuleInterface.PackageLoadedParam) {
        if (General.PACKAGE_NAME == processName) {
            customHooker?.loadPackage(param)
        } else if (General.MODULE_PACKAGE_NAME == processName) {
            customHooker?.loadModelPackage(param)
        }
    }
}