package datacollisions;

public class DataColllisionCalc {
	
	public static void main(String[] args) {
	    System.out.println("A data collision occurs under the following conditions:");
	    System.out.println("  - A row is updated in cache A, and the update replicated to cache B.");
	    System.out.println("  - While replicating, that same row is updated in cache B.");
	    System.out.println("  - The replication from cache A then overlays the local update in cache B.");
	    System.out.println("  - The replication from cache B then overlays the prior update in cache A.");
	    System.out.println();
	    System.out.println("Example: Simultaneous updates to the inventory count of a particular product.");
	    System.out.println("  - Current inventory count for a product: 500 units");
	    System.out.println("  - Cache A updates the inventory count to 490 units (10 sold)");
	    System.out.println("  - During replication, Cache B updates inventory to 495 units (5 sold)");
	    System.out.println("  - Cache B then gets updated to 490 units (replication from cache A update)");
	    System.out.println("  - Cache A then gets updated to 495 units (replication from cache B update)");
	    System.out.println("  - Both are incorrect - total inventory should now read 485 units (15 total sold)");
	    System.out.println();
	    System.out.println("The data collision calculation calculates how many updates will have a high probablility of being " + 
	                       "overlayed due to replication latency " + 
	    		           "(source: https://www.shadowbasesoftware.com/wp-content/uploads/2016/08/Resolving-Data-Collisions.pdf)");
	    System.out.println();
	    
		@SuppressWarnings("resource")
		java.util.Scanner input = new java.util.Scanner(System.in);
	    System.out.print("Number of service instances: ");
	    Long instances = input.nextLong();
	    
	    System.out.print("Update rate (seconds): ");
	    Long updateRate = input.nextLong();
	    
	    System.out.print("Cache size (rows): ");
	    Long cacheSize = input.nextLong();
	    
	    System.out.print("Replication latency (milliseconds): ");
	    double latency = input.nextDouble() / 1000;

	    double collisions = instances * (Math.pow(updateRate,2)/cacheSize) * latency;
	    
	    System.out.println();
	    System.out.println("Updates:    " + updateRate + " per second (" + (updateRate*3600) + " per hour)");
	    System.out.println("Collisions: " + String.format("%.4f", collisions) + " per second (" + String.format("%.1f", (collisions*3600)) + " per hour)");    
	    System.out.println("Percentage: " + String.format("%.4f", ((collisions)/(updateRate)) * 100.0) + "%");
	}
}
