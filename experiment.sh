git pull
mvn clean package -Dmaven.test.skip=true
java -jar -Xms48000m -Xmx48000m ./target/m-closest-entities-matching-1.0-SNAPSHOT-jar-with-dependencies.jar
