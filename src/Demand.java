import java.util.*;

public class Demand
{
    private Resource resource;
    private int requested;
    private int needed;
    private Boolean serviced;

    public Demand(Resource r, int requested)
    {
        this.resource = r;
        // NOTE: Negative values are acceptable. It is rather
        // indicating there is N extra resources available
        this.requested = requested;
        this.needed = requested;
        this.serviced = false;
    }

    public Boolean isServiced()
    {
        return serviced;
    }

    public int getDemand()
    {
        return needed;
    }

    public Boolean serviceDemand(Resource r, int n)
    {
        if (r != resource || n > needed || n < 1)
        {
            return false;
        }
        needed -= n;
        serviced = true;
        return true;
    }
}