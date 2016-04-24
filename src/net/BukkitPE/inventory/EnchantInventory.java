package net.BukkitPE.inventory;

import net.BukkitPE.Player;
import net.BukkitPE.Server;
import net.BukkitPE.item.Item;
import net.BukkitPE.item.ItemBookEnchanted;
import net.BukkitPE.item.ItemDye;
import net.BukkitPE.item.enchantment.Enchantment;
import net.BukkitPE.item.enchantment.EnchantmentEntry;
import net.BukkitPE.item.enchantment.EnchantmentList;
import net.BukkitPE.level.Position;
import net.BukkitPE.math.NukkitRandom;
import net.BukkitPE.network.protocol.CraftingDataPacket;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * author: MagicDroidX
 * BukkitPE Project
 */
public class EnchantInventory extends ContainerInventory {

    private final Random random = new Random();

    private int bookshelfAmount = 0;

    private int[] levels = null;
    private EnchantmentEntry[] entries = null;

    public EnchantInventory(Position position) {
        super(null, InventoryType.get(InventoryType.ENCHANT_TABLE));
        this.holder = new FakeBlockMenu(this, position);
    }

    @Override
    public FakeBlockMenu getHolder() {
        return (FakeBlockMenu) this.holder;
    }

    @Override
    public void onOpen(Player who) {
        super.onOpen(who);

        if (this.levels == null) {
            this.levels = new int[3];
            this.bookshelfAmount = this.countBookshelf();

            if (this.bookshelfAmount < 0) {
                this.bookshelfAmount = 0;
            }

            if (this.bookshelfAmount > 15) {
                this.bookshelfAmount = 15;
            }

            NukkitRandom random = new NukkitRandom();

            double base = (double) random.nextRange(1, 8) + (bookshelfAmount / 2d) + (double) random.nextRange(0, bookshelfAmount);
            this.levels[0] = (int) Math.max(base / 3, 1);
            this.levels[1] = (int) ((base * 2) / 3 + 1);
            this.levels[2] = (int) Math.max(base, bookshelfAmount * 2);
        }

    }

