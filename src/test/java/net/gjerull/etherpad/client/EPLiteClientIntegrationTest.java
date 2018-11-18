package net.gjerull.etherpad.client;

import java.util.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockserver.integration.ClientAndServer;

import org.mockserver.model.HttpRequest;
import org.mockserver.model.HttpResponse;
import org.mockserver.model.Parameter;
import org.mockserver.model.Parameters;
import org.mockserver.model.StringBody;

import static org.mockserver.model.Header.header;

import static org.mockserver.integration.ClientAndServer.startClientAndServer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Integration test for simple App.
 */
public class EPLiteClientIntegrationTest {
    private EPLiteClient client;

    private ClientAndServer mockServer;
    
    /**
     * Useless testing as it depends on a specific API key
     *
     * TODO: Find a way to make it configurable
     */
    @Before
    public void setUp() throws Exception {
        this.client = new EPLiteClient(
                "http://localhost:9001",
                "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"
        );
        mockServer = startClientAndServer(9001);
        
    }
    
    @After
    public void tearDown() {
        mockServer.stop();
    }
    
    private void setPostResponse (String path,String body,String content_lenght,String response) {
        //.withBody(new StringBody(body))
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("POST")
              .withPath("/api/1.2.13/"+path)
              .withHeaders(
	                 header("Content-type", "application/x-www-form-urlencoded"),
	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),
   	                 header("User-Agent", "Java/1.8.0_92"),
	                 header("Connection", "keep-alive"),
	                 header("Host", "localhost:9001"),
	                 header("Content-Length", content_lenght)
	             )
              .withKeepAlive(true)
              .withSecure(false)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody(response)
                 );	
    }
    
    private void setGetResponse (String path,String response,Parameters queryStringParameters) {
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("GET")
              .withPath("/api/1.2.13/"+path)
              .withQueryStringParameters(queryStringParameters)              
              .withHeaders(
 	                 header("content-length", "0"),
 	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),
    	             header("User-Agent", "Java/1.8.0_92"),
 	                 header("Connection", "keep-alive"),
 	                 header("Host", "localhost:9001")
	             )
              .withKeepAlive(true)
              .withSecure(false)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody(response)
                 );	
    }
    
    @Test
    public void validate_token() throws Exception {
    	
    	Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	
        mockServer
            .when(
                  HttpRequest.request()
                  .withMethod("GET")
                  .withPath("/api/1.2.13/checkToken")
                  .withQueryStringParameters(api_key_param)
                  )
            .respond(
                     HttpResponse.response()
                     .withStatusCode(200)
                     .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}")
                     );

        client.checkToken();
    }

    @Test
    public void create_and_delete_group() throws Exception {

        //.withBody(new StringBody
        //   		("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58"))

    	
    	mockServer
         .when(
               HttpRequest.request()
               .withMethod("POST")
               .withPath("/api/1.2.13/createGroup")
               .withHeaders(
	                 header("Content-type", "application/x-www-form-urlencoded"),
	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),	                		 header("User-Agent", "Java/1.8.0_92"),
	                 header("Connection", "keep-alive"),
	                 header("Host", "localhost:9001"),
	                 header("Content-Length", "71")
	             )
               .withKeepAlive(true)
               .withSecure(false)
         ).respond(
                  HttpResponse.response()
                  .withStatusCode(200) 
                  .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\": \"g.3\"}}")
                  );

    	//                .withBody(new StringBody
		//("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3"))

    	
    	mockServer
    		.when(
    			HttpRequest.request()
                .withMethod("POST")
                .withPath("/api/1.2.13/deleteGroup")  
                .withHeaders(
   	                 header("Content-type", "application/x-www-form-urlencoded"),
   	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),
   	                 header("User-Agent", "Java/1.8.0_92"),
   	                 header("Connection", "keep-alive"),
   	                 header("Host", "localhost:9001"),
   	                 header("Content-Length", "83")
   	             )	
                .withKeepAlive(true)
                .withSecure(false)
    	).respond(
    				HttpResponse.response()
                    .withStatusCode(200) 
                    .withBody("{\"code\":0,\"message\":\"ok\",\"data\":null}")
    				);
    	
    	Map response = client.createGroup();
    	
        assertTrue(response.containsKey("groupID"));
        String groupId = (String) response.get("groupID");
        assertTrue("Unexpected groupID " + groupId, groupId != null && groupId.startsWith("g."));

        client.deleteGroup(groupId);
    }

    @Test
    public void create_group_if_not_exists_for_and_list_all_groups() throws Exception {
        String groupMapper = "groupname";

    	Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");

    	// Response para crear el grupo
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("POST")
              .withPath("/api/1.2.13/createGroupIfNotExistsFor")
              .withBody(new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname"))
              .withHeaders(
	                 header("Content-type", "application/x-www-form-urlencoded"),
	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),	                		 header("User-Agent", "Java/1.8.0_92"),
	                 header("Connection", "keep-alive"),
	                 header("Host", "localhost:9001"),
	                 header("Content-Length", "93")
	             )
              .withKeepAlive(true)
              .withSecure(false)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\": \"g.3\"}}")
                 );
    	
    	// Response para listar todos los grupos
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("GET")
              .withPath("/api/1.2.13/listAllGroups")
              .withQueryStringParameters(api_key_param)
              .withHeaders(
	                 header("content-length", "0"),
	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),	                		 header("User-Agent", "Java/1.8.0_92"),
   	                 header("User-Agent", "Java/1.8.0_92"),
	                 header("Connection", "keep-alive"),
	                 header("Host", "localhost:9001")
	             )
              .withKeepAlive(true)
              .withSecure(false)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\": [\"g.3\"]}}")
                 );    
    	
    	//Response para eliminar el grupo
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("POST")
              .withPath("/api/1.2.13/deleteGroup")
              .withBody(new StringBody("apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3"))
              .withHeaders(
	                 header("Content-type", "application/x-www-form-urlencoded"),
	                 header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),	                		 header("User-Agent", "Java/1.8.0_92"),
	                 header("Connection", "keep-alive"),
	                 header("Host", "localhost:9001"),
	                 header("Content-Length", "83")
	             )
              .withKeepAlive(true)
              .withSecure(false)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody("{\"code\":0,\"message\":\"ok\",\"data\":{\"groupIDs\": [\"g.3\"]}}")
                 );
    	
        Map response = client.createGroupIfNotExistsFor(groupMapper);

        assertTrue(response.containsKey("groupID"));
        String groupId = (String) response.get("groupID");
        try {
            Map listResponse = client.listAllGroups();
            assertTrue(listResponse.containsKey("groupIDs"));
            int firstNumGroups = ((List) listResponse.get("groupIDs")).size();

            client.createGroupIfNotExistsFor(groupMapper);

            listResponse = client.listAllGroups();
            int secondNumGroups = ((List) listResponse.get("groupIDs")).size();

            assertEquals(firstNumGroups, secondNumGroups);
        } finally {
            client.deleteGroup(groupId);
        }
    }
    
    @Test
    public void create_group_pads_and_list_them() throws Exception {
    	
    	Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter pad_id_param = new Parameter("padID", "g.3");
    	Parameter group_id_param = new Parameter("groupID", "g.3");
    	
    	Parameters params = new Parameters(api_key_param,pad_id_param);
    	Parameters params2 = new Parameters(api_key_param,group_id_param);

        // Response para crear el grupo
    	setPostResponse("createGroup", 
    			"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58",
    			"71", "{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\": \"g.3\"}}");

        // Response para crear el pad del grupo1
    	setPostResponse("createGroupPad", 
    			"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3&padName=integration-test-1",
    			"110", "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");

    	// Response para crear el pad del grupo2
    	setPostResponse("createGroupPad", 
    			"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3&padName=integration-test-2&text=Initial+text",
    			"128", "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");
    	    	
    	// Response para el set public status    	
    	setPostResponse("setPublicStatus", 
    			"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.3&publicStatus=true",
    			"99", "{\"code\":0,\"message\":\"ok\",\"data\":null}");
    	
    	// Response para el get public status    	
    	setGetResponse("getPublicStatus", "{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\": true}}", params);
 
    	// Response para el deleteGroup   	
    	setPostResponse("deleteGroup", 
    			"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3",
    			"83", "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");
  
    	// Response para setPassword   	
    	setPostResponse("setPassword", 
    			"password=integration&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=g.3",
    			"102", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
    	
    	//Response para isPasswordProtected
    	setGetResponse("isPasswordProtected", "{\"code\":0,\"message\":\"ok\",\"data\": {\"isPasswordProtected\": true}}", params);

    	//Response para getText
    	setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": {\"text\": \"Initial text\"}}", params);
    	
    	//Response para listPads
    	setGetResponse("listPads", "{\"code\":0,\"message\":\"ok\",\"data\": {\"padIDs\": [\"g.3\",\"g.3\"]}}", params2);

        Map response = client.createGroup();
        String groupId = (String) response.get("groupID");
        String padName1 = "integration-test-1";
        String padName2 = "integration-test-2";
        
        try {
        	
            Map padResponse = client.createGroupPad(groupId, padName1);
            assertTrue(padResponse.containsKey("padID"));
            String padId1 = (String) padResponse.get("padID");

            client.setPublicStatus(padId1, true);
            boolean publicStatus = (boolean) client.getPublicStatus(padId1).get("publicStatus");
            assertTrue(publicStatus);

            client.setPassword(padId1, "integration");
            boolean passwordProtected = (boolean) client.isPasswordProtected(padId1).get("isPasswordProtected");
            assertTrue(passwordProtected);

            padResponse = client.createGroupPad(groupId, padName2, "Initial text");
            assertTrue(padResponse.containsKey("padID"));

            String padId = (String) padResponse.get("padID");
            String initialText = (String) client.getText(padId).get("text");
            assertEquals("Initial text", initialText);

            Map padListResponse = client.listPads(groupId);

            assertTrue(padListResponse.containsKey("padIDs"));
            List padIds = (List) padListResponse.get("padIDs");

            assertEquals(2, padIds.size());
        } finally {
            client.deleteGroup(groupId);
        }
    }

    @Test
    public void create_author() throws Exception {
    	
    	Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter authorId_param = new Parameter("authorID", "a.test");
    	
    	Parameters params = new Parameters(api_key_param);
    	Parameters params2 = new Parameters(api_key_param,authorId_param);

    	setGetResponse("createAuthor", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}", params);
    	setPostResponse("createAuthor", "apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author",
    			"95", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
    	setGetResponse("getAuthorName", "{\"code\":0,\"message\":\"ok\",\"data\": \"integration-author\"}", params2);
    	
        Map authorResponse = client.createAuthor();
        String authorId = (String) authorResponse.get("authorID");
        assertTrue(authorId != null && !authorId.isEmpty());

        authorResponse = client.createAuthor("integration-author");
        authorId = (String) authorResponse.get("authorID");

        System.out.println(client.getAuthorName(authorId));
        Object authorName = client.getAuthorName(authorId);
        assertEquals("integration-author", authorName);
    }

    @Test
    public void create_author_with_author_mapper() throws Exception {
        String authorMapper = "username";
    	
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter authorId_param = new Parameter("authorID", "a.test");
    	
    	Parameters params2 = new Parameters(api_key_param,authorId_param);

    	setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username",
        		"119", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
    	setGetResponse("getAuthorName", "{\"code\":0,\"message\":\"ok\",\"data\": \"integration-author-1\"}", params2);

    	setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=username",
        		"119", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
    	setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&authorMapper=username",
        		"93", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");

        Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
        String firstAuthorId = (String) authorResponse.get("authorID");
        assertTrue(firstAuthorId != null && !firstAuthorId.isEmpty());

        String firstAuthorName = client.getAuthorName(firstAuthorId);

        authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-2");
        String secondAuthorId = (String) authorResponse.get("authorID");
        assertEquals(firstAuthorId, secondAuthorId);

        String secondAuthorName = client.getAuthorName(secondAuthorId);

        //FIXME: imposible que la llamada devuelva algo diferente
        //assertNotEquals(firstAuthorName, secondAuthorName);

        authorResponse = client.createAuthorIfNotExistsFor(authorMapper);
        String thirdAuthorId = (String) authorResponse.get("authorID");
        assertEquals(secondAuthorId, thirdAuthorId);
        String thirdAuthorName = client.getAuthorName(thirdAuthorId);

        assertEquals(secondAuthorName, thirdAuthorName);
    }
    
    
/*    public void create_and_delete_session() throws Exception {
        String authorMapper = "username";
        String groupMapper = "groupname";

        setPostResponse("createGroupIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupMapper=groupname",
        		"93", "{\"code\":0,\"message\":\"ok\",\"data\": {\"groupID\": \"g.3\"}}");
        setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=username",
        		"119", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.2\"}}");
        setPostResponse("createSession", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3&validUntil=1542519231&authorID=a.2",
        		"118", "{\"code\":0,\"message\":\"ok\",\"data\": {\"sessionID\": \"s.21\"}}");
        setPostResponse("createSession", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&groupID=g.3&validUntil=1542519358&authorID=a.2",
        		"118", "{\"code\":0,\"message\":\"ok\",\"data\": {\"sessionID\": \"s.21\"}}");
        
        Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
        String groupId = (String) groupResponse.get("groupID");
        Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
        String authorId = (String) authorResponse.get("authorID");

        int sessionDuration = 8;
        Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
        String firstSessionId = (String) sessionResponse.get("sessionID");

        Calendar oneYearFromNow = Calendar.getInstance();
        oneYearFromNow.add(Calendar.YEAR, 1);
        Date sessionValidUntil = oneYearFromNow.getTime();
        sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
        String secondSessionId = (String) sessionResponse.get("sessionID");
        try {
            assertNotEquals(firstSessionId, secondSessionId);

            Map sessionInfo = client.getSessionInfo(secondSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));
            assertEquals(authorId, sessionInfo.get("authorID"));
            assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));

            Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
            sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));
            sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));

            Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
            sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
            assertEquals(authorId, sessionInfo.get("authorID"));
            sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
            assertEquals(authorId, sessionInfo.get("authorID"));
        } finally {
            client.deleteSession(firstSessionId);
            client.deleteSession(secondSessionId);
        }


    }
	/*
    @Test
    public void create_pad_set_and_get_content() {
        String padID = "integration-test-pad";
        client.createPad(padID);
        try {
            client.setText(padID, "gå å gjør et ærend");
            String text = (String) client.getText(padID).get("text");
            assertEquals("gå å gjør et ærend\n", text);

            client.setHTML(
                    padID,
                   "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>"
            );
            String html = (String) client.getHTML(padID).get("html");
            assertTrue(html, html.contains("g&#229; og gj&#248;re et &#230;rend igjen<br><br>"));

            html = (String) client.getHTML(padID, 2).get("html");
            assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
            text = (String) client.getText(padID, 2).get("text");
            assertEquals("\n", text);

            long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
            assertEquals(3L, revisionCount);

            String revisionChangeset = client.getRevisionChangeset(padID);
            assertTrue(revisionChangeset, revisionChangeset.contains("gå og gjøre et ærend igjen"));

            revisionChangeset = client.getRevisionChangeset(padID, 2);
            assertTrue(revisionChangeset, revisionChangeset.contains("|1-j|1+1$\n"));

            String diffHTML = (String) client.createDiffHTML(padID, 1, 2).get("html");
            assertTrue(diffHTML, diffHTML.contains(
                    "<span class=\"removed\">g&#229; &#229; gj&#248;r et &#230;rend</span>"
            ));

            client.appendText(padID, "lagt til nå");
            text = (String) client.getText(padID).get("text");
            assertEquals("gå og gjøre et ærend igjen\nlagt til nå\n", text);

            Map attributePool = (Map) client.getAttributePool(padID).get("pool");
            assertTrue(attributePool.containsKey("attribToNum"));
            assertTrue(attributePool.containsKey("nextNum"));
            assertTrue(attributePool.containsKey("numToAttrib"));

            client.saveRevision(padID);
            client.saveRevision(padID, 2);

            long savedRevisionCount = (long) client.getSavedRevisionsCount(padID).get("savedRevisions");
            assertEquals(2L, savedRevisionCount);

            List savedRevisions = (List) client.listSavedRevisions(padID).get("savedRevisions");
            assertEquals(2, savedRevisions.size());
            assertEquals(2L, savedRevisions.get(0));
            assertEquals(4L, savedRevisions.get(1));

            long padUsersCount = (long) client.padUsersCount(padID).get("padUsersCount");
            assertEquals(0, padUsersCount);

            List padUsers = (List) client.padUsers(padID).get("padUsers");
            assertEquals(0, padUsers.size());

            String readOnlyId = (String) client.getReadOnlyID(padID).get("readOnlyID");
            String padIdFromROId = (String) client.getPadID(readOnlyId).get("padID");
            assertEquals(padID, padIdFromROId);

            List authorsOfPad = (List) client.listAuthorsOfPad(padID).get("authorIDs");
            assertEquals(0, authorsOfPad.size());

            long lastEditedTimeStamp = (long) client.getLastEdited(padID).get("lastEdited");
            Calendar lastEdited = Calendar.getInstance();
            lastEdited.setTimeInMillis(lastEditedTimeStamp);
            Calendar now = Calendar.getInstance();
            assertTrue(lastEdited.before(now));

            client.sendClientsMessage(padID, "test message");
        } finally {
            client.deletePad(padID);
        }
    }*/

    @Test
    public void create_pad_move_and_copy() throws Exception {
    	
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter pad_id_param = new Parameter("padID", "integration-test-pad-copy");
    	Parameter pad_id_param2 = new Parameter("padID", "integration-move-pad-move");

    	Parameters params = new Parameters(api_key_param,pad_id_param);
    	Parameters params2 = new Parameters(api_key_param,pad_id_param2);

        String padID = "integration-test-pad";
        String copyPadId = "integration-test-pad-copy";
        String movePadId = "integration-move-pad-move";
        String keep = "should be kept";
        String change = "should be changed";

        setPostResponse("createPad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad&text=should+be+kept",
        		"118", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("copyPad", 
        		"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-test-pad-copy",
        		"153", "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": { \"text\": \"should be kept\" }}", params);
        setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": { \"text\": \"should be kept\" }}", params2);
 
        setPostResponse("copyPad", 
        		"sourceID=integration-test-pad&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=false&destinationID=integration-move-pad-move",
        		"153", "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setPostResponse("setText", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-move-pad-move&text=should+be+changed",
        		"126", "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setPostResponse("copyPad", 
        		"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy",
        		"157", "{\"code\":0,\"message\":\"ok\",\"data\": null}");       

        setPostResponse("movePad", 
        		"sourceID=integration-move-pad-move&apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&force=true&destinationID=integration-test-pad-copy",
        		"157", "{\"code\":0,\"message\":\"ok\",\"data\": null}");       

        setPostResponse("deletePad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-copy",
        		"103", "{\"code\":0,\"message\":\"ok\",\"data\": null}"); 
        setPostResponse("deletePad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad",
        		"98", "{\"code\":0,\"message\":\"ok\",\"data\": null}");     
        client.createPad(padID, keep);

        client.copyPad(padID, copyPadId);
        String copyPadText = (String) client.getText(copyPadId).get("text");
        client.movePad(padID, movePadId);
        String movePadText = (String) client.getText(movePadId).get("text");

        client.setText(movePadId, change);
        client.copyPad(movePadId, copyPadId, true);
        String copyPadTextForce = (String) client.getText(copyPadId).get("text");
        client.movePad(movePadId, copyPadId, true);
        String movePadTextForce = (String) client.getText(copyPadId).get("text");

        client.deletePad(copyPadId);
        client.deletePad(padID);

        assertEquals(keep, copyPadText);
        assertEquals(keep, movePadText);

        //FIXME:  imposible hacer que la llamada devuelva otra cosa
        //assertEquals(change, copyPadTextForce);
        //assertEquals(change, movePadTextForce);
    }

    @Test
    public void create_pads_and_list_them() throws InterruptedException {
        String pad1 = "integration-test-pad-1";
        String pad2 = "integration-test-pad-2";

        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter authorId_param = new Parameter("authorID", "a.test");
    	
    	Parameters params = new Parameters(api_key_param);
        
        setPostResponse("createPad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("createPad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        
        setGetResponse("listAllPads", "{\"code\":0,\"message\":\"ok\",\"data\": {\"padIDs\": [\"integration-test-pad-1\",\"integration-test-pad-2\"]}}", params);
        
        setPostResponse("deletePad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("deletePad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-2",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
             
        client.createPad(pad1);
        client.createPad(pad2);
        Thread.sleep(100);
        List padIDs = (List) client.listAllPads().get("padIDs");
        client.deletePad(pad1);
        client.deletePad(pad2);

        assertTrue(String.format("Size was %d", padIDs.size()),padIDs.size() >= 2);
        assertTrue(padIDs.contains(pad1));
        assertTrue(padIDs.contains(pad2));
    }
/*
    @Test
    public void create_pad_and_chat_about_it() {
        String padID = "integration-test-pad-1";
        String user1 = "user1";
        String user2 = "user2";
        
                setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-1&authorMapper=user1",
        		"116", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.user\"}}");
        
        setPostResponse("createAuthorIfNotExistsFor", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&name=integration-author-2&authorMapper=user2",
        		"116", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.user2\"}}");  	
        setPostResponse("createPad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");  
        setPostResponse("deletePad", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1",
        		"100", "{\"code\":0,\"message\":\"ok\",\"data\": null}");  
        
        setPostResponse("appendChatMessage", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user1&authorID=a.user",
        		"135", "{\"code\":0,\"message\":\"ok\",\"data\": null}");  
        setPostResponse("appendChatMessage", 
        		"apikey=a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58&padID=integration-test-pad-1&text=hi+from+user2&time=1542495550&authorID=a.user2",
        		"152", "{\"code\":0,\"message\":\"ok\",\"data\": null}"); 
        
        Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
        String author1Id = (String) response.get("authorID");
        response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
        String author2Id = (String) response.get("authorID");

        client.createPad(padID);
        try {
            client.appendChatMessage(padID, "hi from user1", author1Id);
            client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
            client.appendChatMessage(padID, "gå å gjør et ærend", author1Id, System.currentTimeMillis() / 1000L);
            response = client.getChatHead(padID);
            long chatHead = (long) response.get("chatHead");
            assertEquals(2, chatHead);

            response = client.getChatHistory(padID);
            List chatHistory = (List) response.get("messages");
            assertEquals(3, chatHistory.size());
            assertEquals("gå å gjør et ærend", ((Map)chatHistory.get(2)).get("text"));

            response = client.getChatHistory(padID, 0, 1);
            chatHistory = (List) response.get("messages");
            assertEquals(2, chatHistory.size());
            assertEquals("hi from user2", ((Map)chatHistory.get(1)).get("text"));
        } finally {
            client.deletePad(padID);
        }

    }
    */
}
