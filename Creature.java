import java.util.*;

// Combinations
// Race + Occupation

public class Creature
{
  public enum Race
  {
    HUMAN, ELF, DWARF, ORC
  }
  public enum Occupation
  {
    // Poor
    PEASANT,
    // Modest
    LUMBERJACK, MINER,
    // Comfortable
    ARMORER, MERCHANT,
    // Wealthy
    // Aristocratic
    NOBLE,
  }

  private static final Map<Race, Integer> life_expectancy =
    new Hashtable<Race, Integer>()
    {{
      put(Race.HUMAN, 80);
      put(Race.ELF, 700);
      put(Race.DWARF, 350);
      put(Race.ORC, 55);
    }};

  // NOTE - Lifstyle is assumed to be half of the income
  // Upkeep is assumed to be 1/3 of the income
  // Total expenditures (before tax) is thus 5/6
  private static final Map<Occupation, Integer> occupation_income =
    new Hashtable<Occupation, Integer>()
    {{
      put(Occupation.NOBLE, 80);
      put(Occupation.PEASANT, 80);
      put(Occupation.ARMORER, 80);
      put(Occupation.LUMBERJACK, 80);
      put(Occupation.MERCHANT, 80);
      put(Occupation.MINER, 80);
    }};

  private Race race;
  private int age;
  private Occupation occupation;
  private int wealth;
  private int income;
  private int cost_of_living;
  private int last_tax_collected;
  // TODO
  // private Statistics stats;

  public Creature(Race r)
  {
    this.age = 0;
    this.race = r;
    this.wealth = 0;
    this.income = 0;
    this.cost_of_living = 0;
    this.occupation = generate_occupation();
  }

  public Creature(Race r, Occupation o)
  {
    this.age = 0;
    this.race = r;
    this.wealth = 0;
    this.income = 0;
    this.cost_of_living = 0;
    this.occupation = o;
    emboldOccupation(o);
  }
  // Simply set income and cost of living based on occupation
  private void emboldOccupation(Occupation o)
  {
    switch (o)
    {
      case NOBLE:
        income = 1440;
        break;
      case MINER:
        income = 400;
        break;
      case MERCHANT:
        income = 700;
        break;
      case LUMBERJACK:
        income = 200;
        break;
      default:
        income = 1;
        break;
    }
    cost_of_living = (int)(income * 5 / 6);
  }

  private Occupation generate_occupation()
  {
    Occupation occ;
    switch (race)
    {
      case HUMAN:
        occ = Occupation.LUMBERJACK;
        break;
      case ELF:
        occ = Occupation.LUMBERJACK;
        break;
      case DWARF:
        occ = Occupation.ARMORER;
        break;
      default:
        occ = Occupation.PEASANT;
        break;
    }
    emboldOccupation(occ);
    return occ;
  }

  public Boolean equals(Creature c)
  {
    return (c.getRace() == race) &&
          (c.getOccupation() == occupation);
  }

  public String toString()
  {
    return race.name() + " " + occupation.name() +
          " wealth,income,COL: " + wealth + " " + income + " " + cost_of_living;
  }

  public Race getRace()
  {
    return race;
  }

  public Occupation getOccupation()
  {
    return occupation;
  }

  public int getWealth()
  {
    return wealth;
  }

  public int getTax()
  {
    return last_tax_collected;
  }

  public int update(double tax_rate, int n, int inf)
  {
    int new_quantity = n;
    Random rand = new Random();
    // Worked for a year
    last_tax_collected = (int)(income * tax_rate);
    wealth += income - last_tax_collected - cost_of_living;

    // Chance of death based on:
    //  1. health
    //  2. life_expectancy
    //  3. 'n' number of creature we're processing
  
    double expected_deaths = n * (1.0 / life_expectancy.get(race));
    // System.out.println("expected_deaths = " + expected_deaths);
    // Apply health based on infrastructure + wealth
    //  level of infrastructure => more access to medical service
    expected_deaths *= (1.0 - ((inf - 0) / 100) );
    // System.out.println("expected_deaths = " + expected_deaths);
    //  wealth => more access to medical services
    if (wealth >= 1) expected_deaths *= (1.0 - (Math.log10(wealth) / 100) );
    else expected_deaths *= 1.1;
    // System.out.println("expected_deaths = " + expected_deaths);
    if (expected_deaths < 1.0 && expected_deaths > 0.0)
    {
      // If our expected is less than 1, randomize if we subtract 1
      if ( rand.nextInt(100) < (int)(expected_deaths * 100))
      {
        new_quantity -= 1;
      }
    }
    else
    {
      new_quantity -= (int)expected_deaths;
    }

    // Births
    // 1.1% for 72.6 LE
    // 1.1 / (LE / 72.6) = (1.1 * 72.6) / LE = exp_births
    double expected_births = n * ((1.1 / 100) * 72.6) / life_expectancy.get(race);
    // System.out.println("expected_births = " + expected_births);
    if (expected_births < 1.0)
    {
      // If our expected is less than 1, randomize if we add 1
      if ( rand.nextInt(100) < (int)(expected_births * 100))
      {
        new_quantity += 1;
      }
    }
    else
    {
      new_quantity += (int)expected_births;
    }

    // Go into poverty
    if (wealth <= 0)
    {
      occupation = Occupation.PEASANT;
    }

    System.out.println(n + " => " + new_quantity);

    return new_quantity;
  }
}
