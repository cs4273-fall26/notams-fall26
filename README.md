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
### **Setup Instructions**
1. Clone the project using SSH or HTTPS:  
   `git clone <repository-url>`
2. Install Java 17 or higher.
3. Open the project in VS Code (necessary extensions must be installed).
4. Configure FAA API settings if required (API key or endpoint settings).
5. Compile the project using your preferred method (VS Code build tools or javac).
6. Run the program from the command line.

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

 
