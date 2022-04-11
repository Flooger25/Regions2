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
// TODO - Add angular direction configuration
enum Direction
{
  N, S, E, W,
  NW, NE, SW, SE,
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
  private Map<Resource, Integer> resources;
  private Map<Direction, Tile> neighbors;
  private Population pop;

  private Policy tile_policy;

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

  public Tile(TileType type)
  {
    this.type = type;
    this.coord = new Coordinate(0,0);
    this.infrastructure = 0;
    this.pop = new Population();
    this.o_manager = new OccupationManager();
    this.neighbors = new Hashtable<Direction, Tile>();
    this.resources = new Hashtable<Resource, Integer>();
    this.tile_policy = new Policy();
  }

  private final void initialize(Boolean provide_color)
  {
    neighbors = new Hashtable<Direction, Tile>();
    resources = new Hashtable<Resource, Integer>();
    tile_policy = new Policy();
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

  public TileType getTileType()
  {
    return type;
  }

  public Policy getPolicy()
  {
    return tile_policy;
  }

  public Color getColor()
  {
    return color;
  }

  public Coordinate getCoordinate()
  {
    return coord;
  }

  public int getInfrastructureLevel()
  {
    return infrastructure;
  }

  public Map<Resource, Integer> getResources()
  {
    return resources;
  }

  public int getResourceQuantity(Resource r)
  {
    if (resources.get(r) != null)
    {
      return resources.get(r);
    }
    // This resource doesn't exist here, so return 0
    return 0;
  }

  // Extract a Resource 'r' and return the amount extracted
  public int extractResource(Resource r, int quantity)
  {
    int num_available = getResourceQuantity(r);
    // This resource doesn't exist here, so return 0
    if (num_available == 0)
    {
      return 0;
    }
    // We're extracting a quantity less than we have, so update the remaining
    if (quantity < num_available)
    {
      resources.put(r, num_available - quantity);
      return quantity;
    }
    // Values are the same, so remove the resource
    // OR
    // We've been asked to extract more than we have, so just remove the
    //  resource and return what we did extract
    else if (quantity >= num_available)
    {
      resources.remove(r);
      return num_available;
    }
    // Shouldn't get here
    return -1;
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

  // // Return enumerated list of resources available in the tile
  // public Enumeration getResourcesAvailable()
  // {
  //   if (resources == null)
  //   {
  //     System.out.println("ERROR - No resources found!");
  //     return null;
  //   }
  //   return resources.keys();
  // }

  // Wear down the road based on the population that moved.
  // The more creatures traveling in a given route, the easier
  //  it is for future creatures to travel the same route.
  public void migrate(int pop)
  {
    pop_traveled += pop;
  }

  public void migrate(Population p)
  {
    if (p != null)
    {
      // TODO - Support dynamic resource and populations in Tiles
      // i.e. allow resources and populations to exist in Tiles only
      //  during a single cycle as if they're traveling to a destination
      pop_traveled += p.getPopulation();
      pop.absorbPopulation(p);
    }
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
    // NOTE : Infrastructure of 0 needs no maintenance
    if (infrastructure > 0)
    {
      return (int)(100 * Math.pow(1.2, infrastructure));
    }
    return 0;
  }

  public Double getTaxRate()
  {
    if (tile_policy != null)
    {
      return tile_policy.getTaxRate();
    }
    System.out.println("ERROR - No policy found in Tile.getTaxRate()!");
    return 0.0;
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
  public void harvest_resources(Map<Occupation, Integer> labor)
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
      int laborer_multiplier = 0;
      Occupation o = c.getOccupation();
      // This occupation has a harvest policy, therefore we're about to
      //  get some primary resources harvested
      if (harvester.get(o) != null && o_manager.base_harvest_rates.get(o) != null)
      {
        // Add number of laborers assigned to this harvesting Occupation
        // NOTE : Laborers can ONLY be used for harvesting and specific secondary
        //  Occupations.
        if (labor != null)
        {
          if (labor.get(o) != null)
          {
            laborer_multiplier = labor.get(o);
          }
        }
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
            Double new_resources = new_res.getValue() * (laborer_multiplier * o_manager.LABORER_RATE + n_multiplier);
            addResource(new_res.getKey(), new_resources.intValue());
          }
        }
      }
      // Handle secondary resources and products based on what resources
      //  are currently available
      else if (process.get(o) != null)
      {
        // Add number of laborers assigned to this processing Occupation
        // NOTE : Laborers can ONLY be used for harvesting and specific secondary
        //  Occupations.
        if (labor != null)
        {
          if (labor.get(o) != null && o == Occupation.MILLER)
          {
            laborer_multiplier = labor.get(o);
          }
        }
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
          // The recipe for creating item 'processed'
          Map<Resource, Double> single_recipe = recipes.get(processed);
          // Whether all of the needed resources for the recipe were found
          // or not.
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
            if (getResourceQuantity(needed.getKey()) < 1)
            {
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
          // 1. When we have non-inverted values, such as the example above, the below
          //  may look like:
          //   2 > 1 * 1 => quant = 1
          //  When we have inverted values, the below may look like
          //   1500 > 1 * 1 => quant = 1
          // 2. We also need to keep tabs on the recipe rates we have at our disposal.
          // For example, if the minimum quant is 50K, the inf is 1, the n multiplier
          //  is 2, and the rate at which the resource can be made is 1K, we would
          //  get to the following total number we can make:
          //    1 * 2 * 1K = 2K, which is a lot less than 50K.
          // Long story short, we may have the resources to make 50K of something, but
          //  we only have enough workers to make 2K.
          // 3. Things get slightly more dicey when bringing in LABORERs to the mix.
          //  Long story short, for each laborer creating a product, they add
          //  LABORER_RATE quantity to the resources instead.
          Double max_quant_by_work =
            (n_multiplier + laborer_multiplier * o_manager.LABORER_RATE) *  // Number of workers
            infrastructure *                       // Infrastructure level
            o_manager.recipe_rates.get(processed); // Rate of resource generation per worker

          if (minimum_quant > max_quant_by_work.intValue())
          {
            minimum_quant = max_quant_by_work.intValue();
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

  // An order has been issued to modify the 'old_o' Occupation of 'n' creatures to a
  // new Occupation 'new_o'.
  public int processOccupationOrder(Occupation old_o, Occupation new_o, int n)
  {
    if (old_o == null || new_o == null || n < 1)
    {
      System.out.println("ERROR - Invalid input params for OccupationOrder.");
      return -1;
    }
    if (!o_manager.isValidConversion(new_o, old_o))
    {
      // System.out.println("WARNING - Invalid Occupation conversion : " + old_o + " => " + new_o);
      return -1;
    }
    // Utilize Population function and just return its value
    return pop.modifyOccupation(old_o, new_o, n);
  }

  // An order has been issued to generate 'new_o' Occupations.
  public int processOccupationOrder(Occupation new_o, int n)
  {
    if (new_o == null || n < 1)
    {
      System.out.println("ERROR - Invalid input params for OccupationOrder.");
      return -1;
    }
    if (o_manager.occupation_conversion_policy.get(new_o) == null)
    {
      // System.out.println("WARNING - Invalid new_o in conversion policy for OccupationOrder.");
      return -1;
    }
    // Get options on valid old Occupations we can generate from
    Map<Occupation, Integer> options = o_manager.occupation_conversion_policy.get(new_o);
    int leftover = n;
    // Iterate over valid originating occupations and attempt to modify
    for (Map.Entry<Occupation, Integer> entry : options.entrySet())
    {
      if (entry != null)
      {
        // If the new_o can be generated from the old one.
        // NOTE - We should never fail this since we're directly getting info from the
        //  occupation_conversion_policy map.
        if (o_manager.isValidConversion(new_o, entry.getKey()))
        {
          leftover -= pop.modifyOccupation(entry.getKey(), new_o, leftover);
        }
      }
      if (leftover < 1)
      {
        break;
      }
    }
    return n - leftover;
  }

  // Calculate how much food we can generate given the existing
  // Farmers, Millers, and the pool of available laborers.
  //
  // If we don't have enough laborers to achieve the job, then other
  // things happen...
  //
  // Return a map of the number of laborers assigned to each
  // Occupation for us to harvest the best combination of resources
  public void calculateFoodGeneration(Map<Occupation, Integer> labor_assignment)
  {
    Occupation MILLER = Occupation.MILLER;
    Occupation FARMER = Occupation.FARMER;
    // Determine if there's enough food available
    int food_demand = pop.getPopulation();
    int num_wheat = getResourceQuantity(Resource.WHEAT);
    int num_bread = getResourceQuantity(Resource.BREAD);
    // Worker pool
    int farmers = pop.queryNumOfOccupation(FARMER);
    int millers = pop.queryNumOfOccupation(MILLER);
    int laborers = pop.queryNumOfOccupation(Occupation.LABORER);
    // Y BREAD <= X WHEAT
    int wheat_to_bread = recipes.get(Resource.BREAD).get(Resource.WHEAT).intValue();
    // Perform a mock harvest to get how much wheat would be harvested per worker
    Map<Resource, Integer> mock_wheat = o_manager.performHarvest(FARMER, infrastructure);
    int farmable_wheat = 0;
    if (mock_wheat != null)
    {
      farmable_wheat = mock_wheat.get(Resource.WHEAT);
    }

    int expected_food = 0;
    // Loop until we find the correct arrangement of laborers
    while (expected_food < food_demand && laborers > 0)
    {
      // Example:
      // 2 farmers, 2 millers, 10 laborers, need 5000 food
      // 50 wheat, 20 bread
      // 20 + min(2 * 2000, 50 + 2 * 8000)[4000] = 4020
      int max_usable_bread = millers * wheat_to_bread * o_manager.recipe_rates.get(Resource.BREAD);
      int max_possible_wheat = num_wheat + (farmers * farmable_wheat);
      expected_food =
        num_bread +
        Math.min(max_usable_bread, max_possible_wheat);

      // Really only needed for the first iteration
      if (expected_food >= food_demand)
      {
        break;
      }
      // Determine if we need more millers or farmers.
      // Take from laborer pool.
      // Case 1 : More millers
      if (max_usable_bread < max_possible_wheat)
      {
        labor_assignment.put(MILLER, labor_assignment.get(MILLER) + 1);
        millers += 1;
      }
      // Case 2 : More farmers
      else
      {
        labor_assignment.put(FARMER, labor_assignment.get(FARMER) + 1);
        farmers += 1;
      }
      laborers -= 1;
    }
  }

  // 1. Determine if we have enough food or will produce enough on top
  //  - if (millers + farmers) are enough, move on
  //  - if (millers + farmers + laborers) are enough, move on
  //  - Regardless, post the Demand of wheat/bread
  // 2. Harvest resources
  // 3. If we still need food:
  //  - if (stateless), start changing occupancy
  //  - else (state):
  //   - if (can convert?), start changing occupancy
  //   - else (cannot convert), let State take care of Demand
  //
  public void update(Policy policy)
  {
    if (policy == null)
    {
      System.out.println("ERROR - No policy provided!");
      return;
    }
    // Degrade the roads
    pop_traveled *= 0.8;
    if (pop_traveled < 1)
    {
      pop_traveled = 1;
    }
    // TODO - Degrade food stores
  
    // [1] Assign laborers
    Map<Occupation, Integer> labor_assignment = new Hashtable<Occupation, Integer>()
    {{
      put(Occupation.FARMER, 0);
      put(Occupation.MILLER, 0);
    }};
    calculateFoodGeneration(labor_assignment);
  
    // [2] Harvest resources
    harvest_resources(labor_assignment);
  
    // [3] Consume the food
    int total_pop = pop.getPopulation();
    if (total_pop > 0)
      System.out.println("EDDIE - Consuming " + total_pop + " / " + getResourceQuantity(Resource.BREAD) + " food!");
    int consumed_food = extractResource(Resource.BREAD, total_pop);

    // [4] Change Occupations because we need Farmers or Millers!
    // We didn't extract enough food to meet the needs of the whole population,
    // therefore we need to take further measures.
    // Need more laborers! Or better yet, we need more food
    if (consumed_food < total_pop)
    {
      int food_demand = total_pop - consumed_food;
      System.out.println("WARNING - Need " + food_demand + " food!");
      // Case 1 : We're owned by a State
      if (policy.hasState() && !policy.getLockOccupation())
      {
        // TODO - Make accurate numbers
        // Create Demand and publish to Policy
        policy.addDemand(new Demand(Resource.WHEAT, food_demand));
        policy.addDemand(new Demand(Resource.BREAD, food_demand));
      }
      // Case 2 : We're either independent of a State or our State
      //  has allowed us to change Occupations on our own
      else
      {
        if (policy.getGenerateDemands())
        {
          // TODO - Make accurate numbers
          policy.addDemand(new Demand(Resource.BREAD, food_demand));
          policy.addDemand(new Demand(Resource.WHEAT, food_demand));
        }
        // Change Occupations to support having enough food next update
        if (policy.getOccFrom() != null)// && policy.getOccTo() != null)
        {
          ArrayList<Occupation> from = policy.getOccFrom();
          // ArrayList<Occupation> to = p.getOccTo();
          // Loop over acceptable occupations we're allowed to change
          for (int i = 0; i < from.size(); i++)
          {
            // Create and process an internal Occupation Order
            // Attempt to go from 'from.get(i)' to MILLER or FARMER depending on resources
            if (getResourceQuantity(Resource.WHEAT) > getResourceQuantity(Resource.BREAD))
            {
              if (processOccupationOrder(from.get(i),
                                         Occupation.MILLER,
                                         (int)(food_demand / o_manager.recipe_rates.get(Resource.BREAD)) + 1) > 0 )
              {
                break;
              }
            }
            else if (processOccupationOrder(from.get(i),
                                            Occupation.FARMER,
                                            (int)(food_demand / o_manager.base_harvest_rates.get(Occupation.FARMER).get(Resource.WHEAT)) + 1) > 0 )
            {
              break;
            }
          }
        }
      }
    }
    // Second, update population and collect taxes
    int cp = pop.update(policy.getTaxRate(), infrastructure) + getResourceQuantity(Resource.CP);

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
    cp = getResourceQuantity(Resource.CP);
    Random rand = new Random();
    if (cp < 0)
    {
      // TODO - Change decay rate at some point
      if (rand.nextInt(5) < 1)
      {
        infrastructure -= 1;
      }
      // Reset back to 0
      resources.put(Resource.CP, 0);
    }

    // To support world-wide migration + give Tiles more autonomy, we
    //  select certain Occupations to go to a neighboring Tile to travel
    //  and settle.
    // First we do the adventurers since they're easy.
    // TODO - Support other Occupations leaving.
    Map<Creature, Integer> adventurers = pop.getCreatureList(Occupation.ADVENTURER);
    // TODO - Make this more dynamic
    int distance_to_travel = 10;
    int max_iterations = 10;
    // Iterate over all adventurer instances
    for (Map.Entry<Creature, Integer> entry : adventurers.entrySet())
    {
      Tile tile_found = null;
      // We have enough food to send someone out
      if (getResourceQuantity(Resource.BREAD) > distance_to_travel)
      {
        Direction d = Direction.values()[ rand.nextInt(4) ];
        while (tile_found == null)
        {
          // Travel in a random direction that leads to a Tile
          while (neighbors.get(d) == null)
          {
            // TODO - Support angular directions
            d = Direction.values()[ rand.nextInt(4) ];
            max_iterations--;
            if (max_iterations <= 0) break;
          }
          if (max_iterations <= 0) break;
          // Make sure we don't end up in the SEA
          if (neighbors.get(d).getTileType() == TileType.SEA)
          {
            max_iterations--;
            if (max_iterations <= 0) break;
            continue;
          }
          tile_found = neighbors.get(d);
        }
        if (tile_found != null)
        {
          // Figure out how many adventurers we can send based on food available
          final int num_feedable = Math.min(getResourceQuantity(Resource.BREAD) / distance_to_travel, entry.getValue());
          // Move these particular adventurers to the neighbor Tile
          tile_found.migrate(pop.splitPopulation( new Hashtable<Creature, Integer>(){{ put(entry.getKey(), num_feedable); }} ));
          extractResource(Resource.BREAD, num_feedable * distance_to_travel);
          tile_found.addResource(Resource.BREAD, num_feedable * distance_to_travel);
          System.out.println("INFO - Moved " + num_feedable + " from " + coord.toString() + " => " + tile_found.getCoordinate().toString());
        }
      }
    }
  }

  // To update the Tile we perform the following
  // 1. Update population based on tax rate and infrastructure
  // 2. Update the infrastructure and/or pay for maintenance
  public void update()
  {
    update(tile_policy);
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
    wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.LABORER), 2);
    // wp.pushCreature(new Creature(Creature.Race.HUMAN, Occupation.WOODCRAFTER), 2);
    wp.printPopulation();
    // Absorb the population into the tile
    tile.getPopulation().absorbPopulation(wp);
    tile.setAutoUpgrade(true);
    tile.addResource(Resource.CP, 5000);
    // tile.addResource(Resource.FIBERS, 1000);
    // tile.addResource(Resource.METAL, 1000);
    // tile.addResource(Resource.CHARCOAL, 1000);
    tile.printTile();

    System.out.println("Adding policy Occupations");
    tile.getPolicy().getOccFrom().add(Occupation.ARMORER);
    tile.getPolicy().getOccFrom().add(Occupation.WOODCRAFTER);

    tile.update();
    tile.printTile();
    // System.out.println("Adding policy Occupations");
    // tile.getPolicy().getOccFrom().add(Occupation.ARMORER);
    // tile.getPolicy().getOccFrom().add(Occupation.WOODCRAFTER);
    // Farmer being added in this update
    tile.update();
    tile.printTile();

    tile.update();
    tile.printTile();

    // for (int i = 0; i < 1; i++)
    // {
    //   System.out.println("UPDATING...");
    //   tile.update();
    //   System.out.println("PRINTING...");
    //   tile.printTile();
    // }
    // tile.addResource(Resource.CHARCOAL, 1000);
    // tile.update();
    // tile.printTile();
    // System.out.println(tile.getTravelTime());
    // tile.migrate(5900);
    // System.out.println(tile.getTravelTime());

    System.out.println("TESTS PASSED : [" + passes + "/3]");
  }
}
