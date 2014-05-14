package at.ac.ait.ubicity.twitterplugin;

import static org.junit.Assert.assertTrue;

import org.json.JSONObject;
import org.junit.Test;

public class TwitterJsonTest {

	public class JsonTDO {
		private final String id;
		private final String name;

		public JsonTDO(String id, String name) {
			this.id = id;
			this.name = name;
		}

		public String getId() {
			return id;
		}

		public String getName() {
			return name;
		}
	}

	@Test
	public void testClassToJson() {

		String rawJSON = new JSONObject(new JsonTDO("id2343", "myname"))
				.toString();

		assertTrue(rawJSON.contains("id2343"));
		assertTrue(rawJSON.contains("myname"));
	}
}
