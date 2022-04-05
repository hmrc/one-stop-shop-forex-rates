
# one-stop-shop-forex-rates

This service retrieves exchange rates GBP to EUR from the [forex-rates API](https://github.com/hmrc/forex-rates) and posts those to Core every day at 16:10 CET - this time is defined by a chron expression in application.conf [here](https://github.com/hmrc/one-stop-shop-forex-rates/blob/main/conf/application.conf#:~:text=sends%20to%20Core%22-,expression,-%3D%20%220_10_16_*_*_).
The service tries to post the rates 3 times before it fails (the number of times it retries is defined in the [application.conf](https://github.com/hmrc/one-stop-shop-forex-rates/blob/main/conf/application.conf#:~:text=desConnectorMaxAttempts)).

Running locally
---------------
via service manager
sm --start ONE_STOP_SHOP_FOREX_RATES -r


### License

This code is open source software licensed under the [Apache 2.0 License]("http://www.apache.org/licenses/LICENSE-2.0.html").
