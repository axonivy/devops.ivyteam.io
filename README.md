# DevOps @ ivyTeam

Service to manage GitHub repository and provider of a single point of entry
for build automation.

## Development

- Add a file `github.token` with an own GitHub token so that the
service can discover all the repositories at GitHub.
- Run `mvn spring-boot:run` to start the website on local machine.

This service will synchronize data from GitHub and store it in
`github.db`. By deleting this file the cache will be re-newed on
the next site reload.

