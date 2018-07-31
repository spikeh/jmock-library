package uk.davidwei.perfmock.internal.perf;

public class Resource {
    private int maxResources;
    private int availableResources;

    public Resource(Sim sim) {
        this.maxResources = 1;
        this.availableResources = 1;
    }

    public Resource(Sim sim, int resources) {
        this.maxResources = resources;
        this.availableResources = resources;
    }

    public void claim() {
        if (availableResources <= 0) {
            throw new ResourceException("Attempting to claim unavailable resource.");
        }
        availableResources--;
    }

    public void release() {
        if (availableResources >= maxResources) {
            throw new ResourceException("Attempting to release non-existent resource.");
        }
        availableResources++;
    }

    public boolean isAvailable() {
        return availableResources > 0;
    }
}