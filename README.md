# JRadius ![JRadius](doc/images/logo.png) 

JRadius is a Java RADIUS framework for client and server. It's designed to be modular, extensible, and easy to integrate into existing Java applications.

https://coova.github.io/JRadius/

## Features

- **RADIUS Client**: Support for authentication (Access-Request) and accounting (Accounting-Request).
- **RADIUS Server**: Extensible server engine based on Spring.
- **RADIUS Dictionaries**: Comprehensive support for standard RADIUS attributes and VSAs (Vendor Specific Attributes).
- **Authentication Protocols**: Support for PAP, CHAP, MS-CHAPv2, EAP-TLS, etc.
- **Modular Structure**: Maven-based project with several specialized modules.

## Project Structure

The project is divided into several specialized modules:

- `jradius-core`: **Core RADIUS engine**. Contains the fundamental classes for RADIUS packet handling (`RadiusPacket`), attribute management (`RadiusAttribute`), and the base client/server framework. It depends on Spring and Apache Commons.
- `jradius-dictionary`: **Standard & Vendor Dictionaries**. Provides a comprehensive set of RADIUS attributes and Vendor-Specific Attributes (VSAs) generated from FreeRADIUS dictionaries.
- `jradius-dictionary-min`: **Minimal Dictionary**. A lightweight version of the dictionary module containing only the most commonly used attributes, ideal for resource-constrained environments.
- `jradius-client`: **High-level Client API**. A simplified API for building RADIUS clients, providing easy-to-use classes for authentication and accounting.
- `jradius-server`: **RADIUS Server Framework**. A modular RADIUS server implementation based on Spring. It supports multiple listeners (TCP, WebServices, RadSec) and uses a flexible packet-handler architecture.
- `jradius-extended`: **Extended Protocols & Features**. Adds support for advanced features like EAP (Extensible Authentication Protocol) and utilizes Bouncy Castle for cryptographic operations.
- `jradius-apps`: **Command-line Utilities**. Includes useful RADIUS tools like `RadClient` and other command-line applications for testing and management.
- `jradius-example`: **Usage Examples**. Practical examples demonstrating how to implement both clients and servers using the JRadius framework.
- `jradius-extras`: **Additional Utilities**. Contains extra components and integrations that extend the core functionality of JRadius.

## Architecture Overview

JRadius is designed to be highly modular and integrates seamlessly into existing RADIUS infrastructures, particularly with FreeRADIUS.

### Deployment Diagram

````mermaid
graph TD
    subgraph AppLogic [Application specific business logic and resources]
        DB[(Database)] <-->|Connection Pool| Handlers[JRadius Packet & Event Handlers]
    end

    subgraph JRadiusStack [JRadius Stack]
        Dict[JRadius/FreeRADIUS Dictionary]
        Server[JRadius Server]
        JRE[Java Runtime]
        
        Dict --- Server
        Server --- JRE
    end

    subgraph FreeRADIUSStack [FreeRADIUS Stack]
        FR[freeRADIUS]
        Modules[FreeRADIUS Modules]
        RLM[JRadius Module <br/> <i>rlm_jradius</i>]
        
        FR --- Modules
        FR --- RLM
    end

    %% Connexions entre les blocs
    Handlers --- Dict
    RLM <-->|TCP/IP| Server
    
    %% Entrées/Sorties externes
    Inbound([RADIUS/UDP]) <--> FR

    %% Styles
    style AppLogic fill:#f9f9f9,stroke:#333,stroke-width:1px
    style JRadiusStack fill:#fff,stroke:#0000ff,stroke-width:1px
    style RLM stroke:#0000ff,stroke-width:2px
    style Dict stroke:#0000ff,stroke-width:2px
    style Server stroke:#0000ff,stroke-width:2px
````

### Key Components

