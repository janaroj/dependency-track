FROM tomcat:7.0
MAINTAINER priitliivak@gmail.com

ADD target/dtrack.war /usr/local/tomcat/webapps/

CMD ["catalina.sh", "run"]

