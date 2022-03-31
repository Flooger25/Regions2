import java.util.*;
import java.lang.Math;

// JUnit testing infrastructure
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class TileTest
{
    private final Tile tile = new Tile(Tile.TileType.FOREST, new Coordinate(0,0));
    private Dictionary<Resource, Integer> res_saved;

    @Test
    // @org.junit.runner.Order(1)
    // @org.junit.jupiter.testmethod.Order(1)
    public void test1_addPopulation()
    {
        // SETUP
        Population wp = new Population(0);
        wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.ARMORER), 2);
        wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 2);
        tile.getPopulation().absorbPopulation(wp);
        // TEST population stats
        Population out = tile.getPopulation();
        assertEquals(2, out.queryNumOfOccupation(Occupation.ARMORER));
        assertEquals(2, out.queryNumOfOccupation(Occupation.LABORER));
    }

    @Test
    // @Order(2)
    public void test2_baseResources()
    {
        // SETUP
        for (Resource res : Resource.values())
        { 
            tile.extractResource(res, 999999999);
            assertEquals(0, tile.getResourceQuantity(res));
        }
    }

    @Test
    // @Order(3)
    public void test3_baseResources()
    {
        // SETUP
        tile.addResource(Resource.BREAD, 1000);
        tile.addResource(Resource.CP, 2000);
        tile.setAutoUpgrade(false);
        // TEST
        tile.update();
        assertEquals(2, tile.getPopulation().queryNumOfOccupation(Occupation.ARMORER));
        assertEquals(2, tile.getPopulation().queryNumOfOccupation(Occupation.LABORER));
        assertEquals(996, tile.getResourceQuantity(Resource.BREAD));
        assertEquals(1900, tile.getResourceQuantity(Resource.CP));
        System.out.println(tile.getResourceQuantity(Resource.CP));
    }

    public static void main(String[] args)
    {
        JUnitCore JUC = new JUnitCore();
        Result result = JUC.runClasses(TileTest.class);

        for (Failure failure : result.getFailures())
        {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }
}