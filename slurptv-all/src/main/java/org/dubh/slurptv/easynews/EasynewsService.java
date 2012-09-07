package org.dubh.slurptv.easynews;

import java.io.IOException;
import java.net.Authenticator;
import java.net.HttpURLConnection;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.dubh.easynews.slurptv.SlurpTv.Credentials;
import org.dubh.slurptv.ConfigurationModule.Easynews;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.google.common.base.Charsets;
import com.google.inject.Inject;

/**
 * Provides access to easynews.
 * 
 * @author brianduff
 */
public class EasynewsService {
  private static final Logger log = Logger.getLogger(EasynewsService.class.getName());
  private final Credentials credentials;
  
  @Inject
  public EasynewsService(@Easynews Credentials credentials) {
  	this.credentials = credentials;
  }
    
  public List<Result> findFiles(Query query, Collection<ResultFilter> filters, Collection<ResultRanker> rankers) throws IOException {
    StringBuilder params = new StringBuilder("http://members.easynews.com/global4/search.html?");
    params.append("gps=" + URLEncoder.encode(query.getSearch(), Charsets.UTF_8.name()));
    params.append("&pby=9999");
    params.append("&sS=5"); // RSS
    log.info("Querying easynews " + params.toString());
    
    Authenticator.setDefault(new Authenticator() {
      @Override
      protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(credentials.getUsername(), credentials.getPassword().toCharArray());
      }
    });
    HttpURLConnection conn = null;
    try {
      HttpURLConnection.setFollowRedirects(false);
      URL url = new URL(params.toString());
      conn = (HttpURLConnection) url.openConnection();
      conn.setConnectTimeout(30000);
      conn.setReadTimeout(120000);
      
      DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
      dbf.setNamespaceAware(true);
      DocumentBuilder db = dbf.newDocumentBuilder();
      Document doc = db.parse(conn.getInputStream(), conn.getContentEncoding());
      
      Set<Result> results = new HashSet<Result>();
      // I fucking hate XML. We want every rss/channel/item/enclosure.
      List<Node> channels = taggedChildren(doc.getDocumentElement(), "channel");
      List<Node> items = taggedChildren(channels.get(0), "item");
      for (Node item : items) {
      	String linkUrl = "";
      	String sizeText = "";
      	String title = "";
        NodeList properties = item.getChildNodes();
        for (int j=0; j < properties.getLength(); j++) {
          Node prop = properties.item(j);
          if ("enclosure".equals(prop.getLocalName())) {
            linkUrl = prop.getAttributes().getNamedItem("url").getNodeValue();
            sizeText = prop.getAttributes().getNamedItem("length").getNodeValue();
          } else if ("title".equals(prop.getLocalName())) {
          	title = prop.getTextContent();
          }
        }
        results.add(new Result(linkUrl, sizeText, title));
      }
      
      return rank(rankers, filter(filters, results));

    } catch (Exception e) {
      throw new IOException(e);
    } finally {
      if (conn != null) conn.disconnect();
    }
  }
  
  private Set<Result> filter(Collection<ResultFilter> filters, Set<Result> all) {
    if (filters.isEmpty()) {
      return all;
    }
    Set<Result> filtered = new HashSet<Result>();
    for (Result result : all) {
      boolean matched = true;
      for (ResultFilter filter : filters) {
        if (!filter.apply(result)) {
          matched = false;
          break;
        }
      }
      if (matched) {
        filtered.add(result);
      }
    }
    return filtered;
  }
  
  private List<Result> rank(Collection<ResultRanker> rankers, Set<Result> results) {
    List<ResultAndScore> list = new ArrayList<ResultAndScore>();
    for (Result result : results) {
      ResultAndScore ras = new ResultAndScore();
      ras.result = result;
      ras.score = 0;
      for (ResultRanker ranker : rankers) {
        ras.score += ranker.scoreResult(result);
      }
      list.add(ras);
    }
    
    Collections.sort(list);
    Collections.reverse(list);
    
    List<Result> onlyResults = new ArrayList<Result>();
    for (ResultAndScore ras : list) {
      onlyResults.add(ras.result);
    }
    return onlyResults;
  }
  
  private List<Node> taggedChildren(Node parent, String tag) {
    List<Node> result = new ArrayList<Node>();
    NodeList allKids = parent.getChildNodes();
    for (int i=0; i < allKids.getLength(); i++) {
      Node node = allKids.item(i);
      if (tag.equals(node.getLocalName()) || tag.equals(node.getNodeName())) {
        result.add(node);
      }
    }
    return result;
  }
  
  private class ResultAndScore implements Comparable<ResultAndScore> {
    private Result result;
    private int score = 0;
    
    @Override
    public String toString() {
      return result.getUrl() + " - " + score;
    }
    
    @Override
    public int compareTo(ResultAndScore other) {
      return Integer.valueOf(score).compareTo(Integer.valueOf(other.score));
    }
  }
  /*
  public static void main(String[] args) throws Exception {
    EasynewsService service = new EasynewsService(Credentials.newBuilder()
    		.setUsername("dubh").setPassword("gilopa57").build());

    // Eastenders
    service.addResultFilter(new AtLeastSizeFilter(100000000L)); // at least ~100MB
    service.addResultFilter(new NoBiggerThanFilter(2000000000L));
    service.addResultFilter(new FilenameExtensionFilter("mkv", "avi", "mp4"));
    service.addRanker(new PreferExtensionRanker("mkv"));
    service.addRanker(new PreferNoSubsRanker());
    
    DateTime today = new DateTime();
    DateTime current = today.minusMonths(1);
    while (current.isBefore(today)) {
    	System.out.println("== " + DateTimeFormat.forPattern("yyyy-MM-dd").print(current));
    	for (Result result : service.findFiles(new Query().setDate(current).setShowTitle("Coronation"))) {
    		System.out.println(result.getUrl() + " - " + result.getSize());
    	}
    	current = current.plusDays(1);
    }
    
    // Dexter
    /*
    service.addResultFilter(new AtLeastSizeFilter(500000000L)); // at least ~500MB.
    service.addResultFilter(new NoBiggerThanFilter(2000000000L)); // no bigger than 2GB.
    service.addResultFilter(new FilenameExtensionFilter("mkv", "avi", "mp4"));
    service.addRanker(new PreferExtensionRanker("mkv"));
    service.addRanker(new PreferNoSubsRanker());
    
    for (int i=1; i < 13; i++) {
      System.out.println("==== Episode " + i);
      for (Result result : service.findFiles(new Query().setEpisode(i).setSeason(5).setShowTitle("Dexter"))) {
        System.out.println(result.getUrl() + " - " + result.getSizeInBytes() + " - " + result.getSize());
      }
    }
    
  }*/
  
}
