import java.util.*;

public class Population
{
  // Rather than having a massive list of creatures, have a
  //  given creature profile and a number indicating how many
  //  of said profile there are. Manage numbers rather than
  //  a bunch of instances
  private Map<Creature, Integer> creatures;

  public Population()
  {
    this.creatures = new HashMap<Creature, Integer>();
  }

  public Population(int pop)
  {
    this.creatures = new HashMap<Creature, Integer>();
    if (pop > 0)
    {
      populate_default_profiles(pop);
    }
  }
  // Fixed number of creatures
  public Population(HashMap<Creature.Race, Integer> profile)
  {
    this.creatures = new HashMap<Creature, Integer>();
    populate_profile(profile);
  }
  // Percentage of population multiplied by the scale provided
  // public Population(HashMap<Race, Double> profile, int scale)
  // {
  //   this.creatures = new HashMap<Creature, Integer>();
  //   // populate_profile(profile);
  // }

  // A profile is essentially:
  //   race0 - n0 of population
  //   race1 - n1 of population
  //   race2 - n2 of population ...etc
  public void populate_profile(HashMap<Creature.Race, Integer> map)
  {
    // First layer focuses on race
    for (Map.Entry<Creature.Race, Integer> entry : map.entrySet())
    {
      creatures.put(new Creature(entry.getKey()), entry.getValue());
    }
  }
  // Based on Welsh Piper stats
  public void populate_default_profiles(int pop)
  {
    Random rand = new Random();
    int remaining = pop;
    Creature c = new Creature(Creature.Race.HUMAN);
    Map<Occupation, Double> occupation_rate = c.occupation_rate;
    // Apply head nobility, range 3-10 + added nobility
    remaining -= rand.nextInt(11 - 3) + 3;
    creatures.put(new Creature(Creature.Race.HUMAN, Occupation.NOBLE), pop - remaining + (int)(occupation_rate.get(Occupation.NOBLE) * remaining));
    // Apply freeholders
    for (Map.Entry<Occupation, Double> entry : occupation_rate.entrySet())
    {
      Occupation o = entry.getKey();
      // Multiply rate by total population => actual number
      Double new_c = entry.getValue() * pop;
      // if pop = 2100, and rate = 1/1500, we are guaranteed
      //  1. There is a 2100 - 1500 = 600 => 40% chance of another
      if (new_c > 0.0)
      {
        int rate = (int)(1.0 / entry.getValue());
        Double chance = new_c - new_c.intValue();
        // Rand (0-1500) < 2100-1500 == 600
        // System.out.println(rate + " " + new_c + " " + pop + " " + chance);
        if (rand.nextInt(pop) < pop * chance) new_c += 1.0;

        if (new_c.intValue() > 0)
        {
          // Add new creature (race + occ) * new_c into creatures map
          creatures.put(new Creature(Creature.Race.HUMAN, o), new_c.intValue());
          remaining -= new_c.intValue();
        }
      }
    }
    int num_citizens = remaining;
    num_citizens = (int)(num_citizens / 1.5);
    creatures.put(new Creature(Creature.Race.HUMAN, Occupation.PEASANT), remaining - num_citizens);
    creatures.put(new Creature(Creature.Race.HUMAN, Occupation.LABORER), num_citizens);
  }

