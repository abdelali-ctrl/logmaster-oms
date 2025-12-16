package com.logmaster.api;

import com.logmaster.dao.UserDAO;
import com.logmaster.entity.User;
import com.logmaster.entity.UserRole;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/users")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class UserResource {

    @Inject
    private UserDAO userDAO;

    @GET
    public Response getAllUsers() {
        List<User> users = userDAO.findAll();
        return Response.ok(usersToJson(users)).build();
    }

    @GET
    @Path("/{id}")
    public Response getUser(@PathParam("id") Long id) {
        Optional<User> user = userDAO.findById(id);
        if (user.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("User not found"))
                    .build();
        }
        return Response.ok(userToJson(user.get())).build();
    }

    @POST
    public Response createUser(Map<String, String> body) {
        try {
            String email = body.get("email");

            // Check if email exists
            if (userDAO.findByEmail(email).isPresent()) {
                return Response.status(Response.Status.CONFLICT)
                        .entity(errorJson("Email already exists"))
                        .build();
            }

            User user = new User();
            user.setName(body.get("name"));
            user.setEmail(email);
            user.setPassword(body.get("password"));
            user.setRole(UserRole.USER);

            User created = userDAO.create(user);
            return Response.status(Response.Status.CREATED)
                    .entity(userToJson(created))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Invalid data: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateUser(@PathParam("id") Long id, Map<String, String> body) {
        Optional<User> existing = userDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("User not found"))
                    .build();
        }

        User user = existing.get();
        if (body.containsKey("name"))
            user.setName(body.get("name"));
        if (body.containsKey("email"))
            user.setEmail(body.get("email"));
        if (body.containsKey("password") && !body.get("password").isEmpty()) {
            user.setPassword(body.get("password"));
        }

        User updated = userDAO.update(user);
        return Response.ok(userToJson(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteUser(@PathParam("id") Long id) {
        Optional<User> existing = userDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("User not found"))
                    .build();
        }

        userDAO.delete(id);
        return Response.noContent().build();
    }

    @POST
    @Path("/login")
    public Response login(Map<String, String> body) {
        String email = body.get("email");
        String password = body.get("password");

        Optional<User> userOpt = userDAO.findByEmail(email);
        if (userOpt.isEmpty() || !userOpt.get().getPassword().equals(password)) {
            return Response.status(Response.Status.UNAUTHORIZED)
                    .entity(errorJson("Invalid credentials"))
                    .build();
        }

        User user = userOpt.get();
        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("user", userToJson(user));
        result.put("message", "Login successful");

        return Response.ok(result).build();
    }

    // Helper methods
    private List<Map<String, Object>> usersToJson(List<User> users) {
        return users.stream().map(this::userToJson).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> userToJson(User user) {
        Map<String, Object> json = new HashMap<>();
        json.put("id", user.getId());
        json.put("name", user.getName());
        json.put("email", user.getEmail());
        json.put("role", user.getRole().toString());
        // Don't expose password in JSON!
        return json;
    }

    private Map<String, String> errorJson(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
