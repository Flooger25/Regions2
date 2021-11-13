import java.util.*;

public class Population
{
  // Rather than having a massive list of creatures, have a
  //  given creature profile and a number indicating how many
  //  of said profile there are. Manage numbers rather than
  //  a bunch of instances
  private Map<Creature, Integer> creatures;

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
  private void populate_profile(HashMap<Creature.Race, Integer> map)
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
      System.out.println(c.getRace().name() + " " + c.getOccupation().name() + " " + entry.getValue());
    }
  }

  // Remove 'n' number of creatures
  public int pullCreature(Creature c, int n)
  {
    int num_available;
    if (creatures.get(c) != null)
    {
      num_available = creatures.get(c);
      // Remove n creatures since we have greater than n available 
      if (num_available > n)
      {
        creatures.put(c, num_available - n);
        return n;
      }
      // Otherwise return whatever we have left and remove creature key
      else
      {
        creatures.remove(c);
        return num_available;
      }
    }
    return 0;
  }
  // Add 'n' number of creatures
  public void pushCreature(Creature c, int n)
  {
    // If creature doesn't exist, just add n
    if (creatures.get(c) == null)
    {
      creatures.put(c, n);
    }
    // Otherwise overwrite the table with current value + n
    else
    {
      creatures.put(c, creatures.get(c) + n);
    }
  }
  // Split current creatures 'old' into 'n' 'new' creatures
  public Boolean splitCreature(Creature old_c, Creature new_c, int n)
  {
    // Get current number of old creature entries
    int number_old;
    if (creatures.get(old_c) != null)
    {
      number_old = creatures.get(old_c);
      if (number_old > n)
      {
        // Add 'n' new creatures to hashtable
        creatures.put(new_c, n);
        // Add 'number_old' - 'n' old creatures to hashtable
        creatures.put(old_c, number_old - n);
      }
      else
      {
        // Add 'number_old' new creatures to hashtable
        creatures.put(new_c, number_old);
        // Destroy old creature entry
        creatures.remove(old_c);
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
    if (primary.getRace() != secondary.getRace())
    {
      System.out.println("ERROR: Attempted to combine creatures of non-compatible races");
      return false;
    }
    if (creatures.get(primary) != null && creatures.get(secondary) != null)
    {
      num_prime = creatures.get(primary);
      num_second = creatures.get(secondary);
      // Remove secondary from hashtable
      creatures.remove(secondary);
      // Add secondary + primary into primary creature
      creatures.put(primary, num_prime + num_second);
      return true;
    }
    System.out.println("ERROR: One or more creatures do not exist. Cannot combine");
    return false;
  }

  public Boolean modifyOccupation(Creature c, Creature.Occupation o, int n)
  {
    int available_creatures;
    if (creatures.get(c) != null)
    {
      available_creatures = creatures.get(c);
      return true;
    }
    System.out.println("ERROR: One or more creatures do not exist. Cannot combine");
    return false;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    HashMap<Creature.Race, Integer> profile = new HashMap<Creature.Race, Integer>();
    profile.put(Creature.Race.HUMAN, 1000);
    profile.put(Creature.Race.ELF, 50);
    // Basic initialization
    Population pop = new Population(profile);
    pop.printPopulation();
    if (pop.getPopulation() != 1050) return;
    // Test pull
    pop.pullCreature(new Creature(Creature.Race.ELF), 20);
    pop.printPopulation();
    if (pop.getPopulation() != 1030) return;

    // 4-sided
    // Dice a = new Dice(3, DIE_4);
    // for (int i = 0; i < test_iterations; i++)
    // {
    //   value = a.rollDice();
    //   if (value < 3 || value > DIE_4 * 3)
    //   {
    //     pass = false;
    //     break;
    //   }
    // }
  }
}
