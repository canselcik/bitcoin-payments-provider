package controllers;

import internal.BitcoindRPCWrapper;
import play.*;
import play.mvc.*;

import views.html.*;

public class Interface extends Controller {
    public static Result index() {
        return ok(BitcoindRPCWrapper.getInfo("http://localhost:8332/",
                                             "bitcoinrpc",
                                             "6bGCbjN24TvpAKVJyUfWDvpHzat8HYnVrBmFhaxRfEFn"));
    }

    public static Result getUserById(String id) {
        return Results.TODO;
    }

    public static Result getUserByName(String name){
        return Results.TODO;
    }
}
