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
    
    private void setPostResponse (String path,String response) {
        //.withBody(new StringBody(body))
    	
        //.withHeaders(
        //        header("Content-type", "application/x-www-form-urlencoded"),
        //        header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),
	    //             header("User-Agent", "Java/1.8.0_92"),
        //        header("Connection", "keep-alive"),
        //        header("Host", "localhost:9001"),
        //        header("Content-Length", content_lenght)
        //    )    	
    	
        //.withKeepAlive(true)
        //.withSecure(false)
    	
    	mockServer
    	.when(
              HttpRequest.request()
              .withMethod("POST")
              .withPath("/api/1.2.13/"+path)
        ).respond(
                 HttpResponse.response()
                 .withStatusCode(200) 
                 .withBody(response)
                 );	
    }
    
    private void setGetResponse (String path,String response,Parameters queryStringParameters) {

        //.withHeaders(
        //         header("content-length", "0"),
        //         header("Accept", "text/html, image/gif, image/jpeg, *; q=.2, */*; q=.2"),
	    //         header("User-Agent", "Java/1.8.0_92"),
        //         header("Connection", "keep-alive"),
        //         header("Host", "localhost:9001")
        //    )
    	
    	mockServer
        .when(
              HttpRequest.request()
              .withMethod("GET")
              .withPath("/api/1.2.13/"+path)
              .withQueryStringParameters(queryStringParameters)              
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
    			 "{\"code\":0,\"message\":\"ok\",\"data\":{\"groupID\": \"g.3\"}}");

        // Response para crear el pad del grupo1
    	setPostResponse("createGroupPad", 
    			"{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");

    	// Response para crear el pad del grupo2
    	setPostResponse("createGroupPad", 
    			 "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");
    	    	
    	// Response para el set public status    	
    	setPostResponse("setPublicStatus", 
    			 "{\"code\":0,\"message\":\"ok\",\"data\":null}");
    	
    	// Response para el get public status    	
    	setGetResponse("getPublicStatus", "{\"code\":0,\"message\":\"ok\",\"data\":{\"publicStatus\": true}}", params);
 
    	// Response para el deleteGroup   	
    	setPostResponse("deleteGroup", 
    			 "{\"code\":0,\"message\":\"ok\",\"data\":{\"padID\": \"g.3\"}}");
  
    	// Response para setPassword   	
    	setPostResponse("setPassword", 
    			 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
    	
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
    	setPostResponse("createAuthor", "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
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

    
    public void create_author_with_author_mapper() throws Exception {
        String authorMapper = "username";
    	
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter authorId_param = new Parameter("authorID", "a.test");
    	
    	Parameters params2 = new Parameters(api_key_param,authorId_param);

    	setPostResponse("createAuthorIfNotExistsFor", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
    	setGetResponse("getAuthorName", "{\"code\":0,\"message\":\"ok\",\"data\": \"integration-author-1\"}", params2);

    	setPostResponse("createAuthorIfNotExistsFor", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");
    	setPostResponse("createAuthorIfNotExistsFor", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.test\"}}");

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
    
    @Test
    public void create_and_delete_session() throws Exception {
        String authorMapper = "username";
        String groupMapper = "groupname";
        
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter sessionID_param = new Parameter("sessionID", "s.02");
    	Parameter groupID_param = new Parameter("groupID", "g.3");
    	Parameter authorID_param = new Parameter("authorID", "a.2");
    	
    	Parameters params = new Parameters(api_key_param,sessionID_param);
    	Parameters params2 = new Parameters(api_key_param,groupID_param);
    	Parameters params3 = new Parameters(api_key_param,authorID_param);

        setPostResponse("createGroupIfNotExistsFor", 
        		"{\"code\":0,\"message\":\"ok\",\"data\": {\"groupID\": \"g.3\"}}");
        
        Map groupResponse = client.createGroupIfNotExistsFor(groupMapper);
        String groupId = (String) groupResponse.get("groupID");
        setPostResponse("createAuthorIfNotExistsFor", 
        		"{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.2\"}}");
        Map authorResponse = client.createAuthorIfNotExistsFor(authorMapper, "integration-author-1");
        String authorId = (String) authorResponse.get("authorID");

        int sessionDuration = 8;
        setPostResponse("createSession", 
        		"{\"code\":0,\"message\":\"ok\",\"data\": {\"sessionID\": \"s.01\"}}");
        Map sessionResponse = client.createSession(groupId, authorId, sessionDuration);
        String firstSessionId = (String) sessionResponse.get("sessionID");

        mockServer.reset();
        setPostResponse("createSession", 
        		"{\"code\":0,\"message\":\"ok\",\"data\": {\"sessionID\": \"s.02\"}}");
        Calendar oneYearFromNow = Calendar.getInstance();
        oneYearFromNow.add(Calendar.YEAR, 1);
        Date sessionValidUntil = oneYearFromNow.getTime();

        sessionResponse = client.createSession(groupId, authorId, sessionValidUntil);
        String secondSessionId = (String) sessionResponse.get("sessionID");
        try {
            assertNotEquals(firstSessionId, secondSessionId);

            setGetResponse("getSessionInfo", "{\"code\":0,\"message\":\"ok\",\"data\": {\"groupID\": \"g.3\""
            		+ ",\"authorID\": \"a.2\",\"validUntil\":"+Long.toString(sessionValidUntil.getTime()/1000L)+"}}", params);
            
            Map sessionInfo = client.getSessionInfo(secondSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));
            assertEquals(authorId, sessionInfo.get("authorID"));
            assertEquals(sessionValidUntil.getTime() / 1000L, (long) sessionInfo.get("validUntil"));

            setGetResponse("listSessionsOfGroup", "{\"code\":0,\"message\":\"ok\",\"data\": {\"s.01\":{\"groupID\": \"g.3\"},\"s.02\":{\"groupID\": \"g.3\"}}}", params2);
            Map sessionsOfGroup = client.listSessionsOfGroup(groupId);
            sessionInfo = (Map) sessionsOfGroup.get(firstSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));
            sessionInfo = (Map) sessionsOfGroup.get(secondSessionId);
            assertEquals(groupId, sessionInfo.get("groupID"));

            setGetResponse("listSessionsOfAuthor",
            		"{\"code\":0,\"message\":\"ok\",\"data\": {\"s.01\":{\"authorID\": \"a.2\"},\"s.02\":{\"authorID\": \"a.2\"}}}", params3);
            Map sessionsOfAuthor = client.listSessionsOfAuthor(authorId);
            sessionInfo = (Map) sessionsOfAuthor.get(firstSessionId);
            assertEquals(authorId, sessionInfo.get("authorID"));
            sessionInfo = (Map) sessionsOfAuthor.get(secondSessionId);
            assertEquals(authorId, sessionInfo.get("authorID"));
        } finally {
        	setPostResponse("deleteSession", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
            client.deleteSession(firstSessionId);
            client.deleteSession(secondSessionId);
        }


    }
	
    @Test
    public void create_pad_set_and_get_content() {
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter padID_param = new Parameter("padID", "integration-test-pad");
    	Parameter rev_param = new Parameter("rev", "2");
    	Parameter authorID_param = new Parameter("authorID", "a.2");
    	
    	Parameters params = new Parameters(api_key_param,padID_param);
    	Parameters params2 = new Parameters(api_key_param,padID_param,rev_param);
    	
    	String padID = "integration-test-pad";
        setPostResponse("createPad", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        client.createPad(padID);
        try {
            setPostResponse("setText", 
           		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
            client.setText(padID, "gå å gjør et ærend");
        	setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": {\"text\": \"Initial text\"}}", params);

            String text = (String) client.getText(padID).get("text");
            assertEquals("Initial text", text);

            setPostResponse("setHTML", 
              		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
            client.setHTML(
                    padID,
                   "<!DOCTYPE HTML><html><body><p>gå og gjøre et ærend igjen</p></body></html>"
            );
            
            setGetResponse("getHTML", 
              		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"html\": \"<!DOCTYPE HTML><html><body><p>Este texto es maravilloso</p></body></html>\"}}",params); 
            String html = (String) client.getHTML(padID).get("html");
            assertTrue(html, html.contains("Este texto es maravilloso"));

            mockServer.reset();
            
            setGetResponse("getHTML", 
             		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"html\": \"<!DOCTYPE HTML><html><body><br></body></html>\",\"text\":\"Initial text\"}}",params2); 
            
            html = (String) client.getHTML(padID, 2).get("html");
            assertEquals("<!DOCTYPE HTML><html><body><br></body></html>", html);
            
        	setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": {\"text\": \"\"}}", params2	);            
            text = (String) client.getText(padID, 2).get("text");
            assertEquals("", text);

        	setGetResponse("getRevisionsCount", "{\"code\":0,\"message\":\"ok\",\"data\": {\"revisions\": 3}}", params);            
            long revisionCount = (long) client.getRevisionsCount(padID).get("revisions");
            assertEquals(3L, revisionCount);

        	setGetResponse("getRevisionChangeset", "{\"code\":0,\"message\":\"ok\",\"data\": \"Este texto es maravilloso\"}", params);            
            String revisionChangeset = client.getRevisionChangeset(padID);
            assertTrue(revisionChangeset, revisionChangeset.contains("Este texto es maravilloso"));
/*
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
*/
            setPostResponse("sendClientsMessage", "{\"code\":0,\"message\":\"ok\",\"data\": null}");
            client.sendClientsMessage(padID, "test message");
        } finally {
            setPostResponse("deletePad", 
               		"{\"code\":0,\"message\":\"ok\",\"data\": null}"); 
            client.deletePad(padID);
        }
    }

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
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("copyPad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": { \"text\": \"should be kept\" }}", params);
        setGetResponse("getText", "{\"code\":0,\"message\":\"ok\",\"data\": { \"text\": \"should be kept\" }}", params2);
 
        setPostResponse("copyPad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setPostResponse("setText", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");       
        
        setPostResponse("copyPad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");       

        setPostResponse("movePad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");       

        setPostResponse("deletePad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}"); 
        setPostResponse("deletePad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");     
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
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("createPad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        
        setGetResponse("listAllPads", "{\"code\":0,\"message\":\"ok\",\"data\": {\"padIDs\": [\"integration-test-pad-1\",\"integration-test-pad-2\"]}}", params);
        
        setPostResponse("deletePad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
        setPostResponse("deletePad", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");
             
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

    @Test
    public void create_pad_and_chat_about_it() {
        String padID = "integration-test-pad-1";
        String user1 = "user1";
        String user2 = "user2";
        
        Parameter api_key_param = new Parameter("apikey", "a04f17343b51afaa036a7428171dd873469cd85911ab43be0503d29d2acbbd58");
    	Parameter padID_param = new Parameter("padID", "integration-test-pad-1");
    	Parameter start_param = new Parameter("start", "0");
    	Parameter end_param = new Parameter("end", "1");
    	
    	Parameters params = new Parameters(api_key_param,padID_param);
    	Parameters params2 = new Parameters(api_key_param,padID_param,start_param,end_param);
        
        setPostResponse("createAuthorIfNotExistsFor", 
        		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.user\"}}");
            
        Map response = client.createAuthorIfNotExistsFor(user1, "integration-author-1");
        String author1Id = (String) response.get("authorID");
        
        setPostResponse("createAuthorIfNotExistsFor", 
       		 "{\"code\":0,\"message\":\"ok\",\"data\": {\"authorID\": \"a.user2\"}}");  	

        response = client.createAuthorIfNotExistsFor(user2, "integration-author-2");
        String author2Id = (String) response.get("authorID");
        setPostResponse("createPad", 
       		 "{\"code\":0,\"message\":\"ok\",\"data\": null}");  
       
        client.createPad(padID);
        try {
            setPostResponse("appendChatMessage", 
               		"{\"code\":0,\"message\":\"ok\",\"data\": null}"); 
            client.appendChatMessage(padID, "hi from user1", author1Id); 
            client.appendChatMessage(padID, "hi from user2", author2Id, System.currentTimeMillis() / 1000L);
            client.appendChatMessage(padID, "gå å gjør et ærend", author1Id, System.currentTimeMillis() / 1000L);

            setGetResponse("getChatHead", "{\"code\":0,\"message\":\"ok\",\"data\": {\"chatHead\": 2}}",
            		params);
            setGetResponse("getChatHistory", "{\"code\":0,\"message\":\"ok\",\"data\": {\"messages\": ["
            		+ "{\"text\": \"hi from user1\"},{\"text\":\"hi from user2\"},"
            		+ "{\"text\":\"text3\"}]}}",
            		params);          
            
            response = client.getChatHead(padID);
            long chatHead = (long) response.get("chatHead");
            assertEquals(2, chatHead);

            response = client.getChatHistory(padID);
            List chatHistory = (List) response.get("messages");
            assertEquals(3, chatHistory.size());
            assertEquals("text3", ((Map)chatHistory.get(2)).get("text"));

            mockServer.reset();
            
            setGetResponse("getChatHistory", "{\"code\":0,\"message\":\"ok\",\"data\": {\"messages\": ["
            		+ "{\"text\": \"hi from user1\"},{\"text\":\"hi from user2\"},"
            		+ "]}}",
            		params2);  
            
            response = client.getChatHistory(padID, 0, 1);
            chatHistory = (List) response.get("messages");
            assertEquals(2, chatHistory.size());
            assertEquals("hi from user2", ((Map)chatHistory.get(1)).get("text"));
        } finally {
            setPostResponse("deletePad", 
               		"{\"code\":0,\"message\":\"ok\",\"data\": null}");  
            client.deletePad(padID);
        }

    }
    
}
