package net.BukkitPE.entity.passive;

import net.BukkitPE.item.Item;
import net.BukkitPE.level.format.FullChunk;
import net.BukkitPE.nbt.tag.CompoundTag;

/**
 * Author: BeYkeRYkt
 * BukkitPE Project
 */
public class EntityWolf extends EntityAnimal {

    public static final int NETWORK_ID = 14;

    public EntityWolf(FullChunk chunk, CompoundTag nbt) {
        super(chunk, nbt);
    }

    @Override
    public float getWidth() {
        return 0.6f;
    }

    @Override
    public float getLength() {
        return 0.6f;
    }

    @Override
    public float getHeight() {
        if (isBaby()) {
            return 0.8f; // No have information
        }
        return 0.8f;
    }

    @Override
    public float getEyeHeight() {
        if (isBaby()) {
            return 0.8f * getHeight(); // No have information
        }
        return 0.8f * getHeight();
    }

    @Override
    public String getName() {
        return this.getNameTag();
    }

    @Override
    public Item[] getDrops() {
        return new Item[]{};
    }

    @Override
    public int getNetworkId() {
        return NETWORK_ID;
    }

    @Override
    public void initEntity() {
        super.initEntity();
        setMaxHealth(8);
    }
}
