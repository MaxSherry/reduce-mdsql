 beetlsql小工具,用来分析md文件下的sql语句哪些是没用的,在控制台打印出来.
 
 ```
 mvn clean package -Dmaven.test.skip=true
 java -jar reduce-mdsql.0.0.1.jar
 ```