import java.util.*;

public class StatItem
{
  private Dice dice;
  private int modifier;

  public StatItem(Dice dice, int modifier)
  {
    this.dice = dice;
    this.modifier = modifier;
  }
  // Roll dice
  public int rollStat()
  {
    return dice.rollDice() + modifier;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    StatItem stat = new StatItem(new Dice(6, 8), 100);
    for (int i = 0; i < 10; i++)
    {
      System.out.println(i + " : " + stat.rollStat());
    }
  }
}
