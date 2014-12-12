package at.ac.ait.ubicity.twitterplugin;

import static org.junit.Assert.assertTrue;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import at.ac.ait.ubicity.contracts.twitter.TwitterDTO;

import com.google.gson.Gson;

public class TwitterJsonTest {

	private static Gson gson = new Gson();

	@Ignore
	@Test
	public void testClassToJson() {

		TwitterDTO dto = new TwitterDTO("twitterId", new Date(), "userId",
				"userName");
		dto.setPlace(180, 90, "Österreich", "AT", "Vienna");
		dto.setMessage("My Message", "DE", null);

		String rawJSON = gson.toJson(dto);

		System.out.println(rawJSON);

		assertTrue(rawJSON.contains("userName"));
		assertTrue(rawJSON.contains("180.0,90.0"));
	}

	@Ignore
	@Test
	public void testDailyIndex() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(new Date()));
	}
}
