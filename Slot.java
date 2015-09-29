package team006;

public class Slot {
    private static int[] _values = {
            373, 464, 860, 2531, 2765, 4621, 6424, 8808, 8923, 9578, 9741,
            10181, 10751, 12481, 12999, 13505, 14396, 16665, 17236, 18508,
            18652, 19576, 20291, 20566, 20983, 21538, 23163, 24139, 24156,
            26820, 26843, 30142
    };


    public static int ENCAMPMENT_BOT_ID = 1;
    public static int ENCAMPMENT_LOCATION = 2;
    public static int CHARGE_TARGET = 3;
    public static int RESEARCH_ROUND = 4;
    public static int RADIUS = 5;

    public static int get(int slot) {
        return _values[slot];
    }

    private static boolean isDefinedSlot(int slot) {
        for (int v: _values) {
            if (v == slot) {
                return true;
            } else if (v > slot) {
                return false;
            }
        }
        return false;
    };

    public static int getJamChannel() {
        int slot = (int) Math.floor(Math.random() * 32768);
        while (isDefinedSlot(slot)) {
            slot = (int) Math.floor(Math.random() * 32768);
        }
        return slot;
    }
}
