package team._0mods.ecr.api.block

import net.minecraft.core.BlockPos
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.BaseEntityBlock
import net.minecraft.world.level.block.entity.BlockEntity
import net.minecraft.world.level.block.entity.BlockEntityTicker
import net.minecraft.world.level.block.entity.BlockEntityType
import net.minecraft.world.level.block.state.BlockState
import ru.hollowhorizon.hc.common.objects.blocks.IBlockItemProperties
import team._0mods.ecr.api.block.entity.BlockEntityExtensions
import team._0mods.ecr.common.init.registry.ECTabs

abstract class PropertiedEntityBlock(properties: Properties): BaseEntityBlock(properties), IBlockItemProperties {
    override val properties: Item.Properties
        get() = Item.Properties().tab(ECTabs.tabBlocks)
}

open class BlockWithEntity<V: BlockEntity>(val blockEntity: (BlockPos, BlockState) -> V, properties: Properties): PropertiedEntityBlock(properties) {
    override fun newBlockEntity(
        pos: BlockPos,
        state: BlockState
    ): BlockEntity? = blockEntity(pos, state)

    override fun <T : BlockEntity?> getTicker(
        level: Level,
        state: BlockState,
        blockEntityType: BlockEntityType<T?>
    ): BlockEntityTicker<T> = BlockEntityTicker<T> { l, p, s, be ->
        if (be is BlockEntityExtensions<*>) {
            if (l.isClientSide)
                (be as BlockEntityExtensions<T>).onClientTick(l, p, s, be)
            else (be as BlockEntityExtensions<T>).onTick(l, p, s, be)
        }
    }

    override fun onPlace(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        val be = level.getBlockEntity(pos, state)
        if (be is BlockEntityExtensions<*>) {
            be.onPlace(level, state, oldState, isMoving)
        }
    }

    override fun onRemove(state: BlockState, level: Level, pos: BlockPos, oldState: BlockState, isMoving: Boolean) {
        val be = level.getBlockEntity(pos)
        if (be is BlockEntityExtensions<*>)
            be.onRemove(level, state, oldState, isMoving)
        be.dropForgeContents(level, pos)
    }

    override fun setPlacedBy(level: Level, pos: BlockPos, state: BlockState, placer: LivingEntity?, stack: ItemStack) {
        val be = level.getBlockEntity(pos)
        if (be is BlockEntityExtensions<*>)
            be.onPlacedBy(level, state, placer, stack)
    }
}
