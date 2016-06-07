package net.BukkitPE.item;

/**

 * BukkitPE Project
 */
public class ItemClock extends Item {

    public ItemClock() {
        this(0, 1);
    }

    public ItemClock(Integer meta) {
        this(meta, 1);
    }

    public ItemClock(Integer meta, int count) {
        super(CLOCK, meta, count, "Clock");
    }
}