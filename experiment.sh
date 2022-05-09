mvn clean package -Dmaven.test.skip=true
java -jar -Xms56000m -Xmx56000m ./target/m-closest-entities-matching-1.0-SNAPSHOT-jar-with-dependencies.jar
