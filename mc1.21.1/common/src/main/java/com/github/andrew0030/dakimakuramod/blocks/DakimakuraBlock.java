package com.github.andrew0030.dakimakuramod.blocks;

import com.github.andrew0030.dakimakuramod.block_entities.dakimakura.DakimakuraBlockEntity;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.platform.Services;
import com.github.andrew0030.dakimakuramod.util.VoxelShapeTransformer;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import org.jetbrains.annotations.Nullable;
import java.util.List;

public class DakimakuraBlock extends BaseEntityBlock
{
    public static final MapCodec<DakimakuraBlock> CODEC = simpleCodec(DakimakuraBlock::new);

    private static final VoxelShape WALL_BOTTOM_SOUTH = Block.box(3, 1, 12, 13, 16, 16);
    private static final VoxelShape[] WALL_BOTTOM = VoxelShapeTransformer.of(WALL_BOTTOM_SOUTH).createHorizontalArray(Direction.SOUTH);
    private static final VoxelShape[] WALL_TOP = VoxelShapeTransformer.of(WALL_BOTTOM_SOUTH).mirror(Direction.Axis.Y).createHorizontalArray(Direction.SOUTH);

    private static final VoxelShape FLOOR_BOTTOM_SOUTH = Block.box(3, 0, 1, 13, 4, 16);
    private static final VoxelShape[] FLOOR_BOTTOM = VoxelShapeTransformer.of(FLOOR_BOTTOM_SOUTH).createHorizontalArray(Direction.SOUTH);
    private static final VoxelShape[] FLOOR_TOP = VoxelShapeTransformer.of(FLOOR_BOTTOM_SOUTH).mirror(Direction.Axis.Z).createHorizontalArray(Direction.SOUTH);

    private static final VoxelShape[] CEILING_BOTTOM = VoxelShapeTransformer.of(FLOOR_BOTTOM_SOUTH).mirror(Direction.Axis.Y).createHorizontalArray(Direction.SOUTH);
    private static final VoxelShape[] CEILING_TOP = VoxelShapeTransformer.of(FLOOR_BOTTOM_SOUTH).mirror(Direction.Axis.Y).mirror(Direction.Axis.Z).createHorizontalArray(Direction.SOUTH);

    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty TOP = BooleanProperty.create("top");

