PROJECT_VERSION=$(mvn org.apache.maven.plugins:maven-help-plugin:evaluate -Dexpression=project.version -B | grep -v '\[')
if [[ "$PROJECT_VERSION" =~ .*SNAPSHOT ]] && [[ "${BRANCH_NAME}" = "master" ]] && [[ -z "${PULL_REQUEST_NUMBER}"]];
then
   echo "SHOULD DEPLOY SNAPSHOT"
#  mvn deploy -DskipTests -B;
fi
