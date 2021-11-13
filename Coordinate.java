public class Coordinate {

  // Distance from SW-most corner to the East by tiles
  public int x;
  // Distance from SW-most corner to direct North by tiles
  public int y;

  public Coordinate(int x, int y)
  {
    this.x = x;
    this.y = y;
  }

  public Boolean equals(Coordinate c)
  {
    return (x == c.x) && (y == c.y);
  }

  public int compareTo(Coordinate c)
  {
    // ==
    if (this.equals(c)) return 0;
    // <
    if (x < c.x) return -1;
    else if (x == c.x && y < c.y) return -1;
    // > (assert => x > c.x)
    return 1;
  }

  public double distance(Coordinate c)
  {
    return Math.hypot( Math.abs(y - c.y), Math.abs(x - c.x) );
  }

  public void print()
  {
    System.out.println("("+ x + "," + y + ")");
  }

  public static void main(String[] args)
  {
    Coordinate a = new Coordinate(1, 2);
    Coordinate b = new Coordinate(1, 1);
    Coordinate c = new Coordinate(3, 1);
    Coordinate d = new Coordinate(1, 0);
    Coordinate e = new Coordinate(1, 1);
    System.out.println(a.equals(b)+" "+a.compareTo(b)+" "+a.compareTo(d));
    System.out.println(b.equals(b)+" "+b.compareTo(a)+" "+b.compareTo(c));
    System.out.println(c.compareTo(b)+" "+c.compareTo(b)+" "+c.compareTo(a));
    System.out.println(d.compareTo(a)+" "+d.compareTo(b)+" "+d.compareTo(a));
    System.out.println(c.compareTo(a)+" "+a.compareTo(b)+" "+b.compareTo(d));

    System.out.println(a.compareTo(a) + " " + " " + a.compareTo(b));
    System.out.println(b.compareTo(e) + " " + " " + b.equals(e));
    if (b == e) System.out.println("b == e");
    e = b;
    if (b == e) b.print();

    System.out.println(b.distance(d));
    System.out.println(c.distance(d));
  }
}
