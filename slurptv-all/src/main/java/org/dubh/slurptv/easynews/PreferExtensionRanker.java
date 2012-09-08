package org.dubh.slurptv.easynews;

public class PreferExtensionRanker implements ResultRanker {
  private String extension;

  public PreferExtensionRanker(String extension) {
    this.extension = extension;
  }

  @Override
  public long scoreResult(Result result) {
    if (result.getUrl().endsWith(extension)) {
      return 1000;
    }
    return 0;
  }
}
