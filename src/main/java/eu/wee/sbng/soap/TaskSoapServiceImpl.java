package eu.wee.sbng.soap;

import jakarta.jws.WebService;

import java.util.List;

// mocked data - no repository/persistence behind this, just a fixed response to
// demonstrate a real Apache CXF (JAX-WS) endpoint alongside the REST controllers.
@WebService(endpointInterface = "eu.wee.sbng.soap.TaskSoapService")
public class TaskSoapServiceImpl implements TaskSoapService {

    @Override
    public List<Task> getTasks() {
        return List.of(
                new Task("Write CXF demo", true),
                new Task("Publish WSDL", true),
                new Task("Call it from a SOAP client", false)
        );
    }
}
