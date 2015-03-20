package at.ac.ait.ubicity.twitterplugin.impl;

import org.junit.Ignore;
import org.junit.Test;

public class TwitterCronTest {

	private static TwitterPostTask pt = new TwitterPostTask();

	@Test
	@Ignore
	public void testTweetPost() {
		pt.executeTask();
	}

	@Test
	@Ignore
	public void testRandomMsg() {
		System.out.println(pt.generateRandomText());
	}
}
