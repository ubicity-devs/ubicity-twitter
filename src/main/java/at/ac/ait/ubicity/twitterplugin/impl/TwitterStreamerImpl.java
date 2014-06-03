package at.ac.ait.ubicity.twitterplugin.impl;

/**
 Copyright (C) 2013  AIT / Austrian Institute of Technology
 http://www.ait.ac.at

 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Affero General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU Affero General Public License for more details.

 You should have received a copy of the GNU Affero General Public License
 along with this program.  If not, see http://www.gnu.org/licenses/agpl-3.0.html

 */
import java.util.HashMap;
import java.util.Random;
import java.util.UUID;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;
import net.xeoh.plugins.base.annotations.injections.InjectPlugin;

import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterObjectFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import at.ac.ait.ubicity.commons.broker.UbicityBroker;
import at.ac.ait.ubicity.commons.broker.events.ESMetadata;
import at.ac.ait.ubicity.commons.broker.events.ESMetadata.Action;
import at.ac.ait.ubicity.commons.broker.events.ESMetadata.Properties;
import at.ac.ait.ubicity.commons.broker.events.EventEntry;
import at.ac.ait.ubicity.commons.broker.events.Metadata;
import at.ac.ait.ubicity.commons.broker.exceptions.UbicityBrokerException;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.twitterplugin.TwitterStreamer;

@PluginImplementation
public class TwitterStreamerImpl implements TwitterStreamer {

	@InjectPlugin
	public static UbicityBroker broker;

	private int uniqueId;

	private final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
	protected TwitterStream twitterStream = null;
	private final FilterQuery filterQuery = new FilterQuery();

	private String name;
	private String esIndex;
	private String esType;

	private final static Logger logger = Logger
			.getLogger(TwitterStreamerImpl.class);

	@Override
	@Init
	public void init() {
		uniqueId = new Random().nextInt();
		PropertyLoader config = new PropertyLoader(
				TwitterStreamerImpl.class.getResource("/twitter.cfg"));

		setPluginConfig(config);
		setOAuthSettings(config);
		setFilterSettings(config);

		logger.info(name + " loaded");
	}

	/**
	 * Sets the OAuth credentials for twitter access
	 * 
	 * @param config
	 */
	private void setOAuthSettings(PropertyLoader config) {
		configBuilder.setOAuthConsumerKey(config
				.getString("plugin.twitter.oauth_consumer_key"));
		configBuilder.setOAuthConsumerSecret(config
				.getString("plugin.twitter.oauth_consumer_secret"));
		configBuilder.setOAuthAccessToken(config
				.getString("plugin.twitter.oauth_access_token"));
		configBuilder.setOAuthAccessTokenSecret(config
				.getString("plugin.twitter.oauth_access_token_secret"));
		configBuilder.setJSONStoreEnabled(true);
	}

	/**
	 * Sets the Plugin configuration.
	 * 
	 * @param config
	 */
	private void setPluginConfig(PropertyLoader config) {
		this.name = config.getString("plugin.twitter.name");
		this.esIndex = config.getString("plugin.twitter.elasticsearch.index");
		this.esType = config.getString("plugin.twitter.elasticsearch.type");
	}

	/**
	 * Sets the Filter settings for the Query.
	 * 
	 * @param config
	 */
	private void setFilterSettings(PropertyLoader config) {
		String[] minCoordinate = config
				.getStringArray("plugin.twitter.filter.coord_min");
		String[] maxCoordinate = config
				.getStringArray("plugin.twitter.filter.coord_max");

		String[] track = config.getStringArray("plugin.twitter.filter.track");
		String[] language = config
				.getStringArray("plugin.twitter.filter.language");

		if (minCoordinate.length == 2 && maxCoordinate.length == 2) {
			double[][] locations = {
					{ Double.parseDouble(minCoordinate[0]),
							Double.parseDouble(minCoordinate[1]) },
					{ Double.parseDouble(maxCoordinate[0]),
							Double.parseDouble(maxCoordinate[1]) } };
			filterQuery.locations(locations);
		} else {
			logger.info("Location filter ignored due to non existing coordinates.");
		}

		if (track[0].length() != 0) {
			filterQuery.track(track);
		}

		if (language[0].length() != 0) {
			filterQuery.language(language);
		}
	}

	@Override
	public String getName() {
		return name;
	}

	@Thread
	public void run() {
		twitterStream = new TwitterStreamFactory(configBuilder.build())
				.getInstance();
		twitterStream.addListener(this);
		twitterStream.filter(filterQuery);
	}

	@Override
	public void onStatus(Status status) {

		if (status.getGeoLocation() != null) {
			try {
				broker.publish(createEvent(status));
			} catch (UbicityBrokerException e) {
				logger.error("UbicityBroker threw exc." + e.getBrokerMessage());
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		logger.warn("Got a status deletion notice id:"
				+ statusDeletionNotice.getStatusId());
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		logger.warn("Got track limitation notice:" + numberOfLimitedStatuses);
	}

	@Override
	public void onException(Exception ex) {
		logger.error("Got an unspecified exception : " + ex);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		logger.warn("Got scrub_geo event userId:" + userId + " upToStatusId:"
				+ upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		logger.warn("Got a stall warnning : " + warning.toString());

	}

	@Override
	public final int hashCode() {
		return uniqueId;
	}

	@Override
	public final boolean equals(Object o) {

		if (TwitterStreamerImpl.class.isInstance(o)) {
			TwitterStreamerImpl other = (TwitterStreamerImpl) o;
			return other.uniqueId == this.uniqueId;
		}
		return false;
	}

	private EventEntry createEvent(Status status) {

		HashMap<Properties, String> props = new HashMap<ESMetadata.Properties, String>();
		props.put(Properties.ES_INDEX, esIndex);
		props.put(Properties.ES_TYPE, esType);
		Metadata meta = new ESMetadata(Action.INDEX, 1, props);

		String id = this.name + "-" + UUID.randomUUID().toString();

		return new EventEntry(id, meta, TwitterObjectFactory.getRawJSON(status));
	}

	@Override
	@Shutdown
	public void shutdown() {
		twitterStream.cleanUp();
		twitterStream.shutdown();
	}
}
