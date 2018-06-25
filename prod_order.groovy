#!/usr/bin/evn groovy
node {
	stage('检出源码') {            
		echo '检出源码'
		checkout([$class: 'GitSCM', branches: [[name: '*/master']], doGenerateSubmoduleConfigurations: false, extensions: [], submoduleCfg: [], userRemoteConfigs: [[credentialsId: '02b9d129-2917-40d3-b5e4-9bc52f4ea67a', url: 'https://git.coding.net/Mingguilu/order.git/${this.env.Git_Tag}']]])      
		sh 'git checkout $Git_Tag'
	}        
	stage('包构建') {                   
		echo '包构建'
		sh '/usr/local/apache-maven-3.5.3/bin/mvn clean install'
	}
	stage('文件分发') {            
		echo '文件分发'
		sh 'scp target/order.war itadmin@172.29.20.77:/tmp'                  
		#sh (script: "ssh itadmin@172.29.20.77 'scp /tmp/order.war appadmin@${Server_Hosts}:/home/appadmin/update ' ")
		sh (script: "ssh itadmin@172.29.20.77 'sudo salt -L \"${Server_Hosts}\" cmd.run  \" scp itadmin@172.29.20.77:/tmp/order.war /home/appadmin/update \" runas=\"appadmin\" ' ")
	}        
	stage('分批更新') {                
		echo '分批更新'
		sh (script: "ssh itadmin@172.29.20.77 'sudo salt -L \"${Server_Hosts}\" cmd.run  \"cd /home/appadmin/apache-tomcat-7.0.77/bin && sh shutdown.sh && rm -rf /home/appadmin/apache-tomcat-7.0.77/webapps/ROOT && cp /home/appadmin/update/order.war /home/appadmin/apache-tomcat-7.0.77/webapps/ROOT.war && sh startup.sh \" runas=\"appadmin\" ' ")
	}        
	stage('检查进程') {                   
		echo '检查进程'
		sh (script: "ssh itadmin@172.29.20.77 'sudo salt -L \"${Server_Hosts}\" cmd.run  \" ps -ef|grep tomcat \" ' ")
	} 
}
