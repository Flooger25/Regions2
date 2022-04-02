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

public class PopTest
{
    private Population pop;

    @Before
    public void setUp()
    {
        pop = new Population();
    }

    @After
    public void tearDown()
    {
        // TODO
        pop.wipeOutPopulation();
    }

    @Test
    public void test_micro_evolve()
    {
        // SETUP
        pop.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LUMBERJACK), 10);
        pop.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.NOBLE), 3);
        pop.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.WOODCRAFTER), 7);
        pop.printPopulation();
        // TEST
        for (int i = 0; i < pop.EVOLVE_RATE * pop.EVOLVE_RATE; i++)
        {
            pop.micro_evolve();
            assertEquals(20, pop.getPopulation());
        }
        pop.printPopulation();
    }

    public static void main(String[] args)
    {
        JUnitCore JUC = new JUnitCore();
        Result result = JUC.runClasses(PopTest.class);

        for (Failure failure : result.getFailures())
        {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }
}