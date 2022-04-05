
# one-stop-shop-forex-rates

This service retrieves exchange rates GBP to EUR from the [forex-rates API](https://github.com/hmrc/forex-rates) and posts those to Core every day at 16:10 CET - this time is defined by a cron expression in application.conf [here](https://github.com/hmrc/one-stop-shop-forex-rates/blob/main/conf/application.conf#:~:text=sends%20to%20Core%22-,expression,-%3D%20%220_10_16_*_*_).
The service tries to post the rates 3 times before it fails (the number of times it retries is defined in the [application.conf](https://github.com/hmrc/one-stop-shop-forex-rates/blob/main/conf/application.conf#:~:text=desConnectorMaxAttempts)).

Requirements
------------

This service is written in [Scala](http://www.scala-lang.org/) and [Play](http://playframework.com/), so needs at least a [JRE] to run.

## Run the application

To update from Nexus and start all services from the RELEASE version instead of snapshot
```
sm --start ONE_STOP_SHOP_ALL -r
```

### To run the application locally execute the following:
```
sm --stop ONE_STOP_SHOP_FOREX_RATES
```
and 
```
sbt 'run 10199'
```

### To use the test-only endpoints:
```
sbt run -Dapplication.router=testOnlyDoNotUseInAppConf.Routes
```
The test-only endpoint triggers the retrieval of exchange rates from forex-rates API and posting them to Core.

|Method |URI                                                    |
|:-----:|-------------------------------------------------------|
|GET    |/one-stop-shop-forex-rates/test-only/retrieve-and-send |

Unit and Integration Tests
------------

To run the unit and integration tests, you will need to open an sbt session on the browser.

### Unit Tests

To run all tests, run the following command in your sbt session:
```
test
```

To run a single test, run the following command in your sbt session:
```
testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
testOnly *CorrectionControllerSpec
```

### Integration Tests

To run all tests, run the following command in your sbt session:
```
it:test
```

To run a single test, run the following command in your sbt session:
```
it:testOnly <package>.<SpecName>
```

An asterisk can be used as a wildcard character without having to enter the package, as per the example below:
```
it:testOnly *CorrectionRepositorySpec
```
### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