1.  **RadiusPacket**: The base class for all RADIUS packets (`AccessRequest`, `AccessAccept`, `AccountingRequest`, etc.). Each packet contains a list of `RadiusAttribute` objects.
2.  **RadiusAttribute**: Represents a RADIUS attribute. Attributes are typed (String, Integer, IPAddress, etc.) and can be standard or Vendor-Specific (VSA).
3.  **AttributeDictionary**: A registry of known attributes. JRadius loads these at runtime to map attribute IDs to their corresponding Java classes.
4.  **Packet Handlers**: On the server side, packets are processed by a chain of `PacketHandler` objects. This allows for highly customizable logic (e.g., authentication against different backends, logging, proxying).
5.  **rlm_jradius**: A FreeRADIUS module that acts as a bridge, forwarding RADIUS packets to the JRadius Server via a high-performance TCP protocol.

## Supported Authentication Protocols

JRadius supports a wide range of authentication protocols through its `RadiusAuthenticator` implementations.

### Standard Protocols (jradius-core)
- **PAP** (`PAPAuthenticator`): Simple Password Authentication Protocol.
- **CHAP** (`CHAPAuthenticator`): Challenge Handshake Authentication Protocol.
- **MS-CHAPv1** (`MSCHAPv1Authenticator`): Microsoft CHAP version 1.
- **MS-CHAPv2** (`MSCHAPv2Authenticator`): Microsoft CHAP version 2, commonly used with VPNs and WPA2-Enterprise.

### EAP Protocols (jradius-core & jradius-extended)
- **EAP-MD5** (`EAPMD5Authenticator`): EAP with MD5-based challenge.
- **EAP-MSCHAPv2** (`EAPMSCHAPv2Authenticator`): EAP encapsulation of MS-CHAPv2.
- **EAP-TLS** (`EAPTLSAuthenticator`): EAP over Transport Layer Security (requires `jradius-extended`).
- **EAP-TTLS** (`EAPTTLSAuthenticator`): EAP Tunneled Transport Layer Security, supports inner authentication (requires `jradius-extended`).
- **PEAP** (`PEAPAuthenticator`): Protected EAP (requires `jradius-extended`).

### Usage Example with EAP-TLS
```java
import net.jradius.client.auth.EAPTLSAuthenticator;
// ...
EAPTLSAuthenticator auth = new EAPTLSAuthenticator();
auth.setKeyStorePath("client.jks");
auth.setKeyStorePassword("password");
auth.setTrustStorePath("truststore.jks");
auth.setTrustStorePassword("password");

RadiusResponse reply = rc.authenticate(request, auth, 3);
```

## Prerequisites

- **Java 17** or higher.
- **Maven 3.8+** for building.

## Build and Installation

To build and install all modules into your local Maven repository:

```bash
mvn install
```

### Running Tests

To run the unit tests and verify the JDK 17 compatibility:

```bash
mvn test
```

The project now includes core unit tests in `jradius-core` to validate:
- **RadiusPacket**: Basic packet manipulation and attribute management.
- **AttributeList**: Handling of multiple attributes and duplicate types.
- **AttributeFactory**: Dynamic dictionary loading and reflection-based attribute creation.

## JRadius Client Usage Examples

Here's how to use the JRadius client to perform authentication and accounting requests.

### 1. Initialize the Client

Before sending requests, you must load the attribute dictionary and configure the client.

```java
import net.jradius.packet.attribute.AttributeFactory;
import net.jradius.client.RadiusClient;
import java.net.InetAddress;

// Load the default dictionary (mapping IDs to Java classes)
AttributeFactory.loadAttributeDictionary("net.jradius.dictionary.AttributeDictionaryImpl");

// Configure the client: Host, Shared Secret, Auth Port, Acct Port, Timeout (ms)
InetAddress host = InetAddress.getByName("radius.example.com");
RadiusClient rc = new RadiusClient(host, "my_shared_secret", 1812, 1813, 5000);
```

### 2. Authentication Request (Access-Request)

This example shows how to perform an authentication with NAS attributes and how to retrieve the `Reply-Message` from the response.

