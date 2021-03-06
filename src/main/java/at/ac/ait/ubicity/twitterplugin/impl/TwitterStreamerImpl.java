package at.ac.ait.ubicity.twitterplugin.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.Place;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.UserMentionEntity;
import twitter4j.conf.ConfigurationBuilder;
import at.ac.ait.ubicity.commons.broker.BrokerProducer;
import at.ac.ait.ubicity.commons.broker.events.EventEntry;
import at.ac.ait.ubicity.commons.broker.events.EventEntry.Property;
import at.ac.ait.ubicity.commons.exceptions.UbicityBrokerException;
import at.ac.ait.ubicity.commons.util.ESIndexCreator;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.twitterplugin.TwitterStreamer;
import at.ac.ait.ubicity.twitterplugin.dto.TwitterDTO;
import at.ac.ait.ubicity.twitterplugin.dto.TwitterUserDTO;

@PluginImplementation
public class TwitterStreamerImpl extends BrokerProducer implements TwitterStreamer {

	private final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
	protected TwitterStream twitterStream = null;
	private final FilterQuery filterQuery = new FilterQuery();

	private ESIndexCreator ic;

	private String name;
	private String pluginDest;

	private boolean startStream;

	private final static Logger logger = Logger.getLogger(TwitterStreamerImpl.class);

	@Override
	@Init
	public void init() {
		PropertyLoader config = new PropertyLoader(TwitterStreamerImpl.class.getResource("/twitter.cfg"));
		startStream = config.getBoolean("plugin.twitter.search");

		if (startStream) {
			setProducerSettings(config);
			setPluginConfig(config);
			setOAuthSettings(config);
			setFilterSettings(config);

			logger.info(name + " loaded");
		}
	}

	/**
	 * Sets the Apollo broker settings
	 * 
	 * @param config
	 */
	private void setProducerSettings(PropertyLoader config) {
		try {
			super.init();
			pluginDest = config.getString("plugin.twitter.broker.dest");

		} catch (UbicityBrokerException e) {
			logger.error("During init caught exc.", e);
		}
	}

	/**
	 * Sets the OAuth credentials for twitter access
	 * 
	 * @param config
	 */
	private void setOAuthSettings(PropertyLoader config) {
		configBuilder.setOAuthConsumerKey(config.getString("plugin.twitter.oauth_consumer_key"));
		configBuilder.setOAuthConsumerSecret(config.getString("plugin.twitter.oauth_consumer_secret"));
		configBuilder.setOAuthAccessToken(config.getString("plugin.twitter.oauth_access_token"));
		configBuilder.setOAuthAccessTokenSecret(config.getString("plugin.twitter.oauth_access_token_secret"));

		configBuilder.setHttpRetryCount(10);
		configBuilder.setGZIPEnabled(true);
	}

	/**
	 * Sets the Plugin configuration.
	 * 
	 * @param config
	 */
	private void setPluginConfig(PropertyLoader config) {
		this.name = config.getString("plugin.twitter.name");

		ic = new ESIndexCreator(config.getString("plugin.twitter.elasticsearch.index"), config.getString("plugin.twitter.elasticsearch.type"),
				config.getString("plugin.twitter.elasticsearch.pattern"));
	}

