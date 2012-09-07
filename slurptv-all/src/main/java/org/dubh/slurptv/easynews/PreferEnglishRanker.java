package org.dubh.slurptv.easynews;

import com.google.common.collect.ImmutableSet;

/**
 * De-rank (rather than omit) foreign language stuff. We derank because this is quite
 * fuzzy and we don't want to completely exclude episodes (e.g. if the title is "The Submarine")
 */
public class PreferEnglishRanker implements ResultRanker {
	// TODO(bduff) move to configuration.
	private static final ImmutableSet<String> bannedWords = ImmutableSet.of(
			"sub", "dubbed", "german", "french");
	
  @Override
  public long scoreResult(Result result) {
  	String lowerCaseUrl = result.getUrl().toLowerCase();
  	String lowerCaseTitle = result.getTitle().toLowerCase();
  	for (String word : bannedWords) {
  		if (lowerCaseUrl.contains(word) || lowerCaseTitle.contains(word)) {
  			return -5000;
  		}
  	}
    return 0;
  }

}
