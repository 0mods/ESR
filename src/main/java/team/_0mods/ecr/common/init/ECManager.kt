@file:JvmName("ECManager")

package team._0mods.ecr.common.init

import net.minecraft.client.gui.screens.MenuScreens
import net.minecraft.client.renderer.ItemBlockRenderTypes
import net.minecraft.client.renderer.RenderType
import net.minecraft.world.level.block.Block
import ru.hollowhorizon.hc.client.sounds.HollowSoundHandler
import team._0mods.ecr.api.ModId
import team._0mods.ecr.api.utils.loadConfig
import team._0mods.ecr.client.screen.menu.MatrixDestructorScreen
import team._0mods.ecr.client.screen.menu.MithrilineFurnaceScreen
import team._0mods.ecr.client.screen.menu.XLikeScreen
import team._0mods.ecr.common.init.config.ECCommonConfig
import team._0mods.ecr.common.init.registry.ECRegistry

@JvmName("init")
fun initCommon() {
    ECCommonConfig.instance = ECCommonConfig().loadConfig("essential-craft/common")
    HollowSoundHandler.MODS.add(ModId)
}

fun initClient() {
    val renderers by lazy {
        mapOf<Block, RenderType>(
            ECRegistry.airCluster.get() to RenderType.cutout(),
            ECRegistry.earthCluster.get() to RenderType.cutout(),
            ECRegistry.waterCluster.get() to RenderType.cutout(),
            ECRegistry.flameCluster.get() to RenderType.cutout(),

            ECRegistry.magicTable.get() to RenderType.translucent()
        )
    }

    MenuScreens.register(ECRegistry.mithrilineFurnaceMenu.get(),
        ::MithrilineFurnaceScreen
    )
    MenuScreens.register(ECRegistry.matrixDestructorMenu.get(),
        ::MatrixDestructorScreen
    )
    MenuScreens.register(ECRegistry.envoyerMenu.get()) { menu, inv, title ->
        XLikeScreen.Envoyer(menu, inv, title)
    }
    MenuScreens.register(ECRegistry.magicTableMenu.get()) { menu, inv, title ->
        XLikeScreen.MagicTable(menu, inv, title)
    }

    ItemBlockRenderTypes.TYPE_BY_BLOCK.putAll(renderers)
}
