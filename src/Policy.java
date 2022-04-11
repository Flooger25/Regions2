import java.util.*;

// Policy is a class that determines how a particular Tile is to be
// managed. A policy is owned by either the Tile itself (when not
// governed by a state) or the state governing it.
//
// Managing a Tile comes down to the following:
//  1. Generate/collect resources
//  2. Feed the entire population.
//   - If there isn't enough food
//     => if generateDemands, request food via a Demand
//      - Wait for response on food that might be available elsewhere
//     => if !lockOccupation, transfer occupations to generate food
//   - There is enough food
//     => if prioritizeLocal, subtract food and disable starvation
//     => else, publish ALL food resources
//  3. Publish EXCESS food resources
//   - Collect and/or create Orders to transport resources
//
// [Below are the most interesting/meaningful combinations]
//
// enough_food = T,lockOccupation = T,generateDemands = T,prioritizeLocal = T
// - feed local population
// - publish extra food
//
// enough_food = T,lockOccupation = T,generateDemands = T,prioritizeLocal = F
// - publish food
// - consume whatever is not taken
//
// enough_food = T,lockOccupation = F,generateDemands = T,prioritizeLocal = T
// - feed local population
// - consume whatever is not taken
//
// enough_food = F,lockOccupation = T,generateDemands = T,prioritizeLocal = T
// - feed as much of local population
// - publish needed food
//
// enough_food = F,lockOccupation = F,generateDemands = F,prioritizeLocal = T
// - calculate needed food rate
// - change occupations based on needed food, prioritizing low occupation
// - feed as much of local population
// - publish needed food
//
// enough_food = F,lockOccupation = F,generateDemands = F,prioritizeLocal = F
// - calculate needed food rate
// - change occupations based on needed food, prioritizing low occupation
// - feed as much of local population
// - publish needed food
//
public class Policy
{
    State state;
    // Valid occupations we can transform into
    ArrayList<Occupation> avail_to;
    // Valid occupations we can transform from. This is designed to
    // give a government the ability to choose when and who can be
    // utilized during times of war or starvation.
    ArrayList<Occupation> avail_from;
    //
    // ArrayList<Resource> priority;
    // yes?
    ArrayList<Demand> currentDemands;
    // Whether a Tile is not allowed to change Occupations on its
    // own, while being under the control of a State.
    private Boolean lockOccupation = true;
    // Generate and propagate Demands if the Tile needs resources.
    // One can think of this as a notification mechanism.
    private Boolean generateDemands = true;
    // Immediately feed the current Tile if there is enough food,
    // otherwise save food to be transported to another Tile(s).
    // private Boolean prioritizeLocal = true;
    // Other
    private Boolean auto_upgrade = false;
    private double tax_rate = 10.0 / 100.0;

    public Policy(State state)
    {
        this.state = state;
        initialize_policy();
    }

    public Policy()
    {
        this.state = null;
        initialize_policy();
    }

    private void initialize_policy()
    {
        avail_to = new ArrayList<Occupation>();
        avail_from = new ArrayList<Occupation>();
        currentDemands = new ArrayList<Demand>();
        addDefaultValues();
    }

    // Populate Policy with acceptable Occupations we're willing to
    //  use to change into others. The State or Tile may change this
    //  at any point of course, however, this is just for ease of use.
    private void addDefaultValues()
    {
        avail_from.add(Occupation.HIRELING);
        avail_from.add(Occupation.LUMBERJACK);
        avail_from.add(Occupation.MINER);
        avail_from.add(Occupation.FARMER);
        avail_from.add(Occupation.MINER);
        avail_from.add(Occupation.CHARCOALER);
        avail_from.add(Occupation.MASON);
        avail_from.add(Occupation.MILLER);
        avail_from.add(Occupation.WOODCRAFTER);
        avail_from.add(Occupation.ADVENTURER);
    }

    public Boolean hasState()
    {
        if (state == null)
        {
            return false;
        }
        return true;
    }

    public Double getTaxRate()
    {
        return tax_rate;
    }

    public void setTaxRate(Double t)
    {
        tax_rate = t;
    }

    public ArrayList<Occupation> getOccFrom()
    {
        return avail_from;
    }

    public ArrayList<Occupation> getOccTo()
    {
        return avail_to;
    }

    public void setGenerateDemands(Boolean s)
    {
        generateDemands = s;
    }

    public Boolean getGenerateDemands()
    {
        return generateDemands;
    }

    public void setLockOccupation(Boolean s)
    {
        lockOccupation = s;
    }

    public Boolean getLockOccupation()
    {
        return lockOccupation;
    }

    public void addDemand(Demand d)
    {
        currentDemands.add(d);
    }

    public ArrayList<Demand> getDemands()
    {
        return currentDemands;
    }
}
