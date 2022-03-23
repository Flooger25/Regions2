import java.util.*;

public class Die
{
  private int value;
  private Random rand;

  public Die(int value)
  {
    this.value = value;
    this.rand = new Random();
  }
  // Return value assigned to this given die
  public int getValue()
  {
    return value;
  }
  // Roll die. Return 1-n
  public int rollDie()
  {
    return rand.nextInt(value) + 1;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int test_iterations = 1000;
    int value = 0;
    int minimum_val = 1;
    int DIE_4 = 4; int DIE_6 = 6; int DIE_8 = 8;
    int DIE_10 = 10; int DIE_12 = 12; int DIE_20 = 20;
    Boolean pass = true;
    // 4-sided
    Die a = new Die(DIE_4);
    for (int i = 0; i < test_iterations; i++)
    {
      value = a.rollDie();
      if (value < minimum_val || value > DIE_4)
      {
        pass = false;
        break;
      }
    }
    if (pass) System.out.println("Die <4> has passed");
    // 12-sided
    Die e = new Die(DIE_12);
    pass = true;
    for (int i = 0; i < test_iterations; i++)
    {
      value = a.rollDie();
      if (value < minimum_val || value > DIE_12)
      {
        pass = false;
        break;
      }
    }
    if (pass) System.out.println("Die <12> has passed");
  }
}
