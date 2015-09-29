package team006;

import battlecode.common.*;

public class SoldierBot extends Bot {

    private static final int CAMP_SEARCH_CAP = 12;
    protected MapLocation target;

    public SoldierBot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
        super(robot, robotId, team, robotType, rc);
    }

    public final void go() throws GameActionException {
        super.go();

        // read the target and default to HQ if its been corrupted.

        // check for adjacent enemies
        if (rc.senseNearbyGameObjects(Robot.class, myLocation, 1, opponentTeam).length > 0) {
            return;
        }

        if (hasEnemyMine(myLocation) && rc.isActive()) {
            tryMove(myLocation.directionTo(myHqLocation), true);
            return;
        }
        rc.setIndicatorString(0, "Trying Charge");

        if (!doCharge()) {
            rc.setIndicatorString(0, "not charging");
            if (rc.senseEncampmentSquare(myLocation) && rc.isActive()) {
                rc.setIndicatorString(0, "claiming");
                if (claimEncampment()) {
                    return;
                }
            }

            if (rc.getEnergon() < 20) {
                rc.setIndicatorString(0, "dying");
                rc.suicide();
                return;
            }

            int radius = Util.readBroadcastSecure(rc, Slot.RADIUS, -1);
            if (radius == -1) {
                radius = 3;
            }
            int halfSize = Math.max(rc.getMapHeight(), rc.getMapWidth()) / 2;
            if (rc.senseMine(myLocation) == null && rc.isActive()) {
                rc.setIndicatorString(0, "mining");
                rc.layMine();
            } else if (myLocation.distanceSquaredTo(myHqLocation) < (radius*radius) && rc.isActive()) {
                rc.setIndicatorString(0, "moving away");
                try {
                    tryMove(myHqLocation.directionTo(myLocation), true);
                } catch (GameActionException ex) {
                }
            } else if (rc.isActive()){
                try {
                    Direction moveDir = myLocation.directionTo(myHqLocation);
                    if (Math.random() < 0.25) {
                        moveDir = moveDir.rotateLeft();
                    } else if (Math.random() > 0.75) {
                        moveDir = moveDir.rotateRight();
                    }
                    tryMove(moveDir, true);
                } catch (GameActionException ex) {
                }
            }

        }
    }

    private boolean doCharge() {
        int chargeLocInt = Util.readBroadcastSecure(rc, Slot.CHARGE_TARGET, -1);
        if (chargeLocInt != -1) {
            MapLocation chargeLocation = Util.intToMapLocation(chargeLocInt);
            if (chargeLocation.equals(myHqLocation)) {
                return false;
            } else if (myLocation.isAdjacentTo(chargeLocation)) {
                return true;
            } else {
                try {
                    tryMove(myLocation.directionTo(chargeLocation), true);
                } catch (GameActionException e) {
                }
                return true;
            }
        }
        return false;
    }

    private boolean doEncampment() throws GameActionException {
        int botId = Util.readBroadcastSecure(rc, Slot.ENCAMPMENT_BOT_ID, -1);
        if (botId == -1) {
            rc.setIndicatorString(0, "Corrupt Encampment Bot Id");
            return false;
        } else if (botId == 0 || botId == myRobotId) {
            rc.setIndicatorString(0, "I will be the encampment bot!");
            Util.broadcastSecure(rc, Slot.ENCAMPMENT_BOT_ID, myRobotId);

            int encampmentLocationInt = Util.readBroadcastSecure(rc, Slot.ENCAMPMENT_LOCATION, -1);

            if (encampmentLocationInt == -1) {
                rc.setIndicatorString(0, "corrupt encampment location!");
                return false;
            } else {
                if (!rc.isActive()) {
                    return false;
                }
                MapLocation encampmentLocation = Util.intToMapLocation(encampmentLocationInt);
                GameObject onSpot = null;
                try {
                    onSpot = rc.senseObjectAtLocation(encampmentLocation);
                } catch (GameActionException ex) {
                }

                if (myLocation.distanceSquaredTo(encampmentLocation) == 1 && onSpot != null) {
                    rc.setIndicatorString(0, "someone is on the encampment!");
                    return true;
                } else if (myLocation.distanceSquaredTo(encampmentLocation) == 0) {
                    rc.setIndicatorString(0, "claiming");
                    claimEncampment();
                    return true;
                } else {
                    rc.setIndicatorString(0, "moving towards encampment");
                    tryMove(myLocation.directionTo(encampmentLocation), true);
                    return true;
                }
            }
        } else {
            rc.setIndicatorString(0, "Someone else is the encampment bot");
        }
        return false;
    }

    private boolean notStupidMineSpot() {
        Direction enemyDir = myHqLocation.directionTo(enemyHqLocation);
        for (Direction d: getAlternateDirections(myHqLocation.directionTo(myLocation))) {
            if (d == enemyDir) {
                return true;
            }
        }
        return false;
    }

    private boolean claimEncampment() throws GameActionException {

        if (myLocation.distanceSquaredTo(myHqLocation) < 9 && myHqLocation.directionTo(enemyHqLocation) == myHqLocation.directionTo(myLocation)) {
            return false;
        }

        RobotType type = RobotType.ARTILLERY;
        allEncampments = rc.senseAllEncampmentSquares();
        alliedEncampents = rc.senseAlliedEncampmentSquares();
        if (rc.getTeamPower() > rc.senseCaptureCost()) {
            rc.captureEncampment(type);
            return true;
        }
        return false;
    }


    private boolean tryMove(Direction dir, boolean tryAlternates) throws GameActionException {
        if (rc.canMove(dir)) {
            rc.setIndicatorString(0, "Want to move " + dir);
            MapLocation target = myLocation.add(dir);
            if (hasEnemyMine(target)) {
                rc.setIndicatorString(1, "Defusing mine @ " + target);
                rc.defuseMine(target);
            } else {
                rc.setIndicatorString(1, "Moving to " + target);
                rc.move(dir);
            }
            return true;
        } else if (tryAlternates) {
            rc.setIndicatorString(2, "Trying alternatives");
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