```java
import net.jradius.client.auth.MSCHAPv2Authenticator;
import net.jradius.dictionary.Attr_UserName;
import net.jradius.dictionary.Attr_UserPassword;
import net.jradius.dictionary.Attr_NASPortType;
import net.jradius.dictionary.Attr_ReplyMessage;
import net.jradius.packet.AccessRequest;
import net.jradius.packet.RadiusResponse;
import net.jradius.packet.AccessAccept;
import net.jradius.packet.attribute.AttributeList;

// Prepare common attributes
AttributeList attrs = new AttributeList();
attrs.add(new Attr_UserName("test_user"));
attrs.add(new Attr_NASPortType(Attr_NASPortType.Wireless80211));

// Create the request
AccessRequest request = new AccessRequest(rc, attrs);
request.addAttribute(new Attr_UserPassword("test_password"));

// Send the request using MS-CHAPv2 authenticator with 3 retries
RadiusResponse reply = rc.authenticate(request, new MSCHAPv2Authenticator(), 3);

// Analyze the response
if (reply instanceof AccessAccept) {
    System.out.println("Authentication successful!");
    
    // Extract specific attribute value (e.g., Reply-Message)
    String message = (String) reply.getAttributeValue(Attr_ReplyMessage.TYPE);
    if (message != null) System.out.println("Server Message: " + message);
} else {
    System.out.println("Authentication failed.");
}
```

### 3. Accounting Life Cycle (Accounting-Request)

Accounting usually follows a cycle: **Start**, **Interim-Update**, and **Stop**.

```java
import net.jradius.dictionary.Attr_AcctStatusType;
import net.jradius.dictionary.Attr_AcctSessionId;
import net.jradius.dictionary.Attr_AcctInputOctets;
import net.jradius.dictionary.Attr_AcctOutputOctets;
import net.jradius.dictionary.Attr_AcctSessionTime;
import net.jradius.packet.AccountingRequest;
import net.jradius.util.RadiusRandom;

// Generate a unique Session-ID
String sessionId = RadiusRandom.getRandomString(24);
attrs.add(new Attr_AcctSessionId(sessionId));

// --- 1. Accounting-Start ---
AccountingRequest startReq = new AccountingRequest(rc, attrs);
startReq.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.Start));
rc.accounting(startReq, 3);

// --- 2. Accounting-Interim-Update ---
AccountingRequest updateReq = new AccountingRequest(rc, attrs);
updateReq.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.InterimUpdate));
updateReq.addAttribute(new Attr_AcctInputOctets(1024L)); // Data sent
updateReq.addAttribute(new Attr_AcctSessionTime(60L));   // Time in seconds
rc.accounting(updateReq, 3);

// --- 3. Accounting-Stop ---
AccountingRequest stopReq = new AccountingRequest(rc, attrs);
stopReq.addAttribute(new Attr_AcctStatusType(Attr_AcctStatusType.Stop));
stopReq.addAttribute(new Attr_AcctInputOctets(5120L));
stopReq.addAttribute(new Attr_AcctSessionTime(300L));
rc.accounting(stopReq, 3);
```

### 4. RadSec Support (RADIUS over TLS)

JRadius includes support for RadSec (RADIUS over TLS/TCP) as defined in RFC 6614.

#### RadSec Client API

To use RadSec, you must configure the `RadSecClientTransport` with a `KeyManager` and `TrustManager`.

```java
import net.jradius.radsec.RadSecClientTransport;

RadiusClientTransport transport = new RadSecClientTransport(keyManagers, trustManagers);
transport.setRemoteInetAddress(InetAddress.getByName(radiusServer));
transport.setSharedSecret(sharedSecret);
transport.setAuthPort(2083);
transport.setAcctPort(2083);

RadiusClient rc = new RadiusClient(transport);
```

- **Location**: The RadSec implementation is located in the `net.jradius.radsec` package within the `jradius-extended` module.
- **Key Components**: `RadSecClientTransport`, `RadSecListener`, `RadSecProcessor`, and `SimpleProxyHandler`.

### 5. RadClient Utility

JRadius provides a command-line utility called `RadClient`. A shell script is provided in `bin/radclient`.

Example of using `radclient` with an attributes file:
1. Create a file named `radius.pkt`:
   ```
   User-Name = test
   User-Password = test
   ```
2. Run the script:
   ```bash
   bin/radclient localhost sharedsecret radius.pkt
   ```

