package de.butzlabben.world.util;

import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;

public class WorldFileUtils {



    public static boolean deleteOldLocks(File world){
        //Probably not needed
        final String lockFile = world.toPath().toString() + File.separator + "world.lock";
        return new File(lockFile).delete();

    }

    public static void moveDirectoryToDirectory(File src, File dest, boolean create) throws IOException {
        final String srcLock = src.getName()+ ".lock";
        final String destLock = dest.getName()+ ".lock";

        FileChannel channel1 = new RandomAccessFile(srcLock, "rw").getChannel();
        FileChannel channel2 = new RandomAccessFile(destLock, "rw").getChannel();

        // Acquire an exclusive lock on this channel's file (blocks until lock can be retrieved)
        FileLock lock1 = channel1.lock();
        FileLock lock2 = channel2.lock();

        // Acquire an exclusive lock on this channel's file (blocks until lock can be retrieved)

        //lock1 = channel1.tryLock();
        //lock2 = channel2.tryLock();

        FileUtils.moveDirectoryToDirectory(src, dest, create);

        // release the lock
        lock1.release();
        lock2.release();

        // close the channel
        channel1.close();
        channel2.close();

    }

}
