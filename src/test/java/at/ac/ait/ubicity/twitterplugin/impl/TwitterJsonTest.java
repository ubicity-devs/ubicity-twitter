package at.ac.ait.ubicity.twitterplugin.impl;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import at.ac.ait.ubicity.twitterplugin.dto.TwitterDTO;

public class TwitterJsonTest {

	@Ignore
	@Test
	public void testClassToJson() {

		TwitterDTO dto = new TwitterDTO("123", new Date());

		System.out.println(dto.toJson());

	}

	@Ignore
	@Test
	public void testDailyIndex() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(new Date()));
	}
}
