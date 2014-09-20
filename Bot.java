package team028;

import battlecode.common.*;

public abstract class Bot implements IBot {

    protected RobotController rc;

    protected RobotType myRobotType;
    protected int myRobotId;
    protected Team myTeam;

    protected Team opponentTeam;

    protected Robot myRobot;
    protected MapLocation myLocation;

    protected MapLocation enemyHqLocation;
    protected MapLocation myHqLocation;

    protected int width;
    protected int height;

    protected RobotInfo myRobotInfo = null;

    protected double myHealth;

    protected static final Direction[] ALL_DIRECTIONS = new Direction[] {
            Direction.NORTH,
            Direction.SOUTH,
            Direction.EAST,
            Direction.WEST,
            Direction.NORTH_WEST,
            Direction.NORTH_EAST,
            Direction.SOUTH_WEST,
            Direction.SOUTH_EAST
    };

    public int readBroadcastSecure(int slot, int defaultValue) {
        return Util.readBroadcastSecure(rc, slot, defaultValue);
    }

    public boolean broadcastSecure(int slot, int val) {
        return Util.broadcastSecure(rc, slot, val);
    }


    protected boolean hasEnemyMine(MapLocation loc) {
        Team team = rc.senseMine(loc);
        return team == Team.NEUTRAL || team == opponentTeam;
    }

    public Bot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {

        this.myRobot = robot;
        this.myRobotId = robotId;
        this.myTeam = team;
        this.opponentTeam = team.opponent();
        this.myRobotType = robotType;

        this.rc = rc;

        this.enemyHqLocation = rc.senseEnemyHQLocation();
        this.myHqLocation = rc.senseHQLocation();

        this.width = rc.getMapWidth();
        this.height = rc.getMapHeight();
    }


    public MapLocation selectNearestEnemy(int range) throws GameActionException {
        Robot[] enemies = rc.senseNearbyGameObjects(Robot.class, myLocation, range*range, opponentTeam);
        MapLocation nearest = null;
        int nearestDistSq = -1;
        int distSq = -1;
        MapLocation robotLoc;
        for (Robot r: enemies) {
            robotLoc = rc.senseLocationOf(r);
            distSq = robotLoc.distanceSquaredTo(myLocation);
            if (nearest == null || distSq < nearestDistSq) {
                nearest = robotLoc;
                nearestDistSq = distSq;
                if (nearest.isAdjacentTo(myLocation)) {
                    return nearest;
                }
            }
        }
        return nearest;
    }

    public void go() throws GameActionException {
        // keep this up to date each turn
        myRobotInfo = rc.senseRobotInfo(myRobot);
        myHealth = myRobotInfo.energon;
        myLocation = myRobotInfo.location;

        // clear per-bot debug output
        rc.setIndicatorString(0, "");
        rc.setIndicatorString(1, "");
        rc.setIndicatorString(2, "");

    }

}
