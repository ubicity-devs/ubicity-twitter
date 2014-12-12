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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import net.xeoh.plugins.base.annotations.PluginImplementation;
import net.xeoh.plugins.base.annotations.Thread;
import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;

import twitter4j.FilterQuery;
import twitter4j.Place;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import at.ac.ait.ubicity.commons.broker.BrokerProducer;
import at.ac.ait.ubicity.commons.broker.events.EventEntry;
import at.ac.ait.ubicity.commons.broker.events.EventEntry.Property;
import at.ac.ait.ubicity.commons.broker.exceptions.UbicityBrokerException;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.contracts.twitter.TwitterDTO;
import at.ac.ait.ubicity.twitterplugin.TwitterStreamer;

@PluginImplementation
public class TwitterStreamerImpl extends BrokerProducer implements
		TwitterStreamer {

	private final ConfigurationBuilder configBuilder = new ConfigurationBuilder();
	protected TwitterStream twitterStream = null;
	private final FilterQuery filterQuery = new FilterQuery();

	private static SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

	private String name;
	private String esIndex;
	private String esType;

	private final static Logger logger = Logger
			.getLogger(TwitterStreamerImpl.class);

	@Override
	@Init
	public void init() {
		PropertyLoader config = new PropertyLoader(
				TwitterStreamerImpl.class.getResource("/twitter.cfg"));

		setProducerSettings(config);
		setPluginConfig(config);
		setOAuthSettings(config);
		setFilterSettings(config);

		logger.info(name + " loaded");
	}

	/**
	 * Sets the Apollo broker settings
	 * 
	 * @param config
	 */
	private void setProducerSettings(PropertyLoader config) {
		try {
			super.init(config.getString("plugin.twitter.broker.user"),
					config.getString("plugin.twitter.broker.pwd"));
			setProducer(config.getString("plugin.twitter.broker.dest"));

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
		configBuilder.setOAuthConsumerKey(config
				.getString("plugin.twitter.oauth_consumer_key"));
		configBuilder.setOAuthConsumerSecret(config
				.getString("plugin.twitter.oauth_consumer_secret"));
		configBuilder.setOAuthAccessToken(config
				.getString("plugin.twitter.oauth_access_token"));
		configBuilder.setOAuthAccessTokenSecret(config
				.getString("plugin.twitter.oauth_access_token_secret"));

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
				publish(createEvent(status));
			} catch (UbicityBrokerException e) {
				logger.error("UbicityBroker threw exc." + e.getBrokerMessage());
			}
		}
	}

	@Override
	public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
		;
	}

	@Override
	public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
		logger.warn("Got track limitation notice:" + numberOfLimitedStatuses);
	}

	@Override
	public void onException(Exception ex) {
		logger.warn("Got an unspecified exception: ", ex);
	}

	@Override
	public void onScrubGeo(long userId, long upToStatusId) {
		logger.warn("Got scrub_geo event userId:" + userId + " upToStatusId:"
				+ upToStatusId);
	}

	@Override
	public void onStallWarning(StallWarning warning) {
		;
	}

	private EventEntry createEvent(Status status) {
		HashMap<Property, String> header = new HashMap<Property, String>();
		header.put(Property.ES_INDEX, calculateDailyIndexName());
		header.put(Property.ES_TYPE, esType);
		header.put(Property.ID, this.name + "-" + UUID.randomUUID().toString());

		TwitterDTO dto = new TwitterDTO(String.valueOf(status.getId()),
				status.getCreatedAt(),
				String.valueOf(status.getUser().getId()), status.getUser()
						.getName());

		dto.setMessage(status.getText(), status.getLang(),
				calcHashTags(status.getHashtagEntities()));

		if (status.getPlace() != null) {
			Place pl = status.getPlace();
			dto.setPlace(status.getGeoLocation().getLongitude(), status
					.getGeoLocation().getLatitude(), pl.getCountry(), pl
					.getCountryCode(), pl.getName());
		}
		return new EventEntry(header, dto.toJson());
	}

	private String calculateDailyIndexName() {
		return this.esIndex + "-" + df.format(new Date());
	}

	private List<String> calcHashTags(twitter4j.HashtagEntity[] entities) {

		List<String> hashes = new ArrayList<String>();

		if (entities != null) {
			for (int i = 0; i < entities.length; i++) {
				hashes.add(entities[i].getText());
			}
		}

		return hashes;
	}

	@Override
	@Shutdown
	public void shutdown() {
		twitterStream.clearListeners();
		twitterStream.cleanUp();
		twitterStream.shutdown();
	}
}