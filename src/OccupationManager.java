import java.util.*;
import java.lang.Math;

enum Occupation
{
  // Poor
  HIRELING, PEASANT, LABORER,
  // Harvesters
  LUMBERJACK, MINER, FARMER,
  // Secondary Materials
  CHARCOALER,
  // Primary Products
  ARMORER, MASON, MILLER, WEAPONCRAFTER, WOODCRAFTER,
  // Services
  CLERIC, PRIEST, MERCHANT, LAW_ENFORCEMENT,
  // Prestige
  NOBLE, POLITICIAN,
}

public class OccupationManager
{
  // Super table of base harvest rates before other factors
  public static final Map<Occupation, Map<Resource, Integer>>
    base_harvest_rates = new Hashtable<Occupation, Map<Resource, Integer>>()
    {{
      put(Occupation.LUMBERJACK, new Hashtable<Resource, Integer>()
      {{
        put(Resource.LOGS, 500);
        put(Resource.LUMBER, 5000);
      }});
      put(Occupation.MINER, new Hashtable<Resource, Integer>()
      {{
        put(Resource.METAL, 300);
        put(Resource.CP, 150);
        put(Resource.GEMS, 10);
        put(Resource.STONE, 1000);
      }});
      put(Occupation.FARMER, new Hashtable<Resource, Integer>()
      {{
        put(Resource.WHEAT, 8000);
        // put(Resource.LIVESTOCK, 1);
        put(Resource.FIBERS, 5000);
      }});
    }};

  // Table to track how each occupation's resource harvesting is
  //  allocated. Useful if a particular occupation can harvest more than
  //  one type of resource.
  // NOTE - The sum of the N doubles must be 1.0
  private Map<Occupation, Map<Resource, Double>>
    harvest_policy = new Hashtable<Occupation, Map<Resource, Double>>()
    {{
      put(Occupation.LUMBERJACK, new Hashtable<Resource, Double>()
      {{
        put(Resource.LOGS, 0.75);
        put(Resource.LUMBER, 0.25);
      }});
      put(Occupation.MINER, new Hashtable<Resource, Double>()
      {{
        put(Resource.METAL, 0.25);
        put(Resource.CP, 0.0);
        put(Resource.GEMS, 0.0);
        put(Resource.STONE, 0.25);
      }});
      put(Occupation.FARMER, new Hashtable<Resource, Double>()
      {{
        put(Resource.WHEAT, 0.75);
        // put(Resource.LIVESTOCK, 0.0);
        put(Resource.FIBERS, 0.25);
      }});
    }};
  
