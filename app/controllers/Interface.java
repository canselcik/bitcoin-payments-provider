package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Interface extends Controller {
    public static Result index() {
        return ok("OK");
    }

    public static Result getUserById(String id) {
        return Results.TODO;
    }

    public static Result getUserByName(String name) {
        return Results.TODO;
    }
}
