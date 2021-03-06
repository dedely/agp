package test.manual;

import java.io.*;

import persistence.lucene.*;

public class TestLucene {
	public TestLucene() throws Exception {
		String[] testNames = { "site1", "site2", "amazingsite" };

		PrintWriter writer;

		String localDir = System.getProperty("user.dir");

		for (String name : testNames) {

			writer = new PrintWriter(localDir + "/tmp/files/" + name + ".txt", "UTF-8");
			writer.println("Template text about a site, which could be anything but surely wonderful.");
			writer.close();

		}

		String sysdir = System.getProperty("user.dir");

		LuceneIndexer indexer = LuceneIndexer.getInstance(sysdir + "/tmp/index");

		// int num = indexer.indexing(sysdir+"/tmp/files");
		int num2 = indexer.reindex(sysdir + "/tmp/files");

		System.out.println("Indexed " + num2 + " files out of " + testNames.length);

	}
}