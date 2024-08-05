# webportfolio-gateway
gateway service for webportfolio


## Setup guide

### Dependant services
Following services have to be running
- slack (with valid token)
- eureka server
- authentication server
- redis service in local

(Optional) Easy way to run dependent services: https://github.com/MarcoBackman/integration-testing-tool

### active profile
- `default` for production use
- `test` for test purpose

### Environment variables to set

- Actuator Service
  - ACTUATOR_USER
  - ACTUATOR_PASSWORD
- Application
  - APP_PORT
- Eureka Service
  - EUREKA_SERVER
- Slack Service
  - SLACK_CHANNEL_NAME
  - SLACK_PORTFOLIO_CHANNEL_ID
  - SLACK_PORTFOLIO_TOKEN
- Redis
  - REDIS_PORT

```angular2html
ACTUATOR_USER=&#123;VALUE};ACTUATOR_PASSWORD=&#123;VALUE};APP_PORT=&#123;VALUE};EUREKA_SERVER=&#123;VALUE};SLACK_CHANNEL_NAME=&#123;VALUE};SLACK_PORTFOLIO_CHANNEL_ID=&#123;VALUE};SLACK_PORTFOLIO_TOKEN=&#123;VALUE};REDIS_PORT=&#123;VALUE}
```

