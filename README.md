# sap-concur-takehome

This program polls a Github repo, looks for new pull requests (based on the
timestamp of the most recent tweeted-about pull request), and tweets a summary
of each PR on Twitter.

## Building the project

Creates a single jar file at `target/uberjar`.

Requires [leiningen](https://leiningen.org/).

    $ lein uberjar
    
Please note, the files `resources/conf.edn` and
`resources/github-private-key.pem` are excluded from source control. The program
will not run correctly without them. (The final artifact, however, is included in
source control: see `Usage`.)

## Testing

    $ lein test

## Usage

This program is designed to start up, gather the latest PRs (again, based on the
timestamp of the most recent tweeted-about pull request, if existing), post them
to Twitter, then exit.

It is designed to be used on a regular cadence, perhaps with a tool like `cron`.

    $ java -jar target/uberjar/sap-concur-takehome-0.1.0-SNAPSHOT-standalone.jar
