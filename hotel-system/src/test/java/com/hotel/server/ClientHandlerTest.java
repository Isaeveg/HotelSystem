package com.hotel.server;

import com.hotel.common.Request;
import com.hotel.common.RequestType;
import com.hotel.common.Response;
import com.hotel.common.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.*;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ClientHandlerTest {

    @Mock
    private Socket socket;

    @Test
    void testLoginSuccess() throws IOException, ClassNotFoundException {
        // Arrange
        String login = "test@example.com";
        String password = "password";
        Request request = new Request(RequestType.LOGIN, new String[] { login, password });
        User mockUser = new User(1, 1, login, "CLIENT");

        // Serialize the request into a byte array (simulate client sending data)
        ByteArrayOutputStream requestFlow = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(requestFlow);
        objOut.writeObject(request);
        objOut.flush();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestFlow.toByteArray());

        // Capture server output
        ByteArrayOutputStream responseFlow = new ByteArrayOutputStream();

        try (MockedStatic<DatabaseHandler> dbHandler = Mockito.mockStatic(DatabaseHandler.class)) {
            // Mock DB response
            dbHandler.when(() -> DatabaseHandler.loginUser(login, password)).thenReturn(mockUser);

            // Mock Socket streams
            when(socket.getInputStream()).thenReturn(inputStream);
            when(socket.getOutputStream()).thenReturn(responseFlow);

            // Act
            ClientHandler handler = new ClientHandler(socket);
            handler.run();

            // Assert
            // Deserialize the server's response
            try (ObjectInputStream objIn = new ObjectInputStream(
                    new ByteArrayInputStream(responseFlow.toByteArray()))) {
                Response response = (Response) objIn.readObject();
                assertTrue(response.isSuccess());
                assertEquals("Success", response.getMessage());
                assertTrue(response.getData() instanceof User);
            }
        }
    }

    @Test
    void testLoginFailure() throws IOException, ClassNotFoundException {
        // Arrange
        String login = "wrong@example.com";
        String password = "wrong";
        Request request = new Request(RequestType.LOGIN, new String[] { login, password });

        // Serialize request
        ByteArrayOutputStream requestFlow = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(requestFlow);
        objOut.writeObject(request);
        objOut.flush();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestFlow.toByteArray());

        // Capture output
        ByteArrayOutputStream responseFlow = new ByteArrayOutputStream();

        try (MockedStatic<DatabaseHandler> dbHandler = Mockito.mockStatic(DatabaseHandler.class)) {
            dbHandler.when(() -> DatabaseHandler.loginUser(login, password)).thenReturn(null);

            when(socket.getInputStream()).thenReturn(inputStream);
            when(socket.getOutputStream()).thenReturn(responseFlow);

            // Act
            ClientHandler handler = new ClientHandler(socket);
            handler.run();

            // Assert
            try (ObjectInputStream objIn = new ObjectInputStream(
                    new ByteArrayInputStream(responseFlow.toByteArray()))) {
                Response response = (Response) objIn.readObject();
                assertFalse(response.isSuccess());
                assertEquals("Invalid login or password", response.getMessage());
                assertNull(response.getData());
            }
        }
    }

    @Test
    void testRegisterSuccess() throws IOException, ClassNotFoundException {
        // Arrange
        String[] regData = { "John", "Doe", "john@test.com", "123456", "pass" };
        Request request = new Request(RequestType.REGISTER, regData);

        // Serialize request
        ByteArrayOutputStream requestFlow = new ByteArrayOutputStream();
        ObjectOutputStream objOut = new ObjectOutputStream(requestFlow);
        objOut.writeObject(request);
        objOut.flush();
        ByteArrayInputStream inputStream = new ByteArrayInputStream(requestFlow.toByteArray());

        // Capture output
        ByteArrayOutputStream responseFlow = new ByteArrayOutputStream();

        try (MockedStatic<DatabaseHandler> dbHandler = Mockito.mockStatic(DatabaseHandler.class)) {
            dbHandler.when(() -> DatabaseHandler.registerUser(any(), any(), any(), any(), any())).thenReturn(true);

            when(socket.getInputStream()).thenReturn(inputStream);
            when(socket.getOutputStream()).thenReturn(responseFlow);

            // Act
            ClientHandler handler = new ClientHandler(socket);
            handler.run();

            // Assert
            try (ObjectInputStream objIn = new ObjectInputStream(
                    new ByteArrayInputStream(responseFlow.toByteArray()))) {
                Response response = (Response) objIn.readObject();
                assertTrue(response.isSuccess());
                assertEquals("Account created successfully!", response.getMessage());
            }
        }
    }
}
