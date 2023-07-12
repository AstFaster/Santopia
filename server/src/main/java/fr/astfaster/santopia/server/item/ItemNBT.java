package fr.astfaster.santopia.server.item;

import net.minecraft.nbt.NBTTagCompound;
import org.bukkit.craftbukkit.v1_20_R1.inventory.CraftItemStack;
import org.bukkit.inventory.ItemStack;

public class ItemNBT {

    private final NBTTagCompound nbtTagCompound;
    private final net.minecraft.world.item.ItemStack nmsItemStack;

    public ItemNBT(ItemStack itemStack) {
        this.nmsItemStack = CraftItemStack.asNMSCopy(itemStack);
        this.nbtTagCompound = this.nmsItemStack == null ? new NBTTagCompound() : this.nmsItemStack.w();
    }

    public String getString(String tag) {
        return this.nbtTagCompound.l(tag);
    }

    public ItemNBT setString(String tag, String value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public int getInt(String tag) {
        return this.nbtTagCompound.h(tag);
    }

    public ItemNBT setInt(String tag, int value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public short getShort(String tag) {
        return this.nbtTagCompound.g(tag);
    }

    public ItemNBT setShort(String tag, short value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public long getLong(String tag) {
        return this.nbtTagCompound.i(tag);
    }

    public ItemNBT setLong(String tag, long value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public float getFloat(String tag) {
        return this.nbtTagCompound.j(tag);
    }

    public ItemNBT setFloat(String tag, float value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public double getDouble(String tag) {
        return this.nbtTagCompound.k(tag);
    }

    public ItemNBT setDouble(String tag, double value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public byte getByte(String tag) {
        return this.nbtTagCompound.d(tag);
    }

    public ItemNBT setByte(String tag, byte value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public boolean getBoolean(String tag) {
        return this.nbtTagCompound.q(tag);
    }

    public ItemNBT setBoolean(String tag, boolean value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public byte[] getByteArray(String tag) {
        return this.nbtTagCompound.m(tag);
    }

    public ItemNBT setByteArray(String tag, byte[] value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public int[] getIntArray(String tag) {
        return this.nbtTagCompound.n(tag);
    }

    public ItemNBT setIntArray(String tag, int[] value) {
        this.nbtTagCompound.a(tag, value);
        return this;
    }

    public ItemNBT removeTag(String tag) {
        this.nbtTagCompound.b(tag);
        return this;
    }

    public boolean hasTag(String tag) {
        return this.nbtTagCompound.e(tag);
    }

    public ItemStack build() {
        this.nmsItemStack.c(this.nbtTagCompound);

        return CraftItemStack.asBukkitCopy(this.nmsItemStack);
    }

    public ItemBuilder toBuilder() {
        return new ItemBuilder(this.build());
    }

}
