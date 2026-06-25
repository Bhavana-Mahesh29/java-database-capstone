##Section 1: Architecture summary

The Smart Clinic Management System follows a three-tier architecture. The Presentation Layer contains Thymeleaf pages and REST API clients. Requests are handled by Controllers, business rules are implemented in the Service Layer, and data access is performed through Repositories. The application uses MySQL for structured relational data such as patients, doctors, and appointments, and MongoDB for flexible document-based data such as prescriptions. Spring Boot integrates these components, while Docker and CI/CD enable consistent deployment and automated delivery

##Section 2: Numbered flow of data and control

1.The user accesses the application through a Thymeleaf web page (such as the Admin or Doctor Dashboard) or a REST API client (such as the Appointment or Patient module).
2.The request is routed to the appropriate Thymeleaf Controller or REST Controller, depending on the type of request.
3.The controller forwards the request to the Service Layer for processing.
4.The Service Layer applies the business logic, performs validations, and communicates with the appropriate Repository Layer.
5.The repositories access the required database—MySQL for relational data or MongoDB for document-based data—and retrieve or store the information.
6.The retrieved data is mapped into JPA Entity objects (for MySQL) or Document objects (for MongoDB) and returned to the service layer.
7.The controller sends the processed data back to the user as either a Thymeleaf-rendered HTML page or a JSON response through the REST API.
