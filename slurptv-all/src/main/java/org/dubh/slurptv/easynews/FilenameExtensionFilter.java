package org.dubh.slurptv.easynews;

import java.util.Collection;

import com.google.common.collect.ImmutableSet;

public class FilenameExtensionFilter implements ResultFilter {
  private final ImmutableSet<String> extensions;

  public FilenameExtensionFilter(Collection<String> extensions) {
    this.extensions = ImmutableSet.copyOf(extensions);
  }

  @Override
  public boolean apply(Result result) {
    for (String extension : extensions) {
      if (result.getUrl().endsWith(extension)) {
        return true;
      }
    }
    return false;
  }
}
