package com.itm.space.backendresources;

import com.itm.space.backendresources.api.request.UserRequest;
import com.itm.space.backendresources.exception.BackendResourcesException;
import com.itm.space.backendresources.mapper.UserMapper;
import com.itm.space.backendresources.api.response.UserResponse;
import com.itm.space.backendresources.service.UserServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class UserServiceIntegrationTest {

    @InjectMocks
    private UserServiceImpl userService;

    @Mock
    private Keycloak keycloakClient;

    @Mock
    private UserMapper userMapper;

    @Mock
    private RealmResource realmResource;

    @Mock
    private UsersResource usersResource;

    @Mock
    private org.keycloak.admin.client.resource.UserResource userResource;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        when(keycloakClient.realm(anyString())).thenReturn(realmResource);

        when(realmResource.users()).thenReturn(usersResource);
    }

    @Test
    public void testCreateUser_Success() {
        UserRequest userRequest = new UserRequest("username", "email@example.com", "password", "firstName", "lastName");

        CredentialRepresentation credential = new CredentialRepresentation();
        credential.setType(CredentialRepresentation.PASSWORD);
        credential.setValue("password");

        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("username");
        userRepresentation.setEmail("email@example.com");
        userRepresentation.setCredentials(Collections.singletonList(credential));

        try (Response response = mock(Response.class)) {
            when(response.getStatus()).thenReturn(201);
            when(response.getStatusInfo()).thenReturn(Status.CREATED);
            when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

            String createdId = UUID.randomUUID().toString();
            when(CreatedResponseUtil.getCreatedId(response)).thenReturn(createdId);

            userService.createUser(userRequest);

            verify(usersResource).create(any(UserRepresentation.class));
        }
    }

    @Test
    public void testCreateUser_Failure() {
        UserRequest userRequest = new UserRequest("username", "email@example.com", "password", "firstName", "lastName");

        try (Response response = mock(Response.class)) {
            when(response.getStatus()).thenReturn(409);
            when(response.getStatusInfo()).thenReturn(Status.CONFLICT);
            when(usersResource.create(any(UserRepresentation.class))).thenReturn(response);

            BackendResourcesException thrownException = assertThrows(BackendResourcesException.class, () -> userService.createUser(userRequest));
            assertEquals("User already exists", thrownException.getMessage());
        }
    }

    @Test
    public void testGetUserById_Success() {
        UUID userId = UUID.randomUUID();
        UserRepresentation userRepresentation = new UserRepresentation();
        userRepresentation.setUsername("username");
        userRepresentation.setEmail("email@example.com");

        when(usersResource.get(userId.toString())).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(userRepresentation);

        UserResponse userResponse = new UserResponse("firstName", "lastName", "email@example.com", Collections.emptyList(), Collections.emptyList());
        when(userMapper.userRepresentationToUserResponse(any(UserRepresentation.class), anyList(), anyList())).thenReturn(userResponse);

        UserResponse result = userService.getUserById(userId);

        assertEquals("firstName", result.getFirstName());
        assertEquals("lastName", result.getLastName());
        assertEquals("email@example.com", result.getEmail());
    }

    @Test
    public void testGetUserById_NotFound() {
        UUID userId = UUID.randomUUID();

        when(usersResource.get(userId.toString())).thenThrow(new WebApplicationException("User not found", Status.NOT_FOUND));

        assertThrows(BackendResourcesException.class, () -> userService.getUserById(userId));
    }
}