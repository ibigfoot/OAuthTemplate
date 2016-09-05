#Tomcat Template

A simple java web application configured for [Heroku](www.heroku.com) that includes OAuth web flow from Salesforce.
Will require configuration of the OAuth servlet to suit your [ConnectedApp](https://developer.salesforce.com/docs/atlas.en-us.api_rest.meta/api_rest/intro_defining_remote_access_applications.htm) in Salesforce.

#Installation -

Assumes you have configured your local environment with the [Heroku Toolbelt](https://toolbelt.heroku.com/)

Open a terminal window and start typing... (output of each command ommitted)

    cd OAuthTemplate
    git remote -rm origin 
    mvn package
    java -jar target/dependency/webapp-runner.jar target/*.war
    heroku create
    heroku addons:create memcachier:dev
    git push heroku master
    heroku scale web=1

##Run locally

Run locally from the commandline with

    mvn package
    java -jar target/dependency/webapp-runner.jar --session-store memcache target/*.war

Or (my favourite)
Run directly from Eclipse 

- Run->External Tools->External Tool Configurations
- Add new program
- Set Location to your java executible (for my mac os x this was at /usr/bin/java)
- Set Working directory to your project root
- Set arguments to -jar target/dependency/webapp-runner.jar --session-store memcache target/documenter.war
- Go the environment tab and set the three memcachier envrionment passwords from Heroku.

Something slightly broken with the Memcache stuff when running locally, not sure what yet. 
Run in eclipse without the --session-store attribute if this bothers you

##Run Heroku

	cd documenter 
    heroku scale web=1
    heroku logs --tail
    
#OR

Here is a friendly Heroku button... Once deployed you will need to update your config vars to point to use the OAuth params in your IdP 

[![Deploy](https://www.herokucdn.com/deploy/button.png)](https://heroku.com/deploy)