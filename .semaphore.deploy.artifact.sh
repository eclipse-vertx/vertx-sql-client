PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep '^[^\[].*-SNAPSHOT')
if [ -n "${PROJECT_VERSION}" ] && [ "${BRANCH_NAME}" = "master" ] && [ "${TRAVIS_PULL_REQUEST}" = "" ];
then
  mvn deploy -DskipTests -B -s .travis.maven.settings.xml;
fi
