package at.ac.ait.ubicity.twitterplugin.impl;

import org.junit.Ignore;
import org.junit.Test;

public class TwitterCronTest {

	private static TwitterPostTaskUs pt = new TwitterPostTaskUs();

	@Test
	@Ignore
	public void testTweetPost() {
		pt.executeTask();
	}

	@Test
	@Ignore
	public void testRandomMsg() {
		System.out.println(pt.generateRandomText("#hashtag"));
	}
}
