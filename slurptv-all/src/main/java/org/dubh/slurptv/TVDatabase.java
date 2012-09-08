package org.dubh.slurptv;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.dubh.easynews.slurptv.Data.Episode;
import org.dubh.easynews.slurptv.Data.EpisodeDetails;
import org.dubh.easynews.slurptv.SlurpTv.Configuration;
import org.joda.time.DateTime;
import org.joda.time.format.ISODateTimeFormat;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Provider;

/**
 * Provides access to data from the TV database.
 * 
 * @author brianduff
 */
class TVDatabase {
  private final Provider<Configuration> configuration;
  private final Downloader downloader;
  private final LoadingCache<Integer, ImmutableSet<EpisodeDetails>> cache;
  private static final Logger log = Logger.getLogger(TVDatabase.class.getName());

  @Inject
  TVDatabase(Provider<Configuration> configuration, Downloader downloader) {
    this.configuration = configuration;
    this.downloader = downloader;
    
    // TODO(bduff) if configuration changes, reload cache to update the expiration.

    cache = CacheBuilder.newBuilder()
        .expireAfterWrite(configuration.get().getTvdbCache(), TimeUnit.HOURS)
        .build(new CacheLoader<Integer, ImmutableSet<EpisodeDetails>>() {
          @Override
          public ImmutableSet<EpisodeDetails> load(Integer id) throws Exception {
            return loadDetails(id);
          }
        });
  }

  public ImmutableSet<EpisodeDetails> getAllEpisodeDetails(int dbId) throws IOException,
      InterruptedException {
    try {
      return cache.get(dbId);
    } catch (ExecutionException e) {
      if (e.getCause() instanceof IOException) {
        throw (IOException) e.getCause();
      } else if (e.getCause() instanceof InterruptedException) {
        throw (InterruptedException) e.getCause();
      }
      throw new RuntimeException(e);
    }
  }

  public EpisodeDetails findEpisodeDetails(int dbId, Episode episode) throws IOException,
      InterruptedException {
    for (EpisodeDetails details : getAllEpisodeDetails(dbId)) {
      if (details.getEpisode().equals(episode)) {
        return details;
      }
    }
    return null;
  }

  private ImmutableSet<EpisodeDetails> loadDetails(Integer id) throws IOException,
      InterruptedException {
    log.info("Looking up TVDatabase information for show id " + id);

    // Try to load from filesystem (in case the process was restarted)
    File dbFile = new File(configuration.get().getTvdbDir(), id + ".zip");
    if (dbFile.isFile()
        && dbFile.lastModified() > System.currentTimeMillis()
            - TimeUnit.HOURS.toMillis(configuration.get().getTvdbCache())) {
      log.info("Reloading from file " + dbFile);
      return loadFromFile(dbFile);
    }

    // Otherwise, download an up to date file. We should really check whether
    // it's modified on the
    // server first.
    String url = "http://www.thetvdb.com/api/ED0DDA9B6560DD9E/series/" + id + "/all/en.zip";
    downloader.download(url, dbFile);
    return loadFromFile(dbFile);
  }

  private ImmutableSet<EpisodeDetails> loadFromFile(File file) throws IOException {
    ZipFile zipFile = new ZipFile(file);
    ImmutableSet.Builder<EpisodeDetails> result = ImmutableSet.builder();
    try {
      ZipEntry entry = zipFile.getEntry("en.xml");
      DocumentBuilder db = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document doc = db.parse(zipFile.getInputStream(entry));

      Node series = doc.getElementsByTagName("Series").item(0);
      Map<String, String> showProperties = childNodeValues(series);

      NodeList episodes = doc.getElementsByTagName("Episode");
      for (int i = 0; i < episodes.getLength(); i++) {
        Node episode = episodes.item(i);
        Map<String, String> episodeProperties = childNodeValues(episode);

        String airDate = episodeProperties.get("FirstAired") + "T00:00:00.000Z";
        EpisodeDetails.Builder details = EpisodeDetails
            .newBuilder()
            .setDescription(episodeProperties.get("Overview"))
            .setEpisode(
                Episode.newBuilder()
                    .setEpisode(Integer.parseInt(episodeProperties.get("EpisodeNumber")))
                    .setSeason(Integer.parseInt(episodeProperties.get("SeasonNumber"))))
            .setEpisodeName(episodeProperties.get("EpisodeName"))
            .setGenre(showProperties.get("Genre")).setNetwork(showProperties.get("Network"))
            .setShowName(showProperties.get("SeriesName"))
            .setArtworkUrl("http://thetvdb.com/banners/" + episodeProperties.get("filename"))
            .setAirDate(airDate);
        if (!Strings.isNullOrEmpty(episodeProperties.get("FirstAired"))) {
          try {
            DateTime airDateTime = ISODateTimeFormat.dateTime().parseDateTime(airDate);
            details.setAirDateMillis(airDateTime.getMillis());
          } catch (IllegalArgumentException e) {
            log.severe("Bad date from tv database: " + airDate);
          }
        }

        result.add(details.build());
      }
      return result.build();
    } catch (ParserConfigurationException e) {
      throw new IOException(e);
    } catch (SAXException e) {
      throw new IOException(e);
    } finally {
      zipFile.close();
    }
  }

  private Map<String, String> childNodeValues(Node node) {
    Map<String, String> map = new HashMap<String, String>();
    NodeList children = node.getChildNodes();
    for (int i = 0; i < children.getLength(); i++) {
      Node child = children.item(i);
      map.put(child.getNodeName(), child.getTextContent());
    }
    return map;
  }
}
