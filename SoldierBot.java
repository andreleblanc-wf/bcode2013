package team028;

import battlecode.common.*;

public class SoldierBot extends Bot {

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
        if (target.equals(myHqLocation)) {
            turtle();
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
        if (myLocation.isAdjacentTo(myHqLocation)) {
            // move away from HQ;
            tryMove(away, true);
        } else if (myHqLocation.distanceSquaredTo(myLocation) <= 9) {
            // check for an ally directly behind you (towards HQ)
            // and move forward (away from HQ) if there is one.
            Robot behindMe = (Robot) rc.senseObjectAtLocation(myLocation.add(away.opposite()));
            if (behindMe != null && behindMe.getTeam() == myTeam) {
                tryMove(away, true);
            } else {
                // stay put.
            }
        } else {
            tryMove(away.opposite(), true);
        }
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
