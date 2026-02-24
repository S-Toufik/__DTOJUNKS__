java \
  -XX:+UseZGC \
  -Xms2G -Xmx4G \
  -XX:+UseStringDeduplication \
  -XX:+AlwaysPreTouch \
  -XX:+UseContainerSupport \
  -jar your-app.jar
