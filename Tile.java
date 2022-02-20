import java.util.*;
import java.awt.Color;

// Resource types
enum Resource
{
  // Primary resources that are harvested
  LOGS, METAL, CP, GEMS, STONE, WHEAT, LIVESTOCK, FIBERS,
  // Secondary materials that were processed
  CHARCOAL, LUMBER, BRICK,
  // Completed products
  BREAD, ARMOR, WEAPONS,
}

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
enum Direction
{
  NW, NE, W, E, SW, SE, N, S
}

public class Tile
{
  // Geographic tile type
  public enum TileType
  {
    FOREST, GRASSLAND, SEA, MOUNTAIN, HILLS
  }

  // Table of weights of environment on travel time.
  // Each double represents the number that is subtracted against the
  //  base time. The lower the number, the easier it is to travel
  //  through this tile.
  public static final Map<TileType, Double>
    env_travel_weight = new Hashtable<TileType, Double>()
    {{
      put(TileType.FOREST, 0.3);
      put(TileType.GRASSLAND, 0.01);
      put(TileType.MOUNTAIN, 0.99);
      put(TileType.HILLS, 0.5);
    }};

  // Super table of valid primary resources available given a tile type
  public static final Map<Resource, Map<TileType, Boolean>>
    valid_resource_tiles = new Hashtable<Resource, Map<TileType, Boolean>>()
    {{
      put(Resource.LOGS, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.FOREST, true);
        put(TileType.HILLS, true);
      }});
      put(Resource.CP, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.MOUNTAIN, true);
      }});
      put(Resource.GEMS, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.MOUNTAIN, true);
      }});
      put(Resource.STONE, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.FOREST, true);
        put(TileType.HILLS, true);
        put(TileType.MOUNTAIN, true);
      }});
      put(Resource.WHEAT, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.GRASSLAND, true);
      }});
      put(Resource.LIVESTOCK, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.GRASSLAND, true);
        put(TileType.FOREST, true);
        put(TileType.HILLS, true);
      }});
      put(Resource.FIBERS, new Hashtable<TileType, Boolean>()
      {{
        put(TileType.GRASSLAND, true);
        put(TileType.FOREST, true);
        put(TileType.HILLS, true);
      }});
    }};

  // Super table of valid resource conversions
  // 1-N Resources with list of size N
  //
  // R0
  // R1
  // .
  // Rn    => R New
  //
  // First key is the resource we want to output
  // First value is a hash map of the following:
  //   - Key is the resource we need
  //   - Value is the amount of the resource we need to produce
  //     one output resource
  //
  // NOTE - If a frequency is below 1, that means we can actually create
  //  more output resources than input. For example, one log can
  //  create 5000 charcoal pieces.
  // NOTE2 - If any resource does not have a recipe, this means that
  //  it is either primary or an occupation's job is to create it
  public static final Map<Resource, Map<Resource, Double>>
    recipes = new Hashtable<Resource, Map<Resource, Double>>()
    {{
      // SECONDARY RESOURCES
      put(Resource.LUMBER, new Hashtable<Resource, Double>()
      {{
        put(Resource.LOGS, 1.0 / 15.0);
      }});
      put(Resource.CHARCOAL, new Hashtable<Resource, Double>()
      {{
        put(Resource.LOGS, 1.0 / 5000.0);
      }});
      put(Resource.BRICK, new Hashtable<Resource, Double>()
      {{
        put(Resource.STONE, 1.0 / 2.0);
      }});
      // COMPLETED PRODUCTS
      put(Resource.BREAD, new Hashtable<Resource, Double>()
      {{
        put(Resource.WHEAT, 1.0);
      }});
      put(Resource.WEAPONS, new Hashtable<Resource, Double>()
      {{
        put(Resource.FIBERS, 10.0);
        put(Resource.METAL, 3.0);
        put(Resource.CHARCOAL, 50.0);
      }});
      put(Resource.ARMOR, new Hashtable<Resource, Double>()
      {{
        put(Resource.FIBERS, 50.0);
        put(Resource.METAL, 100.0);
        put(Resource.CHARCOAL, 500.0);
      }});
    }};

  public static final int max_infrastructure = 10;

  public static int max_neighbors = 6;
  // Size of tile in km
  public static final int tile_size = 1;

  // POLICY VARIABLES
  // Valid numbers 0-10
  private int infrastructure;
  // Whether we automatically try to upgrade infrastructure in a given
  // update call.
  private Boolean auto_upgrade = false;
  private double tax_rate = 0.0;
  private int pop_traveled = 1;

  private TileType type;
  private Color color;
  private Coordinate coord;
  private OccupationManager o_manager;
  private Dictionary<Resource, Integer> resources;
  private Map<Direction, Tile> neighbors;
  private Population pop;

  public Tile(TileType type, Coordinate c)
  {
    this.type = type;
    this.coord = c;
    this.infrastructure = 0;
    this.pop = new Population();
    this.o_manager = new OccupationManager();
    initialize(false);
  }

  public Tile(Color c, Coordinate coord)
  {
    // Define tile type based on color provided
    this.type = getTypeFromColor(c);
    this.coord = coord;
    this.color = c;
    this.infrastructure = 0;
    this.pop = new Population();
    this.o_manager = new OccupationManager();
    initialize(false);
  }

  private final void initialize(Boolean provide_color)
  {
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    instantiate_resources(provide_color);
  }

  // Instantiate the presence of resources in the tile
  private void instantiate_resources(Boolean provide_color)
  {
    Random rand = new Random();
    switch (type)
    {
      case FOREST:
        if (provide_color) color = Color.WHITE;
        resources.put(Resource.LOGS, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.WHEAT, rand.nextInt(10));
        break;
      case GRASSLAND:
        if (provide_color) color = Color.GREEN;
        resources.put(Resource.WHEAT, rand.nextInt(900));
        resources.put(Resource.STONE, rand.nextInt(90));
        resources.put(Resource.LOGS, rand.nextInt(10));
        break;
      case MOUNTAIN:
        if (provide_color) color = Color.GRAY;
        resources.put(Resource.STONE, rand.nextInt(900));
        resources.put(Resource.METAL, rand.nextInt(90));
        resources.put(Resource.GEMS, rand.nextInt(10));
        break;
      case SEA:
        if (provide_color) color = Color.BLUE;
      default:
        break;
    }
    // Inject 0 copper pieces
    resources.put(Resource.CP, 0);
  }

  private TileType getTypeFromColor(Color color)
  {
    int r = color.getRed();
    int g = color.getGreen();
    int b = color.getBlue();
    // System.out.println("Eddie " + r + " " + g + " " + b);
    // if (r > g && r > b) return Color.RED;
    if (g > r && g > b) return TileType.GRASSLAND;
    else if (b > g && b > r) return TileType.SEA;
    else return TileType.MOUNTAIN;
  }

  public Color getTileColor()
  {
    switch (type)
    {
      case FOREST:
      case GRASSLAND:
        return Color.GREEN;
      case MOUNTAIN:
        return Color.GRAY;
      case SEA:
        return Color.BLUE;
      default:
        break;
    }
    return null;
  }

  public String toString()
  {
    return type.name() + " " + coord.toString() + " " + infrastructure;
  }

  public void printTile()
  {
    System.out.println(toString());
    System.out.println(resources.toString());
    pop.printPopulation();
  }

  public Color getColor()
  {
    return color;
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
    int num_available;
    // This resource doesn't exist here, so return 0
    if (resources.get(r) == null)
    {
      return 0;
    }
    num_available = resources.get(r);
    // We're extracting a quantity less than we have, so update the remaining
    if (quantity < num_available)
    {
      resources.put(r, num_available - quantity);
      return quantity;
    }
    // Values are the same, so remove the resource
    else if (quantity == num_available)
    {
      resources.remove(r);
      return num_available;
    }
    return 0;
  }

  public void addResource(Resource r, int quantity)
  {
    if (resources.get(r) == null)
    {
      resources.put(r, quantity);
    }
    else
    {
      resources.put(r, resources.get(r) + quantity);
    }
  }

  public void setAutoUpgrade(Boolean auto)
  {
    auto_upgrade = auto;
  }

  public void setTaxRate(Double rate)
  {
    if (rate >= 0.0)
    {
      tax_rate = rate;
    }
    else
    {
      tax_rate = 0.0;
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

  // Wear down the road based on the population that moved.
  // The more creatures traveling in a given route, the easier
  //  it is for future creatures to travel the same route.
  public void migrate(int pop)
  {
    pop_traveled += pop;
  }

  // As infrastructure increases, so does the average km/h
  // Return how long, in minutes, to traverse a tile based on
  //  infrastructure level
  // Cost = Time = env_f + pop_traveled + inf
  // => 60% env, 25% road, 15% inf
  // Base rate is 3 kph, the following parameters either
  //  increase or decrease this:
  // Environment => 60% weight
  // Road Travel => 30% weight
  // Infrastructure => 10% weight
  //
  // If ENV = 90% => 90 + 0.6 * 0.1 = .96
  // If RT = 1500 => 3.17 * 0.3 => 0.95
  // If inf = 5 => 5 / 10 => 0.5
  // ... => 3 * ( (0.6 * - 0.1) + (0.3 * 0.95) + (0.1 * 0.5) )
  // 2 * (1 + ( (0.6 * - 0.9) + (0.3 * 4.17) + (0.1 * (6 / 10)) ))
  public Double getTravelTime()
  {
    Double base_rate = 2.0;
    Double env_rate = 0.0;
    if (env_travel_weight.get(type) != null)
    {
      env_rate = env_travel_weight.get(type);
    }
    return 60 / (base_rate * (1 + (0.6 - env_rate) + (0.3 * Math.log10(pop_traveled)) + (0.1 * ((float)infrastructure / max_infrastructure)) ));
  }

  public int getCostOfUpgrade()
  {
    if (infrastructure < max_infrastructure)
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

  private int gcd(int a, int b)
  {
    while (b > 0)
    {
      int temp = b;
      b = a % b;
      a = temp;
    }
    return a;
  }

  private int lcm(int a, int b)
  {
    return a * (b / gcd(a, b));
  }

  // Go through the occupations of the creatures and harvest
  // resources based on infrastructure available
  //
  // Via iterating through occupations:
  // 1. Collect primary resources based on policy and available tile type
  // 2. Create secondary resources based on primary
  // 3. Attempt to fulfill demands for final products
  // 4. Return status on any unfulfilled requests
  private void harvest_resources()
  {
    // Policy for primary resources for harvesting
    Map<Occupation, Map<Resource, Double>> harvester = o_manager.getHarvestPolicy();
    // Policy for non-primary resources, including products
    Map<Occupation, Map<Resource, Boolean>> process = o_manager.process_policy;
    Map<Creature, Integer> creatures = pop.getCreatureList();
    // Iterate through all creatures
    for (Map.Entry<Creature, Integer> entry : creatures.entrySet())
    {
      Creature c = entry.getKey();
      int n_multiplier = entry.getValue();
      Occupation o = c.getOccupation();
      // This occupation has a harvest policy, therefore we're about to
      //  get some primary resources harvested
      if (harvester.get(o) != null && o_manager.base_harvest_rates.get(o) != null)
      {
        // Verify this occupation matches our tile type
        // 1. First get the resources this occupation cares about
        // 2. Check that the tile type the resource needs is the current tile
        // NOTE - We use the base rate map instead of the policy since the
        //  former cannot be changed at runtime.
        Map<Resource, Integer> res = o_manager.base_harvest_rates.get(o);
        Boolean valid_tile = false;
        // Iterate through harvest rates for the given occupation
        for (Map.Entry<Resource, Integer> rates : res.entrySet())
        {
          // If the resource on this iteration is in the valid tile map
          if (valid_resource_tiles.get(rates.getKey()) != null)
          {
            // If the resource's needed tile matches the current tile
            if (valid_resource_tiles.get(rates.getKey()).get(type) != null)
            {
              // Mark valid as true and break
              valid_tile = true;
              break;
            }
          }
        }
        if (valid_tile)
        {
          // Perform the harvest for a single worker
          res = o_manager.performHarvest(o, infrastructure);
          if (res == null)
          {
            continue;
          }
          // Update resource map with added values
          for (Map.Entry<Resource, Integer> new_res : res.entrySet())
          {
            // Add new resources to resource map multiplied by number of
            //  identical workers
            addResource(new_res.getKey(), n_multiplier * new_res.getValue());
          }
        }
      }
      // TODO Secondary and ternary resources
      // Handle secondary resources and products based on what resources
      //  are currently available
      else if (process.get(o) != null)
      {
        Map<Resource, Boolean> valid_res = process.get(o);
        // Iterate over every possible resource the given occupation
        //  is allowed to work with
        for (Map.Entry<Resource, Boolean> new_res : valid_res.entrySet())
        {
          // Resource this occupation is allowed to create
          Resource processed = new_res.getKey();
          // Get recipe to create this resource
          if (recipes.get(processed) == null)
          {
            continue;
          }
          // Verify that our resources in the Tile contain everything
          //  this recipe needs
          Map<Resource, Double> single_recipe = recipes.get(processed);
          // Whether we're processing a resource into more resources or
          //  we're creating a final product
          Boolean criteria_fulfilled = true;
          // Quant is simply the number of input resource iterations we
          //  can extract, rather than the output of the craft.
          int multiple_quant = 1;
          int minimum_quant = 2147000000;

          // First go through recipe and find any inverted items.
          // For every inverted item, find the Least Common Multiple of
          //  of their inversions. This multiple is then used further.
          //  NOTE: Only do this for inverted pairs, i.e. if there is only
          //   one inversion, we just use that.
          // Iterate through the rest of the resources, non-inverted, and
          //  multiply those needed by the common multiple of them all.
          // Follow the same procedure as below for the rest.
          //
          // This process is to alleviate the constraints of balancing
          //  between inverted and non-inverted recipe items. Long story
          //  short, we cannot split a resource into smaller values. This
          //  also standardizes the single inversion and multiple
          //  non-inversion issue.
          //
          // For Example:
          //  Recipe calls for 5 WHEAT, 1/5 FIBER, 1/2 LOGS, and 10 STONE
          //  Multiplier = 10 => 10*1/5 = 2 for FIBER, 10 * 1/2 = 5 for LOGS
          //  => 50 WHEAT, 2 FIBER, 5 LOGS, and 100 STONE => 10 Products
          //
          // For Example 2:
          //  Recipe calls for 1/15 LOGS
          //  Multiplier = 15 => 15*1/15 = 1 for LOGS
          //  => 1 LOGS => 15 Products
          //
          // For Example 3:
          //  Recipe calls for 3 LOGS, 5 STONE
          //  Multiplier = 1 => no inversions
          //  => 3 LOGS, 5 STONE => 1 Product
          //
          // Find quant value for all recipe items with inverted requirements
          for (Map.Entry<Resource, Double> needed : single_recipe.entrySet())
          {
            if (needed.getValue() < 1.0)
            {
              multiple_quant = lcm(multiple_quant, (int)(1 / needed.getValue()));
            }
          }

          for (Map.Entry<Resource, Double> needed : single_recipe.entrySet())
          {
            // A given resource in the recipe ain't available!
            if (resources.get(needed.getKey()) == null)
            {
              criteria_fulfilled = false;
              break;
            }
            // Don't have any of the resources!
            else if (resources.get(needed.getKey()) == 0)
            {
              // Extract to remove from resource list
              extractResource(needed.getKey(), 1);
              criteria_fulfilled = false;
              break;
            }
            // Individually needed resource for this recipe
            Double res_needed = needed.getValue() * multiple_quant;
            // Same resource, but quantity actually available in the Tile
            int res_available = resources.get(needed.getKey());
            // Check to make sure there's enough resources available for us to
            //  satisfy the recipe
            if (res_available >= res_needed && res_needed >= 1.0)
            {
              // The minimum number of times we can satisfy a given
              //  needed resource is the maximum number of products we
              //  can actually create over the whole recipe.
              // For example:
              //  For armor we need:
              //  50 FIBER
              //  100 METAL
              //  500 CHARCOAL
              // If we have 1000 of each, we can at most make 2 ARMOR
              //  1000 / 50 = 20  => 900 left over
              //  1000 / 100 = 10 => 800 left over
              //  1000 / 500 = 2  => 0 left over
              // Update the number of products we can create given the
              //  resources available.
              int num_satisfied = res_available / res_needed.intValue();
              if (num_satisfied < minimum_quant)
              {
                minimum_quant = num_satisfied;
              }
            }
            // Don't have enough of the needed resources
            else
            {
              criteria_fulfilled = false;
              break;
            }
          }
          // Before jumping into extraction, we first need to potentially reduce
          //  the amount of items created based on the number of workers available
          //  to do the work to convert.
          // NOTE:
          //  When we have non-inverted values, such as the example above, the below
          //  may look like:
          //   2 > 1 * 1 => quant = 1
          //  When we have inverted values, the below may look like
          //   1500 > 1 * 1 => quant = 1
          if (minimum_quant > n_multiplier * infrastructure)
          {
            minimum_quant = n_multiplier * infrastructure;
          }
          multiple_quant *= minimum_quant;

          // Now that we've calculated the number of products we can create,
          //  we can go through and extract + add the needed items.
          if (criteria_fulfilled && multiple_quant > 0)
          {
            // Iterate through the different resources needed to craft the new product
            //  and extract them.
            for (Map.Entry<Resource, Double> needed : single_recipe.entrySet())
            {
              extractResource(needed.getKey(), (int)(needed.getValue() * multiple_quant));
            }
            // Add new item to resource map
            addResource(processed, multiple_quant);
          }
        }
      }
    }
  }
  // To update the Tile we perform the following
  // 1. Update population based on tax rate and infrastructure
  // 2. Update the infrastructure and/or pay for maintenance
  public void update()
  {
    // Degrade the roads
    pop_traveled *= 0.8;
    if (pop_traveled < 1)
    {
      pop_traveled = 1;
    }
    // First collect resources
    harvest_resources();
    // Second, update population and collect taxes
    int cp = pop.update(tax_rate, infrastructure);
    // Base check to make sure the CP resources exists
    if (resources.get(Resource.CP) == null)
    {
      resources.put(Resource.CP, 0);
    }
    // Once we got here, we need to apply whatever balance we have
    //  to maintaining or upgrading the infrastructure.
    cp += resources.get(Resource.CP);

    // NOTE - We can only upgrade infrastructure at a max once per update
    if (auto_upgrade && getCostOfUpgrade() <= cp && infrastructure < 11)
    {
      // Update money resource and increment infrastructure
      resources.put(Resource.CP, cp - getCostOfUpgrade());
      infrastructure++;
    }
    else
    {
      // Pay for maintenance
      resources.put(Resource.CP, cp - getCostOfMaintenance());
    }
    // We cannot pay for maintenance, so we risk the infrastructure
    // decaying.
    cp = resources.get(Resource.CP);
    if (cp < 0)
    {
      Random rand = new Random();
      // TODO - Change decay rate at some point
      if (rand.nextInt(5) < 1)
      {
        infrastructure -= 1;
      }
      // Reset back to 0
      resources.put(Resource.CP, 0);
    }
  }
  // Just test stuff
  public static void main(String[] args)
  {
    int passes = 0;
    Coordinate c = new Coordinate(1,1);
    Tile tile = new Tile(Tile.TileType.FOREST, c);
    System.out.println(tile.getTravelTime());
    System.out.println(tile.getCostOfUpgrade());
    System.out.println(tile.getCostOfMaintenance());
    // tile.printTile();

    // BASE update test case
    int ext_bal = 1000;
    tile.addResource(Resource.CP, ext_bal);
    tile.setAutoUpgrade(true);
    tile.update();
    ext_bal = tile.getResourceQuantity(Resource.CP);
    if (ext_bal == 810) passes++;
    System.out.println("1 : " + ext_bal + " " + tile.getCostOfMaintenance() + " " + tile.getCostOfUpgrade());
    // tile.printTile();

    tile.extractResource(Resource.CP, 400);
    tile.update();
    ext_bal = tile.getResourceQuantity(Resource.CP);
    if (ext_bal == 49) passes++;
    System.out.println("2 : " + ext_bal + " " + tile.getCostOfMaintenance() + " " + tile.getCostOfUpgrade());
    // tile.printTile();

    tile.addResource(Resource.CP, 1000);
    tile.setAutoUpgrade(false);
    tile.update();
    ext_bal = tile.getResourceQuantity(Resource.CP);
    if (ext_bal == 905) passes++;
    System.out.println("3 : " + ext_bal + " " + tile.getCostOfMaintenance() + " " + tile.getCostOfUpgrade() + "\n\n");
    // tile.printTile();

    // Tile update with population
    Population p = tile.getPopulation();
    p.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.PEASANT), 1000);
    p.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LUMBERJACK), 100);
    p.pushCreature(new Creature(Creature.Race.DWARF, Occupation.MINER), 50);
    // p.printPopulation();
    tile.setTaxRate(0.1);
    tile.update();
    // tile.printTile();

    // Welsh Piper Population Generation
    // tile = new Tile(Tile.TileType.FOREST, c);
    // Population wp = new Population(412);
    // tile.getPopulation().absorbPopulation(wp);
    // // wp.printPopulation();
    // // p = wp;
    // // p.absorbPopulation(wp);
    // // tile.printTile();
    // tile.setAutoUpgrade(true);
    // tile.addResource(Resource.CP, 5000);
    // tile.printTile();

    // for (int i = 0; i < 2; i++)
    // {
    //   tile.update();
    // }
    // tile.printTile();

    // Test Harvesting
    System.out.println("===== BEGIN HARVEST TEST =====");
    tile = new Tile(Tile.TileType.FOREST, c);
    Population wp = new Population(0);
    // Start off with basic producers
    wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.WOODCRAFTER), 1);
    wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.MILLER), 2);
    wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.ARMORER), 2);
    // wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.WOODCRAFTER), 2);
    wp.printPopulation();
    // Absorb the population into the tile
    tile.getPopulation().absorbPopulation(wp);
    tile.setAutoUpgrade(true);
    tile.addResource(Resource.CP, 5000);
    tile.addResource(Resource.FIBERS, 1000);
    tile.addResource(Resource.METAL, 1000);
    tile.addResource(Resource.CHARCOAL, 1000);
    tile.printTile();

    for (int i = 0; i < 5; i++)
    {
      tile.update();
      tile.printTile();
    }
    tile.addResource(Resource.CHARCOAL, 1000);
    tile.update();
    tile.printTile();
    System.out.println(tile.getTravelTime());
    tile.migrate(5900);
    System.out.println(tile.getTravelTime());

    System.out.println("TESTS PASSED : [" + passes + "/3]");
  }
}
