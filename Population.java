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
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      c = entry.getKey();
      System.out.println(c.toString() + " " + entry.getValue());
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
  public Boolean modifyOccupation(Creature c, Creature.Occupation o, int n)
  {
    // Creature new_occupation = new Creature(c.getRace(), o);
    return splitCreature(c, new Creature(c.getRace(), o), n);
  }
  // Increase or decrease population based on creature-related items
  //  and tile information
  public void advanceTime()
  {
    Creature c;
    int c_quantity;
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      c = entry.getKey();
      c_quantity = entry.getValue();
      // creature.put(c, )
      c.update(0.0);
    }
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
    Creature pull_c_1 = new Creature(Creature.Race.ELF, Creature.Occupation.LUMBERJACK);
    pop.pullCreature(pull_c_1, 20);
    pop.printPopulation();
    if (pop.getPopulation() == 1030) passes++;
    // Test push
    Creature push_c_1 = new Creature(Creature.Race.HUMAN, Creature.Occupation.PEASANT);
    pop.pushCreature(push_c_1, 20);
    pop.printPopulation();
    if (pop.getPopulation() == 1050) passes++;
    pop.pushCreature(push_c_1, 30);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    // Test split
    Creature split_c_1 = new Creature(Creature.Race.HUMAN, Creature.Occupation.LUMBERJACK);
    Creature split_c_2 = new Creature(Creature.Race.HUMAN, Creature.Occupation.PEASANT);
    pop.splitCreature(split_c_1, split_c_2, 100);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    Creature split_c_3 = new Creature(Creature.Race.DWARF, Creature.Occupation.MINER);
    pop.splitCreature(split_c_1, split_c_3, 50);
    Creature split_c_4 = new Creature(Creature.Race.ELF, Creature.Occupation.MERCHANT);
    pop.splitCreature(pull_c_1, split_c_4, 0);
    pop.splitCreature(pull_c_1, split_c_4, 140);
    pop.printPopulation();
    // Test combining
    Creature combine_c_1 = new Creature(Creature.Race.HUMAN, Creature.Occupation.LUMBERJACK);
    Creature combine_c_2 = new Creature(Creature.Race.HUMAN, Creature.Occupation.PEASANT);
    Creature combine_c_3 = new Creature(Creature.Race.HUMAN, Creature.Occupation.MINER);
    pop.combineCreatures(combine_c_1, combine_c_2, 100);
    pop.printPopulation();
    if (pop.getPopulation() == 1080) passes++;
    // Test occupation modification
    pop.modifyOccupation(combine_c_1, Creature.Occupation.MINER, 100);
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

    System.out.println("TESTS PASSED : [" + passes + "/12]");
  }
}
