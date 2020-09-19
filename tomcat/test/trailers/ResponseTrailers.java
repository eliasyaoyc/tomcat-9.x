package trailers;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This example writes some trailer fields to the HTTP response.
 */
public class ResponseTrailers extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Supplier<Map<String,String>> TRAILER_FIELD_SUPPLIER = new TrailerFieldSupplier();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        resp.setTrailerFields(TRAILER_FIELD_SUPPLIER);
        resp.setContentType("text/plain");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter pw  = resp.getWriter();

        pw.print("This response should include trailer fields.");
    }


    private static class TrailerFieldSupplier implements Supplier<Map<String,String>> {

        private static final Map<String,String> trailerFields = new HashMap<>();

        static {
            trailerFields.put("x-trailer-1", "Trailer value one");
            trailerFields.put("x-trailer-2", "Trailer value two");
        }

        @Override
        public Map<String, String> get() {
            return trailerFields;
        }
    }
}