    public DakimakuraBlock(Properties properties)
    {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACE, AttachFace.FLOOR).setValue(FACING, Direction.NORTH).setValue(TOP, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec()
    {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit)
    {
        if (state.getValue(TOP))
            pos = state.getValue(FACE).equals(AttachFace.WALL)
                    ? pos.relative(Direction.DOWN)
                    : pos.relative(state.getValue(FACING).getOpposite());

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (player.isShiftKeyDown() && blockEntity instanceof DakimakuraBlockEntity dakiBlockEntity)
        {
            dakiBlockEntity.flip();
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state)
    {
        ItemStack stack = super.getCloneItemStack(level, pos, state);
        if (state.getValue(TOP))
            pos = state.getValue(FACE).equals(AttachFace.WALL)
                    ? pos.relative(Direction.DOWN)
                    : pos.relative(state.getValue(FACING).getOpposite());

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (blockEntity instanceof DakimakuraBlockEntity dakiBlockEntity)
        {
            Daki daki = dakiBlockEntity.getDaki();
            if (daki != null)
                DakiTagSerializer.serialize(daki, stack);
            DakiTagSerializer.setFlipped(stack, dakiBlockEntity.isFlipped());
        }
        return stack;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context)
    {
        Direction clickedFace = context.getClickedFace();
        Direction direction = context.getHorizontalDirection();
        BlockPos blockpos = context.getClickedPos();
        Level level = context.getLevel();
        BlockPos relativePos = blockpos.relative(clickedFace.equals(Direction.DOWN) ? direction.getOpposite() : (clickedFace.getAxis().isHorizontal() ? Direction.UP : direction));
        if (!level.getBlockState(relativePos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(relativePos))
            return null;

        AttachFace attachFace = clickedFace.equals(Direction.UP) ? AttachFace.FLOOR
                : clickedFace.equals(Direction.DOWN) ? AttachFace.CEILING
                : AttachFace.WALL;

        Direction facingDirection = clickedFace.equals(Direction.DOWN) ? direction.getOpposite()
                : clickedFace.getAxis().isHorizontal() ? clickedFace.getOpposite()
                : direction;

        return this.defaultBlockState().setValue(FACE, attachFace).setValue(FACING, facingDirection);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack)
    {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        Daki daki = DakiTagSerializer.deserialize(stack);
        if (blockEntity instanceof DakimakuraBlockEntity dakiBlockEntity)
        {
            dakiBlockEntity.setDaki(daki);
            dakiBlockEntity.setFlipped(DakimakuraItem.isFlipped(stack));
        }
        if (!level.isClientSide())
        {
            boolean isWall = state.getValue(FACE).equals(AttachFace.WALL);
            BlockPos blockpos = pos.relative(isWall ? Direction.UP : state.getValue(FACING));
            level.setBlockAndUpdate(blockpos, state.setValue(TOP, true));
            state.updateNeighbourShapes(level, pos, 2);
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder)
    {
        List<ItemStack> stacks = super.getDrops(state, builder);
        BlockEntity blockEntity = builder.getOptionalParameter(LootContextParams.BLOCK_ENTITY);
        for (int i = 0; i < stacks.size(); i++)
        {
            ItemStack stack = stacks.get(i);
            if (stack.is(Services.getPlatform().getDakimakuraItem()) && blockEntity instanceof DakimakuraBlockEntity dakiBlockEntity)
            {
                Daki daki = dakiBlockEntity.getDaki();
                if (daki != null)
                    DakiTagSerializer.serialize(daki, stack);
                DakiTagSerializer.setFlipped(stack, dakiBlockEntity.isFlipped());
                stacks.set(i, stack);
            }
        }
        return stacks;
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player)
    {
        if (state.getValue(TOP) && player.isCreative())
        {
            BlockPos offsetPos = state.getValue(FACE).equals(AttachFace.WALL)
                    ? pos.relative(Direction.DOWN)
                    : pos.relative(state.getValue(FACING).getOpposite());
            BlockState offsetState = level.getBlockState(offsetPos);
            if (offsetState.is(this))
            {
                if (!level.isClientSide())
                    level.setBlock(offsetPos, Blocks.AIR.defaultBlockState(), 35);
                this.spawnDestroyParticles(level, player, offsetPos, offsetState);
            }
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston)
    {
        if (!level.isClientSide())
        {
            boolean isWall = state.getValue(FACE).equals(AttachFace.WALL);
            BlockPos offsetPos = pos.relative(isWall ? getVerticalDirection(state.getValue(TOP)) : getNeighbourDirection(state.getValue(TOP), state.getValue(FACING)));
            BlockState offsetState = level.getBlockState(offsetPos);
            if (offsetState.is(this))
                level.destroyBlock(offsetPos, true, null);
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    public static Direction getNeighbourDirection(boolean isTop, Direction direction)
    {
        return !isTop ? direction : direction.getOpposite();
    }

    public static Direction getVerticalDirection(boolean isTop)
    {
        return isTop ? Direction.DOWN : Direction.UP;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context)
    {
        int direction = state.getValue(FACING).get2DDataValue();
        return switch (state.getValue(FACE))
        {
            default -> state.getValue(TOP) ? FLOOR_TOP[direction] : FLOOR_BOTTOM[direction];
            case WALL -> state.getValue(TOP) ? WALL_TOP[direction] : WALL_BOTTOM[direction];
            case CEILING -> state.getValue(TOP) ? CEILING_TOP[direction] : CEILING_BOTTOM[direction];
        };
    }

    @Override
    public RenderShape getRenderShape(BlockState state)
    {
        return RenderShape.ENTITYBLOCK_ANIMATED;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder)
    {
        builder.add(FACE, FACING, TOP);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state)
    {
        return new DakimakuraBlockEntity(pos, state);
    }
}
