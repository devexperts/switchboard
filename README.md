# Switchboard

###### Test Case Processing Toolset
Switchboard is designed to collect, parse, filter and group tests from code,
transform the result and perform the required actions on it.

## Concept
The toolset consists of an `API` module, a `Core` module, a `Runner` and an extendable set of `Integration` modules.

##### Test
An entity representing a single test (e.g. TestNG or JUnit test, a Cucumber scenario, etc.) consisting of an identifier and its `Attributes`.

##### TestRun
An entity representing a group of Tests consisting of an identifier, its `Attributes` and a set of tests.

##### Integration
Each Integration module provides specific features implementation or access to interaction with software,
e.g. `cukes` integration can collect Cucumber test data from .feature files, `jira` integration provides access
to Atlassian Jira REST API functionality etc.
Each integration is autonomous and is run within its own classpath to evade dependency conflicts.

##### IntegrationComponent
An implementation of a single atomic component of an integration.
All components are executed during integration run in the following sequence:
- `TestExtractor` components supplying Tests from an external source, e.g. files, DB, URL etc.
- `TestProcessor` components complementing, decoding, splitting etc. specified tests.
- `TestFilter` components filtering processed Tests.
- `TestSplitter` components splitting tests into TestRuns by specified attributes.
- `TestRunProcessor` components processing (complementing, decoding, splitting etc) tests runs.
- `TestRunConsumer` components consuming the prepared TestRuns (e.g. test runner, report collector etc.).
  Each IntegrationComponent is run within the context of an integration it is specified in.

## How to run
A Switchboard is run by Runner module:
```java -jar {path-to-runner-jar} -c {path-to-configuration} -i {path-to-libs}```
with 2 parameters:
- -c - JSON configuration file path
- -i - Paths to integration lib jars and/or directories with libs; glob pattern is supported.

Runner executes all components marked as runnable (`"runnable": true` in JSON  config).

## Configuration
Each Integration or Integration set configuration is stored as a JSON, describing the structure,
execution order and execution parameters.

### Example
This configuration will:
- collect Cucumber test cases
- filter tests marked with `MarkerAnnotation1` attribute
- create test batches grouped by 5 tests at most
- log the result into the console

```
{
  "@class": "com.devexperts.switchboard.integrations.cukes.CukesIntegration",
  "identifier": "CukesIntegration-1",
  "runnable": false,
  "testExtractors": [
    {
      "@class": "com.devexperts.switchboard.integrations.cukes.CukesTestExtractor",
      "identifier": "CukesTestExtractor-1",
      "testLocations": [
        "src/test/resources/test_classes"
      ],
      "filePattern": "regex:.*.feature",
      "cucumberNativeFilters": "",
      "basePath": "src/test/resources"
    }
  ],
  "testProcessors": [],
  "testFilters": [
    {
      "@class": "com.devexperts.switchboard.impl.filters.AttributesFilter",
      "identifier": "AttributesFilter-1",
      "attributePredicate": {
        "@class": "com.devexperts.switchboard.entities.attributes.AttributeIsPresent",
        "attributeKeyRegex": "MarkerAnnotation1"
      }
    }
  ],
  "testSplitters": [
    {
      "@class": "com.devexperts.switchboard.impl.splitters.TestCountSplitter",
      "identifier": "TestCountSplitter-1",
      "count": 5
    }
  ],
  "testRunProcessors": [],
  "testRunConsumers": [
    {
      "@class": "com.devexperts.switchboard.impl.consumers.LoggingTestRunConsumer",
      "identifier": "TestLoggingConsumer-1"
    }
  ]
}
```

##### ComponentReference
Any integration components can be used in other Integrations using a `ComponentReference` wrapper.
A special component type `ComponentReference` can be used to wrap a component of an integration
executed during another Integration run. Component wrapped by ComponentReference is run within the context of its source integration.

### Environment variables in the configuration
If a value in configuration should be resolved from the environment during execution the variable should be set as `%variable_name%`.
Here's how you can set Jira password from `JIRA_PASSWORD` environment variable:
```
[
  {
    "@class": "com.devexperts.dxci.integrations.javaparser.JavaParserIntegration",
    "identifier": "JavaParserIntegration - runnable",
    "runnable": true,
    ...
    "uri": "https://jira.somewhere.elsewhere.com",
    "login": "somebody",
    "password": ""%JIRA_PASSWORD%"
  }
]
```
