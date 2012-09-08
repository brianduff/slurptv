<html>
  <title>${show.name}</title>
  <body>
    <h1>${show.name}</h1>
    <p>
    <#if show.seasonal>
    ${show.name} is a seasonal show. SlurpTV is configured to download episodes starting from 
    season ${show.oldestSeason} until season ${show.maxSeason}.
    <#else>
    ${show.name} is a never-ending (non-seasonal) show. SlurpTV will check for new episodes
    daily.
    </#if>
    </p>
    <#if show.paused>
      <p>
      <b>This show is paused. No new episodes will be downloaded until it is unpaused.</b>
      </p>
    </#if>
    <ul>
      <#list episodeIds as episodeId>
        <li>${episodeId}</li>        
      </#list>
    </ul>
  </body>
</html>