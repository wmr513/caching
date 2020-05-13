package ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.eviction.lru.LruEvictionPolicyFactory;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.configuration.NearCacheConfiguration;

public class NearTest {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		
		java.util.Scanner input = new java.util.Scanner(System.in);
		System.out.print("Near cache size (1-5): ");
	    int maxNearSize = input.nextInt();

		System.out.println("Connecting to Apache Ignite server...");
		CacheConfiguration serverCfg = new CacheConfiguration("names");
		serverCfg.setStatisticsEnabled(true);
		IgniteConfiguration ic = new IgniteConfiguration();
		ic.setClientMode(true);
		NearCacheConfiguration<String, String> nearCfg = new NearCacheConfiguration<>();
		nearCfg.setNearEvictionPolicyFactory(new LruEvictionPolicyFactory<>(maxNearSize));
		Ignite ignite = Ignition.start(ic);
		IgniteCache<String, String> cache = ignite.getOrCreateCache(serverCfg, nearCfg);

		System.out.println("\n\n\n\n");
		System.out.println("Connected");
		
		System.out.println("near cache max size: " + maxNearSize);
		System.out.println("near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println("evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.println();
		System.out.print("adding [1,Mark] to cache -"); cache.put("1", "Mark");
		System.out.print(" near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println(" evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.print("adding [2,Beth] to cache -"); cache.put("2", "Beth");
		System.out.print(" near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println(" evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.print("adding [3,Neal] to cache -"); cache.put("3", "Neal");
		System.out.print(" near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println(" evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.print("adding [4,Raju] to cache -"); cache.put("4", "Raju");
		System.out.print(" near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println(" evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.print("adding [5,Mary] to cache -"); cache.put("5", "Mary");
		System.out.print(" near size: " + cache.localMetrics().getHeapEntriesCount());
		System.out.println(" evictions: " + cache.localMetrics().getCacheEvictions());
		System.out.println();

		boolean localCacheHit = true;
		long misses = cache.localMetrics().getCacheMisses();

		while (true) {
		    System.out.print("Customer Id: ");
		    String custid = input.next();
		    if (custid.equals("0")) break;
			System.out.print("getting name for " + custid + ": " + cache.get(custid));
			localCacheHit = (cache.localMetrics().getCacheMisses() == misses);
			misses = cache.localMetrics().getCacheMisses();
			System.out.println(", local cache hit = " + localCacheHit);
		}

		input.close();
		ignite.close();
	}
}
