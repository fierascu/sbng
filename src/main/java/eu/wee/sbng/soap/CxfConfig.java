package eu.wee.sbng.soap;

import jakarta.xml.ws.Endpoint;
import org.apache.cxf.Bus;
import org.apache.cxf.jaxws.EndpointImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CxfConfig {

    @Autowired
    private Bus bus;

    // published under the CXFServlet's default "/services" mapping (see cxf.path),
    // so the WSDL is at http://localhost:8080/services/tasks?wsdl
    @Bean
    public Endpoint taskSoapServiceEndpoint() {
        EndpointImpl endpoint = new EndpointImpl(bus, new TaskSoapServiceImpl());
        endpoint.publish("/tasks");
        return endpoint;
    }
}
