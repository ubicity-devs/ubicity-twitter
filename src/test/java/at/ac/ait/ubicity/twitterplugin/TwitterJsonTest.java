package at.ac.ait.ubicity.twitterplugin;

import static org.junit.Assert.assertTrue;

import java.util.Date;

import org.easymock.EasyMock;
import org.junit.Ignore;
import org.junit.Test;

import twitter4j.GeoLocation;
import twitter4j.Place;
import twitter4j.Status;
import twitter4j.User;
import at.ac.ait.ubicity.twitterplugin.dto.GeoTweet;

import com.google.gson.Gson;

public class TwitterJsonTest {

	@Test
	@Ignore
	public void testClassToJson() {

		Status stat = EasyMock.createNiceMock(Status.class);
		EasyMock.expect(stat.getCreatedAt()).andReturn(new Date());
		EasyMock.expect(stat.getId()).andReturn(1234L);

		User usr = EasyMock.createNiceMock(User.class);
		EasyMock.expect(usr.getName()).andReturn("twittrUsr");
		EasyMock.replay(usr);
		EasyMock.expect(stat.getUser()).andReturn(usr);
		EasyMock.expect(stat.getText()).andReturn("Twitter text");
		EasyMock.expect(stat.getLang()).andReturn("de");
		EasyMock.expect(stat.getGeoLocation()).andReturn(
				new GeoLocation(180, 90));
		EasyMock.expect(stat.getHashtagEntities()).andReturn(null);

		Place place = EasyMock.createNiceMock(Place.class);
		EasyMock.expect(place.getCountry()).andReturn("Österreich");
		EasyMock.expect(place.getCountryCode()).andReturn("AT");
		EasyMock.replay(place);

		EasyMock.expect(stat.getPlace()).andReturn(place).anyTimes();
		EasyMock.replay(stat);

		Gson gson = new Gson();
		String rawJSON = gson.toJson(new GeoTweet(stat));

		System.out.println(rawJSON);

		assertTrue(rawJSON.contains("twittrUsr"));
		assertTrue(rawJSON.contains("180.0,90.0"));
	}
}