  public int getPopulation()
  {
    int pop = 0;
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      pop += entry.getValue();
    }
    return pop;
  }

  public void printPopulation()
  {
    Creature c;
    System.out.println("TOTAL : " + getPopulation());
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      c = entry.getKey();
      System.out.println(entry.getValue() + " " + c.toString());
    }
    System.out.println("");
  }

  public Map<Creature, Integer> getCreatureList()
  {
    return creatures;
  }
  // Return a creature which is considered 'equal' to the queried
  private Creature fetchCreature(Creature query)
  {
    Creature c;
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      if (entry != null)
      {
        c = entry.getKey();
        if (c.equals(query))
        {
          return c;
        }
      }
    }
    return null;
  }
  // Remove 'n' number of creatures and return them
  public Map<Creature, Integer> pullCreature(Creature c, int n)
  {
    int num_available;
    Creature actual = fetchCreature(c);
    Map<Creature, Integer> valid_creatures = new HashMap<Creature, Integer>();
    if (actual != null)
    {
      num_available = creatures.get(actual);
      // Remove n creatures since we have greater than n available 
      if (num_available > n)
      {
        creatures.put(actual, num_available - n);
        valid_creatures.put(actual, n);
      }
      // Otherwise return whatever we have left and remove creature key
      else
      {
        creatures.remove(actual);
        valid_creatures.put(actual, num_available);
      }
      return valid_creatures;
    }
    return null;
  }
  // Add 'n' number of creatures
  public void pushCreature(Creature c, int n)
  {
    Creature actual = fetchCreature(c);
    // If creature doesn't exist, just add n
    if (actual == null)
    {
      creatures.put(c, n);
    }
    // Otherwise overwrite the table with current value + n
    else
    {
      creatures.put(actual, creatures.get(actual) + n);
    }
  }
  // Split current creatures 'old' into 'n' 'new' creatures in the same population
  public Boolean splitCreature(Creature old_c, Creature new_c, int n)
  {
    // Get current number of old creature entries
    int number_old;
    Creature old_actual = fetchCreature(old_c);
    Creature new_actual = fetchCreature(new_c);
    // Verify 'n' is half-valid
    if (n < 1)
    {
      System.out.println("ERROR: Attempted to split creatures of non-positive value");
      return false;
    }
    // Verify we don't split some race into another
    if (old_c.getRace() != new_c.getRace())
    {
      System.out.println("ERROR: Attempted to split creatures of non-compatible races");
      return false;
    }
    // Verify the old creature actually exists
    if (old_actual != null)
    {
      number_old = creatures.get(old_actual);
      if (number_old > n)
      {
        // Add 'n' new creatures to hashtable
        // If the new creature already exists, just add to it
        if (new_actual != null)
        {
          creatures.put(new_actual, creatures.get(new_actual) + n);
        }
        // Otherwise simply add the new entry
        else
        {
          creatures.put(new_c, n);
        }
        // Set 'number_old' - 'n' old creatures to hashtable
        creatures.put(old_actual, number_old - n);
      }
      else
      {
        if (new_actual != null)
        {
          creatures.put(new_actual, creatures.get(new_actual) + number_old);
        }
        else
        {
          // Add 'number_old' new creatures to hashtable
          creatures.put(new_c, number_old);
        }
        // Destroy old creature entry
        creatures.remove(old_actual);
      }
      return true;
    }
    return false;
  }
  // Combine the secondary creature into the first
  // Cannot combine creatures of different races
  public Boolean combineCreatures(Creature primary, Creature secondary, int n)
  {
    int num_prime;
    int num_second;
    Creature primary_actual = fetchCreature(primary);
    Creature secondary_actual = fetchCreature(secondary);
    // Verify 'n' is half-valid
    if (n < 1)
    {
      System.out.println("ERROR: Attempted to combines creatures of non-positive value");
      return false;
    }
    if (primary.getRace() != secondary.getRace())
    {
      System.out.println("ERROR: Attempted to combine creatures of non-compatible races");
      return false;
    }
    if (primary_actual != null && secondary_actual != null)
    {
      num_prime = creatures.get(primary_actual);
      num_second = creatures.get(secondary_actual);
      // We don't have enough secondary entries!
      if (n > num_second)
      {
        creatures.remove(secondary_actual);
        creatures.put(primary_actual, num_prime + num_second);
      }
      else
      {
        creatures.put(secondary_actual, num_second - n);
        creatures.put(primary_actual, num_prime + n);
      }
      return true;
    }
    System.out.println("ERROR: One or more creatures do not exist. Cannot combine");
    return false;
  }
  // Split population and return new one
  // First remove creatures from current population and then add to new one
  public Population splitPopulation(Map<Creature, Integer> pull_creatures)
  {
    Population new_pop = new Population();
    Map<Creature, Integer> creature_entry = new HashMap<Creature, Integer>();
    if (pull_creatures == null)
    {
      System.out.println("ERROR: Invalid creature list. Cannot split population!");
      return null;
    }
    else
    {
      for (Map.Entry<Creature, Integer> entry : pull_creatures.entrySet())
      {
        // Remove the creatures from the active population
        // NOTE - Rather than shoving the entry data into the pushCreature call,
        //  we get a key value pair in case not all  of the 'n' number of
        //  creatures exist. Cannot assume we can pull all 'n' creatures 'c'
        creature_entry = pullCreature(entry.getKey(), entry.getValue());
        // Add the entry to the new population
        // TODO - Make this less dumb
        for (Map.Entry<Creature, Integer> sub_entry : creature_entry.entrySet())
        {
          new_pop.pushCreature(sub_entry.getKey(), sub_entry.getValue());
          break;
        }
      }
    }
    return new_pop;
  }
  // Merge population 'p' into the current population
  // NOTE - This method DOES NOT remove p's information
  public Boolean absorbPopulation(Population p)
  {
    Map<Creature, Integer> new_creatures = p.getCreatureList();
    if (new_creatures == null)
    {
      System.out.println("ERROR: Invalid population 'p'. Cannot absorb!");
      return false;
    }
    else
    {
      for (Map.Entry<Creature, Integer> entry : new_creatures.entrySet())
      {
        pushCreature(entry.getKey(), entry.getValue());
      }
    }
    return true;
  }
  // Change 'n' number of 'c' creatures' occupations to 'o'
  public Boolean modifyOccupation(Creature c, Occupation o, int n)
  {
    return splitCreature(c, new Creature(c.getRace(), o), n);
  }
  // Increase or decrease population based on creature-related items
  //  and tile information
  public int update(double tax_rate, int inf)
  {
    Map<Creature, Integer> removed = new HashMap<Creature, Integer>();
    Creature c;
    int c_n;
    int c_new_n;
    int tax_collected = 0;
    // Update each individual creature profile based on tile values
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      c = entry.getKey();
      c_n = entry.getValue();
      // Call update on creature and receive new amount
      c_new_n = c.update(tax_rate, c_n, inf);
      // Track tax collected
      tax_collected += c.getTax() * c_n;
      // Replace old number with new
      if (c_new_n > 0)
      {
        creatures.put(c, c_new_n);
      }
      else
      {
        // If the creature dies, just add their wealth to tax collected
        // TODO - Find a better way to distribute the wealth
        tax_collected += c.getWealth();
        removed.put(c, 0);
      }
    }
    // Remove creatures queued to be removed
    for (Map.Entry<Creature, Integer> entry : removed.entrySet())
    {
      creatures.remove(entry.getKey());
    }
    // System.out.println("tax_collected = " + tax_collected);

    return tax_collected;
  }

  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    HashMap<Creature.Race, Integer> profile = new HashMap<Creature.Race, Integer>();
    profile.put(Creature.Race.HUMAN, 1000);
    profile.put(Creature.Race.ELF, 50);
    // Basic initialization
    Population pop = new Population(profile);
    pop.printPopulation();
    if (pop.getPopulation() == 1050) passes++;
    // Test pull
    Creature pull_c_1 = new Creature(Creature.Race.ELF, Occupation.LUMBERJACK);
    pop.pullCreature(pull_c_1, 20);
    pop.printPopulation();
    if (pop.getPopulation() == 1030) passes++;
    // Test push
    Creature push_c_1 = new Creature(Creature.Race.HUMAN, Occupation.PEASANT);
    pop.pushCreature(push_c_1, 20);
    pop.printPopulation();
    if (pop.getPopulation() == 1050) passes++;
    pop.pushCreature(push_c_1, 30);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    // Test split
    Creature split_c_1 = new Creature(Creature.Race.HUMAN, Occupation.LUMBERJACK);
    Creature split_c_2 = new Creature(Creature.Race.HUMAN, Occupation.PEASANT);
    pop.splitCreature(split_c_1, split_c_2, 100);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    Creature split_c_3 = new Creature(Creature.Race.DWARF, Occupation.MINER);
    pop.splitCreature(split_c_1, split_c_3, 50);
    Creature split_c_4 = new Creature(Creature.Race.ELF, Occupation.MERCHANT);
    pop.splitCreature(pull_c_1, split_c_4, 0);
    pop.splitCreature(pull_c_1, split_c_4, 140);
    pop.printPopulation();
    // Test combining
    Creature combine_c_1 = new Creature(Creature.Race.HUMAN, Occupation.LUMBERJACK);
    Creature combine_c_2 = new Creature(Creature.Race.HUMAN, Occupation.PEASANT);
    Creature combine_c_3 = new Creature(Creature.Race.HUMAN, Occupation.MINER);
    pop.combineCreatures(combine_c_1, combine_c_2, 100);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    // Test occupation modification
    pop.modifyOccupation(combine_c_1, Occupation.MINER, 100);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    // Test splitting
    Map<Creature, Integer> splitMap = new HashMap<Creature, Integer>();
    splitMap.put(combine_c_1, 450);
    splitMap.put(split_c_4, 15);
    Population pop2 = pop.splitPopulation(splitMap);
    if (pop.getPopulation() == 615) passes++;
    if (pop2.getPopulation() == 465) passes++;
    pop.printPopulation();
    pop2.printPopulation();
    // Test absorption
    if (pop.absorbPopulation(pop2)) passes++;
    if (pop.getPopulation() == 1080) passes++;
    if (pop2.getPopulation() == 465) passes++;
    pop.printPopulation();
    pop2.printPopulation();

    // Test update
    System.out.println("====TEST UPDATE====");
    pop.printPopulation();
    for (int i = 0; i < 50; i++)
    {
      pop.update(.1, i);
      pop.printPopulation();
    }

    System.out.println("TESTS PASSED : [" + passes + "/12]");
  }
}
