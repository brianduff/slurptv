package org.dubh.slurptv.easynews;

public class AtLeastSizeFilter implements ResultFilter {
  private final long minSizeInBytes;

  public AtLeastSizeFilter(long minSizeInBytes) {
    this.minSizeInBytes = minSizeInBytes;
  }

  @Override
  public boolean apply(Result result) {
    return result.getSizeInBytes() >= minSizeInBytes;
  }
}
