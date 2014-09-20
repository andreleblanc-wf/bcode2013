package team028;

import battlecode.common.*;


public class HQBot extends Bot {

    MapLocation target = null;
    protected int DESIRED_ARMY_SIZE = 32;
    // number of rounds we've been attacking enemy HQ.
    protected int chargeRounds;

    protected MapLocation[] alliedEncampents;
    protected MapLocation[] allEncampments;

    // the order of upgrades. this should probably be dynamic.
    Upgrade[] upgrades = new Upgrade[] {
            Upgrade.VISION,
            Upgrade.PICKAXE,
            Upgrade.FUSION,
            Upgrade.DEFUSION,
            Upgrade.NUKE
    };

    protected int soldierCount;

    public HQBot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
        super(robot, robotId, team, robotType, rc);
    }

    public void go() throws GameActionException {
        super.go();
        allEncampments = rc.senseAllEncampmentSquares();
        alliedEncampents = rc.senseAlliedEncampmentSquares();
        int maxDim = Math.max(width, height);
        soldierCount = rc.senseNearbyGameObjects(Robot.class, new MapLocation(width/2, height/2), maxDim * maxDim, myTeam).length - alliedEncampents.length;
        chargeRounds = readBroadcastSecure(Slot.CHARGE_ROUNDS, 0);
        rc.setIndicatorString(2, "Charging for " + chargeRounds + " rounds");
        rc.setIndicatorString(1, soldierCount + " Soldiers.");
        target = selectNearestEnemy(getMaxAttackRange());

        if (target == null) {
            if (soldierCount >= DESIRED_ARMY_SIZE || (chargeRounds > 0 && soldierCount > DESIRED_ARMY_SIZE / 2)) {
                broadcastSecure(Slot.CHARGE_ROUNDS, chargeRounds + 1);
                target = enemyHqLocation;
            } else {
                // turtle tactic.
                chargeRounds = 0;
                broadcastSecure(Slot.CHARGE_ROUNDS, 0);
                target = myHqLocation;
            }
        }
        rc.setIndicatorString(0, "Target: " + target);
        broadcastSecure(Slot.TARGET, Util.mapLocationToInt(target));

        // If we're not active there's nothing left we can do
        if (!rc.isActive()) {
            return;
        }

        // upgrade maybe?
        if (Math.random() < 0.1) {
            if (!tryUpgrade()) {
                trySpawn();
            }
        } else if (!trySpawn()) {
            tryUpgrade();
        }
    }

    private boolean trySpawn() throws GameActionException {
        if (rc.getTeamPower() < soldierCount * 2) {
            return false;
        }
        MapLocation loc;
        Direction[] allDirections = ALL_DIRECTIONS.clone();
        Util.shuffleDirections(allDirections);
        for (Direction dir: allDirections) {
            loc = myHqLocation.add(dir);
            if (Clock.getBytecodesLeft() < 100) {
                return false;
            }
            if (hasEnemyMine(loc) || rc.senseObjectAtLocation(loc) != null) {
                continue;
            }
            rc.spawn(dir);
            return true;
        }
        return false;
    }


    private boolean tryUpgrade() {
        for (Upgrade u: upgrades) {
            if (!rc.hasUpgrade(u)) {
                try {
                    rc.researchUpgrade(u);
                    return true;
                } catch (GameActionException ex) {
                    ex.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }


    public int getMaxAttackRange() {
        int max = 14;
        if (rc.hasUpgrade(Upgrade.VISION)) {
            max += GameConstants.VISION_UPGRADE_BONUS;
        }
        int distToEnemyHQ = (int) Math.sqrt(myHqLocation.distanceSquaredTo(enemyHqLocation));
        if (distToEnemyHQ - 4 < max) {
            max = (int) (distToEnemyHQ * 0.666);
        }
        return max;
    }

}
