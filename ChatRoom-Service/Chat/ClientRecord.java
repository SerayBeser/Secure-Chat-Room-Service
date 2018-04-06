package Chat;
//  ClientRecord.java

import java.io.*;


public class ClientRecord {

    private ObjectOutputStream objectOutputStream;

    ClientRecord(ObjectOutputStream outputStream) {
        objectOutputStream = outputStream;

    }

    public ObjectOutputStream getOutputStream() {

        return objectOutputStream;
    }


}
