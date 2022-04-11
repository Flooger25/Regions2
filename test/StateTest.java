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

public class StateTest
{
    private State state;

    @Before
    public void setUp()
    {
        state = new State(1, null, StateType.KINGDOM, "Kingdom of Bird");
    }

    @After
    public void tearDown()
    {
        // TODO
    }

    @Test
    public void test_add_substate()
    {
        // SETUP
        State s0 = new State(2, null, StateType.DUCHY, "Grand Duchy of York");
        State s1 = new State(3, null, StateType.COUNTY, "Candyland");
        State s2 = new State(4, null, StateType.FIEFDOM, "Baron World");
        State s3 = new State(5, null, StateType.FIEFDOM, "Yorkshire");
        assertEquals(true, state.addSubState(s0));
        assertEquals(true, s1.addSubState(s2));
        assertEquals(true, s1.addSubState(s3));
        assertEquals(true, state.addSubState(s1));
        // TEST
        // Basic stuff
        assertEquals(false, state.hasParent());
        assertEquals(state, s2.getHighestParent());
        assertEquals(false, state.containsSubState(null));
        assertEquals(true, state.containsSubState(s3));
        assertEquals(false, s0.containsSubState(s3));
        assertEquals(false, state.addSubState(s1));
        assertEquals(false, state.addSubState(s3));
        assertEquals(false, s0.addSubState(s3));
        // Removal
        assertEquals(false, state.removeSubState(s3));
        assertEquals(true, state.removeSubState(s0));
        // Get direct parent
        assertEquals(null, state.getDirectParent());
        assertEquals(state, s1.getDirectParent());
        // Absorb
        assertEquals(true, s0.absorbState(s2));
        s0.printState();
        state.printState();
    }

    public static void main(String[] args)
    {
        JUnitCore JUC = new JUnitCore();
        Result result = JUC.runClasses(StateTest.class);

        for (Failure failure : result.getFailures())
        {
            System.out.println(failure.toString());
        }
        System.out.println(result.wasSuccessful());
    }
}