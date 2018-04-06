# Chat Room Service

## BIL448 : Internet Security Protocols Spring 2018 Project
## CS255: Cryptography and Computer Security Winter 2004 Programming Project #2

------------------------------------------------------------------------------------------------------

### REQUIREMENTS

- IntelliJ IDEA 2017.3.4 (Community Edition)
- Build #IC-173.4548.28, built on January 30, 2018
- JRE: 1.8.0_152-release-1024-b11 amd64
- JVM: OpenJDK 64-Bit Server VM by JetBrains s.r.o
- Linux 4.13.0-36-generic

------------------------------------------------------------------------------------------------------

### RUNNING PROGRAM

- (only once) To Create KeyStores: [run] generate_keystores.sh 
- Running CA: [run] CertificateAuthority
- Running Chat Server: [run] ChatServer
- Running Chat Client: [run] ChatClient

------------------------------------------------------------------------------------------------------

### KEYSTORES

The Certification Authority has a self-signed certificate. The Client and the Server have long-term RSA key pairs and corresponding certificates signed using the Certificate Authority's certificate. The user's keystore contains the user's key pair, the certificate, and the certificate of the CA. The server's key store has the server's key pair, the certificate, and the certificate of the Certificate Authority. This key pair and certificates are generated and stored with __generate_keystores.sh__ before running the application. All these keys and certificates are generated and stored in Chat/keystores and all passwords are "123456". When CertificateAuthority and ChatClient are run, they receive their keystore information via GUIs. Since ChatServer is not a GUI, its keystore information is embedded in the source code.

------------------------------------------------------------------------------------------------------


### CRYPTOGRAPHIC PROTOCOLS

- All communication between Certificate Authority, Server and Client are done by using Java object streams and Java serialization. ClientPacket, ServerPacket, Message, and RegistrationRequest are for encapsulated objects that are sent over object streams according to protocols. 
- Diffie-Hellman (DH) key exchange is performed for encryption and mutual authentication.
- Digital signatures use SHA256 hash with RSA keys. The encryption is done by RSA algorithm in ECB mode with PKCS1 padding.
- The room key is a 128-bit AES key.
- By using the session key determined during the key exchange, an encrypted connection is established between the server and the client with the AES algorithm in CBC mode with PKCS5 padding.
- A MAC is attached to each message which is SHA256 hash encrypted with the room key of the message.

------------------------------------------------------------------------------------------------------

### 1- REGISTRATION

There is no predefined user database where users' information is kept. Since there is no such database, all users who want to chat are required to register with the Certification Authority. The user sends his or her username and the public key of the RSA key pair to the Certification Authority. The certificate authority checks this username and public key to see if the user is registered. If the user is not registered on the system the Certification Authority will sign a new certificate for the user's public key and return it to the user and also store the username-certificate pair in its keystore to remember that the user was registered.

 ![alt text](https://github.com/SerayBeser/ChatRoom-Service/blob/master/screenshots/1.png)

------------------------------------------------------------------------------------------------------

### 2- SERVER - CLIENT KEY EXCHANGE AND MUTUAL AUTHENTICATION

Diffie-Hellman (DH) key exchange is performed for encryption and mutual authentication. The server uses certificates and digital signatures signed by the CA when authenticating the identity of the user who wants to chat and the user verifying the identity of the server. Thus, the user completes the login process during the key exchange to verify his or her certificate.

The client is connected to the server. The server creates the DH key pair and sends the user the DH public key, which is signed with its own certificate, DH parameters, and its own RSA private key. The client verifies that the certificate of the server is signed by the Certificate Authority and that the DH public key comes from the server using the RSA public key. The client generates the DH key pair using the DH parameters received from the server. Then the user sends his certificate to the server by encrypting his DH public key with the RSA public key of the server and signing the information received from the server with his RSA private key.
The server verifies that the user's certificate is signed by the Certificate Authority in the direction of these packets from the client client and that the signed data is the correct signature using the user's RSA public key. The server decrypts its DH public key with the RSA private key.
The server computes the shared key using the DH private key and the DH public key of the user. The client computes shared keys using the DH private key and the DH public key of the server. Thus, when both parties exchange keys, they verify each other's identities.

 ![alt text](https://github.com/SerayBeser/ChatRoom-Service/blob/master/screenshots/2.png)
 
------------------------------------------------------------------------------------------------------
 
### 3- JOINING TO A ROOM

Room keys are the keys produced for that room when the first user enters the room. An encrypted connection is established between the server and the client using the session key that is decided during the exchange of keys between the server and the user.

The user sends the name (id) of the room he wants to join using this encrypted connection. Server joins the user to the requested room and sends the room's shared key to user using the encrypted connection.

 ![alt text](https://github.com/SerayBeser/ChatRoom-Service/blob/master/screenshots/3.png)

------------------------------------------------------------------------------------------------------
 
### 4- MESSAGING

User continuously sends messages to and receives messages from server. It encrypts/decrypts messages using the room key obtained when joining the room. A MAC is added to each message which is SHA256 hash encrypted with the room key of the message. Server sends encrypted message and MAC it receives from a client to all clients in the same room.

 ![alt text](https://github.com/SerayBeser/ChatRoom-Service/blob/master/screenshots/4.png)

------------------------------------------------------------------------------------------------------
 
