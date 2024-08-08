package team._0mods.ecr.common.init.registry

import net.minecraftforge.fml.ModList
import team._0mods.ecr.api.plugin.ECRModPlugin
import team._0mods.ecr.api.plugin.ECRPlugin
import team._0mods.ecr.api.plugin.registry.impl.InternalPlayerMatrixTypeRegistry
import java.lang.annotation.ElementType

object ECAnnotationProcessor {
    fun init() {
        ModList.get().mods.asSequence()
            .map { it.owningFile.file.scanResult }
            .flatMap { it.annotations }
            .filter { it.annotationType.className == ECRPlugin::class.java.name && it.targetType == ElementType.TYPE }
            .map { Class.forName(it.clazz.className) }
            .toSet()
            .forEach {
                val annotation = it.getAnnotation(ECRPlugin::class.java)
                if (ECRModPlugin::class.java.isAssignableFrom(it)) {
                    val plugin = it.getDeclaredConstructor().newInstance() as ECRModPlugin

                    plugin.onMatrixTypeRegistry(InternalPlayerMatrixTypeRegistry(annotation.modId))
                }
            }
    }
}