	/**
	 * Sets the Filter settings for the Query.
	 * 
	 * @param config
	 */
	private void setFilterSettings(PropertyLoader config) {
		String[] minCoordinate = config.getStringArray("plugin.twitter.filter.coord_min");
		String[] maxCoordinate = config.getStringArray("plugin.twitter.filter.coord_max");

		String[] track = config.getStringArray("plugin.twitter.filter.track");
		String[] language = config.getStringArray("plugin.twitter.filter.language");

		if (minCoordinate.length == 2 && maxCoordinate.length == 2) {
			double[][] locations = { { Double.parseDouble(minCoordinate[0]), Double.parseDouble(minCoordinate[1]) },
					{ Double.parseDouble(maxCoordinate[0]), Double.parseDouble(maxCoordinate[1]) } };

			logger.info("Location filter: " + Arrays.toString(locations[0]) + " - " + Arrays.toString(locations[1]));
			filterQuery.locations(locations);
		}

		if (track[0].length() != 0) {

			logger.info("Track filter: " + Arrays.toString(track));
			filterQuery.track(track);
		}

		if (language[0].length() != 0) {

			logger.info("Language filter: " + Arrays.toString(language));
			filterQuery.language(language);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Thread
	public void run() {
		if (startStream) {
			twitterStream = new TwitterStreamFactory(configBuilder.build()).getInstance();
			twitterStream.addListener(this);
			twitterStream.filter(filterQuery);
		}
	}

	@Override
	public void onStatus(Status status) {
		try {
			publish(createEvent(status));
		} catch (UbicityBrokerException e) {
			logger.error("UbicityBroker threw exc: " + e.getBrokerMessage());
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		;
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		logger.warn("Got track limitation notice: " + numberOfLimitedStatuses);
	}

	@Override
	public void onException(Exception ex) {
		logger.warn("Got an unspecified exception: ", ex);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		logger.warn("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		;
	}

	private EventEntry createEvent(Status status) {
		HashMap<Property, String> header = new HashMap<Property, String>();
		header.put(Property.PLUGIN_CHAIN, EventEntry.formatPluginChain(Arrays.asList(pluginDest)));
		header.put(Property.ES_INDEX, ic.getIndex());
		header.put(Property.ES_TYPE, ic.getType());
		header.put(Property.ID, this.name + "-" + UUID.randomUUID().toString());

		TwitterDTO dto = new TwitterDTO(String.valueOf(status.getId()), status.getCreatedAt());
		User usr = status.getUser();
		dto.setUser(String.valueOf(usr.getId()), usr.getName(), usr.getScreenName(), usr.getLocation(), usr.getLang(), usr.getFollowersCount(),
				usr.getFriendsCount());
		dto.setMessage(status.getText(), status.getLang(), calcHashTags(status.getHashtagEntities()), calcMentionedUsers(status.getUserMentionEntities()));

		String replyTo = status.getInReplyToStatusId() > 0 ? String.valueOf(status.getInReplyToStatusId()) : null;
		boolean retweeted = status.getRetweetedStatus() != null;
		String retweetOrigin = retweeted ? String.valueOf(status.getRetweetedStatus().getId()) : null;
		dto.setMetaData(retweeted, retweetOrigin, replyTo);

		if (status.getPlace() != null) {
			Place pl = status.getPlace();
			dto.setPlace(pl.getCountry(), pl.getCountryCode(), pl.getName());
		}

		if (status.getGeoLocation() != null) {
			dto.setGeo(status.getGeoLocation().getLongitude(), status.getGeoLocation().getLatitude());
		}

		return new EventEntry(header, dto.toJson());
	}

	private List<String> calcHashTags(twitter4j.HashtagEntity[] entities) {

		List<String> hashes = new ArrayList<String>();

		if (entities != null) {
			for (HashtagEntity e : entities) {
				hashes.add(e.getText());
			}
		}
		return hashes;
	}

	private List<TwitterUserDTO> calcMentionedUsers(UserMentionEntity[] userMentionEntities) {

		List<TwitterUserDTO> hashes = new ArrayList<TwitterUserDTO>();

		if (userMentionEntities != null) {
			for (UserMentionEntity e : userMentionEntities) {
				hashes.add(new TwitterUserDTO(String.valueOf(e.getId()), e.getName(), e.getScreenName()));
			}
		}

		return hashes;
	}

	@Override
	@Shutdown
	public void shutdown() {
		if (startStream) {
			twitterStream.clearListeners();
			twitterStream.cleanUp();
			twitterStream.shutdown();

			shutdown(pluginDest);
		}
	}
}