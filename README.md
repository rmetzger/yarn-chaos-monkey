# yarn-chaos-monkey
A Chaos Monkey for YARN


There is an effort within the Hadoop YARN project to implement something similar [YARN-3337](https://issues.apache.org/jira/browse/YARN-3337).

## Compile

```
mvn clean install
```

## Usage

Run it.
```
# Make sure Hadoop can find its configs
export HADOOP_CONF_DIR=/etc/hadoop/conf

./run.sh --appId application_1436277217933_0204 --sleepBetweenFailuresSec 30

```

