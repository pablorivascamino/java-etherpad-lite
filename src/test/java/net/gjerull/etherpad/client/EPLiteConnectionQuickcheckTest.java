package net.gjerull.etherpad.client;

import org.junit.runner.RunWith;

import com.pholser.junit.quickcheck.Property;
import com.pholser.junit.quickcheck.runner.JUnitQuickcheck;
import static org.junit.Assert.*;

import java.util.Map;
import java.util.TreeMap;

import org.junit.Test;

@RunWith(JUnitQuickcheck.class)
public class EPLiteConnectionQuickcheckTest {
	private static final String API_VERSION = "1.2.12";
	private static final String ENCODING = "UTF-8";
	
	@Property (trials = 10) public void domain_with_trailing_slash_when_construction_an_api_path(String exampleMethod) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", "apikey", API_VERSION, ENCODING
        );
        String apiMethodPath = connection.apiPath(exampleMethod);
        assertEquals("/api/1.2.12/"+exampleMethod, apiMethodPath);
    }

	@Property(trials = 10)
    public void domain_without_trailing_slash_when_construction_an_api_path(String exampleMethod) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com", "apikey", API_VERSION, ENCODING
        );
        String apiMethodPath = connection.apiPath(exampleMethod);
        assertEquals("/api/1.2.12/"+exampleMethod, apiMethodPath);
    }
	
	@Property(trials = 10)
    public void query_string_from_map(int rev, String apikey, String padId) throws Exception {
        EPLiteConnection connection = new EPLiteConnection(
                "http://example.com/", apikey, API_VERSION, ENCODING
        );
        Map<String,Object> apiArgs = new TreeMap<>(); // Ensure ordering for testing
        apiArgs.put("padID", padId);
        apiArgs.put("rev", rev);

        String queryString = connection.queryString(apiArgs, false);

        assertEquals("apikey="+apikey+"&padID="+padId+"&rev="+rev, queryString);
    }
	
}
