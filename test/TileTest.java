import java.util.*;
import java.lang.Math;

// JUnit testing infrastructure
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.After;
import org.junit.Before;
import org.junit.runner.notification.Failure;

public class TileTest
{
    private Tile tile;

    @Before
    public void setUp()
    {
        tile = new Tile(Tile.TileType.FOREST);
        // Remove resources within
        for (Resource res : Resource.values())
        {
            tile.extractResource(res, 999999999);
            assertEquals(0, tile.getResourceQuantity(res));
        }
        tile.getPopulation().wipeOutPopulation();
        // Add enough to get infrastructure to 1
        tile.addResource(Resource.CP, 3000);
        tile.addResource(Resource.LUMBER, 100);
        tile.upgradeInfrastructure();
    }

    @After
    public void tearDown()
    {
        // Remove resources within
        for (Resource res : Resource.values())
        {
            tile.extractResource(res, 999999999);
            assertEquals(0, tile.getResourceQuantity(res));
        }
        tile.getPopulation().wipeOutPopulation();
    }

    @Test
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
    public void test_toInfrastructure()
    {
        // SETUP

        // TEST
        assertEquals(1, tile.getInfrastructureLevel());
        assertEquals(120, tile.getCostOfMaintenance());
        // assertEquals(361, tile.getCostOfUpgrade());
        tile.update();
        assertEquals(3000 - 100 - 120, tile.getResourceQuantity(Resource.CP));
    }

