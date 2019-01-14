zooinspector
============

current version: __1.1-SNAPSHOT__

An improved zookeeper inspector

- Use async operations to speed up read
- Znodes sorted by names in tree viewer
- Timestamp and session id in more readable format in node metadata viewer
- Add a dropdown menu to show the last 10 successfully connected zookeeper addresses
- Support text search in node data viewer
- Support read-only mode for node data viewer

Build
- $git clone https://github.com/zzhang5/zooinspector.git
- $cd zooinspector/
- $mvn clean package

Run
- java -jar target/zooinspector-1.1-SNAPSHOT-jar-with-dependencies.jar
