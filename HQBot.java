package team006;

import battlecode.common.*;


public class HQBot extends Bot {

    protected int soldierCount;
    protected int radius = 3;
    protected MapLocation chargeTarget;

    protected int mapWidth;
    protected int mapHeight;

    public HQBot(Robot robot, int robotId, Team team, RobotType robotType, RobotController rc) {
        super(robot, robotId, team, robotType, rc);
    }

    public void go() throws GameActionException {
        super.go();
        rc.setIndicatorString(2, "turnstart " + Clock.getBytecodesLeft());
        mapWidth = rc.getMapWidth();
        mapHeight = rc.getMapHeight();
        allEncampments = rc.senseAllEncampmentSquares();
        alliedEncampents = rc.senseAlliedEncampmentSquares();
        int maxDim = Math.max(width, height);
        int oldRadius = Util.readBroadcastSecure(rc, Slot.RADIUS, -1);
        radius = Math.min(Clock.getRoundNum() / 50, 10);
        if (radius < 3) {
            radius = 3;
        }
        if (oldRadius > radius) {
            radius = oldRadius -1;
        } else if (Math.random() < 0.1 && radius > 2) {
            radius *= 2;
        }
        Util.broadcastSecure(rc, Slot.RADIUS, radius);

        soldierCount = rc.senseNearbyGameObjects(Robot.class, new MapLocation(width/2, height/2), maxDim * maxDim, myTeam).length - alliedEncampents.length;

        soldierCount = getSoldierCount(myTeam);


        rc.setIndicatorString(1, "SoldierCount: " + soldierCount);

        Robot[] enemySoldiers = getSoldiers(opponentTeam);

        chargeTarget = selectNearestEnemy(Math.max(mapWidth, mapHeight), enemySoldiers);

        int threshold = 20;
        int bonus = Math.min(15, Clock.getRoundNum() / 100);
        threshold += bonus;

        if (chargeTarget == null) {
            chargeTarget = myHqLocation;
        }
        rc.setIndicatorString(2, "ChargeTarget: " + chargeTarget);
        Util.broadcastSecure(rc, Slot.CHARGE_TARGET, Util.mapLocationToInt(chargeTarget));

//        // If we're not active there's nothing left we can do
        if (!rc.isActive()) {
            return;
        }


        rc.setIndicatorString(2, "Maybe Upgrade");
        if (!tryUpgrade()) {
            try {
                trySpawn();
                int researchRounds = 15;
                if (Clock.getRoundNum() < 100) {
                    researchRounds = 3;
                }
                Util.broadcastSecure(rc, Slot.RESEARCH_ROUND, researchRounds);
            } catch (GameActionException ex) {
                ex.printStackTrace();
            }
        }
    }

    private Robot[] getSoldiers(Team team) {
        MapLocation mapCenter = new MapLocation(mapWidth/2, mapHeight/2);
        int halfSize = (Math.max(mapWidth, mapHeight)/2) + 1;
        return rc.senseNearbyGameObjects(Robot.class, mapCenter, halfSize * halfSize, team);
    }

    private int getSoldierCount(Team team) {
        return getSoldiers(team).length;
    }

    private boolean trySpawn() throws GameActionException {
        if (Clock.getBytecodesLeft() < 1000) {
            return false;
        }
//
//        if (rc.getTeamPower() < (soldierCount + alliedEncampents.length) * 2) {
//            return false;
//        }
        if (soldierCount > 25) {
            return false;
        }
        MapLocation loc;
        Direction[] allDirections = ALL_DIRECTIONS.clone();
        Util.shuffleDirections(allDirections);

        for (Direction dir: allDirections) {
            loc = myHqLocation.add(dir);
            if (hasEnemyMine(loc) || rc.senseObjectAtLocation(loc) != null) {
                continue;
            }

            if (rc.canMove(dir)) {
                rc.spawn(dir);
                return true;
            }

        }
        return false;
    }


    private static Upgrade[] upgrades = new Upgrade[] {
            Upgrade.PICKAXE,
            Upgrade.NUKE
    };

    private boolean tryUpgrade() throws GameActionException {

        int nukeLeft = Upgrade.NUKE.numRounds - rc.checkResearchProgress(Upgrade.NUKE);
        int roundsLeft = 1999 - Clock.getRoundNum();
        if (nukeLeft >= roundsLeft) {
            try {
                rc.researchUpgrade(Upgrade.NUKE);
            } catch (GameActionException ex) {
            }
            return true;
        }
        int researchRound = Util.readBroadcastSecure(rc, Slot.RESEARCH_ROUND, -1);

        if (researchRound == 0 || researchRound == -1) {
            rc.setIndicatorString(2, "Not a reasearch round " + researchRound);
            return false;
        }

        if (nukeLeft < roundsLeft && chargeTarget != null && !chargeTarget.equals(myHqLocation)) {
            if (chargeTarget.distanceSquaredTo(myHqLocation) < radius * radius) {
                rc.setIndicatorString(2, "Got a target instead");
               return false;
            }
        } else if (nukeLeft < roundsLeft && soldierCount <= 2) {
            rc.setIndicatorString(2, "Need soldiers");
            return false;
        }

        Util.broadcastSecure(rc, Slot.RESEARCH_ROUND, researchRound - 1);

        int remainingRounds = 1000 - Clock.getRoundNum();
        if (!rc.hasUpgrade(upgrades[upgrades.length-2]) && remainingRounds < Upgrade.NUKE.numRounds) {
            try {
                rc.researchUpgrade(Upgrade.NUKE);
                rc.setIndicatorString(2, "researching nuke");
                return true;
            } catch (GameActionException ex) {
                rc.setIndicatorString(2, "nuke excepion");
                return false;
            }
        } else {
            for (Upgrade u: upgrades) {
                if (!rc.hasUpgrade(u)) {
                    try {
                        rc.researchUpgrade(u);
                        rc.setIndicatorString(2, "researching " + u);
                        return true;
                    } catch (GameActionException ex) {
                        ex.printStackTrace();
                        rc.setIndicatorString(2, "researching " + u + " failed");
                        return false;
                    }
                }
            }
            rc.setIndicatorString(2, "No upgrades?");
        }
        return false;

    }

}
