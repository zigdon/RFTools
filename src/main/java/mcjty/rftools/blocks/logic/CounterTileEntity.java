package mcjty.rftools.blocks.logic;

import mcjty.lib.entity.GenericTileEntity;
import mcjty.lib.network.Argument;
import mcjty.lib.varia.BlockTools;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;

import java.util.Map;

public class CounterTileEntity extends GenericTileEntity {

    public static final String CMD_SETCOUNTER = "setCounter";
    public static final String CMD_SETCURRENT = "setCurrent";

    // For pulse detection.
    private boolean prevIn = false;

    private int counter = 1;
    private int current = 0;
    private boolean redstoneOut = false;

    public CounterTileEntity() {
    }

    public int getCounter() {
        return counter;
    }

    public int getCurrent() {
        return current;
    }

    public void setCounter(int counter) {
        this.counter = counter;
        current = 0;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    public void setCurrent(int current) {
        this.current = current;
        worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
    }

    @Override
    public boolean canUpdate() {
        return false;
    }



    @Override
    protected void checkStateServer() {
        super.checkStateServer();

        int meta = worldObj.getBlockMetadata(xCoord, yCoord, zCoord);
        boolean newvalue = BlockTools.getRedstoneSignalIn(meta);
        boolean pulse = newvalue && !prevIn;
        prevIn = newvalue;

        boolean newout = false;

        if (pulse) {
            current++;
            if (current >= counter) {
                current = 0;
                newout = true;
            }

            markDirty();

            if (newout != redstoneOut) {
                redstoneOut = newout;
                notifyBlockUpdate();
            }
        }
    }

    @Override
    protected int updateMetaData(int meta) {
        meta = super.updateMetaData(meta);
        return BlockTools.setRedstoneSignalOut(meta, redstoneOut);
    }

    @Override
    public void readFromNBT(NBTTagCompound tagCompound) {
        super.readFromNBT(tagCompound);
        redstoneOut = tagCompound.getBoolean("rs");
        prevIn = tagCompound.getBoolean("prevIn");
    }

    @Override
    public void readRestorableFromNBT(NBTTagCompound tagCompound) {
        super.readRestorableFromNBT(tagCompound);
        counter = tagCompound.getInteger("counter");
        if (counter == 0) {
            counter = 1;
        }
        current = tagCompound.getInteger("current");
    }

    @Override
    public void writeToNBT(NBTTagCompound tagCompound) {
        super.writeToNBT(tagCompound);
        tagCompound.setBoolean("rs", redstoneOut);
        tagCompound.setBoolean("prevIn", prevIn);
    }

    @Override
    public void writeRestorableToNBT(NBTTagCompound tagCompound) {
        super.writeRestorableToNBT(tagCompound);
        tagCompound.setInteger("counter", counter);
        tagCompound.setInteger("current", current);
    }

    @Override
    public boolean execute(EntityPlayerMP playerMP, String command, Map<String, Argument> args) {
        boolean rc = super.execute(playerMP, command, args);
        if (rc) {
            return true;
        }
        if (CMD_SETCOUNTER.equals(command)) {
            setCounter(args.get("counter").getInteger());
            return true;
        } else if (CMD_SETCURRENT.equals(command)) {
            setCurrent(args.get("current").getInteger());
            return true;
        }
        return false;
    }
}
