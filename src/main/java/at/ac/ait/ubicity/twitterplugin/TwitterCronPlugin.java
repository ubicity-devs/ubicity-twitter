package at.ac.ait.ubicity.twitterplugin;

import java.util.ArrayList;
import java.util.List;

import net.xeoh.plugins.base.annotations.events.Init;
import net.xeoh.plugins.base.annotations.events.Shutdown;

import org.apache.log4j.Logger;

import at.ac.ait.ubicity.commons.cron.AbstractCronPlugin;
import at.ac.ait.ubicity.commons.interfaces.CronTask;
import at.ac.ait.ubicity.commons.interfaces.UbicityPlugin;
import at.ac.ait.ubicity.commons.util.PropertyLoader;
import at.ac.ait.ubicity.twitterplugin.impl.TwitterPostTask;

//@PluginImplementation
public class TwitterCronPlugin extends AbstractCronPlugin implements UbicityPlugin {

	private String name;

	protected static Logger logger = Logger.getLogger(TwitterCronPlugin.class);

	private static PropertyLoader config = new PropertyLoader(TwitterCronPlugin.class.getResource("/twitter_post.cfg"));

	private final List<CronTask> tasks = new ArrayList<CronTask>();

	@Override
	@Init
	public void init() {
		name = config.getString("plugin.twitter_post.name");
		setReactaTasks(config);
		logger.info(name + " loaded");
	}

	private void setReactaTasks(PropertyLoader config) {

		try {
			TwitterPostTask tuTask = new TwitterPostTask();
			tuTask.setTimeInterval(config.getString("plugin.twitter_post.cron.interval.post"));
			tuTask.setName("twitter-post-task");
			tasks.add(tuTask);

			initCron(tasks);
		} catch (Exception e) {
			logger.error("Task creation threw error", e);
		}
	}

	@Override
	@Shutdown
	public void shutdown() {
		super.shutdown();
	}

	@Override
	public String getName() {
		return this.name;
	}
}
