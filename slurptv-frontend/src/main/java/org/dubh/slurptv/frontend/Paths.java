package org.dubh.slurptv.frontend;

class Paths {
  private Paths() {
  }
  
  /**
   * Returns the last path segment in a path. Returns the empty string if
   * there is none (e.g. for these input strings "/foo/", "/", "").
   */
  static String getLastPathSegment(String fullPath) {
    int lastSlash = fullPath.lastIndexOf('/');
    if (lastSlash == -1 || lastSlash == fullPath.length() - 1) {
      return "";
    }
    return fullPath.substring(lastSlash + 1);
  }
}