## JRadius Server & FreeRADIUS Integration

JRadius is not a stand-alone RADIUS server. Instead, it is a Java Server which is called by the `rlm_jradius` module built into the FreeRADIUS server. The module, using pooled connections to the JRadius server, passes the RADIUS request and response packets to JRadius for any of the FreeRADIUS module entry points. Meaning, you can have JRadius process authentication, accounting, or proxy requests.

The JRadius Server itself is a light-weight stand-alone Java server. Within its XML configuration, JRadius can be configured with your specific JRadius/FreeRADIUS Dictionary and any number of custom JRadius Handlers chained together.

### 1. Building and Installing FreeRADIUS with JRadius

The `rlm_jradius` module for FreeRADIUS is a standard module in the FreeRADIUS distribution. However, it is not configured to be built per default. To build, download the latest FreeRADIUS server:

```bash
wget ftp://ftp.freeradius.org/pub/freeradius/freeradius-server-2.1.1.tar.bz2
bzcat freeradius-server-2.1.1.tar.bz2 | tar xf -
cd freeradius-server-2.1.1
echo rlm_jradius >> src/modules/stable

./configure
make
make install
```

Run FreeRADIUS (shown here in debug mode):
```bash
/usr/local/sbin/radiusd -X
```

### 2. FreeRADIUS Configuration

Below are the portions of the FreeRADIUS `etc/raddb/radiusd.conf` file related to JRadius.

```conf
modules {
   ...
   # configure the rlm_jradius module
   jradius {
      name      = "example"             # The "Requester" name (a single
                                        # JRadius server can have
                                        # multiple "applications")
      primary   = "localhost"           # Uses default port 1814
      secondary = "192.168.0.1"         # Fail-over server
      tertiary  = "192.168.0.1:8002"    # Fail-over server on port 8002
      timeout   = 1                     # Connect Timeout
      onfail    = NOOP                  # What to do if no JRadius
                                        # Server is found. Options are:
                                        # FAIL (default), OK, REJECT, NOOP
      keepalive = yes                   # Keep connections to JRadius pooled
      connections = 8                   # Number of pooled JRadius connections
  }
}
```

In this example, the requester name is configured with `name example`. Different requesters can be mapped to different handler chains in the JRadius context. You can also configure primary, secondary, and tertiary JRadius servers for redundancy fail-over. The TCP/IP connections to JRadius are pooled with persistent connections if `keepalive` is `yes`.

Add `jradius` to the desired stages in your FreeRADIUS configuration:

```conf
authorize {
   ...
   jradius
}

post-auth {
   ...
   jradius
   Post-Auth-Type REJECT {             # Use this to also process failures -
       jradius                         # AccessReject replies 
   }                                   # from the post-auth handler.
}

preacct {
   ...
   jradius
}

accounting {
   ...
   jradius
}

# Optional proxy stages:
pre-proxy {
   ...
   jradius
}

post-proxy {
   ...
   jradius
}
```

### 3. JRadius Server Configuration

The JRadius server is configured using a combination of Spring XML and a JRadius-specific XML configuration (`jradius-config.xml`).

#### Spring Configuration (`spring-config.xml`)
Defines the core beans like `AttributeDictionary`, `RadiusProcessor`, and `SessionManager`.

#### JRadius Configuration (`jradius-config.xml`)
Configures the listeners and packet handlers.

Example configuration for a FreeRADIUS listener:
```xml
<listeners>
  <listener name="FreeRadiusListener">
    <description>FreeRADIUS rlm_jradius module listener (TCP)</description>
    <class>bean:radiusListener</class>
    <processor-class>bean:radiusProcessor</processor-class>
    <processor-threads>128</processor-threads>
    <packet-handler type="authorize" handler="MyLocalHandler"/>
    <property name="port" value="1814"/>
  </listener>
</listeners>
```

#### Custom Packet Handlers
You can implement your own logic by extending `PacketHandler`:

