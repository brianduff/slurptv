package org.dubh.slurptv.easynews;

/**
 * Rank so that files with bigger sizes are preferred.
 * 
 * @author brianduff
 */
public class FileSizeRanker implements ResultRanker {

  @Override
  public long scoreResult(Result result) {
    return result.getSizeInBytes() / 1000;
  }

}
