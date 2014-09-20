package team028;

import battlecode.common.*;

import java.util.Arrays;

public class SoldierBot extends Bot {

    private static final int CAMP_SEARCH_CAP = 12;
    protected MapLocation target;

    public SoldierBot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
        super(robot, robotId, team, robotType, rc);
    }

    public final void go() throws GameActionException {
        super.go();

        // read the target and default to HQ if its been corrupted.
        target = Util.intToMapLocation(readBroadcastSecure(Slot.TARGET, Util.mapLocationToInt(myHqLocation)));

        if (!rc.isActive()) {
            return;
        }

        // check for adjacent enemies
        if (rc.senseNearbyGameObjects(Robot.class, myLocation, 1, opponentTeam).length > 0) {
            return;
        }

        rc.setIndicatorString(0, "Target: " + target);

        boolean pastPointofNoReturn = myLocation.distanceSquaredTo(enemyHqLocation) < 36;

        if (target.equals(myHqLocation) && !pastPointofNoReturn) {
            turtle();
        } else if (pastPointofNoReturn) {
            if (tryMove(myLocation.directionTo(enemyHqLocation), true)) {
                return;
            }
        } else {
            if (!myLocation.isAdjacentTo(target)) {
                tryMove(myLocation.directionTo(target), true);
            } else {
                // we're adjacent, so we're attacking, don't move.
            }
        }

  }

    private void turtle() throws GameActionException {
        Direction away = myHqLocation.directionTo(myLocation);
        if (rc.senseEncampmentSquare(myLocation) && claimEncampment()) {
            return;
        } else if (Math.random() > 0.75) {
            MapLocation[] campLocs = rc.senseEncampmentSquares(myLocation, 9, Team.NEUTRAL);
            if (campLocs.length < CAMP_SEARCH_CAP) {
                Util.sortLocationsByNearest(campLocs, myLocation);
            } else {
                campLocs = Arrays.copyOf(campLocs, CAMP_SEARCH_CAP);
            }

            for (MapLocation loc: campLocs) {
                if (loc.equals(myLocation)) {
                    continue;
                }
                if (tryMove(myLocation.directionTo(loc), true)) {
                    return;
                }
            }
        }
        if (myLocation.isAdjacentTo(myHqLocation)) {
            if (rc.senseMine(myLocation) == null) {
                rc.layMine();
                return;
            }
            // move away from HQ;
            tryMove(away, true);
        } else if (myHqLocation.distanceSquaredTo(myLocation) <= 16) {
            // check for an ally directly behind you (towards HQ)
            // and move forward (away from HQ) if there is one.
            Robot behindMe = (Robot) rc.senseObjectAtLocation(myLocation.add(away.opposite()));
            if (behindMe != null && behindMe.getTeam() == myTeam) {
                tryMove(away, true);
            } else {
                // stay put.
                if (rc.senseMine(myLocation) == null) {
                    rc.layMine();
                    return;
                }
            }
        } else if (rc.senseMine(myLocation) == null) {
            rc.layMine();
            return;
        } else if (Math.random() < 0.5) {
            tryMove(away.opposite(), true);
        }
    }

    private boolean claimEncampment() throws GameActionException {
        if (myLocation.isAdjacentTo(myHqLocation)) {
            return false;
        } else if (myLocation.distanceSquaredTo(myHqLocation) < 25 && myHqLocation.directionTo(enemyHqLocation) == myHqLocation.directionTo(myLocation)) {
            return false;
        }

        RobotType type = RobotType.GENERATOR;
        allEncampments = rc.senseAllEncampmentSquares();
        alliedEncampents = rc.senseAlliedEncampmentSquares();
        if (alliedEncampents.length % 2 == 0) {
            type = RobotType.SUPPLIER;
        }
        if (alliedEncampents.length > 4 || Clock.getRoundNum() > 400) {
            type = RobotType.ARTILLERY;
        }

        if (rc.getTeamPower() > rc.senseCaptureCost()) {
            rc.captureEncampment(type);
            return true;
        }
        return false;
    }


    private boolean tryMove(Direction dir, boolean tryAlternates) throws GameActionException {
        if (rc.canMove(dir)) {
            if (hasEnemyMine(myLocation.add(dir))) {
                rc.defuseMine(myLocation.add(dir));
            } else {
                rc.move(dir);

            }
            return true;
        } else if (tryAlternates) {
            Direction[] alternates = getAlternateDirections(dir);
            for (Direction d: alternates) {
                if (tryMove(d, false)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * get an array of Directions which are in the same general direction as 'dir'
     * @param dir - the desired direction
     * @return an array of alternate directions which are 'sort of' the same as the provided direction.
     */
    private Direction[] getAlternateDirections(Direction dir) {
        return new Direction[] {
            dir.rotateLeft(),
            dir.rotateRight(),
            dir.rotateLeft().rotateLeft(),
            dir.rotateRight().rotateRight()
        };
    }

}
