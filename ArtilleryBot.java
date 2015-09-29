package team006;

import battlecode.common.*;

public class ArtilleryBot extends Bot {
    public ArtilleryBot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
        super(robot, robotId, team, robotType, rc);
    }

    public void go() throws GameActionException {
        super.go();
        if (rc.isActive()) {
            Robot[] nearby = rc.senseNearbyGameObjects(Robot.class, myLocation, RobotType.ARTILLERY.attackRadiusMaxSquared, opponentTeam);
            Robot[] tooNearby = rc.senseNearbyGameObjects(Robot.class, myLocation, RobotType.ARTILLERY.attackRadiusMinSquared, opponentTeam);

            MapLocation nearest = null;

            for (Robot r: nearby) {
                if (robotArrayContains(tooNearby, r.getID())) {
                    continue;
                }
                MapLocation loc = rc.senseLocationOf(r);
                if (nearest == null || loc.distanceSquaredTo(myHqLocation) < nearest.distanceSquaredTo(myHqLocation)) {
                    nearest = loc;
                }
            }
            if (nearest != null) {
                rc.attackSquare(nearest);
            }
        }
    }

    public boolean robotArrayContains(Robot[] arr, int robotId) {
        for (Robot r: arr) {
            if (r.getID() == robotId) {
                return true;
            }
        }
        return false;
    }

}
