package controllers;

import play.*;
import play.mvc.*;

import views.html.*;

public class Management extends Controller {
    public static Result index() {
        return ok("OK");
    }

    public static Result initDB() {
        return Results.TODO;
    }

    public static Result resetDB() {
        return Results.TODO;
    }

    public static Result txNotify() {
        return Results.TODO;
    }
}
