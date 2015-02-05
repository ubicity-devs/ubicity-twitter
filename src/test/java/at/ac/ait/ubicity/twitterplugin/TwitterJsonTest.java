package at.ac.ait.ubicity.twitterplugin;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.Gson;

public class TwitterJsonTest {

	private static Gson gson = new Gson();

	@Ignore
	@Test
	public void testClassToJson() {

	}

	@Ignore
	@Test
	public void testDailyIndex() {

		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
		System.out.println(df.format(new Date()));
	}
}
