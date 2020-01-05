package ignite;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteCache;
import org.apache.ignite.Ignition;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.CacheConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;

public class ReplicatedTest1 {
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void main(String[] args) throws Exception {
		
		System.out.println("Starting Apache Ignite...");
		CacheConfiguration cfg = new CacheConfiguration("names");
		cfg.setCacheMode(CacheMode.REPLICATED);
		IgniteConfiguration ic = new IgniteConfiguration();
		ic.setClientMode(false);
		Ignite ignite = Ignition.start(ic);
		IgniteCache<String, String> cache = ignite.getOrCreateCache(cfg);
		System.out.println("Apache Ignite started");
		
		while (true) {
			if (cache.get("1") == null) {
				System.out.println("Cache empty");
			} else {
				System.out.println("Cache entry: 1, " + cache.get("1"));
			}
			Thread.sleep(500);
		}
	}
}
