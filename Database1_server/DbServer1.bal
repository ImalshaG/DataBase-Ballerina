import ballerina/http;
import ballerina/log;
import ballerina/io;
import ballerina/sql;
import ballerinax/jdbc;

jdbc:Client DataBase1 = new({
        url: "jdbc:mysql://localhost:3306/gunasekara_holdings",
        username: "root",
        password: "",
        poolOptions: { maximumPoolSize: 5 },
        dbOptions: { useSSL: false }
    });
type Customer record {
    int id;
    string vehicleNo;
    string name;
    int quantity;
};

listener http:Listener httpListener = new(9092);
//map<json> ordersMap = {"Student":{"ID":2,"age":26,"name":"Thar"}};
@http:ServiceConfig { basePath: "/customerinfo" }

service orderMgt on httpListener {

    @http:ResourceConfig {
        methods: ["GET"],
        path: "/getinfo"
    }
    resource function showOders(http:Caller caller, http:Request req) {
        //json? payload=ordersMap["Student"];
        io:println("\nDisplay data from a table");
        var selectRet = DataBase1->select("SELECT * FROM customer_info", Customer, loadToMemory=true);
        table<Customer> dt;
        if (selectRet is table<Customer>) {
            var jsonConversionRet = json.convert(selectRet);
            if (jsonConversionRet is json) {
                //foreach var row in selectRet {
                //    io:println("Customer:" + row.id + "|" + row.vehicleNo + "|" + row.name+"|"+row.quantity);
                //}
                http:Response response = new;
                if (jsonConversionRet == null) {
                    jsonConversionRet = "Order cannot be found.";
                }
                response.setJsonPayload(untaint jsonConversionRet);
                var result = caller->respond(response);
                if (result is error) {
                    log:printError("Error sending response", err = result);
                }
            } else {
                io:println("Error in table to json conversion");
            }

        } else {
            io:println("Select data from student table failed: "
                    + <string>selectRet.detail().message);
        }
    }

    @http:ResourceConfig {
        methods: ["POST"],
        path: "/addinfo"
    }
    resource function addOrder(http:Caller caller, http:Request req) {
        http:Response response = new;
        var orderReq = req.getJsonPayload();
        if (orderReq is json) {
            int|error quantity=int.convert(orderReq.quantity.toString());
            //int age = 20;
            string vehicleNo = orderReq.vehicleNo.toString();
            string name = orderReq.name.toString();
            if (quantity is int) {
                var ret = DataBase1->update("INSERT INTO customer_info(vehicle_no, customer_name, quantity) values (?, ?, ?)",
                    vehicleNo, name, quantity);
                handleUpdate(ret, "Insert to table ");
            }else{
                io:println("Error in conversion to int");

            }
            json payload = { status: "Order Created."};
            response.setJsonPayload(untaint payload);
            response.statusCode = 201;
            var result = caller->respond(response);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }
        } else {
            response.statusCode = 400;
            response.setPayload("Invalid payload received");
            var result = caller->respond(response);
            if (result is error) {
                log:printError("Error sending response", err = result);
            }

        }
    }
}
function handleUpdate(int|error returned, string message) {
    if (returned is int) {
        io:println(message + " status: " + returned);
    } else {
        io:println(message + " failed: " + <string>returned.detail().message);
    }
}