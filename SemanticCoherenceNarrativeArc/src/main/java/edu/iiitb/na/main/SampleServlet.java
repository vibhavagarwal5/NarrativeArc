package edu.iiitb.na.main;
import edu.iiitb.na.sc.SemanticCoherence;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class SampleServlet extends HttpServlet {
	private static final long serialVersionUID = 1L;

	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException { // reading
																					// the
																					// user
																					// input
		String string1 = request.getParameter("string1");
		String string2 = request.getParameter("string2");
		System.out.println("Checking semantic coherence");
		SemanticCoherence.getSemanticCoherence(string1, string2);
		
	}
}