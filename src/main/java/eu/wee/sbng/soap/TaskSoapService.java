package eu.wee.sbng.soap;

import jakarta.jws.WebMethod;
import jakarta.jws.WebService;

import java.util.List;

@WebService
public interface TaskSoapService {

    @WebMethod
    List<Task> getTasks();
}
