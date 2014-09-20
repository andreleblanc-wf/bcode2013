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

    public int readBroadcastSecure(int slot, int defaultValue) {
        return Util.readBroadcastSecure(rc, slot, defaultValue);
    }

    public boolean broadcastSecure(int slot, int val) {
        return Util.broadcastSecure(rc, slot, val);
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
