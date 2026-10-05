# NOTAMs
## Project Description
This project focuses on finding and organizing Notices to Air Missions (NOTAMs) in order to improve the readability of information before a flight. Pilots are required to review NOTAMs to avoid unexpected hazards throughout their flight, but the current system does not determine the priority of notices. As a result, pilots cannot easily see which notices could affect their operation because high priority notices are surrounded by less important data. This project provides a structured format that organizes NOTAMs in order to present the information in a clear layout. The feature will highlight significant notices so pilots could quickly identify information that directly affects their flight. By reducing clutter and improving NOTAM organization, the system improves flight planning and ensures that important details are easy to see.


## Technologies & Tools
### **Languages**
- **Java 17+** - Used for all of the core functionality and required by the project.

### **API and Data Sources**
- **FAA NOTAM API** - Retrieves every NOTAM that is relevant to a specific flight.

### **Development Tools**
- **Apache Maven** - Builds the project, downloads dependencies, and runs tests.
- **Git + GitHub** - Helps with managing project code and collaborating with team members.
- **Jira** - Used for tracking project tasks and planning sprints.
- **IDE:** VS Code - Serves as the main IDE for Java development and testing.

## Developer Documentation
### **Requirements**
- **JDK 17 or newer**. The project targets Java 17, as configured in `pom.xml`.
- **Apache Maven** installed, with its `bin` folder added to your system's `PATH` so the `mvn` command works.
- **Git** installed to clone the repository.

Set `JAVA_HOME` to your JDK installation folder. Open a new terminal after changing environment variables, then check your setup:

```bash
java -version
javac -version
mvn -version
```

The Java version shown by Maven must be 17 or newer. If `mvn` is not recognized, check that Maven's `bin` folder is on your `PATH` and reopen the terminal.

### **Clone the Project**
```bash
git clone https://github.com/cs4273-fall26/notams-fall26.git
cd notams-fall26
```

If you already cloned the project, open a terminal in your existing project folder instead. Run the commands below from the folder containing `pom.xml`.

You can also open the folder in VS Code or IntelliJ. Configure the IDE to use JDK 17 or newer and import the project as a Maven project.

### **Build the Project**
```bash
mvn clean package
```

This removes previous build output, compiles the code, runs the tests, and creates the JAR in `target/`. Maven downloads the required dependencies on the first run, so an internet connection is needed. A successful build ends with `BUILD SUCCESS`.

### **Run the Tests**
```bash
mvn test
```

This compiles the code as needed and runs the unit tests through Maven. Check the test summary for failures or errors. Detailed test reports are saved in `target/surefire-reports/`.

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

 
