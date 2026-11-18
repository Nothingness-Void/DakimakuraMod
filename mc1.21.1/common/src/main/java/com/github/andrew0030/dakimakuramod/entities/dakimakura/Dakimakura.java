package com.github.andrew0030.dakimakuramod.entities.dakimakura;

import com.github.andrew0030.dakimakuramod.DakimakuraModCommon;
import com.github.andrew0030.dakimakuramod.dakimakura.Daki;
import com.github.andrew0030.dakimakuramod.dakimakura.serialize.DakiTagSerializer;
import com.github.andrew0030.dakimakuramod.items.DakimakuraItem;
import com.github.andrew0030.dakimakuramod.platform.Services;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class Dakimakura extends Entity
{
    private static final EntityDataAccessor<Integer> FACING = SynchedEntityData.defineId(Dakimakura.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FLIPPED = SynchedEntityData.defineId(Dakimakura.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<String> PACK_NAME = SynchedEntityData.defineId(Dakimakura.class, EntityDataSerializers.STRING);
    private static final EntityDataAccessor<String> DIR_NAME = SynchedEntityData.defineId(Dakimakura.class, EntityDataSerializers.STRING);

    public Dakimakura(EntityType<?> entityType, Level level)
    {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder)
    {
        builder.define(FACING, 2);
        builder.define(FLIPPED, false);
        builder.define(PACK_NAME, "");
        builder.define(DIR_NAME, "");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound)
    {
        compound.putInt(DakiTagSerializer.FACING_KEY, this.getRotationInt());
        compound.putBoolean(DakiTagSerializer.FLIPPED_KEY, this.isFlipped());
        if (!this.getEntityData().get(PACK_NAME).isEmpty() && !this.getEntityData().get(DIR_NAME).isEmpty())
        {
            compound.putString(DakiTagSerializer.PACK_NAME_KEY, this.getEntityData().get(PACK_NAME));
            compound.putString(DakiTagSerializer.DIR_NAME_KEY, this.getEntityData().get(DIR_NAME));
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound)
    {
        this.setRotationInt(compound.getInt(DakiTagSerializer.FACING_KEY));
        this.setFlipped(compound.getBoolean(DakiTagSerializer.FLIPPED_KEY));
        if (compound.contains(DakiTagSerializer.PACK_NAME_KEY, Tag.TAG_STRING) && compound.contains(DakiTagSerializer.DIR_NAME_KEY, Tag.TAG_STRING))
        {
            this.getEntityData().set(PACK_NAME, compound.getString(DakiTagSerializer.PACK_NAME_KEY));
            this.getEntityData().set(DIR_NAME, compound.getString(DakiTagSerializer.DIR_NAME_KEY));
        }
    }

    @Override
    public void tick()
    {
        super.tick();
        if (!this.level().isClientSide())
        {
            BlockPos pos = this.blockPosition();
            BlockState state = this.level().getBlockState(pos);
            if (!Services.getPlatform().isBed(this.level(), pos, state))
            {
                this.dropAsItem();
                this.setRemoved(RemovalReason.DISCARDED);
            }
        }
    }

    public void dropAsItem()
    {
        Daki daki = this.getDaki();
        ItemStack itemStack = new ItemStack(Services.getPlatform().getDakimakuraItem());
        if (daki != null)
        {
            DakiTagSerializer.serialize(daki, itemStack);
            DakimakuraItem.setFlipped(itemStack, this.isFlipped());
        }
        ItemEntity itemEntity = new ItemEntity(this.level(), position().x(), position().y(), position().z(), itemStack);
        this.level().addFreshEntity(itemEntity);
    }

    public void setDaki(Daki daki)
    {
        this.getEntityData().set(PACK_NAME, (daki != null) ? daki.getPackDirectoryName() : "");
        this.getEntityData().set(DIR_NAME, (daki != null) ? daki.getDakiDirectoryName() : "");
    }

    public Daki getDaki()
    {
        return DakimakuraModCommon.getDakimakuraManager().getDakiFromMap(this.getEntityData().get(PACK_NAME), this.getEntityData().get(DIR_NAME));
    }

    public boolean isFlipped()
    {
        return this.getEntityData().get(FLIPPED);
    }

    public void setFlipped(boolean flipped)
    {
        this.getEntityData().set(FLIPPED, flipped);
    }

    public void setRotation(Direction facing)
    {
        this.getEntityData().set(FACING, facing.ordinal());
    }

    public void setRotationInt(int val)
    {
        this.setRotation(Direction.values()[val]);
    }

    public Direction getRotation()
    {
        return Direction.values()[this.getRotationInt()];
    }

    public int getRotationInt()
    {
        return this.getEntityData().get(FACING);
    }
}
