package org.dubh.slurptv.easynews;

public class NoBiggerThanFilter implements ResultFilter {
  private long maxByteSize;

  public NoBiggerThanFilter(long maxByteSize) {
    this.maxByteSize = maxByteSize;
  }
  
  @Override
  public boolean apply(Result result) {
    return result.getSizeInBytes() <= maxByteSize;
  }
  
  
}
