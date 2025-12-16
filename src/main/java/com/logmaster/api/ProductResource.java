package com.logmaster.api;

import com.logmaster.dao.ProductDAO;
import com.logmaster.entity.Product;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Path("/products")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class ProductResource {

    @Inject
    private ProductDAO productDAO;

    @GET
    public Response getAllProducts() {
        List<Product> products = productDAO.findAll();
        return Response.ok(productsToJson(products)).build();
    }

    @GET
    @Path("/{id}")
    public Response getProduct(@PathParam("id") Long id) {
        Optional<Product> product = productDAO.findById(id);
        if (product.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Product not found"))
                    .build();
        }
        return Response.ok(productToJson(product.get())).build();
    }

    @GET
    @Path("/instock")
    public Response getInStockProducts() {
        List<Product> products = productDAO.findInStock();
        return Response.ok(productsToJson(products)).build();
    }

    @GET
    @Path("/category/{category}")
    public Response getByCategory(@PathParam("category") String category) {
        List<Product> products = productDAO.findByCategory(category);
        return Response.ok(productsToJson(products)).build();
    }

    @POST
    public Response createProduct(Map<String, Object> body) {
        try {
            Product product = new Product();
            product.setName((String) body.get("name"));
            product.setPrice(((Number) body.get("price")).doubleValue());
            product.setStock(((Number) body.get("stock")).intValue());
            product.setCategory((String) body.get("category"));

            Product created = productDAO.create(product);
            return Response.status(Response.Status.CREATED)
                    .entity(productToJson(created))
                    .build();

        } catch (Exception e) {
            return Response.status(Response.Status.BAD_REQUEST)
                    .entity(errorJson("Invalid data: " + e.getMessage()))
                    .build();
        }
    }

    @PUT
    @Path("/{id}")
    public Response updateProduct(@PathParam("id") Long id, Map<String, Object> body) {
        Optional<Product> existing = productDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Product not found"))
                    .build();
        }

        Product product = existing.get();
        if (body.containsKey("name"))
            product.setName((String) body.get("name"));
        if (body.containsKey("price"))
            product.setPrice(((Number) body.get("price")).doubleValue());
        if (body.containsKey("stock"))
            product.setStock(((Number) body.get("stock")).intValue());
        if (body.containsKey("category"))
            product.setCategory((String) body.get("category"));

        Product updated = productDAO.update(product);
        return Response.ok(productToJson(updated)).build();
    }

    @DELETE
    @Path("/{id}")
    public Response deleteProduct(@PathParam("id") Long id) {
        Optional<Product> existing = productDAO.findById(id);
        if (existing.isEmpty()) {
            return Response.status(Response.Status.NOT_FOUND)
                    .entity(errorJson("Product not found"))
                    .build();
        }

        productDAO.delete(id);
        return Response.noContent().build();
    }

    // Helper methods
    private List<Map<String, Object>> productsToJson(List<Product> products) {
        return products.stream().map(this::productToJson).collect(java.util.stream.Collectors.toList());
    }

    private Map<String, Object> productToJson(Product product) {
        Map<String, Object> json = new HashMap<>();
        json.put("id", product.getId());
        json.put("name", product.getName());
        json.put("price", product.getPrice());
        json.put("stock", product.getStock());
        json.put("category", product.getCategory());
        return json;
    }

    private Map<String, String> errorJson(String message) {
        Map<String, String> error = new HashMap<>();
        error.put("error", message);
        return error;
    }
}
