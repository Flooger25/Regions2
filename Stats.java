import java.util.*;

public class Stats
{
  // STR, DEX, CON, INT, WIS, CHA
  private int[] stats = new int[6];
  private int[] saves = new int[6];
  private int AC;
  private int HP;
  private int temp_HP;

  public Stats(int[] stats, int AC, int HP)
  {
    initStats(stats);
    this.AC = AC;
    this.HP = HP;
    this.temp_HP = HP;
  }

  public Stats(int[] stats, int AC, int hit_die, int multi_lev)
  {
    initStats(stats);
    this.AC = AC;
    rollHP(hit_die, multi_lev);
  }

  private void initStats(int[] s)
  {
    for (int i = 0; i < 6; i++)
    {
      stats[i] = s[i];
      saves[i] = (int)((s[i] - 10) / 2);
    }
  }
  private void rollHP(int hit_die, int multi_lev)
  {
    HP = 0;
    Die die = new Die(hit_die);
    for (int i = 0; i < multi_lev; i++)
    {
      // Roll 1dX + CON mod
      HP += die.rollDie() + (int)((stats[2] - 10) / 2);
    }
    temp_HP = HP;
  }
  // Determine whether we've been hit or not and update temp_HP
  // based on damage provided
  // TODO :
  // stat -1 means critical hit
  // stat 0-5 represents an actual stat
  public Boolean receiveHit(int hit, int dmg, Boolean save_throw, int stat)
  {
    Die die = new Die(20);
    // A regular hit. If the hit is >= AC, we take damage
    if (!save_throw && hit >= AC)
    {
      temp_HP -= dmg;
    }
    // Otherwise roll to save. If we fail, then take full
    else if (save_throw)
    {
      if (die.rollDie() + saves[stat] >= hit)
      {
        temp_HP -= (int)(dmg / 2);
      }
      else
      {
        temp_HP -= dmg;
      }
    }
    System.out.println("HP : " + temp_HP);
    // We've gone unconscious!
    if (temp_HP <= 0)
    {
      return true;
    }
    return false;
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    Stats s = new Stats(new int[]{ 10, 10, 15, 10, 20, 10}, 14, 100);
    if (!s.receiveHit(12, 50, false, 0)) passes++;
    if (!s.receiveHit(14, 50, false, 0)) passes++;
    if (!s.receiveHit(21, 40, false, 0)) passes++;
    if (!s.receiveHit(1, 18, true, 1)) passes++;
    if (s.receiveHit(100, 100, true, 1)) passes++;

    System.out.println("TESTS PASSED : [" + passes + "/5]");
  }
}
