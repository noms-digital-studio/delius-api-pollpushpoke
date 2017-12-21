# delius-api-pollpushpoke

Self-contained fat-jar micro-service to poll a source API for delius API jobs, push to a target API, and POST back to the job scheduler
It is a pretty horrid tactical solution to problems of network access. It is also a POC.


### Building and running

Prerequisites:
- sbt (Scala Build Tool) http://www.scala-sbt.org/release/docs

Build commands:

- Build and run tests `sbt test`
- Run locally `sbt run`
- Build deployable pollPush.jar `sbt assembly`

Running deployable fat jar:
- `java -jar icbind-0.1.jar`

Configuration parameters can be supplied via environment variables, e.g.:
- `POLL_SECONDS=10 sbt run`
- `POLL_SECONDS=10 java -jar icbind-0.1.jar`

### Development notes

Developed in [Scala 2.12](http://www.scala-lang.org/news/2.12.0), using [Akka HTTP](http://doc.akka.io/docs/akka-http/current/scala/http/) for HTTP client functionality, and [Akka Actors](http://doc.akka.io/docs/akka/current/scala/actors.html) to provide a highly scalable multi-threaded state machine.

The pull/push.poke functionality is unit tested via dependency-injected mock APIs. The source and target REST APIs are also directly tested via WireMock HTTP Servers that mock the HTTP endpoints.

The implementation will be updated as reference source and target API environments become available.

### Deployment notes

Configurable via environment parameters:

- `DEBUG_LOG=true` (defaults to `false` for `INFO` level logging, set to `true` for `DEBUG` level)
- `JOB_SCHEDULER_BASE_URL` : Where to pull jobs from (defaults to `http://localhost:8080/job`, but there is a job scheduler on AWS beanstalk at http://delius-api-job-schedular-dev.tqek38d8jq.eu-west-2.elasticbeanstalk.com/job)
- `DELIUS_API_BASE_URL` (defaults to `http://localhost:8080/delius`)
- `POLL_SECONDS` (defaults to `1`)
    
    
See `Configuration.scala` for a full list of configuration parameters.

### Running against a mocked Delius instance
Run wiremock standalone and configure for Delius mock endpoint

- Download wiremock standalone: `http://repo1.maven.org/maven2/com/github/tomakehurst/wiremock-standalone/2.6.0/wiremock-standalone-2.6.0.jar`

- Override the Delius endpoint `PUSH_BASE_URL=http://localhost:8085/delius java -jar pollPush.jar`

- Start wiremock `java -jar wiremock-standalone-2.6.0.jar --port 8085 &` 

- Configure wiremock endpoint `curl -X POST -d @./src/test/resources/mappings/{mapping file}.json http://localhost:8085/__admin/mappings`
