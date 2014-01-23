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
import at.ac.ait.ubicity.core.Core;
import at.ac.ait.ubicity.core.JSONProducer;
import at.ac.ait.ubicity.core.PluginContext;
import at.ac.ait.ubicity.core.interfaces.UbicityPlugin;
import at.ac.ait.ubicity.twitterplugin.Streamer;
import static at.ac.ait.ubicity.twitterplugin.impl.StreamerImpl.msgsRetrieved;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.xeoh.plugins.base.annotations.PluginImplementation;
import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.json.JSONObject;
import twitter4j.FilterQuery;
import twitter4j.HashtagEntity;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.conf.ConfigurationBuilder;
import twitter4j.json.DataObjectFactory;


@PluginImplementation
public class StreamerImpl implements Streamer  {

/**
 * 
 * @author Hermann Huber for the original implementation as a stand-alone app
 * @author Jan van Oort for optimizations & port to a ubicity plugin structure
 * @version 0.1
 * @see UbicityPlugin
 *
 */
    
    
    protected static int msgsRetrieved = 0;

    protected TwitterStream twitterStream = null;
    
    private JSONObject[] bulkArray = new JSONObject[ 100 ]; 
    
    
    protected boolean coreDemandedStop = false;
    
    public final static String NAME = "twitter streamer plugin for ubicity";
    
    private final int cachedHash;
    
    public static int instanceCount = 0;

    private PluginContext context; 
    
    
    //cache the Core instance, in order to spare us many thousands ( or millions ) of calls to Core#getInstance()
    Core core;
    
    
    private static Configuration config;
    
    
    private final static Logger logger = Logger.getLogger( StreamerImpl.class.getName() );
    
    
    static  {
        logger.setLevel( Level.ALL );
        try {
            config = new PropertiesConfiguration( "plugin.cfg" );
        }
        catch( ConfigurationException noConfig )    {
            logger.severe( StreamerImpl.class.getName() + " :: found no config, file plugin.cfg not found or other configuration problem" );
        }
        
    }
    
        

    public StreamerImpl()   {
        instanceCount++;
        cachedHash = doHash( instanceCount );
        core = Core.getInstance();
        core.register( this );
        logger.info( NAME + " registered with ubicity core " );
        Thread t = new Thread( this );
        t.setName( "execution context for " + NAME );
        t.start();
        
    }
		

    @Override
    public void mustStop() {
        coreDemandedStop = true;
        twitterStream.cleanUp();
        twitterStream.shutdown();
        //a rather brute-ish way to go about stopping... 
        Thread.currentThread().stop();
        
    }

    
    @Override
    public String getName() {
        return NAME;
    }

    public void run() {
	    
            ConfigurationBuilder cb = new ConfigurationBuilder();
            cb.setOAuthConsumerKey( config.getString( "OAUTHCONSUMERKEY" ) );
            cb.setOAuthConsumerSecret( config.getString( "OAUTHCONSUMERSECRET") );
            cb.setOAuthAccessToken( config.getString( "OAUTHACCESSTOKEN" ) );
            cb.setOAuthAccessTokenSecret( config.getString( "OAUTHACCESSTOKENSECRET" ) );
            cb.setJSONStoreEnabled( true );
            cb.setJSONStoreEnabled( true );
	    
	    twitterStream = new TwitterStreamFactory( cb.build() ).getInstance();
	    twitterStream.addListener( this );
            double[][] locations = { {-179.9999, -89.9999 }, { 179.9999, 89.9999 } };
	    
	    FilterQuery query = new FilterQuery();
	    query.locations(locations);
	    twitterStream.filter(query);
            
	}
    
    
    
        @Override
        public void onStatus(Status status) {
                
                int _tweetIndex = msgsRetrieved % 100 ;
                
                
                if( ! ( status.getGeoLocation() == null ) ) {
                    String __text  = status.getText();
                    String __user = status.getUser().getName();
                    HashtagEntity[]  __hashTags = status.getHashtagEntities();
                    String __hash = null;
                    if( __hashTags != null ) {
                        try {
                            __hash = __hashTags[ 0 ].getText();
                        }
                        catch( ArrayIndexOutOfBoundsException nothingThere )    {
                            __hash = null;
                        }
                    }
                    System.out.println( "[ TRACE ] [ " + msgsRetrieved + " ]  @" + __user + " : " + ( __hash != null ? ( "#"+ __hash ) : "" ) + " : " + __text );  
                    String rawJSON = DataObjectFactory.getRawJSON( status );
                    bulkArray[ _tweetIndex ] = new JSONObject( rawJSON );
                    if( _tweetIndex == 99 ) {
                        long _start, _lapse;
                        _start = System.nanoTime();
                        core.offerBulk( bulkArray, context );
                        _lapse = System.nanoTime() - _start;
                        System.out.println( _lapse );                        
                    }
                    msgsRetrieved++;
                    //String __text = status.getText();
                    //String __author = status.getUser().getName();
                    //System.out.println( "[ TRACE ] [ " + msgsRetrieved + " ]   @" + __author + "  " +  __text );
                }
        }

        @Override
        public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
            logger.warning( "Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
        }

        @Override
        public void onTrackLimitationNotice(int numberOfLimitedStatuses) {
            logger.warning( "Got track limitation notice:" + numberOfLimitedStatuses);
        }

        @Override
        public void onException(Exception ex) {
            logger.severe( "got an unspecified exception : "  + ex.toString() );
            ex.printStackTrace();
        }

        @Override
        public void onScrubGeo(long userId, long upToStatusId) {
            logger.warning( "[ WARN ] Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
        }

        
        @Override
        public void onStallWarning(StallWarning warning) {
            logger.warning( "Got a stall warnning : " + warning.toString() );
            
        }

        
        @Override
        public final int hashCode() {
            return cachedHash;
        }
        
        
        @Override
        public final boolean equals( Object o ) {
            if( o == null ) return false;
            if( ! ( o instanceof StreamerImpl ) ) return false;
            StreamerImpl other = ( StreamerImpl ) o;
            return ( other.cachedHash == this.cachedHash );
        }
        
        
        private final int doHash( int _instanceCount )  {
            return ( new StringBuilder().append( _instanceCount ).append( " :: " ).append( NAME ) ).toString().hashCode();
        }
        
        
        public final static void main( String... args ) {
            StreamerImpl streamer = new StreamerImpl();
        }

        
        @Override
    public void setContext(PluginContext _context) {
        context = _context;
    }

    
        
    @Override
    public PluginContext getContext() {
        return context;
    }
}
 

  
	
