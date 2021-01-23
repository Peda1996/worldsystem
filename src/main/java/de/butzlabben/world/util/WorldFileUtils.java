package de.butzlabben.world.util;

import de.butzlabben.world.WorldSystem;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.HashMap;
import java.util.function.Function;

public class WorldFileUtils {

    public static boolean deleteOldLocks(File world) {
        //Probably not needed
        final String lockFile = world.toPath().toString() + File.separator + "world.lock";
        return new File(lockFile).delete();
    }

    public static HashMap<File, BukkitTask> waitingForMoveFrom = new HashMap<>();

    public static int moveBackDelay = 2 * 20; //moving back is delayed for 2 seconds
    public static int deleteDelay = 30 * 20; //moving back is delayed for 30 seconds

    public static void moveDirectoryToDirectory(File src, File dest, boolean create) throws IOException {
        if (waitingForMoveFrom.containsKey(dest)) {
            waitingForMoveFrom.get(dest).cancel();
            waitingForMoveFrom.remove(dest);
            return;
        }

        final String srcLock = src.getName() + ".lock";
        final String destLock = dest.getName() + ".lock";

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

    public static void cancelScheduledForLaterMove(File world) {
        if (waitingForMoveFrom.containsKey(world)) {
            waitingForMoveFrom.get(world).cancel();
            waitingForMoveFrom.remove(world);
        }
    }

    public static void moveDirectoryToDirectoryLater(File worldinserver, File worlddir, boolean b, long time, Function<Void, Void> callback) {
        if (waitingForMoveFrom.containsKey(worldinserver))
            return;

        waitingForMoveFrom.put(worldinserver, Bukkit.getScheduler().runTaskLater(WorldSystem.getInstance(),
                () -> {
                    try {
                        moveDirectoryToDirectory(worldinserver, worlddir, b);
                        waitingForMoveFrom.remove(worldinserver);
                        if (callback != null)
                            callback.apply(null);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                },
                time//wait 10 seconds before we move the thing to the next folder

                )
        );
    }

    public static void moveDirectoryToDirectoryLater(File worldinserver, File worlddir, boolean b) {
        moveDirectoryToDirectoryLater(worldinserver, worlddir, b, moveBackDelay, null);
    }
}