<html>
  <h1>Shows</h1>
  <ul>
    <#list shows as show>
      <li><a href="/show/${show.id}">${show.name}</a>
    </#list>
  </ul>
</html>