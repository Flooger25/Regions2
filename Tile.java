import java.util.*;

public class Tile
{
  // Valid directions are N, S, E, W
  //    [ ]
  // [] [X] []
  //    [ ]
  //
  // ...or NW, NE, W, E, SW, SE
  //  []   []
  // [] [X] []
  //  []   []
  //
  public enum Direction
  {
    NW, NE, W, E, SW, SE, N, S
  }
  // Geographic tile type
  public enum TileType
  {
    FOREST, GRASSLAND, SEA, MOUNTAIN
  }
  // Resource types
  public enum Resource
  {
    WHEAT, WOOD, STONE, GEMS, METAL
  }

  public static int max_neighbors = 6;
  // Size of tile in km
  public static final int tile_size = 1;
  // Valid numbers 0-10
  private int infrastructure;
  private TileType type;
  private Coordinate coord;
  private Dictionary<Resource, Integer> resources;
  private Map<Direction, Tile> neighbors;
  private Population pop;

  public Tile(TileType type, Coordinate c)
  {
    this.type = type;
    this.coord = c;
    this.infrastructure = 0;
    this.pop = new Population();
    initialize();
  }

  private final void initialize()
  {
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    instantiate_resources();
  }

  public String toString()
  {
    return type.name() + " " + coord.toString();
  }

  public void printTile()
  {
    System.out.println(toString());
    System.out.println(resources.toString());
    pop.printPopulation();
  }

  public Coordinate getCoordinate()
  {
    return coord;
  }

  public Dictionary<Resource, Integer> getResources()
  {
    return resources;
  }

  public int getResourceQuantity(Resource r)
  {
    return resources.get(r);
  }

  public int extractResource(Resource r, int quantity)
  {
    int num_available = resources.get(r);
    if (quantity < num_available)
    {
      resources.put(r, num_available - quantity);
      return quantity;
    }
    else if (quantity == num_available)
    {
      resources.remove(r);
      return num_available;
    }
    else // (quantity > num_available)
    {
      resources.remove(r);
      return num_available;
    }
  }

  public void addResource(Resource r, int quantity)
  {
    int num_available = resources.get(r);
    if (num_available == 0)
    {
      resources.put(r, quantity);
    }
    else
    {
      resources.put(r, num_available + quantity);
    }
  }

  public Boolean setNeighbor(Direction d, Tile t)
  {
    if (neighbors.get(d) != null)
    {
      System.out.println("ERROR - Direction %s already assigned!" + d);
      return false;
    }
    if (t == null || t == this)
    {
      System.out.println("WARNING - Invalid Tile, cannot assign as neighbor");
      return false;
    }
    neighbors.put(d, t);
    return true;
  }

  public void printNeighbors()
  {
    System.out.println("Neighbors for tile - " + toString() + " are as follows: ");
    for (Map.Entry<Direction, Tile> entry : neighbors.entrySet())
    {
      System.out.println(entry.getKey().name() + " - " + entry.getValue().toString());
    }
  }

  // Return enumerated list of resources available in the tile
  public Enumeration getResourcesAvailable()
  {
    if (resources == null)
    {
      System.out.println("ERROR - No resources found!");
      return null;
    }
    return resources.keys();
  }

  // Instantiate the presence of resources in the tile
  private void instantiate_resources()
  {
    Random rand = new Random();
    switch (type)
    {
      case FOREST:
        resources.put(Resource.WOOD, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.WHEAT, rand.nextInt(10));
        break;
      case GRASSLAND:
        resources.put(Resource.WHEAT, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.WOOD, rand.nextInt(10));
        break;
      case MOUNTAIN:
        resources.put(Resource.STONE, rand.nextInt(900));
        resources.put(Resource.METAL, rand.nextInt(90));
        resources.put(Resource.GEMS, rand.nextInt(10));
        break;
      default:
        break;
    }
  }
  // As infrastructure increases, so does the average km/h
  // Return how long, in minutes, to traverse a tile based on
  //  infrastructure level
  public int getTravelTime()
  {
    return (int)(tile_size * (1 / (infrastructure + 1) * 60)); // minutes/hr
  }

  public int getCostOfUpgrade()
  {
    if (infrastructure < 10)
    {
      return (int)(100 * Math.pow(1.9, infrastructure + 1));
    }
    System.out.println("WARNING : Cannot upgrade infrastructure beyond 10");
    return -1;
  }

  public int getCostOfMaintenance()
  {
    if (infrastructure > 0)
    {
      return (int)(100 * Math.pow(1.2, infrastructure));
    }
    return 0;
  }

  public Population getPopulation()
  {
    return pop;
  }

  public void update(Boolean upgrade, Boolean maintain)
  {
    if (upgrade && infrastructure < 10) infrastructure++;
    else if (!maintain)
    {
      // Natural decay
      infrastructure -= 1;
      if (infrastructure < 0) infrastructure = 0;
    }
    // Receive events
    // Gather resources
    // Modify population based on resource
    // 
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    Coordinate c = new Coordinate(1,1);
    Tile tile = new Tile(Tile.TileType.GRASSLAND, c);
    System.out.println(tile.getTravelTime());
    System.out.println(tile.getCostOfUpgrade());
    System.out.println(tile.getCostOfMaintenance());

    tile.printTile();

    System.out.println("TESTS PASSED : [" + passes + "/7]");
  }
}
