package org.dubh.slurptv.easynews;

public class Result {
  private final String url;
  private final String size;
  private final String title;
  
  public Result(String url, String size, String title) {
    this.url = url;
    this.size = size;
    this.title = title;
  }
  
  public String getTitle() {
  	return title;
  }
  
  public String getUrl() {
    return url;
  }
  
  public String getSize() {
    return size;
  }
  
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((size == null) ? 0 : size.hashCode());
    result = prime * result + ((url == null) ? 0 : url.hashCode());
    return result;
  }

  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Result other = (Result) obj;
    if (size == null) {
      if (other.size != null)
        return false;
    } else if (!size.equals(other.size))
      return false;
    if (url == null) {
      if (other.url != null)
        return false;
    } else if (!url.equals(other.url))
      return false;
    return true;
  }

  public long getSizeInBytes() {
    if (size.endsWith(" GB")) {
      return (long) (Double.parseDouble(size.substring(0, size.length() - 3)) * 1024.0 * 1024.0 * 1024.0);
    } else if (size.endsWith(" MB")) {
      return (long) (Double.parseDouble(size.substring(0, size.length() - 3)) * 1024.0 * 1024.0);      
    } else if (size.endsWith(" K")) {
      return (long) (Double.parseDouble(size.substring(0, size.length() - 2)) * 1024.0);      
    } else {
      return 0; // bug bug bug.
    }
  }
}
