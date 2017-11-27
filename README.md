# P2MP File Transfer Protocol

## About

Usually the FTP works over TCP in a client-server mode. While this ensures reliability, the tradeoff is reduced speed due to TCP overheads. Since, TCP works on client-server mode, we can transfer files only between two nodes.

In this project we are using UDP to reliably transfer files to multiple nodes. This brings all the advantages of UDP along with reliability.

## Project Structure

We have two maven JAVA projects, a "client" and a "server". 

The client will be run on node that wants to transfer the file. The servers will be run on nodes that will procure those files.

You will find the code inside ``` src/main/java ``` directory of respective projects.

There is a ``` Main.java ``` that will start the java application and perform respective tasks.

### About Client

- The ``` Main.java ``` will start two threads. Thread of class ``` SendFiles ``` , will send the file provided by CLI as packets of length MSS and some headers.
- After a packet has been sent to all the servers, this thread will sleep for a specified "Timeout" period.
- Meanwhile another thread of class ``` ListenAcks ``` will monitor response received from servers. If it encounters ACK packets, it will check if this ACK is the required ACK. Once all the ACKs from all the servers has been received, it will interrupt ``` SendFiles ``` thread noticing that ACKs has been received and it can send another packet.
- If all the ACKs were not recieved before timeout, ``` SendFiles ``` will send the same packet to servers that have not send their ACK.
- Once all the ACKs are received, this Thread will stop, indicating end of transaction.
- The timeout is decided dynamically by calculating the RTT of all the servers. It is 25% more than the average of the RTT or 100 ms, whichever is higher.

### About Client

- The ``` Main.java ``` file will start a thread of class ``` Server ``` . This will monitor any 	packets sent to its UDP port.
- If the packet is of type DATA, it will check the probability of dropping the packet.
- If the packet is not dropped, then the data from this packet is saved in the specified file using ``` FileOutputStream ``` .
- Once the file is successfully saved, it will send an ACK to the sender.


## Installation

You will need "maven" installed on your node.

Once you have maven, you will go to respective maven project and run ``` mvn package ``` . This will build the maven project and generate an executable jar file inside "target" directory. The name of jar will be ``` $-0.0.1-SNAPSHOT-jar-with-dependencies.jar ``` .

$ is client/server.

You can run the executable jar as ``` java -jar jarname.jar args ``` .

However, we have provided batch scripts and shell scripts, so that you can run respective projects as per requirement. A detailed explanation is provided in subsequent section.

E.g. 

If you want to run client on a node, you will go inside "client" directory and run ``` mvn package ``` .

## Run the application

We have provided a batch script and a shell script to run the application on command line.

E.g.

In the "client" project, you will see ``` p2mpclient.bat ``` and ``` p2mpserver.sh ``` .

If you are on Windows machine, you can run the application as :

``` p2mpclient server1 server2 serveri #serverport filename MSS ```

If you are on LINUX, you can run the application as:

``` ./p2mpclient.sh server1 server2 serveri #serverport filename MSS ```


Same is the case for "server" project, where you will see ``` p2mpserver.bat ``` and ``` p2mpserver.sh ``` .

