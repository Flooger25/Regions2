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
}
