import java.util.*;

public class Dice
{
  private int numDice;
  private Die dice;

  public Dice(int numDice, int diceType)
  {
    this.numDice = numDice;
    this.dice = new Die(diceType);
  }
  // Return value assigned to this given die
  public int getNumDice()
  {
    return numDice;
  }
  public int getDiceType()
  {
    return dice.getValue();
  }
  // Roll dice
  public int rollDice()
  {
    int value = 0;
    for (int i = 0; i < numDice; i++)
    {
      value += dice.rollDie();
    }
    return value;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int test_iterations = 1000;
    int value = 0;
    int DIE_4 = 4; int DIE_6 = 6;
    Boolean pass = true;
    // 4-sided
    Dice a = new Dice(3, DIE_4);
    for (int i = 0; i < test_iterations; i++)
    {
      value = a.rollDice();
      if (value < 3 || value > DIE_4 * 3)
      {
        pass = false;
        break;
      }
    }
    if (pass) System.out.println("Dice 3d4 has passed");
  }
}
