package wcps.server.cli;

import grammar.*;
import grammar.wcpsParser.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import org.antlr.runtime.ANTLRInputStream;
import org.antlr.runtime.CommonTokenStream;
import org.antlr.runtime.RecognitionException;

/** Test the WCPS grammar parser (generated by ANTLR).
 * Input an Abstract Syntax query.
 * Outputs the corresponding XML tree.
 *
 * @author Andrei Aiordachioaie
 */
public class grammar {

    static String query;

    public static void main(String[] args) throws RecognitionException, IOException
    {
        if (args.length != 1)
        {
            System.err.println("AbstractGrammarGen: no query as parameter !");
            query = "for a in (rgb) return " +
                    "condense + over $x x(1:10), $y y(25:75) " +
                    "using $x * (a[x($x), y($y)]).red";
        }
        else
            query = args[0];
			
		System.out.println("Running with the following query: " + query);

        String xmlString = convertAbstractQueryToXml(query);
        System.out.println("Output XML: \n****************\n" + xmlString);
         
        System.exit(0);

    }

    /** Converts a WCPS abstract syntax query to an XML query
     *
     * @param query Abstract Syntax query
     * @return String XML query
     * @throws java.io.IOException
     * @throws org.antlr.runtime.RecognitionException
     */
    public static String convertAbstractQueryToXml(String query) throws IOException, RecognitionException
    {
        InputStream stream = new ByteArrayInputStream(query.getBytes()); // defaults to ISO-1
        ANTLRInputStream inputStream = new ANTLRInputStream(stream);
//        wcpsLexer lexer = new wcpsLexer( inputStream );
		wcpsLexer lexer = new wcpsLexer( inputStream );

        CommonTokenStream tokenStream = new CommonTokenStream(lexer);
//        wcpsParser parser = new wcpsParser(tokenStream);
		wcpsParser parser = new wcpsParser(tokenStream);

        wcpsRequest_return rrequest = parser.wcpsRequest();
		WCPSRequest request = rrequest.value;

        String result = request.toXML();
        return result;
    }

}
