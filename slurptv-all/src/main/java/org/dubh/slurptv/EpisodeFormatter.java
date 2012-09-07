package org.dubh.slurptv;

import org.dubh.easynews.slurptv.Data.Episode;

import com.google.common.base.Strings;

/**
 * Formats an episode for printing.
 */
public class EpisodeFormatter {
	public String format(Episode episode) {
		if (episode.hasDate()) {
			return pad(episode.getDate().getYear()) 
					+ "-" + pad(episode.getDate().getMonth())
					+ "-" + pad(episode.getDate().getDate());
		}
		return "S" + pad(episode.getSeason()) + "E" + pad(episode.getEpisode());
	}
	
	private static String pad(int s) {
		 return Strings.padStart(String.valueOf(s), 2, '0');
	}
}
