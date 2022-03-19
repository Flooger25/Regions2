import java.util.*;
import java.lang.Math;

// JUnit testing infrastructure
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

public class Creature
{
  public enum Race
  {
    HUMAN, ELF, DWARF, ORC
  }

  private static final Map<Race, Integer> life_expectancy =
    new Hashtable<Race, Integer>()
    {{
      put(Race.HUMAN, 80);
      put(Race.ELF, 700);
      put(Race.DWARF, 350);
      put(Race.ORC, 55);
    }};
  
  // Chance that any given member is of a particular occupation
  public static final Map<Occupation, Double> occupation_rate =
    new Hashtable<Occupation, Double>()
    {{
      // Citizens
      // put(Occupation.CITIZEN, 1/1.5)
      put(Occupation.HIRELING, 1.0/254.0);
      // Freeholders
      put(Occupation.LUMBERJACK, 1.0/700.0);
      // put(Occupation.MINER, 1/1000);
      // put(Occupation.FARMER, 1/1000);
      put(Occupation.CHARCOALER, 1.0/400.0);
      put(Occupation.ARMORER, 1.0/1500.0);
      put(Occupation.MASON, 1.0/500.0);
      put(Occupation.MILLER, 1.0/250.0);
      put(Occupation.WEAPONCRAFTER, 1.0/1000.0);
      put(Occupation.WOODCRAFTER, 1.0/300.0);
      // Religion
      put(Occupation.CLERIC, 1.0/120.0);
      put(Occupation.PRIEST, 1.0/3600.0);
      // Law Enforcement
      put(Occupation.LAW_ENFORCEMENT, 1.0/150.0);
      // Nobility
      put(Occupation.NOBLE, 1.0/450.0);
    }};

  // NOTE - Lifstyle is assumed to be half of the income
  // Upkeep is assumed to be 1/3 of the income
  // Total expenditures (before tax) is thus 5/6
  private static final Map<Occupation, Integer> occupation_income =
    new Hashtable<Occupation, Integer>()
    {{
      // Poor
      //  1-5
      // Modest
      //  6-29
      // Comfortable
      //  30-59
      // Wealthy
      //  60-119
      // Aristocratic
      //  300
      put(Occupation.PEASANT, 3);
      put(Occupation.MERCHANT, 120);
      put(Occupation.MINER, 120);
      // Citizens
      // put(Occupation.CITIZEN, 1/1.5)
      put(Occupation.HIRELING, 6);
      // Freeholders
      put(Occupation.LUMBERJACK, 30);
      // put(Occupation.MINER, 1/1000);
      // put(Occupation.FARMER, 1/1000);
      put(Occupation.CHARCOALER, 6);
      put(Occupation.ARMORER, 60);
      put(Occupation.MASON, 30);
      put(Occupation.MILLER, 30);
      put(Occupation.WEAPONCRAFTER, 60);
      put(Occupation.WOODCRAFTER, 30);
      // Religion
      put(Occupation.CLERIC, 30);
      put(Occupation.PRIEST, 60);
      // Law Enforcement
      put(Occupation.LAW_ENFORCEMENT, 30);
      // Nobility
      put(Occupation.NOBLE, 120);
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
  // arr[age][occupation]
  // Default frequencey of the upper bounded ages
  //                                         13  17  25  30  40 50 60 70   80   ++
  public double[] age_freq = new double[] {20.8, 20, 18, 15, 12, 8, 5, 1, 0.2, 0.1};

  // Occupation + Infrastructure => Resource Gain
  // Resource => +Equipment
  // Equipment => +Occupation or +Soldiers or +Infrastructure
  //
  // [Harvesters]
  // N TIMBERWRIGHT + I => 500 * N * (log(I + 1) + 1) = L logs
  // N TIMBERWRIGHT + I => 500 * N * (log(I + 1) + 1) * 10 logs/plank = k planks
  //
  // N MINER + I => 300 * N * (log(I + 1) + 1) = M Metal
  // N MINER + I => 150 * N * (log(I + 1) + 1) = CP Gold/Silver/Copper Pieces
  // N MINER + I => 10 * N * (log(I + 1) + 1) = G Gems
  // N MINER + I => 1000 * N * (log(I + 1) + 1) = S Stone
  //
  // N FARMER + I => 10K * N * (log(I + 1) + 1) = w wheat bushels
  // N FARMER + I => L * (1 + (log(I + 1) + 1) / 100) = L Livestock
  // N FARMER + I => 1000 * N * (log(I + 1) + 1) = w wool/cotton/fibres
  //
  // [Secondary Materials]
  // N CHARCOLER + L-logs => L * 100 = c charcoal pieces
  //
  // [Primary Products]
  // N MILLER/BAKER + I + W-Wheat => N * W * (log(I + 1) + 1) = B bread
  //
  // N WOODCRAFTER + I + k-planks => cost of inf upgrade -= k-planks
  //
  // N ARMORER + M-Metal + I => M * 1000 GP-worth of armor = A armor
  // N WEAPONCRAFTER +/ M-Metal +/ LE-Leather +/ L-Logs +/ P-Planks + I = weapons
  //
  // N MASON + I + S-Stone => 2 * N * (log(I + 1) + 1) = B Bricks
  // N MASON + I + B-Bricks => some structure's needs
  //
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
    if (occupation_income.get(o) == null)
    {
      income = 1;
    }
    else
    {
      income = occupation_income.get(o);
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

  public int harvest_resource(int I)
  {
    return (int)(100.0 * Math.log(I + 1) + 1);
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

    // System.out.println(n + " => " + new_quantity);

    return new_quantity;
  }

  public static class CreatureTests
  {
    private final Creature creature = new Creature(Race.HUMAN);
    @Test
    public void test1()
    {
      assertEquals(0, creature.getWealth());
    }
  }

  public static void main(String[] args)
  {
    Boolean test = false;
    // Make sure a 'test' argument was provided. If it wasn't, then we
    //  will not utilize the JUnit testing infrastructure
    for (int index = 0; index < args.length; ++index)
    {
      if (args[index].equals("test"))
      {
        test = true;
        break;
      }
    }
    // Use JUnit tests, otherwise do anything else
    if (test)
    {
      JUnitCore JUC = new JUnitCore();
      Result result = JUC.runClasses(CreatureTests.class);

      for (Failure failure : result.getFailures()) {
        System.out.println(failure.toString());
      }
      System.out.println(result.wasSuccessful());
    }

    System.out.println("EDDIE");
  }
}