```java
public class MyLocalHandler extends PacketHandler {
    public int handle(JRadiusEvent event) throws Exception {
        JRadiusRequest request = (JRadiusRequest) event;
        RadiusPacket requestPacket = request.getRequestPacket();
        
        // Add custom logic here
        
        return JRadiusResult.CONTINUE;
    }
}
```

#### RadSec Server Configuration

The JRadius server can handle native RadSec connections. Configure a RadSec listener in `jradius-config.xml`:

```xml
<listener name="RadSecListener">
    <description>RadSec Listener</description>
    <class>bean:radSecListener</class>
    <processor-class>bean:radSecProcessor</processor-class>
</listener>
```

The corresponding beans are defined in `spring-config.xml`:

```xml
<bean id="radSecListener" class="net.jradius.radsec.RadSecListener">
    <property name="port" value="2083" />
    <property name="keyManager" ref="myKeyManager" />
    <property name="trustManager" ref="myTrustManager" />
</bean>

<bean id="radSecProcessor" class="net.jradius.radsec.RadSecProcessor">
    <property name="handler" ref="myHandler" />
</bean>
```

### 4. Running JRadius Server

JRadius works in conjunction with FreeRADIUS, so you should already have your FreeRADIUS installation setup for JRadius.

#### Building from Source
Download the source and build using Maven:
```bash
git clone https://github.com/coova/jradius.git
cd jradius
mvn clean install
```

#### Run Server
Out of the box, JRadius doesn’t really do that much. It is designed such that you can easily write your own handlers to perform your RADIUS logic.

To run the sample JRadius server:
```bash
unzip jradius-server-1.0.0-release.zip
cd jradius
sh start.sh
```


## RADIUS Dictionaries and VSAs

JRadius includes a powerful mechanism for handling RADIUS dictionaries.

- **Automated Generation**: Most dictionary classes are automatically generated from FreeRADIUS dictionary files during the build process of the `jradius-dictionary` module.
- **Using VSAs**: To use a Vendor-Specific Attribute, simply instantiate the corresponding class (e.g., `Attr_CiscoAVPair` for Cisco).

```java
import net.jradius.dictionary.vsa_cisco.Attr_CiscoAVPair;
// ...
request.addAttribute(new Attr_CiscoAVPair("shell:priv-lvl=15"));
```

## JRadius Simulator

The JRadius Simulator is an open-source Java Swing GUI application designed to test RADIUS applications and interconnections by simulating RADIUS traffic.

### GUI Interface Overview
The simulator window is divided into several functional areas:
1. **Connection Bar**: Select transport (UDP/RadSec), Server IP, Shared Secret, and Ports (Auth/Acct).
2. **Attributes Table**: A dynamic list where you can add RADIUS attributes. It features:
   - **Attribute Selector**: Searchable list of all dictionary attributes.
   - **Value Editor**: Smart input that provides drop-down menus for enumerated attributes (e.g., `Service-Type`).
3. **Authentication Panel**: Configure the authentication method (PAP, MS-CHAPv2, EAP-TLS, etc.) and credentials.
4. **Log/Result View**: Displays the raw packet sent and the server's response in a readable tree format.

### How to Run
The simulator is included in the `jradius-extended` module.

- **Main Class**: `net.jradius.client.gui.JRadiusSimulator`
- **Source**: `extended/src/main/java/net/jradius/client/gui/JRadiusSimulator.java`
- **Launch Scripts**: 
  - `bin/jRadiusSimulator.sh`
  - `client/scripts/simulator.sh`
- **Example Simulations**: Pre-configured simulation files can be found in `files/simulations/`.

To run from the command line:
```bash
sh bin/jRadiusSimulator.sh
```

### Configuration in Simulator
1. Select the **Transport** (UDP or RadSec).
2. Enter the **RADIUS Server** IP/hostname and **Shared Secret**.
3. Set the **Auth Port** and **Acct Port** (default 1812/1813 for UDP, 2083 for RadSec).
4. (For RadSec) Select the **Keys** tab to configure X509 certificates and CA.


## License

This project is available under the LGPL 3.0 and GPL 3.0 licenses. See the [`LICENSE`](LICENSE) file for more details.

---
Developed by David Bird (david@coova.com).
