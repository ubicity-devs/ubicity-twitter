package at.ac.ait.ubicity.twitterplugin.impl;

import java.util.Random;

import org.apache.log4j.Logger;
import org.fluttercode.datafactory.impl.DataFactory;

import twitter4j.GeoLocation;
import twitter4j.Status;
import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.conf.ConfigurationBuilder;
import at.ac.ait.ubicity.commons.cron.AbstractTask;
import at.ac.ait.ubicity.commons.util.PropertyLoader;

public class TwitterPostTaskVie extends AbstractTask {

	private static final Logger logger = Logger.getLogger(TwitterPostTaskVie.class);

	private static final PropertyLoader config = new PropertyLoader(TwitterPostTaskVie.class.getResource("/twitter.cfg"));

	private static final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
	private static Twitter twitterInstance = null;
	private static final DataFactory df = new DataFactory();

	private Twitter getTwitterInstance() {

		if (twitterInstance == null) {
			configBuilder.setOAuthConsumerKey(config.getString("plugin.twitter_post.oauth_consumer_key"));
			configBuilder.setOAuthConsumerSecret(config.getString("plugin.twitter_post.oauth_consumer_secret"));
			configBuilder.setOAuthAccessToken(config.getString("plugin.twitter_post.oauth_access_token"));
			configBuilder.setOAuthAccessTokenSecret(config.getString("plugin.twitter_post.oauth_access_token_secret"));

			configBuilder.setHttpRetryCount(10);
			configBuilder.setGZIPEnabled(true);

			twitterInstance = new TwitterFactory(configBuilder.build()).getInstance();
		}

		return twitterInstance;
	}

	@Override
	public void executeTask() {

		// Gramatneusiedel, AT
		final double LONGITUDE = 16.489036;
		final double LATITUDE = 48.030589;

		try {
			StatusUpdate su = new StatusUpdate(generateRandomText("#vie_test"));
			su.setLocation(new GeoLocation(LATITUDE, LONGITUDE));
			su.setDisplayCoordinates(true);

			Status status = getTwitterInstance().updateStatus(su);
			logger.info("Updated Twitter status to [" + status.getText() + "] @ " + LATITUDE + ", " + LONGITUDE);
		} catch (TwitterException e) {
			logger.error("Twitter post threw error: ", e);
		}
	}

	/**
	 * Generates Random text fragments for tweets with continuous number.
	 * 
	 * @return
	 */
	String generateRandomText(String hashTag) {
		Integer lastId = (Integer) getProperty("lastId");
		String txt = "";

		if (lastId == null) {
			lastId = 0;
		}

		for (int i = 0; i < new Random().nextInt(50); i++) {
			df.getFirstName();
		}
		for (int i = 0; i < new Random().nextInt(50); i++) {
			df.getRandomWord();
		}
		for (int i = 0; i < new Random().nextInt(50); i++) {
			df.getBusinessName();
		}

		txt = df.getFirstName() + " " + df.getRandomWord() + " " + df.getBusinessName() + " " + hashTag + " #No" + (lastId + 1);

		setProperty("lastId", lastId + 1);
		return txt;
	}
}