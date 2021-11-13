import java.util.*;

public class Creature
{
  public enum Race
  {
    HUMAN, ELF, DWARF, ORC
  }
  public enum Occupation
  {
    PEASANT, ARMORER, LUMBERJACK, MERCHANT
  }

  private static final Dictionary<Race, Integer> life_expectancy =
    new Hashtable<Race, Integer>()
    {{
      put(Race.HUMAN, 80);
      put(Race.ELF, 700);
      put(Race.DWARF, 350);
      put(Race.ORC, 55);
    }};

  private Race race;
  private int age;
  private Occupation occupation;
  private int wealth;
  private int income;
  private int cost_of_living;
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

  private Occupation generate_occupation()
  {
    Occupation occ;
    switch (race)
    {
      case HUMAN:
        occ = Occupation.LUMBERJACK;
        income = 10;
        break;
      case ELF:
        occ = Occupation.LUMBERJACK;
        income = 10;
        break;
      case DWARF:
        occ = Occupation.ARMORER;
        income = 10;
        break;
      default:
        occ = Occupation.PEASANT;
        income = 1;
        break;
    }
    return occ;
  }

  public Double update(double tax_rate)
  {
    // Worked for a year
    double tax_collected = income * tax_rate;
    wealth += (int)(income - tax_collected - cost_of_living);
    // TODO - Make script to understand yearly % chance of death/survival
    // For now, if we hit expectancy, we die, yay?
    if (age == life_expectancy.get(race))
    {
      return -1.0;
    }
    age++;
    // Go into poverty, bitch
    if (wealth < 0)
    {
      occupation = Occupation.PEASANT;
    }
    return tax_collected;
  }
}
