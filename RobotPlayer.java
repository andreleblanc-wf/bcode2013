package team028;

import battlecode.common.*;

/** The example funcs player is a player meant to demonstrate basic usage of the most common commands.
 * Robots will move around randomly, occasionally mining and writing useless messages.
 * The HQ will spawn soldiers continuously. 
 */
public class RobotPlayer {
    public static void run(RobotController rc) {

        Robot robot = rc.getRobot();

        int robotId = robot.getID();
        RobotType robotType = rc.getType();

        Team team = robot.getTeam();

        Bot bot = null;

        switch (robotType) {
            case HQ:
                bot = new HQBot(robot, robotId, team, robotType, rc);
                break;
            case SOLDIER:
                bot = new SoldierBot(robot, robotId, team, robotType, rc);
                break;
            case SUPPLIER:
                bot = new SupplierBot(robot, robotId, team, robotType, rc);
                break;
            case SHIELDS:
                bot = new ShieldBot(robot, robotId, team, robotType, rc);
                break;
            case ARTILLERY:
                bot = new ArtilleryBot(robot, robotId, team, robotType, rc);
                break;
            case GENERATOR:
                bot = new GeneratorBot(robot, robotId, team, robotType, rc);
                break;
            case MEDBAY:
                bot = new MedBayBot(robot, robotId, team, robotType, rc);
                break;
            default:
                System.out.println("Unhandled bot type: " + robotType);
                break;
        }

        while(true) {
            try {
                bot.go();
            } catch (GameActionException e) {
                e.printStackTrace();
            }
            rc.yield();
        }

    }}
