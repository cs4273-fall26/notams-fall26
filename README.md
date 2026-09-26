# NOTAMs
## Project Description
This project focuses on finding and organizing Notices to Air Missions (NOTAMs) in order to improve the readability of information before a flight. Pilots are required to review NOTAMs to avoid unexpected hazards throughout their flight, but the current system does not determine the priority of notices. As a result, pilots cannot easily see which notices could affect their operation because high priority notices are surrounded by less important data. This project provides a structured format that organizes NOTAMs in order to present the information in a clear layout. The feature will highlight significant notices so pilots could quickly identify information that directly affects their flight. By reducing clutter and improving NOTAM organization, the system improves flight planning and ensures that important details are easy to see.


## Technologies & Tools
### **Languages**
- **Java 17+** - Used for all of the core functionality and required by the project.

### **API and Data Sources**
- **FAA NOTAM API** - Retrieves every NOTAM that is relevant to a specific flight.

### **Development Tools**
- **Git + GitHub** - Helps with managing project code and collaborating with team members.
- **Jira** - Used for tracking project tasks and planning sprints.
- **IDE:** VS Code - Serves as the main IDE for Java development and testing.

## Developer Documentation
### Setup and build
1. Install **JDK 17 or newer** (JDK 21 is supported). Set `JAVA_HOME` to that JDK.
   Run `java -version` and `javac -version` to check it. In VS Code or IntelliJ,
   import the Maven project and set the project SDK to JDK 17+.
2. Clone the repository and open its root folder, which contains `pom.xml`.
3. Run the Maven wrapper; a separate Maven installation is unnecessary:

   | Platform | Build and test |
   | --- | --- |
   | Windows PowerShell | `.\mvnw.cmd verify` |
   | macOS / Linux | `sh mvnw verify` |

   The first run downloads Maven and dependencies. Maven checks the JDK version early and
   compiles against Java 17 APIs. The test suite uses a local HTTP server and needs no FAA credentials.

### FAA NOTAM Management Service (NMS) configuration

Set credentials for the environment you intend to use. Missing settings are reported together.
Do not commit credentials.

| Variable | Purpose |
| --- | --- |
| `FAA_CLIENT_ID` | Required FAA client ID; trimmed before use |
| `FAA_CLIENT_SECRET` | Required FAA client secret; trimmed before use |
| `FAA_ENVIRONMENT` | `staging` (default) or `production` |
| `FAA_STAGING_TOKEN_URL` | Optional staging token URL override |
| `FAA_STAGING_NOTAM_URL` | Optional staging NOTAM URL override |
| `FAA_PRODUCTION_TOKEN_URL` | Required when selecting production |
| `FAA_PRODUCTION_NOTAM_URL` | Required when selecting production |

The staging defaults are:
- `https://api-staging.cgifederal-aim.com/v1/auth/token`
- `https://api-staging.cgifederal-aim.com/nmsapi/v1/notams`

Get production endpoint URLs and matching credentials from your FAA onboarding documentation.
Production mode requires both URLs and never falls back to staging. Configured URLs must use HTTPS
and contain no embedded user info, query, or fragment.

For example, in PowerShell:
```powershell
$env:FAA_CLIENT_ID = "<your staging client ID>"
$env:FAA_CLIENT_SECRET = "<your staging client secret>"
$env:FAA_ENVIRONMENT = "staging"
.\mvnw.cmd compile exec:java "-Dexec.args=KOKC KDFW"
```

For macOS / Linux:
```sh
export FAA_CLIENT_ID="<your staging client ID>"
export FAA_CLIENT_SECRET="<your staging client secret>"
export FAA_ENVIRONMENT=staging
sh mvnw compile exec:java -Dexec.args="KOKC KDFW"
```

The temporary demo takes **command-line arguments**; CAP-19 owns interactive user input.
With no arguments it uses KOKC and KDFW. It labels each response and prints at most 300 characters.
Identical normalized locations are requested and displayed only once.

### API layer contract
The input layer creates `LocationIdentifier` values, which trim, uppercase, and check the
3-5 alphanumeric character format before any network operation. This syntax check does not
verify that an airport exists. The API layer receives validated values:

```java
final LocationIdentifier start = new LocationIdentifier(" kokc ");
final LocationIdentifier end = new LocationIdentifier("KDFW");
final NmsApiClient client = NmsConfiguration.fromEnvironment();
final List<RawNotamResponse> responses = client.fetchNotamsForRoute(start, end);
for (final RawNotamResponse response : responses) {
    // Pass response.rawJson() to the parsing layer.
}
```

Import `java.util.List` for the example. `fetchNotamsByLocation(start)` returns the same immutable
list type. The list contains one response per distinct requested location, in request order.
NOTAM JSON remains unmodified; Jackson parses only the authentication response.

Invalid input/configuration throws `IllegalArgumentException`. Network failures and malformed
authentication responses throw `NmsApiException`. Unsuccessful HTTP responses throw its
`NmsHttpException` subtype, which exposes the actual status code without a sentinel value.

Tokens are cached per client with a 60-second expiry margin and synchronized refresh.
A missing lifetime means the token is used once without caching; invalid lifetimes are rejected.
A NOTAM 401 triggers one refresh/retry. Separate processes sharing credentials still require
confirmation of FAA's concurrent-token policy.

See [CAP-20 review follow-through](docs/CAP-20-review.md) for every mentor discussion,
test coverage, and the outstanding FAA token-policy investigation.

## Goals & Progress Plan
### **Development Goals**
- Retrieves NOTAMs based on the flight path.
- Formats NOTAM into an easily readable structure.
- Classifies NOTAMs based on the potential impact to operations.
- Displays the prioritized results through the command line.


### **Progress Plan**
- **Sprint 1:** Determine project architecture, review FAA API, complete setup of tools
- **Sprint 2:** Implement the NOTAM data retrieval and establish the parsing features
- **Sprint 3:** Add priority classification for NOTAMs, improve the output format for better readability, and possibly adding web implementation based on the group's progress
- **Sprint 4:** Prepare for the final submission by thoroughly testing project and refining the overall functionality

## Contributors
- **Brian Schettler - Client/Mentor**
- **Jakob Linenberger - Product Owner**  
- **Ricky Vincent - SM1** 
- **Johnpaul Nguyen - SM2** 
- **Trinity Tran - SM3 & Quality Assurance**   
- **Khai Nguyen - SM4**