  // Table to track which recipes are associated with which Occupation.
  //  This is useful to determine what to do with each non-primary
  //  resource.
  public static final Map<Occupation, Map<Resource, Boolean>>
    process_policy = new Hashtable<Occupation, Map<Resource, Boolean>>()
    {{
      put(Occupation.WOODCRAFTER, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.LUMBER, true);
      }});
      put(Occupation.CHARCOALER, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.CHARCOAL, true);
      }});
      put(Occupation.MASON, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.STONE, true);
      }});
      put(Occupation.MILLER, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.BREAD, true);
      }});
      put(Occupation.WEAPONCRAFTER, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.WEAPONS, true);
      }});
      put(Occupation.ARMORER, new Hashtable<Resource, Boolean>()
      {{
        put(Resource.ARMOR, true);
      }});
    }};
  
  // Table that tracks which occupations can be converted into the high-level
  // occupation, i.e. to get to some Occupation X, you need Occupations A, B, or C.
  // First Occupation - Occupation you would like to obtain/transfer to
  // Second Occupation - Occupation needed that can be converted to the first
  // Integer - Number of ticks/updates/cycles needed for the change to occur
  //   NOTE - A number of 0 implies an instantaneous transfer
  public static final Map<Occupation, Map<Occupation, Integer>>
    occupation_conversion_policy = new Hashtable<Occupation, Map<Occupation, Integer>>()
    {{
      put(Occupation.PEASANT, new Hashtable<Occupation, Integer>()
      {{
        put(Occupation.LABORER, 0);
      }});
      put(Occupation.LABORER, new Hashtable<Occupation, Integer>()
      {{
        put(Occupation.LABORER, 0);
      }});
      put(Occupation.LUMBERJACK, new Hashtable<Occupation, Integer>()
      {{
        put(Occupation.MINER, 1);
        put(Occupation.FARMER, 2);
        put(Occupation.MASON, 1);
        put(Occupation.MILLER, 3);
        put(Occupation.WOODCRAFTER, 1);
      }});
      put(Occupation.MINER, new Hashtable<Occupation, Integer>()
      {{
        put(Occupation.LUMBERJACK, 1);
        put(Occupation.FARMER, 2);
        put(Occupation.MASON, 1);
        put(Occupation.MILLER, 3);
        put(Occupation.WOODCRAFTER, 1);
      }});
    }};

  private Occupation type;

  public OccupationManager() {}

  public Map<Occupation, Map<Resource, Double>> getHarvestPolicy()
  {
    return harvest_policy;
  }

  public Boolean updatePolicy(Occupation o, Resource r, Double rate)
  {
    if (harvest_policy.get(o) == null)
    {
      return false;
    }
    if (harvest_policy.get(o).get(r) == null)
    {
      return false;
    }
    harvest_policy.get(o).put(r, rate);
    return true;
  }

  // Find total weights in a policy
  // TODO - Check that the weights in a given occupation sum to 1.0
  public Double verifyPolicyWeight(Occupation o)
  {
    // Make sure the Occupation given is a harvest one
    if (harvest_policy.get(o) != null)
    {
      Double total = 0.0;
      Map<Resource, Double> weights = harvest_policy.get(o);
      // Iterate through weights and find total value
      for (Map.Entry<Resource, Double> entry : weights.entrySet())
      {
        total += entry.getValue();
      }
      return total;
    }
    return 0.0;
  }

  public Map<Resource, Integer> performHarvest(Occupation o, int I)
  {
    System.out.println("Eddie harvest : " + I);
    if (I < 1 || harvest_policy.get(o) == null || base_harvest_rates.get(o) == null)
    {
      return null;
    }

    Map<Resource, Integer> harvested = new Hashtable<Resource, Integer>();
    Double total_weight = verifyPolicyWeight(o);
    Map<Resource, Integer> base_rates = base_harvest_rates.get(o);
    Map<Resource, Double> weights = harvest_policy.get(o);

    // Iterate through weights and find total value
    for (Map.Entry<Resource, Double> entry : weights.entrySet())
    {
      Resource r = entry.getKey();
      // harvested = base_rate * weight * (log2 (I) + 1)
      int quantity = (int)( (base_rates.get(r) * entry.getValue() * ((Math.log(I) / Math.log(2)) + 1)) / total_weight);
      harvested.put(r, quantity);
    }
    System.out.println(harvested);
    return harvested;
  }

  // Check if going from Occupation from to Occupation to
  //  is a valid conversion
  public Boolean isValidConversion(Occupation dest, Occupation src)
  {
    if (occupation_conversion_policy.get(dest) != null)
    {
      if (occupation_conversion_policy.get(dest).get(src) != null)
      {
        return true;
      }
    }
    return false;
  }

  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    OccupationManager om = new OccupationManager();
    System.out.println("Ed : " + occupation_conversion_policy);
    if (om.isValidConversion(Occupation.LUMBERJACK, Occupation.WOODCRAFTER)) passes++;
    if (!om.isValidConversion(Occupation.HIRELING, Occupation.NOBLE)) passes++;
    if (!om.isValidConversion(Occupation.NOBLE, Occupation.PEASANT)) passes++;

    System.out.println("Ed : " + om.performHarvest(Occupation.LUMBERJACK, 1));
    System.out.println("Ed : " + om.performHarvest(Occupation.LUMBERJACK, 3));
    System.out.println("Ed : " + om.performHarvest(Occupation.LUMBERJACK, 8));
    System.out.println("Ed : " + om.performHarvest(Occupation.LUMBERJACK, 10));
    System.out.println(om.verifyPolicyWeight(Occupation.LUMBERJACK));

    System.out.println(om.updatePolicy(Occupation.LUMBERJACK, Resource.METAL, 100.0));
    System.out.println(om.verifyPolicyWeight(Occupation.LUMBERJACK));
    System.out.println(om.updatePolicy(Occupation.LUMBERJACK, Resource.LUMBER, 0.1));
    System.out.println(om.verifyPolicyWeight(Occupation.LUMBERJACK));
    System.out.println("Ed : " + om.performHarvest(Occupation.LUMBERJACK, 1));

    System.out.println("TESTS PASSED : [" + passes + "/3]");
  }
}