    @Override
    public void onSlotChange(int index, Item before) {
        super.onSlotChange(index, before);

        if (index == 0) {
            Item item = this.getItem(0);
            if (item.getId() == Item.AIR) {
                this.entries = null;
            } else if (before.getId() == Item.AIR && !item.hasEnchantments()) {
                //before enchant
                if (this.entries == null) {
                    int enchantAbility = item.getEnchantAbility();
                    this.entries = new EnchantmentEntry[3];
                    for (int i = 0; i < 3; i++) {
                        ArrayList<Enchantment> result = new ArrayList<>();

                        int level = this.levels[i];
                        int k = level;
                        k += ThreadLocalRandom.current().nextInt(0, Math.round(enchantAbility / 2f));
                        k += ThreadLocalRandom.current().nextInt(0, Math.round(enchantAbility / 2f));
                        k++;
                        float bonus = (ThreadLocalRandom.current().nextFloat() + ThreadLocalRandom.current().nextFloat() - 1) * 0.15f + 1;
                        int modifiedLevel = (int) (k * (1 + bonus) + 0.5f);

                        ArrayList<Enchantment> possible = new ArrayList<>();
                        for (Enchantment enchantment : Enchantment.getEnchantments()) {
                            if (enchantment.canEnchant(item)) {
                                for (int enchLevel = enchantment.getMinLevel(); enchLevel < enchantment.getMaxLevel(); enchLevel++) {
                                    if (modifiedLevel >= enchantment.getMinEnchantAbility(enchLevel) && modifiedLevel <= enchantment.getMaxEnchantAbility(enchLevel)) {
                                        enchantment.setLevel(enchLevel);
                                        possible.add(enchantment);
                                    }
                                }

                            }
                        }

                        int[] weights = new int[possible.size()];
                        int total = 0;

                        for (int j = 0; j < weights.length; j++) {
                            int weight = possible.get(j).getWeight();
                            weights[j] = weight;
                            total += weight;
                        }

                        int v = ThreadLocalRandom.current().nextInt(total + 1);

                        int sum = 0;
                        int key;
                        for (key = 0; key < weights.length; ++key) {
                            sum += weights[key];
                            if (sum >= v) {
                                key++;
                                break;
                            }
                        }
                        key--;

                        Enchantment enchantment = possible.get(key);
                        result.add(enchantment);
                        possible.remove(key);

                        //Extra enchantment
                        while (!possible.isEmpty()) {
                            modifiedLevel = Math.round(modifiedLevel / 2f);
                            v = ThreadLocalRandom.current().nextInt(0, 51);
                            if (v <= (modifiedLevel + 1)) {

                                for (Enchantment e : new ArrayList<>(possible)) {
                                    if (!e.isCompatibleWith(enchantment)) {
                                        possible.remove(e);
                                    }
                                }

                                weights = new int[possible.size()];
                                total = 0;

                                for (int j = 0; j < weights.length; j++) {
                                    int weight = possible.get(j).getWeight();
                                    weights[j] = weight;
                                    total += weight;
                                }

                                v = ThreadLocalRandom.current().nextInt(total + 1);
                                sum = 0;
                                for (key = 0; key < weights.length; ++key) {
                                    sum += weights[key];
                                    if (sum >= v) {
                                        key++;
                                        break;
                                    }
                                }
                                key--;

                                enchantment = possible.get(key);
                                result.add(enchantment);
                                possible.remove(key);
                            } else {
                                break;
                            }
                        }

                        this.entries[i] = new EnchantmentEntry(result.stream().toArray(Enchantment[]::new), level, Enchantment.getRandomName());
                    }
                }

                this.sendEnchantmentList();
            }
        }
    }

    @Override
    public void onClose(Player who) {
        super.onClose(who);

        for (int i = 0; i < 2; ++i) {
            this.getHolder().getLevel().dropItem(this.getHolder().add(0.5, 0.5, 0.5), this.getItem(i));
            this.clear(i);
        }

        if (this.getViewers().size() == 0) {
            this.levels = null;
            this.entries = null;
            this.bookshelfAmount = 0;
        }
    }

    public void onEnchant(Player who, Item before, Item after) {
        Item result = (before.getId() == Item.BOOK) ? new ItemBookEnchanted() : before;
        if (!before.hasEnchantments() && after.hasEnchantments() && after.getId() == result.getId() && this.levels != null && this.entries != null) {
            Enchantment[] enchantments = after.getEnchantments();
            for (int i = 0; i < 3; i++) {
                if (Arrays.equals(enchantments, this.entries[i].getEnchantments())) {
                    Item lapis = this.getItem(1);
                    int level = who.getExperienceLevel();
                    int exp = who.getExperience();
                    int cost = this.entries[i].getCost();
                    if (lapis.getId() == Item.DYE && lapis.getDamage() == ItemDye.BLUE && lapis.getCount() > i && level >= cost) {
                        result.addEnchantment(enchantments);
                        this.setItem(0, result);
                        lapis.setCount(lapis.getCount() - i - 1);
                        this.setItem(1, lapis);
                        who.setExperience(exp, level - cost);
                        break;
                    }
                }
            }
        }
    }

    public int countBookshelf() {
        return 15;
        //todo calculate bookshelf
    }

    public void sendEnchantmentList() {
        CraftingDataPacket pk = new CraftingDataPacket();
        if (this.entries != null && this.levels != null) {
            EnchantmentList list = new EnchantmentList(this.entries.length);
            for (int i = 0; i < list.getSize(); i++) {
                list.setSlot(i, this.entries[i]);
            }
            pk.addEnchantList(list);
        }

        Server.broadcastPacket(this.getViewers(), pk);
    }

}