package team028;

import battlecode.common.*;

import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Random;

public class Util {

    protected static final int CHECK_ADD = 691;


    public static void sortLocationsByNearest(MapLocation[] arr, final MapLocation loc) {

        Arrays.sort(arr, new Comparator<MapLocation>() {
            @Override
            public int compare(MapLocation o1, MapLocation o2) {
                return o1.distanceSquaredTo(loc) - o2.distanceSquaredTo(loc);
            }
        });

    }

    public static int mapLocationToInt(MapLocation loc) {
        return ((((short) loc.x) << 16) | ((short)loc.y) & 0xFFFF);
    }

    public static MapLocation intToMapLocation(int loc) {
        return new MapLocation(loc >>> 16, loc & 0xFFFF);
    }

    public static int roll(int max) {
        return (int) Math.floor(Math.random() * max);
    }

    public static int readBroadcastSecure(RobotController rc, int slot, int defaultValue) {
        int val;
        try {
            val = rc.readBroadcast(slot);
            int check = rc.readBroadcast(slot + 32767);
            if (val + CHECK_ADD != check) {
                val = defaultValue;
                //broadcastSecure(rc, slot, defaultValue);
            }
        } catch (GameActionException ex) {
            val = defaultValue;
        }
        return val;
    }

    public static boolean broadcastSecure(RobotController rc, int slot, int val) {
        try {
            rc.broadcast(slot, val);
            rc.broadcast(slot + 32767, val + CHECK_ADD);
            return true;
        } catch (GameActionException ex) {}
        return false;
    }

    public static void shuffleDirections(Direction[] ar) {

        for (int i = ar.length - 1; i > 0; i --) {
            int index = roll(i + 1);
            // Simple swap
            Direction a = ar[index];
            ar[index] = ar[i];
            ar[i] = a;
        }

    }
}
