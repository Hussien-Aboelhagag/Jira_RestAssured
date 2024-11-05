import io.restassured.RestAssured;
import io.restassured.filter.session.SessionFilter;
import io.restassured.path.json.JsonPath;
import org.testng.Assert;

import java.io.File;

import static io.restassured.RestAssured.given;

public class TestAddComment {
    public static void main(String[] args) {
        RestAssured.baseURI="http://localhost:8080";
        SessionFilter session=new SessionFilter();
        given().header("Content-type","application/json").body("""
                {
                    "username":"hussienaboelhagag",
                    "password":"Hu01129888329#"
                }""").filter(session).when().post("/rest/auth/1/session").then().log().all().assertThat().statusCode(200).extract().response().asString();
        //Adding a comment
        String expectedComment="assertion comment";
        String addCommentResponse =given().log().all().header("Content-type","application/json")
                .pathParam("key","10101").body("{\n" +
                        "    \"body\":\""+expectedComment+"\",\n" +
                        "    \"visibility\":{\n" +
                        "        \"type\":\"role\",\n" +
                        "        \"value\":\"Administrators\"\n" +
                        "    }\n" +
                        "}").filter(session).when().log().all().post("/rest/api/2/issue/{key}/comment")
                .then().assertThat().statusCode(201).extract().response().asString();
        JsonPath addCommentJs=new JsonPath(addCommentResponse);
        String commentId=addCommentJs.get("id").toString();
        //Adding an attachment
        given().log().all().header("X-Atlassian-Token","no-check")
                .header("Content-type","multipart/form-data").filter(session)
                .pathParam("key","10101").multiPart("file",new File("myfile.txt"))
                .when().post("/rest/api/2/issue/{key}/attachments").then().assertThat().statusCode(200);
        //get issue and filter it with query parameters
        String getFilterComment=given().filter(session).pathParam("key","10101").queryParam("fields","comment")
                .when().get("rest/api/2/issue/{key}")
                .then().log().all().assertThat().statusCode(200).extract().response().asString();
        JsonPath filterJs=new JsonPath(getFilterComment);
        int commentsSize=filterJs.getInt("fields.comment.comments.size()");
        for (int i=0;i<commentsSize;i++){
            String actualCommentID =filterJs.getString("fields.comment.comments["+i+"].id");
            if (actualCommentID.equals(commentId)){
                String actualCommentBody=filterJs.getString("fields.comment.comments["+i+"].body");
                Assert.assertEquals(actualCommentBody,expectedComment);
                System.out.println(actualCommentID);
                System.out.println(actualCommentBody);
            }
        }
    }
}
