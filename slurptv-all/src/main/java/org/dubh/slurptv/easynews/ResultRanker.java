package org.dubh.slurptv.easynews;

/**
 * Ranks a result.
 * 
 * @author brianduff
 */
public interface ResultRanker {
  long scoreResult(Result result);
}