    @Test
    public void test_baseTaxation()
    {
        // SETUP
        tile.addResource(Resource.BREAD, 1000);
        Population wp = new Population(0);
        wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.ARMORER), 2);
        wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 2);
        tile.getPopulation().absorbPopulation(wp);

        // TEST
        tile.update();
        assertEquals(1000 - 4, tile.getResourceQuantity(Resource.BREAD));
        assertEquals(2900 - 120 + 12, tile.getResourceQuantity(Resource.CP));
        assertEquals(1, tile.getInfrastructureLevel());
    }

    @Test
    public void test_resourceHarvesting()
    {
        // SETUP
        tile.addResource(Resource.BREAD, 100);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.FARMER), 3);

        // TEST
        assertEquals(0, tile.getResourceQuantity(Resource.WHEAT));
        tile.update();
        assertEquals(3 * 6000, tile.getResourceQuantity(Resource.WHEAT));
        // NOTE : 75% of 8,000
        // TODO - Figure out race condition
        // Add LABORERs to create more WHEAT
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 10);
        // Reset wheat resource to 0
        tile.extractResource(Resource.WHEAT, 6000 * 3);
        assertEquals(0, tile.getResourceQuantity(Resource.WHEAT));
        // NOTE: Laborers won't be used due to no demand for more wheat
        tile.update();
        assertEquals(6000 * 3, tile.getResourceQuantity(Resource.WHEAT));
    }

    @Test
    public void test_calculateFoodGeneration()
    {
        // SETUP
        Map<Occupation, Integer> labor_assignment = new Hashtable<Occupation, Integer>()
        {{
            put(Occupation.FARMER, 0);
            put(Occupation.MILLER, 0);
        }};
        int miller = 0; int farmer = 0;
        // NOTE : Need an infrastructure of at least one to harvest, let alone calculate
        //  how much food we can generate.

        // TEST
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(0, miller);
        assertEquals(0, farmer);
        // Add creatures
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 10);
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(1, miller);
        assertEquals(1, farmer);
        // Test with lots of creatures
        labor_assignment.put(Occupation.MILLER, 0);
        labor_assignment.put(Occupation.FARMER, 0);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.PEASANT), 10000);
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(6, miller);
        assertEquals(2, farmer);
        // Test with a stockpile of both food and wheat
        labor_assignment.put(Occupation.MILLER, 0);
        labor_assignment.put(Occupation.FARMER, 0);
        tile.addResource(Resource.WHEAT, 6500);
        tile.addResource(Resource.BREAD, 10000);
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(1, miller);
        assertEquals(0, farmer);
        // Test with a stockpile of both food and wheat 2
        labor_assignment.put(Occupation.MILLER, 0);
        labor_assignment.put(Occupation.FARMER, 0);
        tile.addResource(Resource.BREAD, 10); // Now 10010
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(0, miller);
        assertEquals(0, farmer);
        // Test with no laborers available
        tile.getPopulation().pullCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 10);
        labor_assignment.put(Occupation.MILLER, 0);
        labor_assignment.put(Occupation.FARMER, 0);
        tearDown();
        tile.calculateFoodGeneration(labor_assignment);
        miller = labor_assignment.get(Occupation.MILLER);
        farmer = labor_assignment.get(Occupation.FARMER);
        assertEquals(0, miller);
        assertEquals(0, farmer);
    }

    @Test
    public void test_harvestation_food()
    {
        // SETUP
        Map<Occupation, Integer> labor_assignment = new Hashtable<Occupation, Integer>()
        {{
            put(Occupation.FARMER, 1);
            put(Occupation.MILLER, 1);
        }};

        // TEST
        // Laborers but no Occupationals
        tile.harvest_resources(labor_assignment);
        assertEquals(0, tile.getResourceQuantity(Resource.WHEAT));
        assertEquals(0, tile.getResourceQuantity(Resource.BREAD));
        // Occupationals but no laborers
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.FARMER), 1);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.MILLER), 1);
        tile.harvest_resources(null);
        // TODO - Figure out race condition
        // Race condition occurs because sometimes Farmers get to process before Millers
        // TODO - Solution could be to add resources to a "new_resource" map that's added at the end
        if (6000 == tile.getResourceQuantity(Resource.WHEAT))
        {
            assertEquals(6000, tile.getResourceQuantity(Resource.WHEAT));
            assertEquals(0, tile.getResourceQuantity(Resource.BREAD));
        }
        else
        {
            assertEquals(4000, tile.getResourceQuantity(Resource.WHEAT));
            assertEquals(2000, tile.getResourceQuantity(Resource.BREAD));
        }
        // tile.printTile();
        // tile.harvest_resources(null);
        // tile.printTile();
        // assertEquals(10000, tile.getResourceQuantity(Resource.WHEAT)); // 6K + 6K - 2K
        // assertEquals(2000, tile.getResourceQuantity(Resource.BREAD));
        // // Occupational with laborers
        // tile.harvest_resources(labor_assignment);
        // assertEquals(14800, tile.getResourceQuantity(Resource.WHEAT)); // 10K + (6K + 1.2K) - (2K + 400)
        // assertEquals(4400, tile.getResourceQuantity(Resource.BREAD));  // 2K + (2K + 400)
    }

    @Test
    public void test_harvestation_recipes()
    {
        // SETUP
        Map<Occupation, Integer> labor_assignment = new Hashtable<Occupation, Integer>()
        {{
            put(Occupation.FARMER, 0);
            put(Occupation.MILLER, 0);
        }};
        tile.addResource(Resource.CP, 2000);
        tile.addResource(Resource.FIBERS, 1000);
        tile.addResource(Resource.METAL, 1000);
        tile.addResource(Resource.CHARCOAL, 1000);

        // TEST
        // Nothing
        tile.harvest_resources(null);
        assertEquals(0, tile.getResourceQuantity(Resource.ARMOR));
        // Basic Armoring
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.ARMORER), 1);
        tile.harvest_resources(null);
        assertEquals(2, tile.getResourceQuantity(Resource.ARMOR));
        assertEquals(900, tile.getResourceQuantity(Resource.FIBERS));
        assertEquals(800, tile.getResourceQuantity(Resource.METAL));
        assertEquals(0, tile.getResourceQuantity(Resource.CHARCOAL));
        // Armor limits
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.ARMORER), 1);
        tile.addResource(Resource.FIBERS, 100000);
        tile.addResource(Resource.METAL, 100000);
        tile.addResource(Resource.CHARCOAL, 100000);
        tile.harvest_resources(null);
        assertEquals(2 + 2 * 20, tile.getResourceQuantity(Resource.ARMOR));
        assertEquals(900 + 100000 - 2 * 20 * 50, tile.getResourceQuantity(Resource.FIBERS));
        assertEquals(800 + 100000 - 2 * 20 * 100, tile.getResourceQuantity(Resource.METAL));
        assertEquals(100000 - 2 * 20 * 500, tile.getResourceQuantity(Resource.CHARCOAL));
    }

    @Test
    // TODO - Implement actual starvation logic
    public void test_starvation()
    {
        // SETUP
        Policy policy = tile.getPolicy();
        tile.addResource(Resource.BREAD, 10);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.WOODCRAFTER), 4);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.FARMER), 2);
        tile.getPopulation().pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 1);
        tile.getPopulation().setRandomnessOverride(true);

        // TEST
        // Everything's fine!
        tile.update();
        assertEquals(2 * 6000, tile.getResourceQuantity(Resource.WHEAT));
        assertEquals(3, tile.getResourceQuantity(Resource.BREAD));
        assertEquals(0, policy.getDemands().size());
        // Things definitely are NOT cash money. Turn FARMER into MILLER
        // NOTE: The default values added in Policy addDefaultValues() are ordered
        tile.update();
        assertEquals(4 * 6000, tile.getResourceQuantity(Resource.WHEAT));
        assertEquals(0, tile.getResourceQuantity(Resource.BREAD));
        assertEquals(2, policy.getDemands().size());
        assertEquals(4, policy.getDemands().get(0).getDemand());
        assertEquals(4, policy.getDemands().get(1).getDemand());
        assertEquals(4, tile.getPopulation().queryNumOfOccupation(Occupation.WOODCRAFTER));
        assertEquals(1, tile.getPopulation().queryNumOfOccupation(Occupation.FARMER));
        assertEquals(1, tile.getPopulation().queryNumOfOccupation(Occupation.LABORER));
        assertEquals(1, tile.getPopulation().queryNumOfOccupation(Occupation.MILLER));
        // Everything is better
        tile.update();
    }

    // @Test
    // public void test_randomOccupationGen()
    // {
    //     // TODO
    // }

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